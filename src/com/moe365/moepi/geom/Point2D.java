package com.moe365.moepi.geom;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.moe365.moepi.util.ReflectionUtils;

/**
 * A 2D point. This class uses doubles, so it has better range & accuracy, when
 * compared to integer points.
 * 
 * @author mailmindlin (FRC Team 365)
 */
public class Point2D implements Externalizable, Cloneable {
	protected final double x;
	protected final double y;
	
	/**
	 * Create a point at (0, 0). Used for deserailization.
	 */
	protected Point2D() {
		this(0, 0);
	}
	
	/**
	 * Create a point at the given coordinates
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 */
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Get the x coordinate of this point
	 * 
	 * @return the x coordinate
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Get the y coordinate of this point
	 * 
	 * @return the y coordinate
	 */
	public double getY() {
		return y;
	}
	
	public Point2D subtract(Point2D other) {
		return new Point2D(getX() - other.getX(), getY() - other.getY());
	}
	
	/**
	 * Get a point at <code>(|x|,|y|)</code>.
	 * 
	 * @return the transformed point
	 */
	public Point2D abs() {
		return new Point2D(Math.abs(getX()), Math.abs(getY()));
	}
	
	/**
	 * Get the slope from this point to the other point.
	 * 
	 * @return slope
	 */
	public double getSlopeTo(Point2D other) {
		return (other.getY() - getY()) / (other.getX() - getX());
	}
	
	/**
	 * Get the point immediately between this and another point.
	 * 
	 * @param other
	 *            the other point
	 * @return the midpoint
	 */
	public Point2D getMidpoint(Point2D other) {
		return new Point2D(.5 * (getX() + other.getX()), .5 * (getY() + other.getY()));
	}
	
	/**
	 * Get the midpoint, relative to this point.
	 * 
	 * @param other
	 *            the other point
	 * @return the midpoint offset
	 */
	public Point2D getMidpointOffset(Point2D other) {
		return new Point2D(.5 * (other.getX() - getX()), .5 * (other.getY() - getY()));
	}
	
	/**
	 * Essentially <code>{@link #getDistance(Point2D)}^2</code>. Because this
	 * method does not require a square root operation, it is
	 * <span title="Because of hardware optimization"><del>much</del>
	 * <ins>somewhat</ins></span> faster.
	 * 
	 * @param other
	 * @return distance squared
	 */
	public double getDistanceSquared(Point2D other) {
		return Math.pow(getX() - other.getX(), 2) + Math.pow(getY() - other.getY(), 2);
	}
	
	/**
	 * Pythagorean calculation of distance
	 * 
	 * @param other
	 * @return distance
	 */
	public double getDistance(Point2D other) {
		return Math.sqrt(getDistanceSquared(other));
	}
	
	/**
	 * Gets the <a href="en.wikipedia.org/wiki/Taxicab_geometry">taxicab
	 * distance</a> between this point and another point. This has the advantage
	 * of being a bit faster (no multiplication/square root ops) to calculate
	 * than the normal distance.
	 * 
	 * @param other
	 *            The other point
	 * @return the distance
	 */
	public double getTaxicabDistance(Point2D other) {
		return Math.abs(getX() - other.getX()) + Math.abs(getY() - other.getY());
	}
	
	@Override
	public String toString() {
		return new StringBuilder("[x:").append(String.format("%.2f", getX())).append(",y:")
				.append(String.format("%.2f", getY())).append(']').toString();
	}
	
	@Override
	public int hashCode() {
		long bits = Double.doubleToLongBits(getX()) ^ (Double.doubleToLongBits(getY()) * 31);
		return ((int) bits) ^ ((int) (bits >> 32));
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof Point2D) {
			Point2D otherPt = (Point2D) other;
			return otherPt.getX() == getX() && otherPt.getY() == getY();
		}
		return false;
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			ReflectionUtils.setDouble(this, "x", in.readDouble());
			ReflectionUtils.setDouble(this, "y", in.readDouble());
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			IOException ex = new IOException("Unable to set field", e);
			throw ex;
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeDouble(this.x);
		out.writeDouble(this.y);
	}
}