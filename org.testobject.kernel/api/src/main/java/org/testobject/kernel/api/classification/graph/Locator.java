package org.testobject.kernel.api.classification.graph;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.kernel.api.classification.classifiers.Classifier;

/**
 * 
 * @author enijkamp
 *
 */
public interface Locator {
	
	interface Qualifier {
		
		LinkedList<Descriptor> getPath();
		
		class Factory {
			
			private static final Locator.Qualifier none = create();
			
			public static Qualifier create(final LinkedList<Descriptor> discriptors) {
				return new Qualifier() {
					@Override
					public LinkedList<Descriptor> getPath() {
						return discriptors;
					}
				};
			}
			
			public static Qualifier create(final Descriptor ... discriptors) {
				final LinkedList<Descriptor> path = Lists.newLinkedList(discriptors);
				return new Qualifier() {
					@Override
					public LinkedList<Descriptor> getPath() {
						return path;
					}
				};
			}

			public static Locator.Qualifier none() {
				return none;
			}
		}
		
	}

    @JsonDeserialize(as = Node.Factory.Impl.class)
	interface Node {
		
    	@JsonProperty("children")
		List<Node> getChildren();
		
    	@JsonProperty("element")
		Descriptor getDescriptor();
		
		class Factory {

            private static final class Impl implements Node {

                private final Descriptor element;
                private final List<Node> children;

                @JsonCreator
                private Impl(@JsonProperty("element") Descriptor element, @JsonProperty("children") List<Node> children) {
                    this.element = element;
                    this.children = children;
                }

                @Override
                public List<Node> getChildren() {
                    return children;
                }

                @Override
                public Descriptor getDescriptor() {
                    return element;
                }
            }

			public static Node create(final Descriptor descriptor, final List<Node> childs) {
				return new Impl(descriptor, childs);
			}

			public static Node none() {
				return new Impl(Descriptor.Factory.none(), Lists.<Node>empty());
			}
		}
		
	}

    @JsonDeserialize(as = Descriptor.Factory.Impl.class)
	interface Descriptor {
		
    	@JsonProperty("id")
		int getId();

    	@JsonProperty("label")
		Classifier.Qualifier getLabel();
    	
    	@JsonProperty("features")
		List<Variable<?>> getFeatures();
		
		class Factory {

			private static final class Impl implements Descriptor {

				private final int id;
				private final Classifier.Qualifier label;
                private final List<Variable<?>> features;

                @JsonCreator
                private Impl(@JsonProperty("id") int id, @JsonProperty("label") Classifier.Qualifier label, @JsonProperty("features") List<Variable<?>> features) {
                    this.id = id;
					this.label = label;
                    this.features = features;
                }
                
                @Override
                public int getId() {
                	return id;
                }

                @Override
                public Classifier.Qualifier getLabel() {
                    return label;
                }

				@Override
				public List<Variable<?>> getFeatures() {
					return features;
				}
				
				@Override
				public String toString() {
					return "{id='" + id + "', label='" + label + "'}";
				}
            }
			
			public static Descriptor create(int id, Classifier.Qualifier qualifier, List<Variable<?>> variables) {
				return new Impl(id, qualifier, variables);
			}
			
			public static Descriptor none() {
				return new Impl(0, Classifier.Qualifier.Factory.none(), Lists.<Variable<?>>empty());
			}

			public static Descriptor toDescriptor(int id, Element element) {
				
				final List<Variable<?>> features = copy(element.getLabel().getFeatures());
				
				features.add(Variable.Builder.value(Variable.Names.Depiction.masks, element.getMasks()));
				features.add(Variable.Builder.value(Variable.Names.Geometric.position, element.getBoundingBox().getLocation()));
				features.add(Variable.Builder.value(Variable.Names.Geometric.size, element.getBoundingBox().getSize()));
				
				return new Impl(id, element.getLabel().getQualifier(), features);
			}

			private static List<Variable<?>> copy(List<Variable<?>> source) {
				return Lists.newArrayList(source);
			}
		}
	}
	
	class Printer {
		
		public static String toString(List<Descriptor> path) {
			String result = "";
			if (path.isEmpty()) {
				return result;
			} else {
				Iterator<Descriptor> iter = path.iterator();
				for (int i = 0; i < path.size() - 1; i++) {
					Descriptor descriptor = iter.next();
					result += descriptor.getLabel().toString() + "->";
				}
				return result + iter.next().toString();
			}
		}
		
		public static void print(Node node, PrintStream out) {
			print(node, 0, out);
		}
		
		public static void print(Node node) {
			print(node, 0, System.out);
		}
		
		public static void print(Node node, int indent, PrintStream out, String ... variables) {
			// elements
			{
				print(node.getDescriptor(), indent, out, variables);
			}
			// nodes
			indent++;
			{
				for(Node child : node.getChildren()) {
					print(child, indent, out, variables);
				}
			}
			indent--;
		}
		
		public static void print(Descriptor element, PrintStream out) {
			print(element, 0, out);
		}
		
		public static void print(Descriptor element, int indent, PrintStream out, String ... names) {
			out.println(spaces(indent) + toString(element));
            List<Variable<?>> variables = element.getFeatures();
            Set<String> filter = Sets.from(names);
            for (Variable<?> variable : variables) {
            	if(filter.isEmpty() || filter.contains(variable.getName())) {
            		out.println(spaces(indent + 1) + variable);
            	}
            }
        }

		private static String spaces(int indent) {
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < indent; i++) {
				builder.append(" ");
			}
			return builder.toString();
		}
		
		private static String toString(Descriptor element) {
			return element.getLabel().toString();
		}
	}

}
