package com.moe365.moepi.geom;

/**
 * A set of points that create a polygon. The set of points (should) be
 * circularly- and doubly-linked.
 * 
 * @author mailmindlin (FRC Team 365)
 */
public class Polygon {
	/**
	 * The starting point for this polygon.
	 */
	protected PointNode start;
	/**
	 * Whether the points in this polygon have been modified since the area was
	 * last calculated.
	 */
	protected boolean modified = true;
	/**
	 * The last calculated area.
	 */
	protected double area;
	/**
	 * The last calculated bounding box. Null if not valid anymore.
	 */
	protected PreciseRectangle bounds;
	
	/**
	 * Create a polygon starting at the given coordinates
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	public Polygon(double x, double y) {
		this.start = new PointNode(x, y);
		this.start.next = start;
		this.start.prev = start;
	}
	
	/**
	 * A doubly-linked point.
	 * 
	 * @author mailmindlin
	 */
	public class PointNode extends Point2D {
		/**
		 * The next node
		 */
		protected PointNode next;
		/**
		 * The previous node
		 */
		protected PointNode prev;
		
		/**
		 * Create an empty node (for deserialization; do not use)
		 */
		public PointNode() {
			
		}
		
		/**
		 * Create an orphaned node at the given coordinates.
		 * 
		 * @param x
		 *            x-coordinate
		 * @param y
		 *            y-coordinate
		 */
		public PointNode(double x, double y) {
			super(x, y);
		}
		
		/**
		 * Create a node at the given coordinates immediately after
		 * <code>prev</code>
		 * 
		 * @param prev
		 *            preceding node
		 * @param x
		 *            x-coordinate
		 * @param y
		 *            y-coordinate
		 */
		public PointNode(PointNode prev, double x, double y) {
			this(x, y);
			this.prev = prev;
		}
		
		/**
		 * Create a node.
		 * 
		 * @param prev
		 *            preceding node
		 * @param x
		 *            x-coordinate
		 * @param y
		 *            y-coordinate
		 * @param next
		 *            next node
		 */
		public PointNode(PointNode prev, double x, double y, PointNode next) {
			this(prev, x, y);
			this.next = next;
		}
		
		/**
		 * Get the next node in the series.
		 */
		public PointNode next() {
			return next;
		}
		
		/**
		 * Gets the next node, if it is not null.
		 */
		public PointNode nextIfNotNull() {
			if (next == null)
				return this;
			return next;
		}
		
		/**
		 * Insert a node with the given coordinates immediately after this node.
		 * 
		 * @param x
		 *            x coordinate of the node to insert
		 * @param y
		 *            y coordinate of the node to insert
		 * @return created node
		 */
		public PointNode insertNext(double x, double y) {
			setModified();
			PointNode node = new PointNode(this, x, y, this.next);
			this.next.prev = node;
			this.next = node;
			return node;
		}
		
		/**
		 * Insert the given node immediately after this one.
		 * 
		 * @param node
		 *            to insert
		 * @return inserted node
		 */
		public PointNode insertNext(PointNode node) {
			setModified();
			this.next.prev = node;
			node.next = this.next;
			this.next = node;
			node.prev = this;
			return node;
		}
		
		/**
		 * Insert a node with the given coordinates immediately before this
		 * node.
		 * 
		 * @param x
		 *            x coordinate of the node to insert
		 * @param y
		 *            y coordinate of the node to insert
		 * @return created node
		 */
		public PointNode insertBefore(double x, double y) {
			setModified();
			PointNode node = new PointNode(this.prev, x, y, this);
			this.prev.next = node;
			this.prev = node;
			return node;
		}
		
		/**
		 * Insert the given node immediately before this one.
		 * 
		 * @param node
		 *            to insert
		 * @return inserted node
		 */
		public PointNode insertBefore(PointNode node) {
			setModified();
			this.prev.next = node;
			node.prev = this.prev;
			this.prev = node;
			node.next = this;
			return node;
		}
		
		/**
		 * Remove the next node by calling <code>this.next().remove()</code>.
		 * 
		 * @return self
		 */
		public PointNode removeNext() {
			next.remove();
			return this;
		}
		
		/**
		 * Remove this node from the chain.
		 * 
		 * @return the next node
		 */
		public PointNode remove() {
			setModified();
			this.prev.next = this.next;
			this.next.prev = this.prev;
			if (this == start)
				start = this.next;
			return this.next;
		}
		
		/**
		 * 'Set' the value of this node by removing it and inserting a new node
		 * with the given coordinates.
		 * 
		 * @param x
		 *            x coordinate of the new node
		 * @param y
		 *            y coordinate of the new node
		 * @return the new node
		 */
		public PointNode set(double x, double y) {
			setModified();
			PointNode node = new PointNode(this.prev, x, y, this.next);
			this.prev.next = node;
			this.next.prev = node;
			if (start == this)
				start = node;
			return node;
		}
	}
	
	/**
	 * Mark this polygon as having been modified, clearing previously calculated
	 * values.
	 */
	protected void setModified() {
		this.bounds = null;
		this.modified = true;
	}
	
	/**
	 * Add point to the end of the polygon chain.
	 */
	public void addPoint(double x, double y) {
		start.insertBefore(x, y);
	}
	
	/**
	 * Calculate the area of the polygon. If this method is called multiple
	 * times without changing any of the points between method calls, it will
	 * return its previous value.
	 * 
	 * @return the area of this polygon
	 */
	public double getArea() {
		if (modified) {
			double sum = 0.0;
			PointNode current = start;
			PointNode next = current.next();
			do
				sum += (current.getX() * next.getX() - current.getY() * next.getY());
			while ((current = next) != null && (next = next.nextIfNotNull()) != start);
			this.area = sum * .5;
			modified = false;
		}
		return area;
	}
	
	/**
	 * Convert this rectangle to a bounding box. If this method is called
	 * multiple times without changing any of the points between method calls,
	 * it will return its previous value.
	 * 
	 * @return bounding box
	 */
	public PreciseRectangle getBoundingBox() {
		if (this.bounds == null) {
			double minX = start.getX(), maxX = minX;
			double minY = start.getY(), maxY = minY;
			PointNode node = start;
			while (!(node = node.next()).equals(start)) {
				if (node.getX() < minX)
					minX = node.getX();
				else if (node.getX() > maxX)
					maxX = node.getX();
				
				if (node.getY() < minY)
					minY = node.getY();
				else if (node.getY() > maxY)
					maxY = node.getY();
			}
			this.bounds = new PreciseRectangle(minX, minY, maxX - minX, maxY - minY);
		}
		return bounds;
	}
	
	/**
	 * Get the starting point
	 * 
	 * @return the starting point
	 * @see #start
	 */
	public PointNode getStartingPoint() {
		return start;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		PointNode node = start;
		do
			sb.append(node).append(',');
		while ((node = node.next()) != start);
		sb.append(node).append(']');
		return sb.toString();
	}
}