package org.testobject.kernel.imaging.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.blob.Histogram;

public class HistogramPlot
{

	@SuppressWarnings("serial")
	public static JPanel histogramPlot(Iterable<? extends BoundingBox> boxes)
	{
		final int[] histx = Histogram.computeHistogramX(boxes);
		final int[] histy = Histogram.computeHistogramY(boxes);

		return new JPanel()
		{
			int maxx;
			int maxy;

			int scalex = 1;
			int scaley = 1;

			{
				maxx = histx.length;
				for (int h : histy)
				{
					maxx = Math.max(maxx, h);
				}

				maxy = histy.length;
				for (int h : histx)
				{
					maxy = Math.max(maxy, h);
				}

				if (maxx <= 10) {
					scalex = 10;
				} else if (maxx <= 100) {
					scalex = 3;
				} else if (maxx <= 200) {
					scalex = 2;
				}

				if (maxy <= 10) {
					scaley = 10;
				} else if (maxy <= 100) {
					scaley = 3;
				} else if (maxx <= 200) {
					scaley = 2;
				}

				setSize(new Dimension(maxx * scalex, maxy * scaley));
				setPreferredSize(getSize());
				setMinimumSize(getSize());
			}

			@Override
			public void paintComponent(Graphics graphics)
			{
				Graphics2D g = (Graphics2D) graphics;

				g.setPaint(Color.WHITE);
				g.fillRect(0, 0, maxx, maxy);

				Composite cp = g.getComposite();

				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

				g.setPaint(Color.BLUE);
				for (int i = 0; i < histx.length; i++)
				{
					g.fillRect(i * scalex, 0, scalex, scaley * histx[i]);
				}
				g.setPaint(Color.ORANGE);
				for (int i = 0; i < histy.length; i++)
				{
					g.fillRect(0, i * scaley, scalex * histy[i], scaley);
				}

				g.setComposite(cp);
			}
		};
	}

	@SuppressWarnings("serial")
	public static JPanel histogramPlotX(Iterable<? extends BoundingBox> boxes)
	{
		final int[] histx = Histogram.computeHistogramX(boxes);

		final int BORDER = 25;

		return new JPanel()
		{
			int maxx = histx.length;
			int maxy;

			int scalex = 1;
			int scaley = 1;

			{
				maxy = 0;
				for (int h : histx)
				{
					maxy = Math.max(maxy, h);
				}

				if (maxx <= 10) {
					scalex = 10;
				} else if (maxx <= 100) {
					scalex = 3;
				} else if (maxx <= 200) {
					scalex = 2;
				}

				if (maxy <= 10) {
					scaley = 10;
				} else if (maxy <= 100) {
					scaley = 3;
				} else if (maxy <= 200) {
					scaley = 2;
				}

				setSize(new Dimension(maxx * scalex + BORDER, maxy * scaley + BORDER));
				setPreferredSize(getSize());
				setMinimumSize(getSize());
			}

			@Override
			public void paintComponent(Graphics graphics)
			{
				Graphics2D g = (Graphics2D) graphics;

				g.setPaint(Color.WHITE);
				g.fillRect(0, 0, maxx * scalex + 2 * BORDER, maxy * scaley + 2 * BORDER);

				Composite cp = g.getComposite();

				//               g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

				g.setPaint(Color.BLUE);
				for (int i = 0; i < histx.length; i++)
				{
					g.fillRect(BORDER + i * scalex, BORDER + scaley * (maxy - histx[i]), scalex, scaley * maxy);
				}

				g.setComposite(cp);
			}
		};
	}

	@SuppressWarnings("serial")
	public static JPanel histogramPlotY(Iterable<? extends BoundingBox> boxes)
	{
		final int[] histy = Histogram.computeHistogramY(boxes);
		final int BORDER = 25;

		return new JPanel()
		{
			int maxx;
			int maxy = histy.length;

			int scalex = 1;
			int scaley = 1;

			{
				maxx = 0;
				for (int h : histy)
				{
					maxx = Math.max(maxx, h);
				}

				if (maxx <= 10) {
					scalex = 10;
				} else if (maxx <= 100) {
					scalex = 3;
				} else if (maxx <= 200) {
					scalex = 2;
				}

				if (maxy <= 10) {
					scaley = 10;
				} else if (maxy <= 100) {
					scaley = 3;
				} else if (maxy <= 200) {
					scaley = 2;
				}

				setSize(new Dimension(maxx * scalex + 2 * BORDER, maxy * scaley + 2 * BORDER));
				setPreferredSize(getSize());
				setMinimumSize(getSize());
			}

			@Override
			public void paintComponent(Graphics graphics)
			{
				Graphics2D g = (Graphics2D) graphics;

				g.setPaint(Color.WHITE);
				g.fillRect(0, 0, maxx * scalex + 2 * BORDER, maxy * scaley + 2 * BORDER);

				Composite cp = g.getComposite();

				//                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

				g.setPaint(Color.BLUE);
				for (int i = 0; i < histy.length; i++)
				{
					g.fillRect(BORDER, BORDER + i * scaley, scalex * histy[i], scaley);
				}

				g.setComposite(cp);
			}
		};
	}

	public static void displayHistogramPlot(String title, Iterable<? extends BoundingBox> boxes)
	{
		JPanel panel = histogramPlot(boxes);

		final JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				frame.pack();
				frame.setVisible(true);
			}

		});

	}

	public static void displayHistogramPlotX(String title, Iterable<? extends BoundingBox> boxes)
	{
		JPanel panel = histogramPlotX(boxes);

		final JFrame frame = new JFrame("x-plot: " + title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				frame.pack();
				frame.setVisible(true);
			}

		});
	}

	public static void displayHistogramPlotY(String title, Iterable<? extends BoundingBox> boxes)
	{
		JPanel panel = histogramPlotY(boxes);

		final JFrame frame = new JFrame("y-plot: " + title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				frame.pack();
				frame.setVisible(true);
			}

		});

	}

	public static void displayHistogramPlotXAsDialog(JFrame frame, String title, Iterable<? extends BoundingBox> boxes)
	{
		JPanel panel = histogramPlotX(boxes);

		final JDialog dialog = new JDialog(frame, true);
		dialog.setTitle("x-plot: " + title);

		//        dialog.getContentPane().setLayout(new BorderLayout());
		dialog.add(panel, BorderLayout.CENTER);

		dialog.pack();
		dialog.setSize(new Dimension(dialog.getSize().width + 50, dialog.getSize().height + 50));
		dialog.pack();

		Rectangle r = frame.getBounds();
		int x = r.x + (r.width - dialog.getSize().width) / 2;
		int y = r.y + (r.height - dialog.getSize().height) / 2;

		dialog.setLocation(x, y);

		dialog.setVisible(true);
	}

	public static void displayHistogramPlotYAsDialog(JFrame frame, String title, Iterable<? extends BoundingBox> boxes)
	{
		JPanel panel = histogramPlotY(boxes);

		final JDialog dialog = new JDialog(frame, true);
		dialog.setTitle("y-plot: " + title);

		//       dialog.getContentPane().setLayout(new BorderLayout());
		dialog.add(panel, BorderLayout.CENTER);

		dialog.pack();
		dialog.setSize(new Dimension(dialog.getSize().width + 50, dialog.getSize().height + 50));
		dialog.pack();

		Rectangle r = frame.getBounds();
		int x = r.x + (r.width - dialog.getSize().width) / 2;
		int y = r.y + (r.height - dialog.getSize().height) / 2;

		dialog.setLocation(x, y);
		dialog.setVisible(true);
	}
}
