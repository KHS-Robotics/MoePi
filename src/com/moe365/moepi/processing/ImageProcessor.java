package com.moe365.moepi.processing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.geom.TargetType;
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
	public static final int DEFAULT_MAX_ZEROS_IN_A_ROW = 4;

	private final int maxZerosInARow; // for rejection math, how many x'(y) = 0 in a row constitutes a box
	
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
	
	public ImageProcessor(int frameWidth, int frameHeight, int minBlobWidth, int minBlobHeight, int maxZerosInARow, Consumer<List<PreciseRectangle>> handler) {
		this(frameWidth, frameHeight, minBlobWidth, minBlobHeight, maxZerosInARow, false, null, handler);
	}

	public ImageProcessor(int frameWidth, int frameHeight, int minBlobWidth, int minBlobHeight, int maxZerosInARow, boolean saveDiff, Consumer<List<PreciseRectangle>> handler) {
		this(frameWidth, frameHeight, minBlobWidth, minBlobHeight, maxZerosInARow, saveDiff, "img", handler);
	}
	
	public ImageProcessor(int frameWidth, int frameHeight, int minBlobWidth, int minBlobHeight, int maxZerosInARow, boolean saveDiff, String saveLoc, Consumer<List<PreciseRectangle>> handler) {
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
		this.maxZerosInARow = maxZerosInARow;
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
		// List of the rectangles to be generated by boundingBox and processBoxesDestinationDeepSpace
		List<PreciseRectangle> rectangles = new ArrayList<>();

		// find rectangles
		// long boundingBoxStart = System.nanoTime();
		boundingBox(processed, rectangles, 0, w - 1, 0, h - 1);
		// long boundingBoxEnd = System.nanoTime();
		// System.out.println("Time BoundingBox: " + (boundingBoxEnd - boundingBoxStart) );
		
		// process rectangles for left/right detection/rejection
		// long processBoxesStart = System.nanoTime();
		rectangles = processBoxesDestinationDeepSpace(rectangles, processed);
		// long processBoxesEnd = System.nanoTime();
		// System.out.println("Time ProcessBox: " + (processBoxesEnd - processBoxesStart) );
		
		//sort the rectangles by area
		final double xFactor = 1.0 / ((double) getFrameWidth());
		final double yFactor = 1.0 / ((double) getFrameHeight());
		//scale the rectangles to be in terms of width/height
		rectangles = rectangles.stream()
				.map(PreciseRectangle.scalar(xFactor, yFactor, xFactor, yFactor))
				.sorted((a, b)->(Double.compare(a.getX(), b.getX())))
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
	 * Determines which targets are a left or a right, and rejects those
	 * that do not fit into our criteria. This is probably not the most
	 * efficient way, but it is one of the most straightforward.
	 * @return boxes after going through left/right detection/rejection math for Destination Deep Space
	 */
	private List<PreciseRectangle> processBoxesDestinationDeepSpace(List<PreciseRectangle> rectangles, BinaryImage processedImg) {
		List<PreciseRectangle> processed = new ArrayList<>();

		for(int boxIndex = 0; boxIndex < rectangles.size(); boxIndex++) {
			final PreciseRectangle box = rectangles.get(boxIndex);

			// System.out.println("BOX " + boxIndex);

			if(box.getWidth() > box.getHeight()) {
				// System.out.println("REJECT reason1: W > H");
				// System.out.println(box);
				continue;
			}

			final int yStart = (int) Math.ceil(box.getY()) + 1;
			final int xStart = (int) Math.ceil(box.getX());
			final int yMax = (int) Math.ceil(box.getHeight() / 4.0) + yStart;
			final int xMax = (int) Math.ceil(box.getWidth()) + xStart;

			// List<Integer> xs = new ArrayList<>(yMax - yStart), xPrimes = new ArrayList<>(yMax - yStart); // for debugging
			
			int leftScore = 0, rightScore = 0, boxScore = 0, zerosInARow = 0;
			boolean hitTrue = false, rejectedInLoop = false;
			int currentCount = 0, lastCount = Integer.MAX_VALUE, delta = 0;
			for(int y = yStart; y < yMax; y++) {
				for(int x = xStart; x < xMax; x++) {
					if(processedImg.test(x, y)) {
						hitTrue = true;
						break;
					}
					currentCount++;
				}

				// if(hitTrue) {
				// 	xs.add(currentCount);
				// }

				if(hitTrue && lastCount != Integer.MAX_VALUE) {
					delta = currentCount - lastCount;
					// xPrimes.add(delta);

					if(delta == -1) {
						leftScore++;
						rightScore--;

						if(zerosInARow > 0) {
							leftScore++;
						}

						zerosInARow = 0;
					}
					else if(delta == 1 || delta == 2) {
						rightScore++;
						leftScore--;

						if(delta == 2) {
							boxScore--;
						}

						if(zerosInARow > 0) {
							rightScore++;
						}

						zerosInARow = 0;
					}
					else if(delta >= -15 && delta <= -2) {
						rightScore++;
						leftScore--;
						boxScore--;
						zerosInARow = 0;
					}
					else if(delta == 0) {
						zerosInARow++;

						if(zerosInARow > maxZerosInARow) {
							boxScore++;
						}
					}
					else {
						rejectedInLoop = true;
						break;
					}
				}

				if(hitTrue) {
					lastCount = currentCount;
				} else {
					lastCount = Integer.MAX_VALUE;
				}

				currentCount = delta = 0;
			}

			// For debugging
			// System.out.println("f(y) = " + xs);
			// System.out.println("f'(y) = " + xPrimes);
			// System.out.println("Left Score: " + leftScore);
			// System.out.println("Right Score: " + rightScore);
			// System.out.println("Box Score: " + boxScore);
			
			if(!rejectedInLoop) {
				if(leftScore > 0 && leftScore > rightScore && leftScore >= (boxScore-1)) {
					box.setTargetType(TargetType.LEFT);
					processed.add(box);
				}
				else if(rightScore > 0 && rightScore > leftScore && rightScore >= (boxScore-1)) {
					box.setTargetType(TargetType.RIGHT);
					processed.add(box);
				}
				// else {
				// 	System.out.println("REJECTED reason3: proper target criteria not met");
				// }
			// } else {
			// 	System.out.println("REJECTED reason2: absurd delta");
			}

			// System.out.println(box);

			// System.out.println();
		}

		return processed;
	}
}
