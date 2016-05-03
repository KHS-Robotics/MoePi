package com.moe365.mopi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.moe365.mopi.geom.PreciseRectangle;
import com.moe365.mopi.processing.AbstractImageProcessor;

import au.edu.jcu.v4l4j.VideoFrame;

public class ImageProcessor extends AbstractImageProcessor<List<PreciseRectangle>> {
	public static final int MIN_SIZE = 8;
	public static final int step = 1, tolerance = 70;
	/**
	 * Whether to save the diff generated.
	 */
	public boolean saveDiff = false;
	protected final AtomicInteger i = new AtomicInteger(0);
	public ImageProcessor(int width, int height, Consumer<List<PreciseRectangle>> handler) {
		super(0, 0, width, height, handler);
	}
	
	public boolean[][] calcDeltaWithDiff(VideoFrame frameOn, VideoFrame frameOff) {
		// calculated yet)
		boolean[][] processed = new boolean[getFrameHeight()][getFrameWidth()];
		// boolean array of the results. A cell @ result[y][x] is only
		// valid if processed[y][x] is true.
		boolean[][] result = new boolean[getFrameHeight()][getFrameWidth()];
		BufferedImage imgR = new BufferedImage(getFrameWidth(), getFrameHeight(), BufferedImage.TYPE_INT_RGB);
		BufferedImage imgG = new BufferedImage(getFrameWidth(), getFrameHeight(), BufferedImage.TYPE_INT_RGB);
		BufferedImage imgB = new BufferedImage(getFrameWidth(), getFrameHeight(), BufferedImage.TYPE_INT_RGB);
		BufferedImage imgFlt = new BufferedImage(getFrameWidth(), getFrameHeight(), BufferedImage.TYPE_INT_RGB);
		System.out.println("Calculating...");
		BufferedImage offImg = frameOff.getBufferedImage();
		BufferedImage onImg = frameOn.getBufferedImage();
		System.out.println("CM: " + onImg.getColorModel());
		System.out.println("CMCL: " + onImg.getColorModel().getClass());
		int[] pxOn = new int[3], pxOff = new int[3];
		for (int y = frameMinY + step; y < frameMaxY - step; y += step) {
			final int idxY = y - frameMinY;
			for (int x = frameMinX + step + ((y % (2 * step) == 0) ? step/2 : 0); x < frameMaxX; x += step) {
				final int idxX = x - frameMinX;
				if (processed[idxY][idxX])
					continue;
				processed[idxY][idxX] = true;
				splitRGB(onImg.getRGB(x, y), pxOn);
				splitRGB(offImg.getRGB(x, y), pxOff);
				int dR = pxOn[0] - pxOff[0];
				int dG =  pxOn[1] - pxOff[1];
				int dB =  pxOn[2] - pxOff[2];
				if (dG > tolerance) {//TODO fix
					result[idxY][idxX] = true;
					imgFlt.setRGB(x, y, 0xFFFFFF);
				}
				imgR.setRGB(x, y, saturateByte(dR) << 16);
				imgG.setRGB(x, y, saturateByte(dG) << 8);
				imgB.setRGB(x, y, saturateByte(dB));
			}
		}
		try {
			File imgDir = new File("img");
			if (!(imgDir.exists() && imgDir.isDirectory()))
				imgDir.mkdirs();
			int num = i.getAndIncrement();
			File file = new File(imgDir, "delta" + num + ".png");
			System.out.println("Saving image to " + file);
			ImageIO.write(imgR, "PNG", new File(imgDir, "dr" + num + ".png"));
			ImageIO.write(imgG, "PNG", new File(imgDir, "dg" + num + ".png"));
			ImageIO.write(imgB, "PNG", new File(imgDir, "db" + num + ".png"));
			ImageIO.write(imgFlt, "PNG", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public boolean[][] calcDeltaAdv(VideoFrame frameOn, VideoFrame frameOff) {
		// Whether the value of any cell in result[][] is valid (has been
		// calculated yet)
		boolean[][] processed = new boolean[getFrameHeight()][getFrameWidth()];
		// boolean array of the results. A cell @ result[y][x] is only
		// valid if processed[y][x] is true.
		boolean[][] result = new boolean[getFrameHeight()][getFrameWidth()];
		System.out.println("Calculating...");
		BufferedImage offImg = frameOff.getBufferedImage();
		BufferedImage onImg = frameOn.getBufferedImage();
		int[] pxOn = new int[3], pxOff = new int[3];
		for (int y = frameMinY + step; y < frameMaxY - step; y += step) {
			final int idxY = y - frameMinY;
			for (int x = frameMinX + step + ((y % (2 * step) == 0) ? step/2 : 0); x < frameMaxX; x += step) {
				final int idxX = x - frameMinX;
				if (processed[idxY][idxX])
					continue;
				processed[idxY][idxX] = true;
				splitRGB(onImg.getRGB(x, y), pxOn);
				splitRGB(offImg.getRGB(x, y), pxOff);
				int dR = pxOn[0] - pxOff[0];
				int dG =  pxOn[1] - pxOff[1];
				int dB =  pxOn[2] - pxOff[2];
				if (dG > tolerance && (dR < dG - 10 || dR < tolerance))//TODO fix
					result[idxY][idxX] = true;
			}
		}
		return result;
	}
	protected List<PreciseRectangle> processBooleanMap(boolean[][] processed) {
		// List of the rectangles to be generated by boundingBoxRecursive
		List<PreciseRectangle> rectangles = new LinkedList<>();
		//find rectangles
		BoundingBoxThing.boundingBoxRecursive(processed, rectangles, 0, processed[0].length - 1, 0, processed.length - 1, -1, -1, -1, -1);
		//sort the rectangles by area
		final double xFactor = 1.0 / ((double) getFrameWidth());
		final double yFactor = 1.0 / ((double) getFrameHeight());
		//scale the rectangles to be in terms of width/height
		rectangles = rectangles.stream()
				.map(PreciseRectangle.scalar(xFactor, yFactor, xFactor, yFactor))
				.sorted((a, b)->(Double.compare(b.getArea(), a.getArea())))
				.collect(Collectors.toList());
		System.out.println("(done)");
		return rectangles;
	}

	@Override
	public  List<PreciseRectangle> apply(VideoFrame frameOn, VideoFrame frameOff) {
		boolean[][] result;
		if (saveDiff)
			result = calcDeltaWithDiff(frameOn, frameOff);
		else
			result = calcDeltaAdv(frameOn, frameOff);
		if (result == null)
			return null;
		return processBooleanMap(result);
	}
}
