package com.moe365.moepi.processing;

import java.awt.image.BufferedImage;
import java.util.function.BiFunction;

/**
 * Calculates the difference between two frames.
 * <p>
 * This algorithm isn't the most efficient implementation to do this, yet is the most streightforward.
 * For every pixel in the two images provided, the differences of the R, G, and B components are calculated,
 * and a heuristic is applied to them to determine if there is a real difference between the points.
 * </p>
 * @author mailmindlin
 * @see DebuggingDiffGenerator
 * @see LazyDiffGenerator
 */
public class DiffGenerator implements BiFunction<BufferedImage, BufferedImage, BinaryImage> {
	protected final int frameMinX, frameMaxX, frameMinY, frameMaxY;
	protected final int tolerance;//70
	
	public DiffGenerator(int frameMinX, int frameMinY, int frameMaxX, int frameMaxY, int tolerance) {
		this.frameMinX = frameMinX;
		this.frameMinY = frameMinY;
		this.frameMaxX = frameMaxX;
		this.frameMaxY = frameMaxY;
		this.tolerance = tolerance;
	}
	
	@Override
	public BinaryImage apply(BufferedImage onImg, BufferedImage offImg) {
		// boolean array of the results. A cell @ result[y][x] is only
		boolean[][] result = new boolean[this.frameMaxY - this.frameMinY][this.frameMaxX - this.frameMinX];
		
		int[] pxOn = new int[3], pxOff = new int[3];
		for (int y = frameMinY; y < frameMaxY; y++) {
			//Y index into result array
			final int idxY = y - frameMinY;
			for (int x = frameMinX; x < frameMaxX; x++) {
				//X index into result array
				final int idxX = x - frameMinX;
				
				//Calculate deltas
				AbstractImageProcessor.splitRGB(onImg.getRGB(x, y), pxOn);
				AbstractImageProcessor.splitRGB(offImg.getRGB(x, y), pxOff);
				int dR = pxOn[0] - pxOff[0];
				int dG =  pxOn[1] - pxOff[1];
				//int dB =  pxOn[2] - pxOff[2];
				
				//Decide whether the pixel is on. This predicate is kinda magic-y, but
				//basically, it requires green to increase by a lot, but red not much.
				if (dG > tolerance && (dR < dG - 10 || dR < tolerance))//TODO fix
					result[idxY][idxX] = true;
			}
		}
		
		return new BinaryImage() {
			@Override
			public boolean test(int x, int y) {
				return result[y][x];
			}
		};
	}
}