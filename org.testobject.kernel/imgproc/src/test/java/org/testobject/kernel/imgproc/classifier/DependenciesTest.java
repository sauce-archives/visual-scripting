package org.testobject.kernel.imgproc.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classes.Widget;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.Dependencies;

/**
 * 
 * @author enijkamp
 *
 */
public class DependenciesTest
{
	public static Classifier newClassifier(List<Class<? extends Widget>> requires, Class<? extends Widget> returns)
	{
		final Classifier.Specification spec = new Classifier.Specification(requires, returns);
		return new Classifier()
		{
			@Override
			public Specification getSpec()
			{
				return spec;
			}

			@Override
			public Match match(Context context, Blob blob)
			{
				throw new IllegalStateException();
			}
		};
	}

	public static List<Class<? extends Widget>> none()
	{
		return Collections.<Class<? extends Widget>> emptyList();
	}

	public static List<Class<? extends Widget>> toList(Class<? extends Widget> c1)
	{
		List<Class<? extends Widget>> list = new ArrayList<Class<? extends Widget>>();
		list.add(c1);
		return list;
	}

	public static List<Class<? extends Widget>> toList(Class<? extends Widget> c1, Class<? extends Widget> c2)
	{
		List<Class<? extends Widget>> list = toList(c1);
		list.add(c2);
		return list;
	}

	@Test
	public void testTree()
	{
		// dependencies
		Classifier g = newClassifier(none(), Classes.Group.class);
		Classifier gt = newClassifier(toList(Classes.Group.class), Classes.TextChar.class);
		Classifier gtb = newClassifier(toList(Classes.TextChar.class), Classes.Button.class);
		Classifier gtbd = newClassifier(toList(Classes.Button.class), Classes.Dialog.class);
		Classifier gtc = newClassifier(toList(Classes.TextChar.class), Classes.ContextMenu.class);

		// total order
		List<Classifier> order = Dependencies.order(Arrays.asList(new Classifier[]{gtc, g, gt, gtbd, gtb}));

		// assert
		Set<Class<? extends Widget>> provided = new HashSet<Class<? extends Widget>>();
		for (Classifier classifier : order)
		{
			if (!classifier.getSpec().requires.isEmpty())
			{
				Assert.assertTrue(provided.containsAll(classifier.getSpec().requires));
			}
			provided.add(classifier.getSpec().returns);
		}
	}

	@Test
	public void testCycle()
	{
		// dependencies
		Classifier g = newClassifier(none(), Classes.Group.class);
		Classifier gt = newClassifier(toList(Classes.Group.class, Classes.Button.class), Classes.TextChar.class);
		Classifier gb = newClassifier(toList(Classes.Group.class, Classes.TextChar.class), Classes.Button.class);

		// total order
		boolean thrown = false;
		try
		{
			Dependencies.order(Arrays.asList(new Classifier[] { g, gt, gb }));
		} catch (Throwable t)
		{
			thrown = true;
		}

		// assert
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testCross()
	{
		// dependencies
		Classifier tg = newClassifier(toList(Classes.TextChar.class), Classes.Group.class);
		Classifier gt = newClassifier(toList(Classes.Group.class), Classes.TextChar.class);

		// total order
		boolean thrown = false;
		try
		{
			Dependencies.order(Arrays.asList(new Classifier[] { tg, gt }));
		} catch (Throwable t)
		{
			thrown = true;
		}

		// assert
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testSameReturn()
	{
		
		// dependencies
		Classifier g = newClassifier(none(), Classes.Group.class);
		Classifier t = newClassifier(none(), Classes.TextChar.class);
		Classifier tgt = newClassifier(toList(Classes.TextChar.class, Classes.Group.class), Classes.TextBox.class);
		Classifier tt = newClassifier(toList(Classes.TextWord.class), Classes.TextBox.class);

		// total order
		boolean thrown = false;
		try
		{
			Dependencies.order(Arrays.asList(new Classifier[] { g, t, tgt, tt }));
		} catch (Throwable thr)
		{
			thrown = true;
		}

		// assert
		Assert.assertTrue(thrown);
	}

	
	@Test
	public void testTransitiv()
	{
		// dependencies
		Classifier i = newClassifier(none(), Classes.Icon.class);
		Classifier tg = newClassifier(toList(Classes.TextChar.class), Classes.Group.class);
		Classifier gb = newClassifier(toList(Classes.Group.class), Classes.Button.class);
		Classifier bt = newClassifier(toList(Classes.Button.class), Classes.TextChar.class);

		// total order
		boolean thrown = false;
		try
		{
			Dependencies.order(Arrays.asList(new Classifier[] { i, tg, gb, bt }));
		} catch (Throwable t)
		{
			thrown = true;
		}

		// assert
		Assert.assertTrue(thrown);
	}

}
