package com.moe365.moepi.processing;

/**
 * A BinaryImage is a image that has a value of true or false at any given point.
 * You can think of it as a lazily-populated boolean array.
 * @author mailmindlin
 * @since April 2016 (v. 0.2.7)
 */
@FunctionalInterface
public interface BinaryImage {
	/**
	 * Get the value of the image at the given coordinate
	 * @param x x coordinate of pixel to test
	 * @param y y coordinate of pixel to test
	 * @return the value of the image at the point
	 */
	boolean test(int x, int y);
	
	/**
	 * Test if <i>any</i> pixel in the specified range is set.
	 * <p>
	 * Implementations may override this method in the name of performance, but
	 * all implementations MUST (as in
	 * <a href="https://www.ietf.org/rfc/rfc2119.txt">RFC 2119 MUST</a>):
	 * <ol>
	 * <li>Have no side-effects</li>
	 * <li>Be functionally equivalent to:
	 * 
	 * <pre>
	 * for (int x = xMin; x < xMax; x++)
	 * 	   if (text(x, y))
	 * 		   return true;
	 * return false;
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * </p>
	 * 
	 * @param y
	 *            Row to test
	 * @param xMin
	 *            Leftmost column of range to test (included in range)
	 * @param xMax
	 *            Rightmost column of range to test (included in range)
	 * @return {@code true} iff <i>any</i> pixel in the specified range is true.
	 */
	default boolean testRow(int y, int xMin, int xMax) {
		for (int x = xMin; x < xMax; x++)
			if (test(x, y))
				return true;
		return false;
	}
	
	/**
	 * TODO document
	 * @param x
	 * @param yMin
	 * @param yMax
	 * @return
	 * @see #testRow(int, int, int)
	 */
	default boolean testCol(int x, int yMin, int yMax) {
		for (int y = yMin; y < yMax; y++)
			if (test(x, y))
				return true;
		return false;
	}

	/**
	 * Test the coordinate (x, y) by rounding the doubles.
	 * @param x x coordinate of pixel to test
	 * @param y y coordinate of pixel to test
	 * @return the value of the image at the point
	 * @see #test(int, int)
	 */
	default boolean test(double x, double y) {
		return test((int)Math.round(x), (int)Math.round(y));
	}
}
