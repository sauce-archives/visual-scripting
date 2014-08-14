package org.testobject.kernel.ocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.testobject.matching.image.OpenCV;

public class VisualCharDistanceFunction {

	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static void main(String[] args) throws IOException {
		//render();
		matchAll();
	}
	
	private static void matchAll() throws IOException{
		for (char a : CHARACTERS.toCharArray()) {
			for (char b : CHARACTERS.toCharArray()) {
				match(a, b);
			}
		}
	}

	private static void match(char a, char b) throws IOException {
		System.loadLibrary("opencv_java246");

		Mat dest1 = OpenCV.convertBufferedImageToMat(ImageIO.read(new File("/home/aluedeke/Desktop/chars/" + a + ".png")));
		Mat dest1BW = OpenCV.convertBufferedImageToMat(ImageIO.read(new File("/home/aluedeke/Desktop/chars/" + a + ".png")));

		Mat dest2 = OpenCV.convertBufferedImageToMat(ImageIO.read(new File("/home/aluedeke/Desktop/chars/" + b + ".png")));
		Mat dest2BW = OpenCV.convertBufferedImageToMat(ImageIO.read(new File("/home/aluedeke/Desktop/chars/" + b + ".png")));

		Imgproc.cvtColor(dest1, dest1BW, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(dest2, dest2BW, Imgproc.COLOR_RGB2GRAY);

		List<MatOfPoint> contours1 = new ArrayList<>();
		List<MatOfPoint> contours2 = new ArrayList<>();

		Imgproc.findContours(dest1BW, contours1, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.findContours(dest2BW, contours2, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		double shapeMatch = Imgproc.matchShapes(contours1.get(0), contours2.get(0), Imgproc.CV_CONTOURS_MATCH_I2, 0.0);
		System.out.println(a + "-" + b + " " + shapeMatch);
	}
	
	public static void render() throws IOException {
		Font font = new Font("Roboto", 0, 300);
		
		for (char c: CHARACTERS.toCharArray()) {
			BufferedImage target = new BufferedImage(300, 350, BufferedImage.TYPE_INT_RGB);
			Graphics graphics = target.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, 300, 350);
			displayText(target, c + "", font, 20, 280);
			
			ImageIO.write(target, "png", new File("/home/aluedeke/Desktop/chars/" + c + ".png"));
		}
		
	}

	public static void displayText(BufferedImage image, String text, Font font, int x, int y) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setFont(font);
		g.drawString(text, x, y);
		g.dispose();
	}

}
