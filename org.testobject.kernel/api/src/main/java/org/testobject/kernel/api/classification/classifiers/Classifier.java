package org.testobject.kernel.api.classification.classifiers;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Point.Double;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Maps;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.imaging.contours.Trace;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Classifier {

    interface Specification {

        List<Qualifier> requires();

        Qualifier returns();
    }
	
	interface Images {
		
		Image.Int raw();
		
		class Factory {
			public static Images create(final Image.Int raw) {
				return new Images() {
					@Override
					public Int raw() {
						return raw;
					}
				};
			}
		}

	}
	
	interface Lookups {
		interface Contour {
			List<Point.Double> contour(Mask mask);
			
			class Factory {
				public static Contour create() {
					return new Contour() {
						@Override
						public List<Double> contour(Mask mask) {
							return Lists.immutable(Trace.contourTrace(mask));
						}
					};
				}
			}
		}
	}
	
	interface Context {
		
		Classifier.Qualifier qualifier();
		
		class Factory {
			public static Context create(final Classifier.Qualifier qualifier) {
				return new Context() {
					@Override
					public Qualifier qualifier() {
						return qualifier;
					}
				};
			}

			public static Context none() {
				return new Context() {
					@Override
					public Qualifier qualifier() {
						return Qualifier.Factory.none();
					}
				};
			}
		}
	}
	
	// FIXME returns Element or Mask? (en)
	interface Lookup extends Lookups.Contour {
		
		List<Element> find(Rectangle.Int rect);
		
		List<Element> neighbours(Mask mask);
		
		List<Mask> childs(Mask mask);
		
		class Factory {
			
			public static Lookup blob() {
				return new Classifier.Lookup() {
					@Override
					public List<Element> find(Rectangle.Int rect) {
						throw new UnsupportedOperationException();
					}

					@Override
					public List<Element> neighbours(Mask mask) {
						throw new UnsupportedOperationException();
					}

					@Override
					public List<Mask> childs(Mask mask) {
						if(mask instanceof Blob) {
							Blob blob = (Blob) mask;
							List<Mask> childs = Lists.newArrayList(blob.children.size());
							for(Blob child : blob.children) {
								childs.add(child);
							}
							return childs;
						} else {
							return Lists.empty();
						}
					}
					
					Map<Mask, List<Point.Double>> cache = Maps.newIdentityMap();
					
					@Override
					public List<Point.Double> contour(Mask mask) {
						if(cache.containsKey(mask) == false) {
							cache.put(mask, Lists.immutable(Trace.contourTrace(mask)));
						}
						return cache.get(mask);
					}
				};
			}
			
			public static Lookup node(final Node root) {
				// FIXME messy (en)
				final Map<Mask, Node> masks = Maps.newIdentityMap();
				fillMasks(root, masks);
				
				return new Classifier.Lookup() {
					@Override
					public List<Element> find(Rectangle.Int rect) {
						throw new UnsupportedOperationException();
					}

					@Override
					public List<Element> neighbours(Mask mask) {
						throw new UnsupportedOperationException();
					}

					@Override
					public List<Mask> childs(Mask mask) {
						Node node = masks.get(mask);
						List<Mask> masks = Lists.newArrayList(node.getChildren().size());
						for(Node child : node.getChildren()) {
							masks.addAll(child.getElement().getMasks());
						}
						return masks;
					}
					
					Map<Mask, List<Point.Double>> cache = Maps.newIdentityMap();
					
					@Override
					public List<Point.Double> contour(Mask mask) {
						if(cache.containsKey(mask) == false) {
							cache.put(mask, Lists.immutable(Trace.contourTrace(mask)));
						}
						return cache.get(mask);
					}
				};
			}

			private static void fillMasks(Node node, Map<Mask, Node> masks) {
				for(Mask mask : node.getElement().getMasks()) {
					masks.put(mask, node);
				}
				for(Node child : node.getChildren()) {
					fillMasks(child, masks);
				}
			}
		}
		
	}
	
	interface Likelihood {
		
		double geometric();
		
		double photometric();
		
		double normalized();
		
		class Factory {
			private static final Likelihood none = new Likelihood() {
				@Override
				public double geometric() {
					return 0d;
				}

				@Override
				public double photometric() {
					return 0d;
				}

				@Override
				public double normalized() {
					return 0d;
				}
			};
			
			public static Likelihood none() {
				return none;
			}

			public static Likelihood likelihood(final double geometric, final double photometric) {
				return new Likelihood() {
					@Override
					public double geometric() {
						return geometric;
					}

					@Override
					public double photometric() {
						return photometric;
					}

					@Override
					public double normalized() {
						return (geometric() + photometric()) / 2d;
					}
				};
			}
		}
		
		class Builder {
			
			private double geometric, photometric;
			
			public static Builder likelihood() {
				return new Builder();
			}
			
			public Builder geometric(double geometric) {
				this.geometric = geometric;
				return this;
			}

			public Builder photometric(double photometric) {
				this.photometric = photometric;
				return this;
			}
			
			public Likelihood build() {
				return Factory.likelihood(geometric, photometric);
			}
		}
	
	}

    @JsonDeserialize(as = Qualifier.Factory.Impl.class)
	interface Qualifier {
		
    	@JsonProperty("type")
		String getType();

    	@JsonProperty("id")
        String getId();
        
		class Factory {

            private static final class Impl implements Qualifier {

                private final String id;
                private final String type;

                @JsonCreator
                private Impl(@JsonProperty("type") String type, @JsonProperty("id") String id) {
                    this.id = id;
                    this.type = type;
                }

                @Override
                public String getType() {
                    return type;
                }

                @Override
                public String getId() {
                    return id;
                }

                @Override
                public boolean equals(Object obj) {
                    if(obj instanceof Qualifier == false) {
                        return false;
                    }
                    Qualifier qualifier = (Qualifier) obj;

                    return qualifier.getType().equals(this.getType()) && qualifier.getId().equals(this.getId());
                }

                @Override
                public int hashCode() {
                    return toString().hashCode();
                }

                @Override
                public String toString() {
                    return type + "." + id;
                }
            }

			public static Qualifier create(final String type, final String id) {
				return new Impl(type, id);
			}
			

			public static Qualifier none() {
				return new Impl("none", "none");
			}
			
			public static class Class {
				
				public static final String node = "node";
				public static final String blob = "blob";
				public static final String group = "group";
				
				public static final String segment = "segment";
				public static final String icon = "icon";
				public static final String image = "image";
				
				public static Qualifier image(String qualifier) {
					return create(image, qualifier);
				}
				
				public static Qualifier icon(String qualifier) {
					return create(icon, qualifier);
				}
				
				public static Qualifier node(String qualifier) {
					return create(node, qualifier);
				}
				
				public static Qualifier blob(int qualifier) {
					return create(blob, Integer.toString(qualifier));
				}
				
				public static Qualifier segment(int id) {
					return create(segment, Integer.toString(id));
				}
				
				public static Qualifier segment(Qualifier qualifier) {
					return create(segment, qualifier.toString());
				}
				
				public static Qualifier group(int qualifier) {
					return create(group, Integer.toString(qualifier));
				}
			}
		}
	}
	
	interface Proposal {
		
		Classifier.Likelihood likelihood();
		
		Element element();
		
		class Builder {
			private final List<Proposal> proposals = Lists.newLinkedList();
			
			public static Builder create() {
				return new Builder();
			}
			
			public Builder proposal(Classifier.Likelihood likelihood,  Element element) {
				this.proposals.add(toProposal(likelihood, element));
				
				return this;
			}
			
			private Proposal toProposal(final Likelihood likelihood, final Element element) {
				return new Proposal() {
					@Override
					public Likelihood likelihood() {
						return likelihood;
					}

					@Override
					public Element element() {
						return element;
					}
				};
			}

			public List<Proposal> build() {
				return this.proposals;
			}
		}
	}
	
	List<Proposal> classify(Images images, Lookup lookup, List<Mask> masks, Context context);

}
