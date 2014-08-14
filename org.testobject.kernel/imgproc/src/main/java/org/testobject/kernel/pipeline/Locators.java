package org.testobject.kernel.pipeline;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Meta;
import org.testobject.kernel.imgproc.diff.TopDownStringTreeDiff;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.TextRecognizer;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classes.TextChar;
import org.testobject.kernel.imgproc.classifier.Classes.Widget;
import org.testobject.kernel.imgproc.diff.WidgetOrders;
import org.testobject.kernel.imgproc.diff.WidgetOrders.Order;
import org.testobject.kernel.locator.api.Button;
import org.testobject.kernel.locator.api.Container;
import org.testobject.kernel.locator.api.ContextMenu;
import org.testobject.kernel.locator.api.Dialog;
import org.testobject.kernel.locator.api.Icon;
import org.testobject.kernel.locator.api.IconButton;
import org.testobject.kernel.locator.api.Label;
import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Paragraph;
import org.testobject.kernel.locator.api.Popup;
import org.testobject.kernel.locator.api.Root;
import org.testobject.kernel.locator.api.Tab;
import org.testobject.kernel.locator.api.TabContent;
import org.testobject.kernel.locator.api.TabDeco;
import org.testobject.kernel.locator.api.TabPanel;
import org.testobject.kernel.locator.api.TextBox;
import org.testobject.kernel.locator.api.Toolbar;
import org.testobject.kernel.script.api.Script;
import org.testobject.kernel.script.api.Script.Responses.Appears;
import org.testobject.kernel.script.api.Script.Responses.Disappears;
import org.testobject.kernel.script.api.Script.Responses.Update;

/**
 * Domain model that specifies the required properties for validation purposes. Widget objects in this model can be interpreted as queries
 * e.g. "button with label 'foo' and contained in xy". Locator query has no references to blobs or widget-hierarchy objects, it describes
 * the widget identity in abstract terms.
 * 
 * @author enijkamp
 */
public interface Locators {

	final Log log = LogFactory.getLog(Locators.class);

	class Util {
		public static void print(PrintStream out, Locator locator) {
			print(out, locator, 0);
		}

		public static void print(PrintStream out, Locator locator, int level) {
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < level; i++) {
				sb.append("  ");
			}

			sb.append(locator.toString());

			out.println(sb.toString());

			if (locator instanceof Container) {
				Container container = (Container) locator;
				for (Locator child : container.getChilds()) {
					print(out, child, level + 1);
				}
			}
		}
	}

	/*
	 * class LazyString { private final TextRecognizer<Blob> recognizer; private final List<Blob> blobs; private String value;
	 * 
	 * public LazyString(TextRecognizer<Blob> recognizer, List<Blob> blobs) { this.recognizer = recognizer; this.blobs = blobs; }
	 * 
	 * @Override public boolean equals(Object obj) { if(value == null) { if(obj instanceof LazyString) { LazyString other = (LazyString)
	 * obj; if(blobs.size() != other.blobs.size()) { return false; } return true; } } if(obj instanceof String) { return
	 * toString().equals(obj); } else if(obj instanceof LazyString) { return toString().equals(obj.toString()); } return false; }
	 * 
	 * @Override public String toString() { if(value == null) { value = recognizer.recognize(blobs).text;
	 * log.debug("lazy string ocr -> '"+value+"'"); } return value; }
	 * 
	 * @Override public int hashCode() { return toString().hashCode(); } }
	 * 
	 * class LazyStringEnhancer { final static String[] DOMAIN = { "Label", "Button", "Dialog", "Tab", "TabContent", "TabDeco", "TabPanel"
	 * };
	 * 
	 * public static void initialize() { try { final InstrumentedClassLoader classLoader = new
	 * InstrumentedClassLoader(ClassLoader.getSystemClassLoader()); final String javaPackage = Locators.class.getName(); for(String entity :
	 * DOMAIN) { classLoader.loadInstrumentedClass(javaPackage + "$" + entity); } } catch(Exception e) { throw new RuntimeException(e); } }
	 * 
	 * public static void enhance(final Widget entity, final String field, final LazyString string) {
	 * if(contains(entity.getClass().getDeclaredFields(), field) == false) { throw new IllegalArgumentException(field); }
	 * InterceptStringFieldCallback callback = new InterceptStringFieldCallback() {
	 * 
	 * @Override protected String readString(Object _this, Field field, Object oldValue) { System.out.println("lazy loading '" +
	 * entity.getClass().getName() + "." + field.getName() + "' -> '" + string.toString() + "'"); return string.toString(); } };
	 * InterceptFieldEnabled intercept = (InterceptFieldEnabled) entity; intercept.setInterceptFieldCallback(callback); }
	 * 
	 * private static boolean contains(Field[] fields, String name) { for(Field field : fields) { if(field.getName().equals(name)) { return
	 * true; } } return false; } }
	 * 
	 * class LazyFactory { // FIXME instead of hardcoded field names used @Named attributes of widget constructor (en)
	 * 
	 * public static String missing(Class<?> parent, String field) { return "intercept callback missing for field '" + parent.getName() +
	 * "." + field + "'"; }
	 * 
	 * public static Label newLabel(LazyString string) { String field = "label"; Label widget = new Label(missing(Label.class, field));
	 * LazyStringEnhancer.enhance(widget, field, string); return widget; }
	 * 
	 * public static Button newButton(LazyString string) { String field = "label"; Button widget = new Button(missing(Button.class, field));
	 * LazyStringEnhancer.enhance(widget, field, string); return widget; }
	 * 
	 * public static TabDeco newTabDeco(LazyString string, boolean active) { String field = "label"; TabDeco widget = new
	 * TabDeco(missing(TabDeco.class, field), active); LazyStringEnhancer.enhance(widget, field, string); return widget; }
	 * 
	 * public static Dialog newDialog(Widget[] childs, LazyString string) { String field = "title"; Dialog widget = new Dialog(childs,
	 * missing(Dialog.class, field)); LazyStringEnhancer.enhance(widget, field, string); return widget; } }
	 */

	// FIXME cleanup mapping (en)
	class Transform {
		// TODO implement mapping sub-class for each locator type, register in a registry, compare activated classifers versus registered
		// mappings at boot-up (en)
		public interface ClassToLocator {
			Locator map(Classes.Widget widget);
		}

		static {
			// Locators.LazyStringEnhancer.initialize();
		}

		private final TextRecognizer<Blob> recognizer;
		private final Map<Class<? extends Classes.Widget>, Order<Blob>> orders = new HashMap<Class<? extends Classes.Widget>, Order<Blob>>();
		{
			orders.put(Classes.Dialog.class, new WidgetOrders.XY<Blob>());
		}

		public Transform(TextRecognizer<Blob> recognizer) {
			this.recognizer = recognizer;
		}

		// FIXME return list contains invalid locator references
		// e.g. A->B->C, A.getChild() != B, since B is re-instantiated by blobToLocator
		// maybe okay since locator identity is defined by equals (en)
		public LinkedList<Locator> pathToLocator(Image.Int image, List<Blob> blobs) {
			if (blobs.isEmpty()) {
				return new LinkedList<Locator>();
			}

			LinkedList<Locator> locators = new LinkedList<>();
			Locator locator = null;
			for (Blob blob : blobs) {
				if (blob.meta instanceof Classes.Group) {
					continue;
				} else if (blob.meta instanceof Classes.TextChar) {
					if (locator != null && locator instanceof Container) {
						locator = blobToLocator(blob, image, (Classes.Widget) blob.meta, null, null);
						locators.add(locator);
					}
				} else if (blob.meta instanceof Classes.Widget) {
					locator = blobToLocator(blob, image, (Classes.Widget) blob.meta, null, null);
					// FIXME specialized code ... (en)
					if (locator instanceof Tab) {
						Tab tab = (Tab) locator;
						locator = tab.decoration;
						locators.add(tab);
						locators.add(locator);
					} else {
						locators.add(locator);
					}
				}
			}

			// FIXME messy hack, root blob should have meta 'root' attached and be automatically mapped to root locator (en)
			List<Locator> childs = new LinkedList<>();
			childs.addAll(blobsToLocator(image, blobs.get(0).children, null, null));
			Root root = new Root(toArray(childs));
			locators.add(0, root);
			return locators;
		}

		public Root blobToLocator(Image.Int image, Blob blob) {
			List<Locator> childs = new LinkedList<Locator>();
			if (blob.meta instanceof Classes.Widget) {
				Classes.Widget widget = (Classes.Widget) blob.meta;
				childs.add(blobToLocator(blob, image, widget, null, null));
			} else {
				childs.addAll(blobsToLocator(image, blob.children, null, null));
			}
			return new Root(toArray(childs));
		}

		public Root blobToLocator(Image.Int image, Blob blob, Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator) {
			List<Locator> childs = new LinkedList<Locator>();
			if (blob.meta instanceof Classes.Widget) {
				Classes.Widget widget = (Classes.Widget) blob.meta;
				childs.add(blobToLocator(blob, image, widget, locatorToBlob, blobToLocator));
			} else {
				childs.addAll(blobsToLocator(image, blob.children, locatorToBlob, blobToLocator));
			}

			Root root = new Root(toArray(childs));
			put(locatorToBlob, blobToLocator, root, blob);
			return root;
		}

		private LinkedList<Locator> blobsToLocator(Image.Int image, List<Blob> blobs, Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator) {
			LinkedList<Locator> locators = new LinkedList<Locator>();
			for (Blob blob : blobs) {
				// skip blobs: no classification meta attached
				if (blob.meta instanceof Meta.Blob) {
					locators.addAll(blobsToLocator(image, blob.children, locatorToBlob, blobToLocator));
				}
				// skip groups: groups are widgets but do not have an associated locator equivalent
				else if (blob.meta instanceof Classes.Group) {
					locators.addAll(blobsToLocator(image, blob.children, locatorToBlob, blobToLocator));
				}
				// map widgets: widgets can be mapped to locators
				else if (blob.meta instanceof Classes.Widget) {
					Classes.Widget widget = (Classes.Widget) blob.meta;
					Locator locator = blobToLocator(blob, image, widget, locatorToBlob, blobToLocator);
					locators.add(locator);
				}
			}
			return locators;
		}

		// FIXME modularize mapping e.g. put mapping into Classes.Widget sub-classes (en)
		// FIXME improve classifier activation, there has to exist a mapping for activated classifiers in pipeline (en)
		// TODO add classifier registry which handles activation of classifiers, updates to pipeline configuration, pre-checks mapping (en)
		private Locator blobToLocator(Blob blob, Image.Int image, Classes.Widget widget, Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator) {

			if (Classes.TextParagraph.class == widget.getClass()) {
				Classes.TextParagraph textParagraph = (Classes.TextParagraph) widget;
				Paragraph locator = new Paragraph(toArray(paragraphToLocator(image, textParagraph.lines, locatorToBlob, blobToLocator)), textParagraph.blobs);
				return put(locatorToBlob, blobToLocator, locator, blob);
			}
			
			if (Classes.TextChar.class == widget.getClass()) {
				Label locator = new Label(toString(image, blob.children));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}
			
			if (Classes.TextWord.class == widget.getClass()) {
				Label locator = new Label(toString(image, blob.children));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}
			
			if (Classes.TextLine.class == widget.getClass()) {
				Classes.TextLine textLine = (Classes.TextLine) widget;
				
				String text = "";;
				for (Blob word : textLine.words) {
					Classes.TextWord textWord = (Classes.TextWord) word.meta;
					text += toString(image, textWord.chars);
				}
				Label locator = new Label(text);
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.Button.class == widget.getClass()) {
				Classes.Button button = (Classes.Button) widget;
				Button locator = new Button(button.hasLabel ? toString(image, button.label) : button.icon);
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.Tab.class == widget.getClass()) {
				Classes.Tab tab = (Classes.Tab) widget;
				TabDeco deco = new TabDeco(toString(image, tab.label.children), tab.active);

				// FIXME enhance tab-panel classifier, should create virtual blobs (en)
				// FIXME deco locator is required in locatorToBlob map (en)
				if (locatorToBlob != null) {
					locatorToBlob.put(deco, blob);
				}

				TabContent content = new TabContent(toArray(blobsToLocator(image, tab.content.children, locatorToBlob, blobToLocator)));

				// FIXME content locator is required in locatorToBlob map (en)
				if (locatorToBlob != null) {
					locatorToBlob.put(content, tab.content);
				}

				Tab locator = new Tab(deco, content);
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.TabPanel.class == widget.getClass()) {
				List<Locator> tabs = blobsToLocator(image, blob.children, locatorToBlob, blobToLocator);
				TabPanel locator = new TabPanel(tabs.toArray(new Locator[] {}));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.Dialog.class == widget.getClass()) {
				Classes.Dialog dialog = (Classes.Dialog) widget;
				Order<Blob> ordering = orders.get(Classes.Dialog.class);
				List<Blob> ordered = ordering.order(dialog.body.children);
				List<Locator> widgets = blobsToLocator(image, ordered, locatorToBlob, blobToLocator);
				Dialog locator = new Dialog(toArray(widgets), toString(image, dialog.title.children));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.ContextMenu.class == widget.getClass()) {
				ContextMenu locator = new ContextMenu();
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.Toolbar.class == widget.getClass()) {
				Classes.Toolbar toolbar = (Classes.Toolbar) widget;
				List<Locator> icons = blobsToLocator(image, toolbar.buttons, locatorToBlob, blobToLocator);
				Toolbar locator = new Toolbar(toArray(icons));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.IconButton.class == widget.getClass()) {
				Classes.IconButton button = (Classes.IconButton) widget;
				IconButton locator = new IconButton(ImageUtil.toBufferedImage(button.image));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.Icon.class == widget.getClass()) {
				Classes.Icon icon = (Classes.Icon) widget;
				Icon locator = new Icon(icon.name);
				return put(locatorToBlob, blobToLocator, locator, blob);
			}
			
			if (Classes.Image.class == widget.getClass()) {
				Classes.Image imageClass = (Classes.Image) widget;
				org.testobject.kernel.locator.api.Image locator = new org.testobject.kernel.locator.api.Image(imageClass.name);
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.TextBox.class == widget.getClass()) {
				Classes.TextBox box = (Classes.TextBox) widget;
				TextBox locator = new TextBox(toString(image, box.text));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			if (Classes.Popup.class == widget.getClass()) {
				Classes.Popup popup = (Classes.Popup) widget;
				Popup locator = new Popup(toArray(blobsToLocator(image, popup.body, locatorToBlob, blobToLocator)));
				return put(locatorToBlob, blobToLocator, locator, blob);
			}

			throw new IllegalArgumentException(widget.getClass().getName());
		}

		private List<Locator> paragraphToLocator(Int image, List<Blob> lines,
				Map<Locator, Blob> locatorToBlob,
				Map<Blob, Locator> blobToLocator) {
			List<Locator> labels = new LinkedList<>();
			
			// FIXME this makes no sense (en)
			for (Blob blob : lines) {
					labels.add(new Label(""));
			}
		
			return labels;
		}

		private Locator put(Map<Locator, Blob> locatorToBlob, Map<Blob, Locator> blobToLocator, Locator locator, Blob blob) {
			if (locatorToBlob != null) {
				locatorToBlob.put(locator, blob);
			}
			if(blobToLocator != null) {
				blobToLocator.put(blob, locator);
			}
			return locator;
		}

		/*
		 * private LazyString toLazyString(final List<Blob> blobs) { return new LazyString(recognizer, blobs); }
		 */

		private String toString(final Image.Int image, final List<Blob> blobs) {
			return recognizer.recognize(image, filter(blobs, TextChar.class)).text;
		}

		private List<Blob> filter(List<Blob> blobs, Class<? extends Widget> type) {
			List<Blob> result = new LinkedList<>();
			for (Blob blob : blobs) {
				if (blob.meta.getClass() == type) {
					result.add(blob);
				}
			}
			return result;
		}

		private final Locator[] toArray(List<Locator> list) {
			return list.toArray(new Locator[] {});
		}
	}

	class Diff {
		private static class LocatorQualifier {
			private final LinkedList<Locator> path;

			public LocatorQualifier(LinkedList<Locator> path) {
				this.path = path;
			}

			public Locator getLocator() {
				return path.getLast();
			}

			public LinkedList<Locator> getPath() {
				return path;
			}

			@Override
			public boolean equals(Object other) {
				if (other instanceof LocatorQualifier == false) {
					return false;
				}

				LocatorQualifier qualifier = (LocatorQualifier) other;

				if (qualifier.getPath().size() != this.getPath().size()) {
					return false;
				}

				for (int i = 0; i < this.getPath().size(); i++) {
					if (qualifier.getPath().get(i).equals(getPath().get(i)) == false) {
						return false;
					}
				}

				return true;
			}
		}

		private static class LocatorAdapter implements TopDownStringTreeDiff.Adapter<LocatorQualifier> {
			@Override
			public boolean isContainer(LocatorQualifier qualifier) {
				Locator locator = qualifier.getLocator();
				if (locator instanceof Root) {
					return true;
				} else if (locator instanceof Container) {
					Container container = (Container) locator;
					return container.getChilds().length != 0;
				}
				return false;
			}

			@Override
			public LocatorQualifier[] getChilds(LocatorQualifier qualifier) {
				Locator locator = qualifier.getLocator();
				Container container = (Container) locator;
				LocatorQualifier[] childs = new LocatorQualifier[container.getChilds().length];
				for (int i = 0; i < childs.length; i++) {
					childs[i] = new LocatorQualifier(concat(qualifier.getPath(), container.getChilds()[i]));
				}
				return childs;
			}

			private LinkedList<Locator> concat(LinkedList<Locator> path, Locator locator) {
				LinkedList<Locator> result = new LinkedList<Locator>(path);
				result.add(locator);
				return result;
			}

			@Override
			public boolean equalsType(LocatorQualifier left, LocatorQualifier right) {
				if (left.getPath().size() != right.getPath().size()) {
					return false;
				}

				for (int i = 0; i < left.getPath().size(); i++) {
					if (left.getPath().get(i).getClass() != right.getPath().get(i).getClass()) {
						return false;
					}
				}

				return true;
			}
		}

		private final static TopDownStringTreeDiff<LocatorQualifier> treeDiff = new TopDownStringTreeDiff<LocatorQualifier>(
				new LocatorAdapter());

		public static List<Script.Responses.Response> between(Locator before, Locator after) {
			List<Script.Responses.Response> response = new LinkedList<Script.Responses.Response>();

			List<TopDownStringTreeDiff.Mutation<LocatorQualifier>> mutations = treeDiff.mutations(toQualifier(before), toQualifier(after));

			for (TopDownStringTreeDiff.Mutation<LocatorQualifier> mutation : mutations) {
				switch (mutation.type) {
				case INSERT:
					response.add(new Script.Responses.Appears(mutation.after.getPath()));
					break;
				case DELETE:
					response.add(new Script.Responses.Disappears(mutation.after.getPath()));
					break;
				case UPDATE:
					response.add(new Script.Responses.Update(mutation.before.getPath(), mutation.after.getPath(), properties(
							mutation.before.getLocator(),
							mutation.after.getLocator())));
					break;
				}
			}
			
			Collections.sort(response, new Comparator<Script.Responses.Response>() {
				@Override
				public int compare(Script.Responses.Response o1, Script.Responses.Response o2) {
					if(o1 instanceof Appears){
						return  o2 instanceof Appears ? 0 : 1;
					} else if(o1 instanceof Disappears){
						if( o2 instanceof Appears){
							return -1;
						}else if(o2 instanceof Disappears){
							return 0;
						}else{
							return 1;
						}
					}
					
					return (o1 instanceof Update && o2 instanceof Update) ? 0 : -1;
				}
			});

			return response;
		}

		private static LocatorQualifier toQualifier(Locator locator) {
			return new LocatorQualifier(Lists.toLinkedList(locator));
		}

		private static List<Script.Responses.Update.Property> properties(Locator before, Locator after) {
			try {
				List<Script.Responses.Update.Property> properties = new LinkedList<Script.Responses.Update.Property>();
				for (Field field : before.getClass().getFields()) {
					Object oldValue = field.get(before);
					Object newValue = field.get(after);
					if (oldValue.equals(newValue) == false) {
						properties.add(new Script.Responses.Update.Property(field.getName(), oldValue, newValue));
					}
				}
				return properties;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
