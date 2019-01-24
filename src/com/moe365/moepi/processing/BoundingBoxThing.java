package com.moe365.moepi.processing;

import java.awt.Rectangle;
import java.util.List;

import com.moe365.moepi.geom.PreciseRectangle;

/**
 * @author FRC Team 365
 */
public class BoundingBoxThing {
	private static final int MINDIM = 40; // Smallest allowable dimension for any
											// side of box

/**
 * Recursive function to create a List of Rectangles bbr 
 * that represent all bounding boxes of minimum size MINDIM
 * in boolean array img (which represents a thresholded image) 
 * It returns true or false to indicate if it found any boxes
 * This method attempts to split the image into 2 (first with a vertical line, then with a horizontal one) 
 * then call itself on the resulting 2 areas of the image (area set by the lim parameters)
 * The bound parameters store known edges of boxes, once the fully encased box it is added to list. 
 * Add to the bbr list are side effects and are not thread safe.  
 * It is executed in a single thread, depth first.
 * 
 * @param  img  A boolean array which represents a thresholded image
 * @param  bbr List of Rectangles bbr that represent all bounding boxes of minimum size MINDIM
 * @param  limXmin  lower limit of the x position in the area to search
 * @param  limXmax  upper limit of the x position in the area to search
 * @param  limYmin  lower limit of the y position in the area to search
 * @param  limYmax  upper limit of the y position in the area to search 
 * @param  boundXmin  location of valid left edge of box (-1 if none)
 * @param  boundXmax  location of valid right edge of box (-1 if none)
 * @param  boundYmin  location of valid top edge of box (-1 if none)
 * @param  boundYmax  location of valid bottom edge of box (-1 if none)
 * @return      if there are any bounding boxes
 * @see   Rectangle      
 */
	public static boolean boundingBoxRecursive(boolean[][] img, List<PreciseRectangle> bbr, final int limXmin, final int limXmax,
			final int limYmin, final int limYmax, int boundXmin, int boundXmax, int boundYmin, int boundYmax) {
		if (((limXmax - limXmin) < MINDIM) || ((limYmax - limYmin) < MINDIM))
			// BASE CASE box is too small, disregard
			return false;
		// try to split the box in half vertically or horizontally and call
		// recursively on the 2 halves
		int x, y; //defined here since they will be reused and tested after for loops
		int splitX = limXmin + (limXmax - limXmin) / 2; //half the width first vertical split line to try
		xLoop:
		for (x = splitX; x > limXmin; x--) {
			// Left side of half split, test all vertical lines till one doesn't go thru a contour
			
			//if a split line is free from connected pixels so ignore it and move the limits by 1
			boolean leftOff = false, rightOff = false;
			
			//top edge case
			if (test(img, x, limYmin)) {
				//indicates if pixel is connected to the right or left
				boolean leftBool  = test(img, x - 1, limYmin) && test(img, x - 1, limYmin + 1);
				boolean rightBool = test(img, x + 1, limYmin) && test(img, x + 1, limYmin + 1);
				if (leftBool && rightBool)
					continue; //fully connected, try next split line
				if (leftBool)
					leftOff = true; //if valid line, it is also a right edge
				if (rightBool)
					rightOff = true; //if valid line, it is also a left edge
			}
			//bottom edge case
			if (test(img, x, limYmax)) {
				boolean leftBool  = test(img, x - 1, limYmax) && test(img, x - 1, limYmax - 1);
				boolean rightBool = test(img, x + 1, limYmax) && test(img, x + 1, limYmax - 1);
				if (leftBool && rightBool)
					continue;
				if (leftBool)
					leftOff = true; //if valid line, it is also a right edge
				if (rightBool)
					rightOff = true; //if valid line, it is also a left edge
			}
			
			//test the middle of the line
			for (y = limYmin + 1; y < limYmax; y++) {
				if (test(img, x, y)) {
					boolean leftBool  = adjV(img, x - 1, y);
					boolean rightBool = adjV(img, x + 1, y);
					if (leftBool && rightBool)
						continue xLoop; //fully connected, try next split line
					if (leftBool)
						leftOff = true; //if valid line, it is also a right edge
					if (rightBool)
						rightOff = true; //if valid line, it is also a left edge
				}
			}
			// valid split line, so split the rectangle and return results
			// if leftOff, we found a right edge, so include it as known edge, else
			//line is not a right edge, so don't check again by moving limit left
			return boundingBoxRecursive(img, bbr, limXmin, x - (leftOff ? 0 : 1), limYmin, limYmax, boundXmin, leftOff ? x : -1, -1, -1)
				// if rightOff, we found a left edge
				| boundingBoxRecursive(img, bbr, x + (rightOff ? 0 : 1), limXmax, limYmin, limYmax, rightOff ? x : -1, boundXmax, -1, -1);
		}
		
		// check for pixels on left edge of box since it is not a known edge
		if (boundXmin != x && updateXbound(img, limYmin, limYmax, x, true))
			boundXmin = x;

		xLoop:
		for (x = splitX + 1; x < limXmax; x++) {
			// Right side of half split, test all vertical lines till one doesn't go thru a contour
			boolean leftOff = false, rightOff = false;
			if (test(img, x, limYmin)) {
				boolean leftBool  = test(img, x - 1, limYmin) && test(img, x - 1, limYmin + 1);
				boolean rightBool = test(img, x + 1, limYmin) && test(img, x + 1, limYmin + 1);
				if (leftBool && rightBool)
					continue;
				if (leftBool)
					leftOff = true; //if valid line, it is also a right edge
				if (rightBool)
					rightOff = true; //if valid line, it is also a left edge
			}
			if (test(img, x, limYmax)) {
				boolean leftBool  = test(img, x - 1, limYmax) && test(img, x - 1, limYmax - 1);
				boolean rightBool = test(img, x + 1, limYmax) && test(img, x + 1, limYmax - 1);
				if (leftBool && rightBool)
					continue;
				if (leftBool)
					leftOff = true; //if valid line, it is also a right edge
				if (rightBool)
					rightOff = true; //if valid line, it is also a left edge
			}
			for (y = limYmin + 1; y < limYmax; y++) {
				if (test(img, x, y)) {
					boolean leftBool  = adjV(img, x - 1, y);
					boolean rightBool = adjV(img, x + 1, y);
					if (leftBool && rightBool)
						continue xLoop;
					if (leftBool)
						leftOff = true; //if valid line, it is also a right edge
					if (rightBool)
						rightOff = true; //if valid line, it is also a left edge
				}
			}
			// valid split line, so split the rectangle and return results
			// if leftOff, we found a right edge
			return boundingBoxRecursive(img, bbr, limXmin, x - (leftOff ? 0 : 1), limYmin, limYmax, boundXmin, leftOff ? x : -1, -1, -1)
				// if rightOff, we found a left edge
				| boundingBoxRecursive(img, bbr, x + (rightOff ? 0 : 1), limXmax, limYmin, limYmax, rightOff ? x : -1, boundXmax, -1, -1);
		}
		// check for pixels on right edge of box
		if (boundXmax != x && updateXbound(img, limYmin, limYmax, x, false))
			boundXmax = x;
		
		int splitY = limYmin + (limYmax - limYmin) / 2;
		for (y = splitY; y > limYmin; y--) {
			// Top side of half split, test all horizontal lines till one doesn't go thru a contour
			boolean topOff = false, botOff = false;
			if (test(img, limXmin, y)) {
				boolean topBool = test(img, limXmin, y - 1) && test(img, limXmin + 1, y - 1);
				boolean botBool = test(img, limXmin, y + 1) && test(img, limXmin + 1, y + 1);
				if (topBool && botBool)
					continue;
				if (topBool)
					topOff = true;
				if (botBool)
					botOff = true;
			}
			if (test(img, limXmax, y)) {
				boolean topBool = test(img, limXmax, y - 1) && test(img, limXmax - 1, y - 1);
				boolean botBool = test(img, limXmax, y + 1) && test(img, limXmax - 1, y + 1);
				if (topBool && botBool)
					continue;
				if (topBool)
					topOff = true;
				if (botBool)
					botOff = true;
			}
			for (x = limXmin + 1; x < limXmax; x++) {
				if (test(img, x, y)) {
					boolean topBool = adjH(img, x, y - 1);
					boolean botBool = adjH(img, x, y + 1);
					if (topBool && botBool)
						break;
					if (topBool)
						topOff = true;
					if (botBool)
						botOff = true;
				}
			}
			if (x == limXmax)
				// valid split line, so split the rectangle and return results
				// if topOff==true, we found a bottom edge
				return boundingBoxRecursive(img, bbr, limXmin, limXmax, limYmin, y - (topOff ? 0 : 1), -1, -1, boundYmin, topOff ? y : -1)
					// if rightOff == true, we found a top edge
					| boundingBoxRecursive(img, bbr, limXmin, limXmax, y + (botOff ? 0 : 1), limYmax, -1, -1, botOff ? y : -1, boundYmax);
		}
		
		// check for pixels on top edge of box
		if (boundYmin!= y && updateYbound(img, limXmin, limXmax, y, true))
			boundYmin = y;
		
		// Bottom side of half split, test all horizontal lines till one doesn't go thru a contour
		yLoop:
		for (y = splitY + 1; y < limYmax; y++) {
			boolean topOff = false, botOff = false;
			if (test(img, limXmin, y)) {
				boolean topBool = test(img, limXmin, y - 1) && test(img, limXmin + 1, y - 1);
				boolean botBool = test(img, limXmin, y + 1) && test(img, limXmin + 1, y + 1);
				if (topBool && botBool)
					continue;
				if (topBool)
					topOff = true;
				if (botBool)
					botOff = true;
			}
			if (test(img, limXmax, y)) {
				boolean topBool = test(img, limXmax, y - 1) && test(img, limXmax - 1, y - 1);
				boolean botBool = test(img, limXmax, y + 1) && test(img, limXmax - 1, y + 1);
				if (topBool && botBool)
					continue;
				if (topBool)
					topOff = true;
				if (botBool)
					botOff = true;
			}
			for (x = limXmin + 1; x < limXmax; x++) {
				if (test(img, x, y)) {
					boolean topBool = adjH(img, x, y - 1);
					boolean botBool = adjH(img, x, y + 1);
					if (topBool && botBool)
						continue yLoop;
					if (topBool)
						topOff = true;
					if (botBool)
						botOff = true;
				}
			}
			// valid split line, so split the rectangle and return results
			// if topOff, we found a bottom edge
			return boundingBoxRecursive(img, bbr, limXmin, limXmax, limYmin, y - (topOff ? 0 : 1), -1, -1, boundYmin, topOff ? y : -1)
				// if rightOff, we found a top edge
				| boundingBoxRecursive(img, bbr, limXmin, limXmax, y + (botOff ? 0 : 1), limYmax, -1, -1, botOff ? y : -1, boundYmax);
		}
		
		// check for pixels on bottom edge of box
		if (boundYmax != y && updateYbound(img, limXmin, limXmax, y, false))
			boundYmax = y;

		if ((boundXmin < boundXmax) && (boundXmin > -1) && (boundYmin < boundYmax) && (boundYmin > -1))
			//BASE CASE we have a valid bounding box described by the bound variables that cannot be futher split
			return bbr.add(new PreciseRectangle(boundXmin, boundYmin, boundXmax - boundXmin, boundYmax - boundYmin));
		return false;
	}
	
	/**
	 * 
	 * 
	 * @param img
	 * @param limXmin
	 * @param limXmax
	 * @param y
	 * @param boundYmax
	 * @param top Whether you are checking for pixels on the top of the box, or the bottom
	 * @return
	 */
	private static boolean updateYbound(boolean[][] img, int limXmin, int limXmax, int y, boolean top) {
		// check for pixels on top/bottom edge of box
		if (test(img, limXmin, y)) {
			if (test(img, limXmin, y + (top ? 1 : -1)) && test(img, limXmin + 1, y + (top ? 1 : -1)))
				return true;
		} else if (test(img, limXmax, y)) {
			if (test(img, limXmax, y + (top ? 1 : -1)) && test(img, limXmax - 1, y + (top ? 1 : -1)))
				return true;
		} else {
			for (int x = limXmin + 1; x < limXmax - 1; x++)
				if (test(img, x, y) && adjH(img, x, y + (top ? 1 : -1)))
					return true;
		}
		return false;
	}
	
	private static boolean updateXbound(boolean[][] img, int limYmin, int limYmax, int x, boolean left) {
		// check for pixels on left/right edge of box
		if (test(img, x, limYmin)) {
			if (test(img, x + (left ? 1 : -1), limYmin) && test(img, x + (left ? 1 : -1), limYmin + 1))
				return true;
		} else if (test(img, x, limYmax)) {
			if (test(img, x + (left ? 1 : -1), limYmax) && test(img, x + (left ? 1 : -1), limYmax - 1))
				return true;
		} else {
			for (int y = limYmin + 1; y < limYmax; y++)
				if (test(img, x, y) && adjV(img, x + ( left ? 1 : -1), y))
					return true;
		}
		return false;
	}

	// used to test a 3 pixel vertical line to determine if it is adjacent.
	// Middle pixel must meet threshold, plus on of the two others
	private static final boolean adjV(boolean[][] img, int x, int y) {
		return ((test(img, x, y)) && ((test(img, x, y - 1)) || (test(img, x, y + 1))));
	}

	// used to test a 3 pixel horizontal line to determine if it is adjacent.
	// Middle pixel must meet threshold, plus on of the two others
	private static final boolean adjH(boolean[][] img, int x, int y) {
		return ((test(img, x, y)) && ((test(img, x - 1, y)) || (test(img, x + 1, y))));
	}

	private static final boolean test(boolean[][] img, int x, int y) {
		return img[y][x];
	}
}