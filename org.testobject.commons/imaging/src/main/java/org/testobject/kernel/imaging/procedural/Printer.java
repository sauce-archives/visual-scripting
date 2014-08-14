package org.testobject.kernel.imaging.procedural;


/**
 * 
 * @author enijkamp
 *
 */
public class Printer {
	
	public static void print(Node node) {
		print(node, 0);
	}
	
	private static void print(Node node, int indent) {
		
		// node
		{
			System.out.println(spaces(indent) + "node");
			indent++;
			{
				System.out.println(spaces(indent) + "transform");
				indent++;
				{
					Transform transform = node.getTransform();
					System.out.println(spaces(indent) + transform.toString());
				}
				indent--;
			}
			indent--;
		}
		
		indent++;
		// elements
		if(node.getElements().isEmpty() == false) {
			System.out.println(spaces(indent) + "elements");
			indent++;
			for(Edge<Element> edge : node.getElements()) {
				if(edge.getTarget() != null) {
					print(indent, edge.getTarget());
				}
			}
			indent--;
		}
		
		// nodes
		{
			for(Edge<Node> edge : node.getNodes()) {
				print(edge.getTarget(), indent);
			}
		}
		indent--;
	}
	
	private static void print(int indent, Element element) {
		System.out.println(spaces(indent) + toString(element));
		indent++;
		{
			if(element.getStyle() != null) {
				Style style = element.getStyle();
				System.out.println(spaces(indent) + "style");
				indent++;
				{
					if(style.getFill() != null) {
						System.out.println(spaces(indent) + "fill = " + style.getFill());
					}
					
					if(style.getStroke() != null) {
						System.out.println(spaces(indent) + "stroke = " + style.getStroke());
					}
				}
				indent--;
			}
		}
		indent--;
	}
	
	private static String spaces(int indent) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < indent; i++) {
			builder.append(" ");
		}
		return builder.toString();
	}
	
	private static String toString(Element element) {
		return element.getClass().getInterfaces()[0].getSimpleName().toLowerCase();
	}
	
}
