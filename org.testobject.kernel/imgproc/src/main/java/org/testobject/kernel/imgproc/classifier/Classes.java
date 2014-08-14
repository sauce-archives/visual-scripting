package org.testobject.kernel.imgproc.classifier;

import static org.testobject.kernel.imgproc.classifier.Utils.toList;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.testobject.kernel.imgproc.blob.Meta;

/**
 * 
 * @author nijkamp
 * 
 */
public interface Classes
{
	interface Widget extends Meta
	{
		List<org.testobject.kernel.imgproc.blob.Blob> getReferences();
	}

	interface Container extends Widget
	{

	}

	abstract class WidgetBase implements Widget
	{
		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return Collections.emptyList();
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName();
		}
	}
	
	public class Image extends WidgetBase{
		private final List<org.testobject.kernel.imgproc.blob.Blob> references;
		
		public final String name;

		public Image(String name) {
			this(name, Collections.<org.testobject.kernel.imgproc.blob.Blob> emptyList());
		}

		public Image(String name, List<org.testobject.kernel.imgproc.blob.Blob> references) {
			this.name = name;
			this.references = references;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences() {
			return references;
		}
	}

	class Group extends WidgetBase
	{
		private final List<org.testobject.kernel.imgproc.blob.Blob> references;

		public Group() {
			this.references = Collections.<org.testobject.kernel.imgproc.blob.Blob> emptyList();
		}

		public Group(List<org.testobject.kernel.imgproc.blob.Blob> references) {
			this.references = references;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences() {
			return references;
		}
	}

	class TextChar extends WidgetBase
	{
		public static class Candidate {
			public final char chr;
			public final int size;
			public final String font;

			public final int x;
			public final int width;

			public final double distance;

			public Candidate(char chr, int size, String font, int x, double distance, int width) {
				this.chr = chr;
				this.size = size;
				this.font = font;
				this.x = x;
				this.distance = distance;
				this.width = width;
			}

			@Override
			public String toString() {
				return "[ " + chr + ", " + size + ", " + x + ", " + distance + " ]";
			}
		}

		public final List<Candidate> candidates;

		public TextChar(List<Candidate> candidates) {
			this.candidates = candidates;
		}

	}

	// FIXME this is a container (en)
	class TextWord extends WidgetBase {
		public final List<org.testobject.kernel.imgproc.blob.Blob> chars; // FIXME use reference to TextChar (en)
		public final Set<Integer> fontSizes;

		public TextWord(List<org.testobject.kernel.imgproc.blob.Blob> chars, Set<Integer> fontSizes) {
			this.chars = chars;
			this.fontSizes = fontSizes;
		}
	}

	// TODO add identity to text-line class (e.g. statistical description of chars or so) (en)
	// FIXME this is a container (en)
	class TextLine extends WidgetBase {
		public final List<org.testobject.kernel.imgproc.blob.Blob> words; // FIXME use reference to TextWord (en)

		public TextLine(List<org.testobject.kernel.imgproc.blob.Blob> words) {
			this.words = words;
		}
	}
	
	// TODO add identity to text-paragraph class (e.g. statistical description of chars or so) (en)
	// FIXME this is a container (en)
	class TextParagraph extends WidgetBase {
		
		// FIXME hack to get identity for tech-demo (en)
		public final int[] blobs;
		
		public final List<org.testobject.kernel.imgproc.blob.Blob> lines; // FIXME use reference to TextWord (en)

		public TextParagraph(List<org.testobject.kernel.imgproc.blob.Blob> lines, int numBlobs) {
			this.lines = lines;
			this.blobs = new int[lines.size()];
			
			int counter = 0;
			for (org.testobject.kernel.imgproc.blob.Blob blob : lines) {
				blobs[counter++] = blob.children.size();
			}
		}
	}


	class TextBox extends WidgetBase
	{
		public final List<org.testobject.kernel.imgproc.blob.Blob> text;

		public TextBox(List<org.testobject.kernel.imgproc.blob.Blob> text)
		{
			this.text = text;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return text;
		}
	}

	class Icon extends WidgetBase
	{
		public final String name;

		public Icon(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return super.toString() + '[' + name + ']';
		}
	}

	class Button extends WidgetBase
	{
		public final boolean hasLabel;
		public final List<org.testobject.kernel.imgproc.blob.Blob> label;
		public final String icon;

		public Button(boolean hasLabel, List<org.testobject.kernel.imgproc.blob.Blob> label)
		{
			this.hasLabel = hasLabel;
			this.label = label;
			this.icon = "";
		}
		
		// FIXME messy hack (en)
		public Button(boolean hasLabel, List<org.testobject.kernel.imgproc.blob.Blob> label, String icon)
		{
			this.hasLabel = hasLabel;
			this.label = label;
			this.icon = icon;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return label;
		}
	}

	// FIXME rename (originally this was a icon button in a toolbar) (en)
	class IconButton implements Widget
	{
		public final org.testobject.commons.util.image.Image.Int image;

		public IconButton(org.testobject.commons.util.image.Image.Int image)
		{
			this.image = image;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return Collections.emptyList();
		}
	}

	class ContextMenu extends WidgetBase
	{

	}

	class Popup extends WidgetBase
	{
		public final List<org.testobject.kernel.imgproc.blob.Blob> body;

		public Popup(List<org.testobject.kernel.imgproc.blob.Blob> body)
		{
			this.body = body;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return body;
		}
	}

	class Dialog implements Widget
	{
		public final org.testobject.kernel.imgproc.blob.Blob title;
		public final org.testobject.kernel.imgproc.blob.Blob body;

		public Dialog(org.testobject.kernel.imgproc.blob.Blob title, org.testobject.kernel.imgproc.blob.Blob body)
		{
			this.title = title;
			this.body = body;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return Utils.toList(title, body);
		}
	}

	public static class Tab implements Widget
	{
		public final org.testobject.kernel.imgproc.blob.Blob body;
		public final org.testobject.kernel.imgproc.blob.Blob content;
		public final org.testobject.kernel.imgproc.blob.Blob label;
		public final boolean active;

		public Tab(org.testobject.kernel.imgproc.blob.Blob body, org.testobject.kernel.imgproc.blob.Blob content,
				org.testobject.kernel.imgproc.blob.Blob label, boolean active)
		{
			this.body = body;
			this.content = content;
			this.label = label;
			this.active = active;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return Utils.toList(body, content, label);
		}
	}

	class TabPanel implements Widget
	{
		public final org.testobject.kernel.imgproc.blob.Blob contour;
		public final org.testobject.kernel.imgproc.blob.Blob body;

		public TabPanel(org.testobject.kernel.imgproc.blob.Blob contour, org.testobject.kernel.imgproc.blob.Blob body)
		{
			this.contour = contour;
			this.body = body;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return Utils.toList(contour, body);
		}
	}

	class Toolbar implements Widget
	{
		public final List<org.testobject.kernel.imgproc.blob.Blob> buttons;

		public Toolbar(List<org.testobject.kernel.imgproc.blob.Blob> buttons)
		{
			this.buttons = buttons;
		}

		@Override
		public List<org.testobject.kernel.imgproc.blob.Blob> getReferences()
		{
			return buttons;
		}
	}

	/*
	 * class ScrollBar implements Widget { public final Blob button1, button2; public final Blob bar;
	 * 
	 * public ScrollBar(Blob button1, Blob button2, Blob bar) { this.button1 = button1; this.button2 = button2; this.bar = bar; } }
	 * 
	 * class ScrollArea implements Widget { public final Blob body; public final List<ScrollBar> scrollbars;
	 * 
	 * public ScrollArea(Blob body, List<ScrollBar> scrollbars) { this.body = body; this.scrollbars = scrollbars; } }
	 */
}
