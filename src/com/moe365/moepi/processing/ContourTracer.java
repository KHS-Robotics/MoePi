package com.moe365.moepi.processing;

import java.awt.image.BufferedImage;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.moe365.moepi.geom.Polygon;
import com.moe365.moepi.geom.Polygon.PointNode;
import com.moe365.moepi.geom.PreciseRectangle;

import au.edu.jcu.v4l4j.VideoFrame;

/**
 * An image processor that finds blobs in images by tracing their contours.
 * @author mailmindlin (FRC Team 365)
 */
public class ContourTracer extends AbstractImageProcessor<List<Polygon>> {
	protected int minBlobWidth = 20;
	protected int minBlobHeight = 10;
	protected double maxSegmentLength = 10.0;
	protected double stepSize = 4.0;
	public static final int minGreenTolerance = 70;
	public static final int maxRedTolerance = 70;
	public ContourTracer(int width, int height) throws IllegalArgumentException {
		this(width, height, null);
	}
	public ContourTracer(int width, int height, Consumer<List<Polygon>> handler) {
		super(0, 0, width, height, handler);
		System.out.println("W: " + width + "\tH: " + height);
	}
	
	public ContourTracer(ContourTracerParams params, Consumer<List<Polygon>> handler) {
		super(params.getFrameMinX(), params.getFrameMinY(), params.getFrameMaxX(), params.getFrameMaxY(), handler);
		this.minBlobWidth = params.getMinBlobWidth();
		this.minBlobHeight = params.getMinBlobHeight();
		this.maxSegmentLength = params.getMaxSegmentLength();
		this.stepSize = params.getStepSize();
	}
	@Override
	public List<Polygon> apply(VideoFrame frameOn, VideoFrame frameOff) {
		BufferedImage imgOn = frameOn.getBufferedImage();
		BufferedImage imgOff = frameOff.getBufferedImage();
		final boolean[][] processed = new boolean[getFrameHeight()][getFrameWidth()];
		final boolean[][] cache = new boolean[getFrameHeight()][getFrameWidth()];
		//System.out.println("Starting pass1");
		List<Polygon> result = tracePass1((x, y) -> {
			if (processed[y][x])
				return cache[y][x];
			processed[y][x] = true;
			int pxOn  = imgOn.getRGB(x, y);
			int pxOff = imgOff.getRGB(x, y);
			return cache[y][x] = ((pxOn >> 8) & 0xFF) - ((pxOff >> 8) & 0xFF) > minGreenTolerance && ((pxOn >> 16) & 0xFF) - ((pxOff >> 16) & 0xFF) < maxRedTolerance;
		});
		//System.out.println("(done)");
		return result;
	}
	
	protected List<Polygon> tracePass1(BinaryImage image) {
		List<Polygon> blobs = new LinkedList<Polygon>();
		List<PreciseRectangle> bounds = new LinkedList<>();
		
		for (int y = frameMinY + minBlobHeight; y < frameMaxY - minBlobHeight; y+= minBlobHeight) {
			List<PreciseRectangle> rowBounds = new ArrayList<>();
			for (PreciseRectangle rectangle : bounds)
				if (rectangle.getY() <= y && rectangle.getY() + rectangle.getHeight() >= y)
					rowBounds.add(rectangle);
			xLoop:
			for (int x = frameMinX + minBlobWidth + ((y % (2 * minBlobHeight) == 0) ? minBlobWidth/2 : 0); x < frameMaxX - minBlobWidth; x+= minBlobWidth) {
				Iterator<PreciseRectangle> rectangles = rowBounds.iterator();
				while (rectangles.hasNext()) {
					PreciseRectangle rectangle = rectangles.next();
					double maxX = rectangle.getX() + rectangle.getWidth();
					if (rectangle.getX() < x && maxX > x) {
						//skip to the end of the rectangle
						x += (int)(maxX - x + minBlobWidth/2) % minBlobWidth;
						
						continue xLoop;
					}
				}
				if (image.test(x, y)) {
					int topY, bottomY, leftX, rightX;
					for (leftX = x; leftX > frameMinX && image.test(leftX, y); leftX--);
					Polygon blob = new Polygon(++leftX, y);
					for (topY = y; topY > frameMinY && image.test(x, topY); topY--);
					blob.addPoint(x, ++topY);
					for (rightX = x; rightX < frameMaxX && image.test(rightX, y); rightX++);
					blob.addPoint(--rightX, y);
					for (bottomY = y; bottomY < frameMaxY && image.test(x, bottomY); bottomY++);
					blob.addPoint(x, --bottomY);
					tracePass2(image, blob);
					tracePass3(blob);
					blobs.add(blob);
					//skip to the end of this rectangle
					PreciseRectangle blobBounds = blob.getBoundingBox();
					x += (int)(blobBounds.getX() + blobBounds.getWidth() - x + minBlobWidth/2) % minBlobWidth;
					rowBounds.add(blobBounds);
					bounds.add(blobBounds);
					continue;
				}
			}
		}
		return blobs;
	}
	/**
	 * Pass2 fills out the polygon.
	 * @param image image that the polygon is in
	 * @param blob partially formed polygon
	 */
	protected void tracePass2(BinaryImage image, Polygon blob) {
		System.out.println("Pass2: " + blob);
		final PointNode startingPoint = blob.getStartingPoint();
		PointNode pointA = startingPoint, pointB = pointA.next();
		while (true) {
			// Use distance^2, because x^2 < r^2 if x < r, and x^2 > r^2 if x > r, and it's faster, because no sqrt operations.
			if (pointA.equals(pointB)) {
				pointA.removeNext();
			} else if (pointA.getDistanceSquared(pointB) > maxSegmentLength * maxSegmentLength) {
				// point A and B are >r px apart
				
				double midpointOffsetX = .5 * (pointB.getX() - pointA.getX());
				double midpointOffsetY = .5 * (pointB.getY() - pointA.getY());
				
				double midpointX = pointA.getX() + midpointOffsetX;
				double midpointY = pointA.getY() + midpointOffsetY;
				boolean midpointValue = image.test(midpointX, midpointY);
				if (midpointOffsetY == 0) {
					if ((midpointOffsetX > 0) == midpointValue) {
						while (midpointY >= frameMinY && image.test(midpointX, midpointY) == midpointValue)
							midpointY--;
						midpointY++;
					} else {
						while (midpointY < frameMaxY && image.test(midpointX, midpointY) == midpointValue)
							midpointY++;
						midpointY--;
					}
				} else if (midpointOffsetX == 0) {
					if ((midpointOffsetY > 0) == midpointValue) {
						while (midpointX >= frameMinX && image.test(midpointX, midpointY) == midpointValue)
							midpointX--;
						//Take a step backwards
						midpointX++;
					} else {
						while ((midpointX + .5 < frameMaxX) && image.test(midpointX, midpointY) == midpointValue)
							midpointX++;
						//Take a step backwards
						midpointX--;
					}
				} else {
					final double invSlope = Math.abs(midpointOffsetX / midpointOffsetY);
					double stepX = stepSize / Math.sqrt(invSlope * invSlope + 1);
					double stepY = invSlope * stepX;
					if ((midpointOffsetX < 0) ^ midpointValue)
						stepY = -stepY;
					if ((midpointOffsetY > 0) ^ midpointValue)
						stepX = -stepX;
					while ((midpointX >= frameMinX) && (midpointY >= frameMinY) && (midpointX + .5 < frameMaxX) && (midpointY + .5 < frameMaxY) && image.test(midpointX, midpointY) == midpointValue) {
						midpointX += stepX;
						midpointY += stepY;
					}
					//Take a step backwards
					midpointX -= stepX;
					midpointY -= stepY;
				}
				pointB = pointA.insertNext(midpointX, midpointY);
			} else {
				if ((pointA = pointA.next()).equals(startingPoint))
					break;
			}
			if ((pointB = pointA.next()) == null)
				break;
		}
		System.out.println("(done pass2): " + blob);
	}
	/**
	 * Pass3 smoothes straight edges.
	 * @param blob polygon to smooth
	 */
	protected void tracePass3(Polygon blob) {
		//TODO finish
//		PointNode pointA = blob.getStartingPoint(), pointB, pointC;
//		while ((pointB = pointA.next()) != null && (pointC = pointB.next()) != null) {
//			
//		}
	}
	/**
	 * Parameters for the ContourTracer, so you can use getter/setters instead of really long constructors.
	 * @author mailmindlin
	 */
	public static class ContourTracerParams implements Externalizable {
		protected int frameMinX = 0;
		protected int frameMinY = 0;
		protected int frameMaxX;
		protected int frameMaxY;
		protected int minBlobWidth = 20;
		protected int minBlobHeight = 10;
		protected double maxSegmentLength = 10.0;
		protected double stepSize = 4.0;

		/**
		 * @return the step size
		 */
		public double getStepSize() {
			return stepSize;
		}

		/**
		 * @param stepSize the stepSize to set
		 * @return self
		 */
		public ContourTracerParams setStepSize(double stepSize) {
			this.stepSize = stepSize;
			return this;
		}

		/**
		 * @return The minimum x value that this ContourTracer will search within
		 */
		public int getFrameMinX() {
			return frameMinX;
		}

		/**
		 * Set the minimum x value that this ContourTracer will search within
		 * @param value Value to set it to
		 * @return self
		 */
		public ContourTracerParams setFrameMinX(int value) {
			this.frameMinX = value;
			return this;
		}
		
		/**
		 * @return The minimum y value that this ContourTracer will search within
		 */
		public int getFrameMinY() {
			return frameMinY;
		}

		public ContourTracerParams setFrameMinY(int frameHeight) {
			this.frameMinY= frameHeight;
			return this;
		}

		/**
		 * Get the minimum with of a blob that is considered valid. Blobs with a smaller
		 * width will be discarded
		 * @return The minimum valid blob width
		 */
		public int getMinBlobWidth() {
			return minBlobWidth;
		}

		/**
		 * Set the minimum with of a blob that is considered valid. Blobs with a smaller
		 * width will be discarded
		 * @param minBlobWidth New value
		 * @return self
		 */
		public ContourTracerParams setMinBlobWidth(int minBlobWidth) {
			this.minBlobWidth = minBlobWidth;
			return this;
		}
		
		public ContourTracerParams setFrameMaxX(int frameMaxX) {
			this.frameMaxX = frameMaxX;
			return this;
		}
		
		public int getFrameMaxX() {
			return this.frameMaxX;
		}
		
		public ContourTracerParams setFrameMaxY(int frameMaxY) {
			this.frameMaxY = frameMaxY;
			return this;
		}
		
		public int getFrameMaxY() {
			return this.frameMaxY;
		}
		
		public int getMinBlobHeight() {
			return minBlobHeight;
		}

		public ContourTracerParams setMinBlobHeight(int minBlobHeight) {
			this.minBlobHeight = minBlobHeight;
			return this;
		}

		public double getMaxSegmentLength() {
			return maxSegmentLength;
		}

		/**
		 * Set the max segment length.
		 */
		public ContourTracerParams setMaxSegmentLength(double maxSegmentLength) {
			this.maxSegmentLength = maxSegmentLength;
			return this;
		}
		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(this.getFrameMinX());
			out.writeInt(this.getFrameMinY());
			out.writeInt(this.getFrameMaxX());
			out.writeInt(this.getFrameMaxY());
			out.writeInt(this.getMinBlobWidth());
			out.writeInt(this.getMinBlobHeight());
			out.writeDouble(this.getMaxSegmentLength());
			out.writeDouble(this.getStepSize());
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			this.setFrameMinX(in.readInt());
			this.setFrameMinY(in.readInt());
			this.setFrameMaxX(in.readInt());
			this.setFrameMaxY(in.readInt());
			this.setMinBlobWidth(in.readInt());
			this.setMinBlobHeight(in.readInt());
			this.setMaxSegmentLength(in.readDouble());
			this.setStepSize(in.readDouble());
		}
		
	}
}