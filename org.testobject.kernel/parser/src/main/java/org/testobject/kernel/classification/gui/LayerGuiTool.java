package org.testobject.kernel.classification.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Rectangle.Int;
import org.testobject.commons.tools.gui.DirectoryChooser;
import org.testobject.commons.tools.gui.ImageChooser;
import org.testobject.commons.tools.plot.ScalableFigure;
import org.testobject.commons.tools.plot.Visualizer.Graphics;
import org.testobject.commons.tools.plot.Visualizer.Renderable;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.functional.Functions;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.tree.r.CartesianLookupIndex;
import org.testobject.commons.util.tree.r.RTree;
import org.testobject.commons.util.tree.r.SpatialIndex;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Depiction;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.classification.classifiers.advanced.Icon;
import org.testobject.kernel.classification.graph.Optimizer;
import org.testobject.kernel.classification.graph.Printer;
import org.testobject.kernel.classification.gui.Tree.BlobAdapter;
import org.testobject.kernel.classification.gui.Tree.NodeTranslater;
import org.testobject.kernel.classification.gui.Tree.RenderGraphTranslater;
import org.testobject.kernel.classification.parser.Classification;
import org.testobject.kernel.classification.parser.Context;
import org.testobject.kernel.classification.parser.Grouping;
import org.testobject.kernel.classification.parser.Optimization;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.classification.parser.Segmentation;
import org.testobject.kernel.classification.parser.Segmenter;
import org.testobject.kernel.imaging.procedural.Transform;
import org.testobject.kernel.imaging.segmentation.Blob;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.GraphBlobBuilder;
import org.testobject.kernel.imaging.segmentation.Group;
import org.testobject.kernel.imaging.segmentation.GroupBuilder;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class LayerGuiTool {

    private static float SIGMA = 0.5f;
    private static double THRESHOLD = 4500d;

	interface Layer {

		class Raw implements Renderable {
			
			public BufferedImage image;
			
			@Override
			public void render(Graphics graphics) {
				if(image != null) {
					graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
				}
			}
			
			public void setImage(BufferedImage image) {
				this.image = image;
			}
			
			public Color getColor(int x, int y) {
				if (image != null && x < image.getWidth() && y < image.getHeight()) {
					return new Color(image.getRGB(x, y));
				} else {
					return Color.BLACK;
				}
			}
		}
		
		class Blobs implements Renderable {
			
			public Blob[] blobs;
			public BufferedImage image;

			@Override
			public void render(Graphics graphics) {
				if(image != null) {
					graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
				}
			}

			public void setImage(BufferedImage buffer, float sigma, double threshold) {
				Image.Int image = ImageUtil.Convert.toImage(buffer);
				GraphBlobBuilder blobBuilder = new GraphBlobBuilder(image.w, image.h, sigma, threshold);
                this.blobs = blobBuilder.build(image);
				this.image = ImageUtil.Convert.toBufferedImage(BlobUtils.Draw.drawHierarchy(blobs));
			}
        }
		
		class GroupsExperimental implements Renderable {
			
			private final static int FAT_X = 3, FAT_Y = 3;
			
			public Blob blobs;
			public BufferedImage image;

            private GraphBlobBuilder blobBuilder;

			@Override
			public void render(Graphics graphics) {
				if(image != null) {
					graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
				}
			}
			
			public void setImage(BufferedImage buffer) {
				// TODO user-defined sigma and threshold (en)
				Image.Int image = ImageUtil.Convert.toImage(buffer);
				this.blobs = blobBuilder.build(image)[0];
				
				int height = this.blobs.bbox.h;
				int width = this.blobs.bbox.w;
				
				BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				
				List<List<Mask>> groups = new LinkedList<>();
				groupFat(blobs.children, groups, bufferedImage, 0);	
				
				this.image = bufferedImage;
			}

            public void setBlobBuilder(GraphBlobBuilder blobBuilder) {
                this.blobBuilder = blobBuilder;
            }
			
			private class MutableGroup {
				private List<Blob> blobs = new LinkedList<>();
				private Rectangle.Int bbox = new Rectangle.Int(0, 0, 0, 0);
				
				public MutableGroup(List<MutableGroup> groups) {
					int x1 = Integer.MAX_VALUE;
					int y1 = Integer.MAX_VALUE;
					int x2 = Integer.MIN_VALUE;
					int y2 = Integer.MIN_VALUE;
					for(MutableGroup group : groups) {
						x1 = Math.min(x1, group.bbox.x);
						y1 = Math.min(y1, group.bbox.y);
						x2 = Math.max(x2, group.bbox.x + group.bbox.w);
						y2 = Math.max(y2, group.bbox.y  + group.bbox.h);
						blobs.addAll(group.blobs);
					}
					
					bbox.x = x1;
					bbox.y = y1;
					bbox.w = x2 - x1;
					bbox.h = y2 - y1;
				}
				
				public MutableGroup(Blob blob) {
					bbox.x = blob.bbox.x;
					bbox.y = blob.bbox.y;
					bbox.w = blob.bbox.w;
					bbox.h = blob.bbox.h;
					blobs.add(blob);
				}
			}
			
			
			public void groupIntersect(List<Blob> children, List<Mask> masks, BufferedImage image) {
				
	            // build spatial index
				@SuppressWarnings({ "rawtypes", "unchecked" })
	            SpatialIndex<MutableGroup> index = new CartesianLookupIndex.Factory().create(createSpatialAdapter());
	            for(Blob blob : children) {
	                index.put(new MutableGroup(blob));
	            }
				
				boolean merge = true;
				while(merge) {
					merge = false;
					for(MutableGroup group : index.entries()) {
						List<MutableGroup> intersects = new LinkedList<>();
						index.intersects(toDoubleRect(group.bbox), collect(intersects));
						
						if(intersects.size() > 1) {
							MutableGroup mergedGroup = new MutableGroup(intersects);
							
							for(MutableGroup g : intersects) {
								index.remove(g);
							}
							
							index.put(mergedGroup);
						
							merge = true;
							break;
						}
					}
				}
				
				Color[] colors = BlobUtils.Draw.generateColors();
				
				for(MutableGroup group : index.entries()) {
					Mask union = Mask.Builder.create(group.blobs);
					masks.add(union);

					int position = union.getBoundingBox().x + union.getBoundingBox().w + union.getBoundingBox().y + union.getBoundingBox().h;
					Color color = colors[position % colors.length];
					
					for(int y = 0; y < union.getBoundingBox().h; y++) {
						for(int x = 0; x < union.getBoundingBox().w; x++) {
							if(union.get(x, y)) {
								image.setRGB(union.getBoundingBox().x + x, union.getBoundingBox().y + y, color.getRGB());
							}
						}
					}
				}
				
				for(Blob child : children) {
					groupIntersect(child.children, masks, image);
				}
			}	
			
	        private <T> SpatialIndex.Visitor<T> collect(final List<T> result) {
	            return new SpatialIndex.Visitor<T>() {
	                @Override
	                public boolean visit(T payload) {
	                    result.add(payload);
	                    return true;
	                }
	            };
	        }

	        private SpatialIndex.Adapter<MutableGroup> createSpatialAdapter() {
	            return new SpatialIndex.Adapter<MutableGroup>() {
	                @Override
	                public Rectangle.Double getBoundingBox(MutableGroup payload) {
	                    return toDoubleRect(payload.bbox);
	                }
	            };
	        }
	        
            private org.testobject.commons.math.algebra.Rectangle.Double toDoubleRect(org.testobject.commons.math.algebra.Rectangle.Int rect) {
                return new org.testobject.commons.math.algebra.Rectangle.Double(rect.x, rect.y, rect.w, rect.h);
            }

			public static void groupFat(List<Blob> children, List<List<Mask>> masks, BufferedImage image, int level) {
				Color[] colors = BlobUtils.Draw.generateColors();
				
				if(level > 0) {
					List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(children, new Insets(FAT_X, FAT_Y, FAT_X, FAT_Y));
					
					for(Group<Blob> group : groups) {
						List<Mask> maskGroup = Lists.newArrayList(group.getContent().size());
						for(Blob blob : group.getContent()) {
							maskGroup.add(blob);
						}
						masks.add(maskGroup);
	
						int position = group.getBoundingBox().x + group.getBoundingBox().w + group.getBoundingBox().y + group.getBoundingBox().h;
						Color color = colors[position % colors.length];
						
						for(Blob blob : group.getContent()) {
							for(int y = 0; y < blob.bbox.h; y++) {
								for(int x = 0; x < blob.bbox.w; x++) {
									if(blob.get(x, y)) {
										image.setRGB(blob.bbox.x + x, blob.bbox.y + y, color.getRGB());
									}
								}
							}
						}
					}
				} else {
					for(Blob blob : children) {
						masks.add(Lists.<Mask>toList(blob));
	
						int position = blob.getBoundingBox().x + blob.getBoundingBox().w + blob.getBoundingBox().y + blob.getBoundingBox().h;
						Color color = colors[position % colors.length];
						
						for(int y = 0; y < blob.bbox.h; y++) {
							for(int x = 0; x < blob.bbox.w; x++) {
								if(blob.get(x, y)) {
									image.setRGB(blob.bbox.x + x, blob.bbox.y + y, color.getRGB());
								}
							}
						}
					}
				}
				
				for(Blob child : children) {
					groupFat(child.children, masks, image, level + 1);
				}
			}	
		}
		
		class Groups implements Renderable {

			public Node labels;
			public BufferedImage image;

			@Override
			public void render(Graphics graphics) {
				if(image != null) {
					graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
				}
			}
			
			public void setImage(BufferedImage buffer, float sigma, double threshold) {
				Image.Int image = ImageUtil.Convert.toImage(buffer);

				Segmentation segmentation = new Segmentation(new Segmenter.Graph(sigma, threshold));
				Grouping.Fat grouping = new Grouping.Fat();

				Node segments = segmentation.apply(image);
				Node groups = grouping.apply(segments, Context.Factory.none());

				BufferedImage bufferedImage = new BufferedImage(image.w, image.h, BufferedImage.TYPE_INT_ARGB);

				Color[] colors = BlobUtils.Draw.generateColors();
				draw(groups, colors, bufferedImage);

				this.labels = groups;
				this.image = bufferedImage;
			}

			private void draw(Node node, Color[] colors, BufferedImage bufferedImage) {

				// draw
				if(Qualifier.Factory.Class.group.equals(node.getElement().getLabel().getQualifier().getType())) {

					for(Mask mask : node.getElement().getMasks()) {
						int position = mask.getBoundingBox().x + mask.getBoundingBox().w + mask.getBoundingBox().y + mask.getBoundingBox().h;
						Color color = colors[position % colors.length];
						for(int y = 0; y < mask.getBoundingBox().h; y++) {
							for(int x = 0; x < mask.getBoundingBox().w; x++) {
								if(mask.get(x, y)) {
									bufferedImage.setRGB(mask.getBoundingBox().x + x, mask.getBoundingBox().y + y, color.getRGB());
								}
							}
						}
					}
				}

				// recursion
				for(Node child : node.getChildren()) {
					draw(child, colors, bufferedImage);
				}
			}
		}

		class Classes implements Renderable {
			
			public Node labels;
			public BufferedImage image;

            private Map<String, Image.Int> trainedImages = new HashMap<>();

            @Override
			public void render(Graphics graphics) {
				if(image != null) {
					graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
				}
			}

            public void setTrainedImages(Map<String, Image.Int> trainedImages) {
                this.trainedImages = trainedImages;
            }

            public void setImage(BufferedImage buffer, float sigma, double threshold) {
				Image.Int raw = ImageUtil.Convert.toImage(buffer);

				List<Classifier> classifiers = Lists.newLinkedList();
				{
					// image
					{
						org.testobject.kernel.classification.classifiers.advanced.Image.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Image.Trainer();
						Classifier classifier = fromMap(trainer, trainedImages);
						classifiers.add(classifier);
					}
					// icon
					{
						org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer();
						Classifier classifier = trainer.train(Lists.toList("region", "tour", "map"), readImages("regions.png", "tour.png", "map.png"));
						classifiers.add(classifier);
					}
				}
				
				Segmentation segmentation = new Segmentation(new Segmenter.Graph(sigma, threshold));
				Grouping.Fat grouping = new Grouping.Fat();
				Classification classification = new Classification(classifiers);
				
				Node segments = segmentation.apply(raw);
				Node groups = grouping.apply(segments, Context.Factory.none());
				Node labels = classification.apply(groups, Context.Factory.create(raw));
				
				this.labels = labels;
			}

            private List<Image.Int> readImages(String ... files) {
				try {
					List<Image.Int> images = Lists.newArrayList(files.length);
					for(String file : files) {
						images.add(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/images/" + file)));
					}
					return images;
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}

            private Classifier fromMap(org.testobject.kernel.classification.classifiers.advanced.Image.Trainer trainer, Map<String, Image.Int> map) {
                List<String> names = Lists.newArrayList(map.size());
                List<Image.Int> images = Lists.newArrayList(map.size());

                for (Map.Entry<String, Image.Int> entry : map.entrySet()) {
                    names.add(entry.getKey());
                    images.add(entry.getValue());
                }

                return trainer.train(names, images);
            }
		}
		
		class Winners implements Renderable {
			
			public Node labels;
			public BufferedImage image;

            @Override
			public void render(Graphics graphics) {
				if(image != null) {
					graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
				}
			}

            public void setImage(BufferedImage buffer, float sigma, double threshold) {
            	
            	// low pass
				Image.Int lowpass = ImageUtil.Convert.toImage(buffer);
				Blob[] segments = new GraphBlobBuilder(lowpass.w, lowpass.h, 0, 50000, 20).build(lowpass);
            	
            	// high pass
				Image.Int highpass = ImageUtil.Convert.toImage(buffer);
				Blob[] blobs = new GraphBlobBuilder(highpass.w, highpass.h, sigma, threshold, 20).build(highpass);
				
				// classifiers
				org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer trainer = new Icon.Trainer();
				Icon.Classifier classifier = trainer.train(Lists.toList("region", "tour", "map"), readImages("regions.png", "tour.png", "map.png"));
				Classifier.Images images = org.testobject.kernel.api.classification.classifiers.Classifier.Images.Factory.create(highpass);

				labels = Node.Factory.create(Element.Factory.node("root", new Rectangle.Int(0, 0, highpass.w, highpass.h)));
				
				// grouping
				int height = blobs[0].bbox.h;
				int width = blobs[0].bbox.w;
				BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				List<List<Mask>> groups = new LinkedList<>();
				GroupsExperimental.groupFat(blobs[0].children, groups, bufferedImage, 0);
				
				// classify
				int id = 0;
				for(List<Mask> group : groups) {
					List<Classifier.Proposal> proposals = classifier.classify(images, Classifier.Lookup.Factory.blob(), group, Classifier.Context.Factory.create(Qualifier.Factory.Class.group(id++)));
					for(Classifier.Proposal proposal : proposals) {
						labels.getChildren().add(Node.Factory.create(proposal.element()));
					}
				}
				
				// optimize
				Optimizer.BoxAdapter<Blob> adapter1 = new Optimizer.BoxAdapter<Blob>() {
                    @Override
                    public org.testobject.commons.math.algebra.Rectangle.Double getBoundingBox(Blob contestant) {
                        return toDoubleRect(contestant.getBoundingBox());
                    }

                    private org.testobject.commons.math.algebra.Rectangle.Double toDoubleRect(org.testobject.commons.math.algebra.Rectangle.Int rect) {
                        return new org.testobject.commons.math.algebra.Rectangle.Double(rect.x, rect.y, rect.w, rect.h);
                    }
                };
				Optimizer.LabelAdapter<Node> adapter2 = new Optimizer.LabelAdapter<Node>() {
                    @Override
                    public org.testobject.commons.math.algebra.Rectangle.Double getBoundingBox(Node contestant) {
                        return toDoubleRect(contestant.getElement().getBoundingBox());
                    }

                    private org.testobject.commons.math.algebra.Rectangle.Double toDoubleRect(org.testobject.commons.math.algebra.Rectangle.Int rect) {
                        return new org.testobject.commons.math.algebra.Rectangle.Double(rect.x, rect.y, rect.w, rect.h);
                    }

                    @Override
                    public double getProbability(Node contestant) {
                        return contestant.getElement().getLabel().getLikelihood().photometric();
                    }
                };
                Optimizer.LowPassBreedWinners<Blob, Node> optimizer = new Optimizer.LowPassBreedWinners<Blob, Node>(adapter1, adapter2, new RTree.Factory<Node>());

                List<Node> leaves = Lists.newLinkedList();
                for (Node node : optimizer.optimize(Arrays.asList(segments), labels.getChildren())) {
                    leaves.add(node);
                    Printer.print(node);
                }

                labels.getChildren().clear();
                labels.getChildren().addAll(leaves);

				Printer.print(labels);
			}
			
            private List<Image.Int> readImages(String ... files) {
				try {
					List<Image.Int> images = Lists.newArrayList(files.length);
					for(String file : files) {
						images.add(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/images/" + file)));
					}
					return images;
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		class Synthetic implements Renderable {
			
			public org.testobject.kernel.imaging.procedural.Node labels;
			public BufferedImage image;

            private Map<String, Image.Int> trainedImages = new HashMap<>();

            @Override
			public void render(Graphics graphics) {
				if(image != null) {
					graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
				}
			}

            public void setImage(BufferedImage buffer, float sigma, double threshold) {
				final Image.Int raw = ImageUtil.Convert.toImage(buffer);
				
				List<Classifier> classifiers = Lists.newLinkedList();
				{
					// image
					{
						org.testobject.kernel.classification.classifiers.advanced.Image.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Image.Trainer();
						Classifier classifier = fromMap(trainer, trainedImages);
						classifiers.add(classifier);
					}
					// icon
					{
						org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer();
						Classifier classifier = trainer.train(Lists.toList("region", "tour", "map"), readImages("regions.png", "tour.png", "map.png"));
						classifiers.add(classifier);
					}
				}
				
				Segmentation segmentation = new Segmentation(new Segmenter.Graph(sigma, threshold));
				Grouping.Fat grouping = new Grouping.Fat();
				Classification classification = new Classification(classifiers);
				Optimization optimization = new Optimization.Flat(Parser.Builder.lowpass(), new Functions.Function0<Node>() {
					@Override
					public Node apply() {
						return new Segmentation(new Segmenter.Graph(0f, 50000d)).apply(raw);
					}
				});
				
				Node segments = segmentation.apply(raw);
				Node groups = grouping.apply(segments, Context.Factory.none());
				Node labels = classification.apply(groups, Context.Factory.create(raw));
				Node winners = optimization.apply(Lists.toList(labels), Context.Factory.create(raw));
				
				this.labels = toGraph(winners);
				this.image = ImageUtil.Convert.toBufferedImage(draw(this.labels));
				
				org.testobject.kernel.imaging.procedural.Printer.print(this.labels);
			}
            
            private Image.Int draw(org.testobject.kernel.imaging.procedural.Node graph) {
            	return org.testobject.kernel.imaging.procedural.Util.render(graph);
            }
            
            private org.testobject.kernel.imaging.procedural.Node toGraph(Node source) {
            	org.testobject.kernel.imaging.procedural.Node.Builder target = org.testobject.kernel.imaging.procedural.Node.Builder.node();
            	toGraph(source, target);
            	
            	return target.build();
            }
            
            private void toGraph(Node source, org.testobject.kernel.imaging.procedural.Node.Builder target) {
            	
            	Depiction depiction = getDepiction(source);
            	
            	if(depiction != null) {
            		org.testobject.kernel.imaging.procedural.Node.Builder child = org.testobject.kernel.imaging.procedural.Node.Builder
            				.node(Transform.Builder.translate(depiction.getTranslate().x, depiction.getTranslate().y))
            				.child(depiction.getGraph());
            		
            		target.child(child);
            	}

            	
            	for(Node child : source.getChildren()) {
            		toGraph(child, target);
            	}
			}
            
            private Depiction getDepiction(Node node) {
            	for(Variable<?> feature : node.getElement().getLabel().getFeatures()) {
					if(feature.getValue() instanceof Depiction) {
						return (Depiction) feature.getValue();
					}
				}
            	
            	return null;
            }
            
            public void draw(Node node, BufferedImage image) {
            	for(Variable<?> feature : node.getElement().getLabel().getFeatures()) {
					if(feature.getValue() instanceof Depiction) {
						Depiction depiction = (Depiction) feature.getValue();
						org.testobject.kernel.imaging.procedural.Node graph = depiction.getGraph();
						Point.Double offset = new Point.Double(depiction.getTranslate().x, depiction.getTranslate().y);
						org.testobject.kernel.imaging.procedural.Util.render(graph, offset, image);
					}
				}
            	
            	for(Node child : node.getChildren()) {
            		draw(child, image);
            	}
			}

            private List<Image.Int> readImages(String ... files) {
				try {
					List<Image.Int> images = Lists.newArrayList(files.length);
					for(String file : files) {
						images.add(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/images/" + file)));
					}
					return images;
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
            
            private Classifier fromMap(org.testobject.kernel.classification.classifiers.advanced.Image.Trainer trainer, Map<String, Image.Int> map) {
                List<String> names = Lists.newArrayList(map.size());
                List<Image.Int> images = Lists.newArrayList(map.size());

                for (Map.Entry<String, Image.Int> entry : map.entrySet()) {
                    names.add(entry.getKey());
                    images.add(entry.getValue());
                }

                return trainer.train(names, images);
            }
		}

		
		class BoundingBoxes implements Renderable {
			
			private List<org.testobject.commons.math.algebra.Rectangle.Int> boxes;
	
			@Override
			public void render(Graphics graphics) {
				if(boxes != null) {
					graphics.setAlpha(0.6f);
					{
						graphics.setColor(Color.RED);
						for(org.testobject.commons.math.algebra.Rectangle.Int box : boxes) {
							graphics.drawRect(box.x, box.y, box.w, box.h);
						}
					}
					graphics.setAlpha(1f);
				}
			}
			
			public void setBoxes(List<org.testobject.commons.math.algebra.Rectangle.Int> boxes) {
				this.boxes = boxes;
			}
		}
		
		class Box implements Renderable {
			
			private org.testobject.commons.math.algebra.Rectangle.Int box;
	
			@Override
			public void render(Graphics graphics) {
				if(box != null) {
					graphics.setAlpha(0.6f);
					{
						graphics.setColor(Color.RED);
						final int border = 10;
						for (int i = 0; i < 5; i++) {
							graphics.drawRect(box.x - border + i, box.y - border + i, box.w + 2 * border - 2 * i, box.h + 2 * border - 2 * i);
						}
					}
					{
						graphics.setColor(Color.GREEN);
						graphics.fillRect(box.x, box.y, box.w, box.h);
					}
					graphics.setAlpha(1f);
				}
			}
			
			public void setBox(org.testobject.commons.math.algebra.Rectangle.Int box) {
				this.box = box;
			}
		}
	}
	
	private class Status {
		
		public final JLabel status;
		public double x = 0, y = 0;
		public Color color = Color.black;
		
		public Status(JLabel status) {
			this.status = status;
		}
		
		public void refresh() {
			status.setText("scale: " + figure.scale + " position (x,y): " + x + "," + y 
					+ " color (a,r,g,b): " + color.getAlpha() + "," + color.getRed() + "," 
					+ color.getGreen() + "," + color.getBlue());
		}
	}
	
	// const
	private final float SCALE_STEP = 0.5f;
	
	// swing
	private final JFrame frame = new JFrame();
	private final JScrollPane imagePanel;
	private final JTree tree;
	
	// layers
	private final ScalableFigure figure = new ScalableFigure();
    private final Layer.Raw rawLayer;
    private final Layer.BoundingBoxes boxesLayer;
    private final Layer.Box boxLayer;
    private final Layer.Blobs blobLayer;
    private final Layer.Groups groupsLayer;
    private final Layer.Classes classesLayer;
    private final Layer.Winners winnersLayer;
    private final Layer.Synthetic syntheticLayer;

	// state
	private final Status status;

    private File file;
    private Map<String, Image.Int> trainedImages = new HashMap<>();


    private JTextField sigma = new JTextField(Float.toString(SIGMA), 4);
    private JTextField threshold = new JTextField(Double.toString(THRESHOLD), 6);

	public LayerGuiTool(File image) {
        this.file = image;
		
		// frame
		{
			this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		// figure
		{
			this.rawLayer = new Layer.Raw();
			this.boxesLayer = new Layer.BoundingBoxes();
			this.boxLayer = new Layer.Box();
            this.blobLayer = new Layer.Blobs();
            this.groupsLayer = new Layer.Groups();
            this.classesLayer = new Layer.Classes();
            this.winnersLayer = new Layer.Winners();
            this.syntheticLayer = new Layer.Synthetic();

			this.setActiveLayer(this.rawLayer);
			this.figure.resize();
		}
		
		// scroll pane
		{
			this.imagePanel = new JScrollPane(this.figure);
            this.imagePanel.setPreferredSize(new Dimension(600, 800));
            fixScrollRendering(this.imagePanel.getViewport());
		}
		
		// layout
		{
			this.frame.getContentPane().setLayout(new BorderLayout());
		}
		
		// tree
		{
			this.tree = Tree.Factory.empty();
			this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
			this.tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					TreeNode node = (TreeNode) tree.getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					org.testobject.commons.math.algebra.Rectangle.Int box = Tree.Util.getBoundingBox(node);
					boxLayer.setBox(box);
                    System.out.println(box);
					imagePanel.repaint();
				}
			});
		}

		// pane
		{
			JScrollPane leftPane = new JScrollPane(tree);
			{
				fixScrollRendering(leftPane.getViewport());
				leftPane.setPreferredSize(new Dimension(200, 200));
			}
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, imagePanel);
			this.frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		}

		// menu-bar
		{
            JMenuBar bar = new JMenuBar();
			{
                JMenu file = new JMenu("File");
                file.add(item("Load Image", new ActionListener() {
							public void actionPerformed(ActionEvent event) {
								ImageChooser.choose(frame, new ImageChooser.OnLoad() {
                                    @Override
                                    public void load(File... file) {
                                        LayerGuiTool.this.file = file[0];
                                        setImage(file[0]);
                                    }
                                }, new File("./src/test/resources"), false);
							}
						}));
                file.add(item("Save blobs", new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        DirectoryChooser.choose(frame, new DirectoryChooser.OnLoad() {
                            @Override
                            public void load(File file) {
                                try {
                                    Blob[] blobs = LayerGuiTool.this.blobLayer.blobs;
                                    Image.Int image = ImageUtil.Convert.toImageInt(ImageIO.read(LayerGuiTool.this.file));

                                    for (int i = 0; i < blobs.length; i++) {
                                        Blob blob = blobs[i];
                                        BufferedImage bufferedImageContour = ImageUtil.Convert.toBufferedImage(BlobUtils.Cut.cutByContour(image, blob));
                                        ImageIO.write(bufferedImageContour, "png", new File(file, i + "Contour.png"));
                                        BufferedImage bufferedImageMask = ImageUtil.Convert.toBufferedImage(BlobUtils.Cut.cutByMask(image, blob));
                                        ImageIO.write(bufferedImageMask, "png", new File(file, i + "Mask.png"));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new File("./src/test/resources"));
                    }
                }));
				file.add(item("Exit", new ActionListener() {
							public void actionPerformed(ActionEvent event) {
								frame.dispose();
							}
						}));
				bar.add(file);
			}
			{
				JMenu layer = new JMenu("Graph");
				layer.add(item("Blobs", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
                        fillGraphWithBlobs();
                    }
				}));
				layer.add(item("Groups", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraphWithGroups();
					}
				}));
				layer.add(item("Classes", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
                        fillGraphWithClasses();
                    }
				}));
				layer.add(item("Winners", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraphWithWinners();
                    }
				}));
				layer.add(item("Synthetic", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraphWithSynthetic();
                    }
				}));
				bar.add(layer);
			}
			{
				JMenu layer = new JMenu("Layer");
				layer.add(item("Raw", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						setActiveLayer(rawLayer);
					}
				}));
				layer.add(item("Blobs", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						setActiveLayer(blobLayer);
					}
				}));
				layer.add(item("Groups", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						setActiveLayer(groupsLayer);
					}
				}));
				layer.add(item("Synthetic", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						setActiveLayer(syntheticLayer);
					}
				}));
				layer.add(item("Boxes", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						setActiveLayer(boxesLayer);
					}
				}));
				bar.add(layer);
			}
			{
				JMenu layer = new JMenu("Options");
                layer.add(item("Train images", new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        ImageChooser.choose(frame, new ImageChooser.OnLoad() {
                            @Override
                            public void load(File ... files) {
                                for (File file : files) {
                                    try {
                                        Image.Int image = ImageUtil.Convert.toImage(ImageIO.read(file));
                                        LayerGuiTool.this.trainedImages.put(file.getName(), image);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new File("./src/test/resources"), true);
                    }
                }));
				bar.add(layer);
			}
			this.frame.setJMenuBar(bar);
		}
		
		// toolbar
		{
			final JToolBar toolbar = new JToolBar();
			{
				final JButton button = new JButton("Zoom in");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						increaseScale(+SCALE_STEP);
						figure.resize();
						imagePanel.revalidate();
						frame.repaint(50);
					}
				});
				toolbar.add(button);
			}
			{
				final JButton button = new JButton("Zoom out");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						increaseScale(-SCALE_STEP);
						figure.resize();
						imagePanel.revalidate();
						frame.repaint(50);
					}
				});
				toolbar.add(button);
			}
            {
                JLabel label = new JLabel("Threshold");
                threshold.setInputVerifier(new InputVerifier() {
                    @Override
                    public boolean verify(JComponent input) {
                        String text = ((JTextField) input).getText();
                        try {
                            Double value = new Double(text);
                            return value >= 0;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                });
                toolbar.add(label);
                toolbar.add(threshold);
            }
            {
                JLabel label = new JLabel("Sigma");
                sigma.setInputVerifier(new InputVerifier() {
                    @Override
                    public boolean verify(JComponent input) {
                        String text = ((JTextField) input).getText();
                        try {
                            Float value = new Float(text);
                            return value >= 0;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                });
                toolbar.add(label);
                toolbar.add(sigma);
            }
            {
                final JButton button = new JButton("Reload");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setImage(file);
                    }
                });
                toolbar.add(button);
            }
			this.frame.getContentPane().add(toolbar, BorderLayout.NORTH);
		}
		
		// status bar
		{
			final JLabel label = new JLabel("status");
			final JPanel statusPanel = new JPanel();
			statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			statusPanel.add(label);

			this.frame.add(statusPanel, BorderLayout.SOUTH);
			this.status = new Status(label);
		}
		
		// mouse
		{
			figure.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
					float scale = figure.getScale();
					status.x = Math.floor(e.getX() / scale);
					status.y = Math.floor(e.getY() / scale);
					status.color = rawLayer.getColor((int) status.x, (int) status.y);
					status.refresh();
				}

				@Override
				public void mouseDragged(MouseEvent e) {
				}
			});
		}

        // state
        {
            this.setImage(image);
        }
	}
	
	private void fixScrollRendering(JViewport viewport) {
		viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
	}

    private void fillGraphWithClasses() {
		List<Int> boxes = Tree.Factory.fill(tree, new NodeTranslater(), classesLayer.labels);
		this.boxesLayer.setBoxes(boxes);
    }
    
    private void fillGraphWithWinners() {
		List<Int> boxes = Tree.Factory.fill(tree, new NodeTranslater(), winnersLayer.labels);
		this.boxesLayer.setBoxes(boxes);
    }
    
    private void fillGraphWithSynthetic() {
		List<Int> boxes = Tree.Factory.fill(tree, new RenderGraphTranslater(), syntheticLayer.labels);
		this.boxesLayer.setBoxes(boxes);
    }
    
    private void fillGraphWithGroups() {
		List<Int> boxes = Tree.Factory.fill(tree, new NodeTranslater(), groupsLayer.labels);
		this.boxesLayer.setBoxes(boxes);
    }

    private void fillGraphWithBlobs() {
    	List<Int> boxes = Tree.Factory.fill(tree, new BlobAdapter(), blobLayer.blobs[0]);
    	this.boxesLayer.setBoxes(boxes);
    }

    private void setActiveLayer(Renderable renderable) {
		this.figure.removeLayers();
		this.figure.addLayer(renderable);
		this.figure.addLayer(this.boxLayer);
		this.frame.repaint(50);
	}
	
	private JMenuItem item(String title, final ActionListener listener) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(listener);
		return item;
	}

	public void increaseScale(float step) {
		this.figure.scale += step;
		this.status.refresh();
		this.frame.repaint(50);
	}
	
	public void setImage(File file) {
		BufferedImage image = read(file);

        float sigma = Float.valueOf(this.sigma.getText());
        double threshold = Double.valueOf(this.threshold.getText());

        this.classesLayer.setTrainedImages(trainedImages);

		this.rawLayer.setImage(image);
		this.blobLayer.setImage(image, sigma, threshold);
		this.groupsLayer.setImage(image, sigma, threshold);
		this.classesLayer.setImage(image, sigma, threshold);
        this.winnersLayer.setImage(image, sigma, threshold);
        this.syntheticLayer.setImage(image, sigma, threshold);

        fillGraphWithBlobs();

        this.frame.setTitle(file.getName());
        this.frame.repaint(50);
	}
	
	public void display() {
		this.frame.pack();
		this.frame.setVisible(true);
	}
	
	private static BufferedImage read(File file) {
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String... args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		final LayerGuiTool gui = new LayerGuiTool(new File("../resources/src/main/resources/android/4_0_3/matching/framebuffers/komoot/fertig_2_3_3.png"));

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.display();
			}
		});
	}
}