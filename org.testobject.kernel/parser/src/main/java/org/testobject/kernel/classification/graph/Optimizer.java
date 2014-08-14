package org.testobject.kernel.classification.graph;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.tree.r.SpatialIndex;

/**
 * 
 * @author enijkamp
 *
 */
public interface Optimizer<Box, Label> {
	
	Iterable<Label> optimize(List<Box> segments, List<Label> contestants);
	
    interface BoxAdapter<T> {

        Rectangle.Double getBoundingBox(T contestant);

    }

    interface LabelAdapter<T> {

        Rectangle.Double getBoundingBox(T contestant);

        double getProbability(T contestant);

    }

    class Greedy<T> {

        private final SpatialIndex.Factory<T> factory;
        private final LabelAdapter<T> adapter;

        public Greedy(SpatialIndex.Factory<T> factory, LabelAdapter<T> adapter) {
            this.factory = factory;
            this.adapter = adapter;
        }

        public Iterable<T> optimize(List<T> contestants) {
            // build spatial index
            SpatialIndex<T> index = factory.create(createSpatialAdapter());
            for(T contestant : contestants) {
                index.put(contestant);
            }

            // intersections
            for(T contestant : contestants) {
                // find best match in neighborhood
                List<T> neighbours = Lists.newLinkedList();
                index.intersects(adapter.getBoundingBox(contestant), collect(neighbours));
                List<T> losers = Lists.newLinkedList();
                T winner = contestant;
                for(T neighbour : neighbours) {
                    losers.add(neighbour);
                    if(adapter.getProbability(neighbour) > adapter.getProbability(winner)) {
                        winner = neighbour;
                    }
                }
                losers.remove(winner);

                // prune index
                for(T loser : losers) {
                    index.remove(loser);
                }
            }

            return index.entries();
        }

        // FIXME index methods should return iterable? (en)
        private SpatialIndex.Visitor<T> collect(final List<T> result) {
            return new SpatialIndex.Visitor<T>() {
                @Override
                public boolean visit(T payload) {
                    result.add(payload);
                    return true;
                }
            };
        }

        private SpatialIndex.Adapter<T> createSpatialAdapter() {
            return new SpatialIndex.Adapter<T>() {
                @Override
                public Rectangle.Double getBoundingBox(T payload) {
                    return adapter.getBoundingBox(payload);
                }
            };
        }
    }
    
    class Weighting<T> {

        private final SpatialIndex.Factory<T> factory;
        private final LabelAdapter<T> adapter;

        public Weighting(SpatialIndex.Factory<T> factory, LabelAdapter<T> adapter) {
            this.factory = factory;
            this.adapter = adapter;
        }

        public Iterable<T> optimize(List<T> contestants) {
            // build spatial index
            SpatialIndex<T> index = factory.create(createSpatialAdapter());
            for(T contestant : contestants) {
                index.put(contestant);
            }
            
            // sort by size
            List<T> bySize = sort(contestants, bySize());
            
            Set<T> winners = Sets.newIdentitySet();
            
            // intersections
            for(T contestant : bySize) {
                // find neighborhood
                List<T> intersects = Lists.newLinkedList();
                index.intersects(adapter.getBoundingBox(contestant), collect(intersects));
                
                List<T> contains = Lists.newLinkedList();
                index.contains(adapter.getBoundingBox(contestant), collect(contains));
                
                // case 1 : A overlaps B
                {
                	List<T> overlaps = sort(overlaps(intersects, contains, contestant), bySize());
                	
                	if(overlaps.size() == 0) {
                		if(adapter.getProbability(contestant) > 0.7d) {
                			winners.add(contestant);
                		}
                	} else {
	                	T A = overlaps.get(0);
	                	overlaps.remove(A);
	                	
	                	// greedy
	                	for(T B : overlaps) {
	                		Rectangle.Double boxA = adapter.getBoundingBox(A);
	                		Rectangle.Double boxB = adapter.getBoundingBox(B);
	                		
	                		Rectangle.Double intersection = boxA.intersect(boxB);
	                		Rectangle.Double union = boxA.union(boxB);
	                		
	                		double ratio = intersection.area() / union.area();
	                		
	                		if(ratio > .9d) {
	                		
	                			double likelihoodA = adapter.getProbability(A);
		                		double likelihoodB = adapter.getProbability(B);
		                		
		                		if((likelihoodB * ratio) > likelihoodA) {
		                			A = B;
		                		}
	                		}
	                	}
	                	
	                	if(adapter.getProbability(A) > 0.7d) {
	                		winners.add(A);
	                	}
                	}
                }

                // case 2 : A contains B
                {
                	
                }
                
            }

            return winners;
        }

        private List<T> overlaps(List<T> intersects, List<T> contains, T contestant) {
        	List<T> overlaps = Lists.newLinkedList();
        	for(T candidate : intersects) {
        		if(adapter.getBoundingBox(candidate).contains(adapter.getBoundingBox(contestant)) == false && contains.contains(candidate) == false) {
        			overlaps.add(candidate);
        		}
        	}
			return overlaps;
		}

		private List<T> sort(List<T> contestants, Comparator<T> comparator) {
        	List<T> copy = Lists.newArrayList(contestants);
        	Collections.sort(copy, comparator);
			return copy;
		}

		private Comparator<T> bySize() {
			return new Comparator<T>() {
				@Override
				public int compare(T t1, T t2) {
					return Double.compare(adapter.getBoundingBox(t2).area(), adapter.getBoundingBox(t1).area());
				}
			};
		}

		// FIXME index methods should return iterable? (en)
        private SpatialIndex.Visitor<T> collect(final List<T> result) {
            return new SpatialIndex.Visitor<T>() {
                @Override
                public boolean visit(T payload) {
                    result.add(payload);
                    return true;
                }
            };
        }

        private SpatialIndex.Adapter<T> createSpatialAdapter() {
            return new SpatialIndex.Adapter<T>() {
                @Override
                public Rectangle.Double getBoundingBox(T payload) {
                    return adapter.getBoundingBox(payload);
                }
            };
        }

    }
    
	class LowPassBreedWinners<Box, Label> implements Optimizer<Box, Label> {

		private final BoxAdapter<Box> boxAdapter;
		private final LabelAdapter<Label> labelAdapter;
		private final SpatialIndex.Factory<Label> factory;

		public LowPassBreedWinners(BoxAdapter<Box> boxAdapter, LabelAdapter<Label> labelAdapter, SpatialIndex.Factory<Label> factory) {
			this.boxAdapter = boxAdapter;
			this.labelAdapter = labelAdapter;
			this.factory = factory;
		}
		
		public Iterable<Label> optimize(List<Box> segments, List<Label> contestants) {
			// build spatial index
			SpatialIndex<Label> index = factory.create(createSpatialAdapter());
			for (Label contestant : contestants) {
				index.put(contestant);
			}

			// sort by size
			Set<Label> winners = Sets.newIdentitySet();

			// intersections
			for (Box segment : segments) {
				// find neighborhood
				List<Label> intersects = Lists.newLinkedList();
				index.intersects(boxAdapter.getBoundingBox(segment), collect(intersects));

				List<Label> contains = Lists.newLinkedList();
				index.contains(boxAdapter.getBoundingBox(segment), collect(contains));

				// case 1 : A overlaps B
				{
					List<Label> overlaps = sort(overlaps(intersects, contains, segment), bySize());
					
					if (overlaps.size() > 0) {
						
						Rectangle.Double boxA = boxAdapter.getBoundingBox(segment);
						
						double bestLikelihood = java.lang.Double.MIN_VALUE;
						Label winner = overlaps.get(0);
						
						// greedy
						for (Label B : overlaps) {
							
							Rectangle.Double boxB = labelAdapter.getBoundingBox(B);
							Rectangle.Double intersection = boxA.intersect(boxB);
							Rectangle.Double union = boxA.union(boxB);
							
							double ratio = intersection.area() / union.area();
							
							if (ratio > .9d) {

								double likelihood = labelAdapter.getProbability(B);
								
								if ((likelihood * ratio) > bestLikelihood) {
									winner = B;
									bestLikelihood = (likelihood * ratio);
								}
							}
						}

						if (labelAdapter.getProbability(winner) > 0.7d) {
							winners.add(winner);
						}
					}
				}

				// case 2 : A contains B
				{
					// TODO implement, see photo of whiteboard (en)
				}

			}

			return winners;
		}

		// FIXME overlaps in this form not required, ratio prunes contestants with area intersection < 0.9 (en)
		private List<Label> overlaps(List<Label> intersects, List<Label> contains, Box contestant) {
			List<Label> overlaps = Lists.newLinkedList();
			for (Label candidate : intersects) {
				//if (contains.contains(candidate) == false) {
					overlaps.add(candidate);
				//}
			}
			return overlaps;
		}

		private List<Label> sort(List<Label> contestants, Comparator<Label> comparator) {
			List<Label> copy = Lists.newArrayList(contestants);
			Collections.sort(copy, comparator);
			return copy;
		}

		private Comparator<Label> bySize() {
			return new Comparator<Label>() {
				@Override
				public int compare(Label t1, Label t2) {
					return Double.compare(labelAdapter.getBoundingBox(t2).area(), labelAdapter.getBoundingBox(t1).area());
				}
			};
		}

		// FIXME index methods should return iterable? (en)
		private SpatialIndex.Visitor<Label> collect(final List<Label> result) {
			return new SpatialIndex.Visitor<Label>() {
				@Override
				public boolean visit(Label payload) {
					result.add(payload);
					return true;
				}
			};
		}

		private SpatialIndex.Adapter<Label> createSpatialAdapter() {
			return new SpatialIndex.Adapter<Label>() {
				@Override
				public Rectangle.Double getBoundingBox(Label payload) {
					return labelAdapter.getBoundingBox(payload);
				}
			};
		}

	}
	
	class LowPassBreedLoosers<Label> implements Optimizer<Label, Label> {

		private final LabelAdapter<Label> labelAdapter;
		private final SpatialIndex.Factory<Label> factory;

		public LowPassBreedLoosers(LabelAdapter<Label> labelAdapter, SpatialIndex.Factory<Label> factory) {
			this.labelAdapter = labelAdapter;
			this.factory = factory;
		}
		
		public Iterable<Label> optimize(List<Label> segments, List<Label> contestants) {
			// build spatial index
			SpatialIndex<Label> index = factory.create(createSpatialAdapter());
			for (Label contestant : contestants) {
				index.put(contestant);
			}

			// sort by size
			Set<Label> winners = Sets.newIdentitySet();

			// intersections
			for (Label segment : segments) {
				// find neighborhood
				List<Label> intersects = Lists.newLinkedList();
				index.intersects(labelAdapter.getBoundingBox(segment), collect(intersects));

				List<Label> contains = Lists.newLinkedList();
				index.contains(labelAdapter.getBoundingBox(segment), collect(contains));

				// case 1 : A overlaps B
				{
					List<Label> overlaps = sort(overlaps(intersects, contains, segment), bySize());
					
					if (overlaps.size() > 0) {
						
						Rectangle.Double boxA = labelAdapter.getBoundingBox(segment);
						
						double bestLikelihood = java.lang.Double.MIN_VALUE;
						Label winner = overlaps.get(0);
						
						// greedy
						for (Label B : overlaps) {
							
							Rectangle.Double boxB = labelAdapter.getBoundingBox(B);
							Rectangle.Double intersection = boxA.intersect(boxB);
							Rectangle.Double union = boxA.union(boxB);
							
							double ratio = intersection.area() / union.area();
							
							if (ratio > .9d) {

								double likelihood = labelAdapter.getProbability(B);

								if ((likelihood * ratio) > bestLikelihood) {
									winner = B;
									bestLikelihood = (likelihood * ratio);
								}
							}
						}

						if (labelAdapter.getProbability(winner) > 0.7d) {
							winners.add(winner);
						} else {
							winners.add(segment);
						}
					}
				}

				// case 2 : A contains B
				{
					// TODO implement, see photo of whiteboard (en)
				}

			}

			return winners;
		}

		// FIXME overlaps in this form not required, ratio prunes contestants with area intersection < 0.9 (en)
		private List<Label> overlaps(List<Label> intersects, List<Label> contains, Label contestant) {
			List<Label> overlaps = Lists.newLinkedList();
			for (Label candidate : intersects) {
				//if (contains.contains(candidate) == false) {
					overlaps.add(candidate);
				//}
			}
			return overlaps;
		}

		private List<Label> sort(List<Label> contestants, Comparator<Label> comparator) {
			List<Label> copy = Lists.newArrayList(contestants);
			Collections.sort(copy, comparator);
			return copy;
		}

		private Comparator<Label> bySize() {
			return new Comparator<Label>() {
				@Override
				public int compare(Label t1, Label t2) {
					return Double.compare(labelAdapter.getBoundingBox(t2).area(), labelAdapter.getBoundingBox(t1).area());
				}
			};
		}

		// FIXME index methods should return iterable? (en)
		private SpatialIndex.Visitor<Label> collect(final List<Label> result) {
			return new SpatialIndex.Visitor<Label>() {
				@Override
				public boolean visit(Label payload) {
					result.add(payload);
					return true;
				}
			};
		}

		private SpatialIndex.Adapter<Label> createSpatialAdapter() {
			return new SpatialIndex.Adapter<Label>() {
				@Override
				public Rectangle.Double getBoundingBox(Label payload) {
					return labelAdapter.getBoundingBox(payload);
				}
			};
		}

	}

}
