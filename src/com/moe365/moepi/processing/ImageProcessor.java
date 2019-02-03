package com.moe365.moepi.processing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.processing.AbstractImageProcessor;
import com.moe365.moepi.processing.BinaryImage;
import com.moe365.moepi.processing.DebuggingDiffGenerator;
import com.moe365.moepi.processing.DiffGenerator;

import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.UnsupportedMethod;

/**
 * 
 * @author mailmindlin
 * @see DiffGenerator
 * @see com.moe365.moepi.processing.LazyDiffGenerator LazyDiffGenerator
 */
public class ImageProcessor extends AbstractImageProcessor<List<PreciseRectangle>> {
	public static final int DEFAULT_TOLERANCE = 70;
	
	public final DiffGenerator diff;
	
	/**
	 * Smallest allowed width of a bounding box, in pixels.
	 * 
	 * Decreasing the value of this constant will find smaller blobs,
	 * but will be more computationally expensive.
	 */
	protected final int minBlobWidth;
	
	/**
	 * Smallest allowed height of a bounding box.
	 * @see #minBlobWidth
	 */
	protected final int minBlobHeight;
	
	public ImageProcessor(int frameWidth, int frameHeight, int minBlobWidth, int minBlobHeight, Consumer<List<PreciseRectangle>> handler) {
		this(frameWidth, frameHeight, minBlobWidth, minBlobHeight, handler, false, null);
	}

	public ImageProcessor(int frameWidth, int frameHeight, int minBlobWidth, int minBlobHeight, Consumer<List<PreciseRectangle>> handler, boolean saveDiff) {
		this(frameWidth, frameHeight, minBlobWidth, minBlobHeight, handler, saveDiff, "img");
	}
	
	public ImageProcessor(int frameWidth, int frameHeight, int minBlobWidth, int minBlobHeight, Consumer<List<PreciseRectangle>> handler, boolean saveDiff, String saveLoc) {
		super(0, 0, frameWidth, frameHeight, handler);
		
//		this.diff = new LazyDiffGenerator(0, 0, frameWidth, frameHeight, DEFAULT_TOLERANCE);
		if (saveDiff) {
			if(saveLoc == null) {
				saveLoc = "img";
			}

			System.out.println("Saving Diff images to folder " + saveLoc);
			
			this.diff = new DebuggingDiffGenerator(0, 0, frameWidth, frameHeight, DEFAULT_TOLERANCE, saveLoc);
		} else {
			this.diff = new DiffGenerator(0, 0, frameWidth, frameHeight, DEFAULT_TOLERANCE);
		}
		
		this.minBlobWidth = minBlobWidth;
		this.minBlobHeight = minBlobHeight;
	}
	
	/**
	 * Try to split the image horizontally (perpendicular to the Y axis)
	 * @param img Image
	 * @param xMin Left bound of search area
	 * @param xMax Right bound of search area
	 * @param yMin Bottom of search area
	 * @param yMax Top of search area
	 * @return The index that can be split at, or -1 if no split is found
	 */
	private static final int splitH(BinaryImage img, final int xMin, final int xMax, final int yMin, final int yMax) {
		int step = nextPowerOf2(yMax - yMin);
		while (step > 2) {
			for (int split = yMin + step / 2; split < yMax; split += step) {
				if (!img.testRow(split, xMin, xMax))
					return split;
			}
			step /= 2;
		}
		return -1;
	}
	
	/**
	 * @see #splitH(boolean[][], int, int, int, int)
	 * @param img
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @return
	 */
	private static final int splitV(final BinaryImage img, final int xMin, final int xMax, final int yMin, final int yMax) {
		int step = nextPowerOf2(xMax - xMin);
		while (step > 2) {
			for (int split = xMin + step / 2; split < xMax; split += step) {
				if (!img.testCol(split, yMin, yMax))
					return split;
			}
			step /= 2;
		}
		return -1;
	}
	
	/**
	 * Searches an image for blobs. You can think of it as a kind of binary
	 * search of a 2d array.
	 * 
	 * @param img
	 *            A boolean image, ordered row, column
	 * @param result
	 *            List to populate with bounding boxes
	 * @param xMin
	 *            Left bound of image to search (minimum index of the array)
	 * @param xMax
	 *            Right bound of the image to search (maximum index of the
	 *            array)
	 * @param yMin
	 *            Top bound
	 * @param yMax
	 *            Bottom bound
	 * @return if any bounding boxes were found
	 */
	public boolean boundingBox(BinaryImage img, List<PreciseRectangle> result, final int xMin, final int xMax, final int yMin, final int yMax) {
		int width = xMax - xMin;
		int height= yMax - yMin;
		if (width < minBlobWidth || height < minBlobHeight)
			// The image is too small to find any boxes
			return false;
		int xSplit = -2;
		int ySplit = -2;
		//It should be faster to calculate a split perpendicular to the widest axis
		if (width >= height) {
			if ((ySplit = splitH(img, xMin, xMax, yMin, yMax)) < 0)
				xSplit = splitV(img, xMin, xMax, yMin, yMax);
		} else {
			if ((xSplit = splitV(img, xMin, xMax, yMin, yMax)) < 0)
				ySplit = splitH(img, xMin, xMax, yMin, yMax);
		}
		if (xSplit >= 0)
			return boundingBox(img, result, xMin, xSplit - 1, yMin, yMax) | boundingBox(img, result, xSplit + 1, xMax, yMin, yMax);
		if (ySplit >= 0)
			return boundingBox(img, result, xMin, xMax, yMin, ySplit - 1) | boundingBox(img, result, xMin, xMax, ySplit + 1, yMax);
		return result.add(new PreciseRectangle(xMin, yMin, xMax - xMin, yMax - yMin));
	}
	
	protected List<PreciseRectangle> processBooleanMap(BinaryImage processed, int w, int h) {
		// List of the rectangles to be generated by boundingBoxRecursive
		List<PreciseRectangle> boxes = new ArrayList<>();
		//find rectangles
//		long start = System.nanoTime();
		boundingBox(processed, boxes, 0, w - 1, 0, h - 1);
//		long end = System.nanoTime();
//		System.out.println("T: " + (end - start) );
		
		// TODO: process for rejection
		// List<PreciseRectangle> rectangles = processBoxesDestinationDeepSpace(boxes, processed);
		List<PreciseRectangle> rectangles = boxes;
		
		//sort the rectangles by area
		final double xFactor = 1.0 / ((double) getFrameWidth());
		final double yFactor = 1.0 / ((double) getFrameHeight());
		//scale the rectangles to be in terms of width/height
		rectangles = rectangles.stream()
				.map(PreciseRectangle.scalar(xFactor, yFactor, xFactor, yFactor))
				.sorted((a, b)->(Double.compare(b.getArea(), a.getArea())))
				.collect(Collectors.toList());
		return rectangles;
	}

	@Override
	public List<PreciseRectangle> apply(VideoFrame frameOn, VideoFrame frameOff) {
		try {
			BufferedImage offImg = frameOff.getBufferedImage();
			BufferedImage onImg = frameOn.getBufferedImage();
			return apply(onImg, offImg);
		} catch (UnsupportedMethod e) {
			//JPEG decode failed
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	public List<PreciseRectangle> apply(BufferedImage onImg, BufferedImage offImg) {
		// TODO maybe add null check for images
		BinaryImage result = this.diff.apply(onImg, offImg);
		
		if (result == null)
			return null;
		return processBooleanMap(result, offImg.getWidth(), offImg.getHeight());
	}

	/**
	 * TODO: rejection math for bounding boxes
	 * @return boxes after going through rejection math
	 */
	private List<PreciseRectangle> processBoxesDestinationDeepSpace(List<PreciseRectangle> boxes, BinaryImage processedImg) {
		List<PreciseRectangle> retval = new ArrayList<>();

		for(int boxIndex = 0; boxIndex < boxes.size(); boxIndex++) {
			final PreciseRectangle box = boxes.get(boxIndex);
			System.out.println("BOX " + boxIndex);
			System.out.println(box);
			if(box.getWidth() > box.getHeight()) {
				System.out.println("W > H");
				continue;
			}

			final int yStart = (int) Math.ceil(box.getY()) + 1;
			final int xStart = (int) Math.ceil(box.getX());
			final int yMax = (int) Math.ceil(box.getHeight() / 4.0) + yStart;
			final int xMax = (int) Math.ceil(box.getWidth()) + xStart;
			
			int leftScore = 0, rightScore = 0, boxScore = 0;
			boolean hitTrue = false, rejectedInLoop = false;
			List<Integer> xs = new ArrayList<>(yMax - yStart), xPrimes = new ArrayList<>(yMax - yStart);
			int currentFirstDerivative = Integer.MAX_VALUE, lastFirstDerivative = Integer.MAX_VALUE;
			int currentCount = 0, lastCount = Integer.MAX_VALUE;
			for(int y = yStart; y < yMax; y++) {
				for(int x = xStart; x < xMax; x++) {
					if(processedImg.test(x, y)) {
						//System.out.println(currentCount);
						hitTrue = true;
						break;
					}
					currentCount++;
				}

				if(hitTrue) {
					xs.add(currentCount);
				}

				if(hitTrue && lastCount != Integer.MAX_VALUE) {
					currentFirstDerivative = currentCount - lastCount;
					xPrimes.add(currentFirstDerivative);

					if(currentFirstDerivative == 0 && lastFirstDerivative == 0) {
						boxScore++;
					}
					else if(currentFirstDerivative == -1) {
						leftScore++;
					}
					else if(currentFirstDerivative == 1) {
						rightScore++;
					}
					else if(currentFirstDerivative >= -12 && currentFirstDerivative <= -2) {
						rightScore++;
					}
					else if(currentFirstDerivative == 0) {

					}
					else {
						rejectedInLoop = true;
						break;
					}
				}

				if(hitTrue) {
					lastCount = currentCount;
					lastFirstDerivative = currentFirstDerivative == 0 && lastFirstDerivative == 0 ? Integer.MAX_VALUE : currentFirstDerivative;;
				} else {
					lastCount = Integer.MAX_VALUE;
					lastFirstDerivative = Integer.MAX_VALUE;
				}

				currentFirstDerivative = 0;
				currentCount = 0;
			}

			boxScore /= 2;

			System.out.println("f(y) = " + xs);
			System.out.println("f'(y) = " + xPrimes);

			System.out.println("Left Score: " + leftScore);
			System.out.println("Right Score: " + rightScore);
			System.out.println("Box Score: " + boxScore);
			
			if(!rejectedInLoop) {
				if(leftScore > rightScore && leftScore > (boxScore-1)) {
					System.out.println("LEFT");
					retval.add(box);
				}
				else if(rightScore > leftScore && rightScore > (boxScore-1)) {
					System.out.println("RIGHT");
					retval.add(box);
				}
				else {
					System.out.println("REJECTED reason2");
				}
			} else {
				System.out.println("REJECTED reason1");
			}

			System.out.println();
		}
		
		// try{
		// 	Thread.sleep(5*1000);
		// 	System.out.println(System.lineSeparator());
		// }catch(Exception ex){
		// 	ex.printStackTrace();
		// 	System.exit(1);
		// }

		return retval;
	}
}
