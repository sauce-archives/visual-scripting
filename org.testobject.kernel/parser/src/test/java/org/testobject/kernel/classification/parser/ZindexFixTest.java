package org.testobject.kernel.classification.parser;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Rectangle.Int;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.util.VariableUtil;

/**
 * 
 * @author inbar
 *
 */
public class ZindexFixTest {

	private final Random random = new Random(10);

	private final boolean enableVisualDisplay = false;

	public static class Element {

		public final Rectangle.Int rect;
		private List<Element> childs;

		public Element(Int rect, List<Element> childs) {
			this.rect = rect;
			this.childs = childs;
		}

		@Override
		public String toString() {
			return "{ " + printRect(rect) + ", " + printRectList(getChilds()) + " }";

		}

		public String printRect(Rectangle.Int rect) {
			return "(" + rect.x + "," + rect.y + "," + rect.h + "," + rect.w + ")";
		}

		public String printRectList(List<Element> list) {
			String res = "[ ";
			for (Element elm : list) {
				res += elm + ",";
			}

			res += " ]";

			return res;

		}

		public void setChilds(List<Element> childs) {
			this.childs = childs;
		}

		public List<Element> getChilds() {
			return childs;
		}
	}

	public interface Adapter<T> {

		Rectangle.Int getBox(T t);

		List<T> getChilds(T t);

		void setChilds(T t, List<T> childs);

	}

	public static class ElementAdapter implements Adapter<Element> {
		@Override
		public Rectangle.Int getBox(Element t) {
			return t.rect;
		}

		@Override
		public List<Element> getChilds(Element t) {
			return t.childs;
		}

		@Override
		public void setChilds(Element t, List<Element> childs) {
			t.setChilds(childs);
		}
	}

	public static class LocatorAdapter implements Adapter<Locator.Node> {
		@Override
		public Rectangle.Int getBox(Locator.Node node) {
			Locator.Descriptor descriptor = node.getDescriptor();

			Point.Int position = VariableUtil.getPosition(descriptor.getFeatures());
			Size.Int size = VariableUtil.getSize(descriptor.getFeatures());

			return new Rectangle.Int(position, size);
		}

		@Override
		public List<Locator.Node> getChilds(Locator.Node t) {
			return t.getChildren();
		}

		@Override
		public void setChilds(Locator.Node node, List<Locator.Node> childs) {
			node.getChildren().clear();
			node.getChildren().addAll(childs);
		}
	}

	public static class ZLevelFix<T> {

		private final Adapter<T> adapter;

		public ZLevelFix(Adapter<T> adapter) {
			this.adapter = adapter;
		}

		public T fix(T t) {

			List<T> childs = adapter.getChilds(t);
			if (childs != null && !childs.isEmpty())
				adapter.setChilds(t, fixChilds(childs));

			return t;

		}

		private List<T> fixChilds(List<T> childs) {

			ArrayList<T> newChilds = Lists.newArrayList();
			boolean contained[] = new boolean[childs.size()];
			System.out.println("fix childs: " + childs);

			// first pass 
			for (int i = 0; i < childs.size() - 1; i++) {
				for (int j = i + 1; j < childs.size(); j++) {
					T a = childs.get(i);
					T b = childs.get(j);
					if (!contained[j] && isContained(a, b)) {
						System.out.println("1) " + a + " contains " + b);
						adapter.getChilds(a).add(b);
						contained[j] = true;
					}
				}
			}

			// second pass  
			for (int i = childs.size() - 1; i >= 1; i--) {
				for (int j = i - 2; j >= 0; j--) {
					T a = childs.get(i);
					T b = childs.get(j);
					if (!contained[j] && isContained(a, b)) {
						System.out.println("2) i=" + i + " j=" + j + "  " + a + " contains " + b);
						adapter.getChilds(a).add(b);
						contained[j] = true;
					}
				}
			}

			for (int i = 0; i < contained.length; i++) {
				if (!contained[i]) {
					System.out.println("adding " + childs.get(i));
					newChilds.add(childs.get(i));
				}
			}

			for (T elm : newChilds) {
				fix(elm);
			}

			return newChilds;

		}

		private boolean isContained(T t1, T t2) {

			Int box2 = adapter.getBox(t2);
			Int box1 = adapter.getBox(t1);

			return box1.contains(box2.x, box2.y, box2.h, box2.w);

		}
	}

	public void drawElement(Element elm, BufferedImage buf, int level) {

		Graphics g = buf.createGraphics();
		g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
		g.fillRect(elm.rect.x, elm.rect.y, elm.rect.w, elm.rect.h);
		g.setColor(Color.white);
		g.drawString(String.valueOf(level), elm.rect.x + 10, elm.rect.y + 10);
		g.dispose();

		for (Element e : elm.getChilds()) {
			drawElement(e, buf, level + 1);
		}

	}

	@Test
	public void fixRects() throws IOException {

		Element r_0 = createElement(10, 10, 300, 300);
		Element r_0_0 = createElement(50, 50, 200, 200);
		Element r_0_0_1 = createElement(100, 100, 100, 100);
		Element r_0_0_1_0 = createElement(150, 150, 40, 40);
		Element r_0_1_1_1 = createElement(110, 110, 50, 50);
		Element r_0_1_1_1_0 = createElement(120, 130, 20, 20);

		List<Element> childs = Lists.newLinkedList(new Element[] { r_0, r_0_0, r_0_0_1, r_0_0_1_0, r_0_1_1_1, r_0_1_1_1_0 });

		Element r = new Element(new Rectangle.Int(0, 0, 500, 500), childs);

		if (enableVisualDisplay) {
			BufferedImage buf = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
			drawElement(r, buf, 0);
			ImageUtil.show(buf, "1");
			System.in.read();
		}

		System.out.println("to fix: " + r);

		ZLevelFix<Element> fix = new ZLevelFix<>(new ElementAdapter());

		Element fixed_r = fix.fix(r);
		System.out.println("fixed root: " + fixed_r);

		if (enableVisualDisplay) {
			BufferedImage buf = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
			drawElement(fixed_r, buf, 0);
			ImageUtil.show(buf, "1");
			System.in.read();
		}

		assertThat(r_0.childs.contains(r_0_0), is(true));
		assertThat(r_0_0.childs.contains(r_0_0_1), is(true));
		assertThat(r_0_0_1.childs.contains(r_0_0_1_0), is(true));
		assertThat(r_0_0_1.childs.contains(r_0_1_1_1), is(true));
		assertThat(r_0_1_1_1.childs.contains(r_0_1_1_1_0), is(true));
		assertThat(r_0_0_1_0.childs.contains(r_0_1_1_1), is(false));

		assertThat(r_0.childs.size(), is(1));
		assertThat(r_0_0.childs.size(), is(1));
		assertThat(r_0_0_1.childs.size(), is(2));
		assertThat(r_0_0_1_0.childs.size(), is(0));
		assertThat(r_0_1_1_1.childs.size(), is(1));
		assertThat(r_0_1_1_1_0.childs.size(), is(0));

		System.out.println("finish");
	}

	private Element createElement(int x, int y, int w, int h) {
		return new Element(new Rectangle.Int(x, y, w, h), new ArrayList<Element>());
	}

}
