package eu.antonkrug;

import java.awt.Color;
import java.util.Random;
import java.security.SecureRandom;

/**
 * Just container structure to keep together meta of groups and their count.
 * Plus few methods to do some operations with them.
 * 
 * Final is used because it's not intended to extend this class and as in
 * Effective Java book if I do not design and document for inheritance then I
 * should prohibit it. It should get better performance as well because no
 * tables are used with method calls for final classes.
 * 
 * posibly it's overkill to keep min/max for x/y for each group, especialy if
 * the groups could become small and many. but then this allows very quick
 * rendering because we don't have to parse whole canvas, just for each group go
 * from min x to max x and from min y to max y.
 * 
 * it's tradeoff between memorry and perfomace to keep some data precalculated
 * but now it offers very quick way to disable small groups (noise) and
 * re-rendering the whole image much effeciently than without all this meta-data
 * 
 * @author Anton Krug
 */

public final class UnionGroup {
	private static Random	rnd	= new SecureRandom();
	private Color					color;
	private int						size;
	private Boolean				disabled;
	private int						minX, maxX;
	private int						minY, maxY;

	public UnionGroup() {
		// Set the size of a group 1
		this.size = 1;

		this.minX = Integer.MAX_VALUE;
		this.minY = Integer.MAX_VALUE;
		this.maxX = -1;
		this.maxY = -1;

		this.disabled = false;
	}

	public void generateColor() {
		// Generate bright random color to each group

		// Saturation between 0.4 and 0.9 like (4 + rnd(5)) / 10
		// float saturation = (4000 + rnd.nextInt(5000)) / 10000f;
		float saturation = (4 + rnd.nextInt(5)) / 10f;
		this.color = Color.getHSBColor(rnd.nextInt(10) / 10f, saturation, 0.9f);
	}

	public void generateColorRainbow(float size) {
		// Generate bright colors by size of each group
		this.color = Color.getHSBColor(size * 0.8f, 0.8f, 0.9f);
	}

	/*
	 * Just getters and setters
	 */

	public Color getColor() {
		return color;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public int getMinX() {
		return minX;
	}

	public int getMinY() {
		return minY;
	}

	public int getSize() {
		return size;
	}

	public void incSize() {
		this.size++;
	}

	public void incSize(int amount) {
		this.size += amount;
	}

	public Boolean isDisabled() {
		return disabled;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public void setMaxX(int maxx) {
		this.maxX = maxx;
	}

	public void setMaxY(int maxy) {
		this.maxY = maxy;
	}

	public void setMinX(int minx) {
		this.minX = minx;
	}

	public void setMinY(int miny) {
		this.minY = miny;
	}

	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * In case stats about a group have to be displayed, to string can output
	 * verbose and formated info about the group
	 */
	public String toString() {
		return String.format("Group weight=%6d \t X=%4d -%4d \t Y=%4d -%4d \t (%4d x %4d)",
				this.getSize(), this.getMinX(), this.getMaxX(), this.getMinY(), this.getMaxY(),
				this.getMaxX() - this.getMinX() + 1, this.getMaxY() - this.getMinY() + 1);
	}

	/**
	 * Will update min and max of X/Y of this group
	 * @param x
	 * @param y
	 */
	public void updateMinMax(int x, int y) {
		if (x < minX) this.minX = x;

		if (x > maxX) this.maxX = x;

		if (y < minY) this.minY = y;

		if (y > maxY) this.maxY = y;
	}

}
