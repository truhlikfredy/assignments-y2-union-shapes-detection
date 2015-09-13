package eu.antonkrug;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Just container structure to keep together meta of groups and their count.
 * Plus few methods to do some operations with them.
 * 
 * Final is used because it's not intended to extend this class and as 
 * in Effective Java book if I do not design and document for inheritance 
 * then I should prohibit it. It should get better performance as well because
 * no tables are used with method calls for final classes.
 * 
 * @author Anton Krug 
 */

public final class TreeMeta {
	private HashMap<Integer, UnionGroup>	meta;
	private int													groups;

	/**
	 * Contructor initialising the instances and default values
	 */
	public TreeMeta() {
		this.meta = new HashMap<Integer, UnionGroup>();
		this.groups = 0;
	}

	/**
	 * Get the color from meta data structure
	 * @param key
	 * @return
	 */
	public Color color(int key) {
		return this.meta.get(key).getColor();
	}

	/**
	 * Calls random color generator on each group
	 */
	public void generateColors() {
		for (UnionGroup group:this.getAllGroups()) {
			group.generateColor();	
		}
	}
	
	/**
	 * Call color generator on each group and give the color depending by its size
	 * @param min
	 * @param max
	 */
	public void generateColorsBySize(int min, int max) {
		float max_less=max-min;
		for (UnionGroup group:this.getAllGroups()) {
			group.generateColorRainbow((group.getSize()-min)/max_less);	
		}
	}

	/**
	 * Get metadata info by given key
	 * @param key
	 * @return
	 */
	public UnionGroup get(int key) {
		return this.meta.get(key);
	}
	
	/**
	 * Return whole meta data collections with all groups
	 * @return
	 */
	public Collection<UnionGroup> getAllGroups() {
		return this.meta.values();
	}
	
	/**
	 * Return number of groups contained in this object
	 * @return
	 */
	public int getGroups() {
		return this.groups;
	}
	
	/**
	 * Get all keys and values for all groups
	 * @return
	 */
	public Set<Entry<Integer,UnionGroup>> getKeyValueGroups() {
		return this.meta.entrySet();
	}

	/**
	 * Increment group size by one, in case it doesn't exist create new one
	 * @param key
	 */
	public void inc(int key) {
		if (this.meta.containsKey(key)) {
			this.meta.get(key).incSize();
		} else {
			this.meta.put(key, new UnionGroup());
			this.groups++;
		}
	}
	
	/**
	 * Merge 2 groups metadata together
	 * @param keyA
	 * @param keyB
	 */
	public void merge(int keyA, int keyB) {
		this.meta.get(keyA).incSize(this.meta.get(keyB).getSize());
		this.meta.remove(keyB);
		this.groups--;
	}
	
	/**
	 * Get size of given group
	 * @param key
	 * @return
	 */
	public int size(int key) {
		return this.meta.get(key).getSize();
	}

	/**
	 * Update the min and max x/y of given group 
	 * @param key
	 * @param x
	 * @param y
	 */
	public void updateMinMax(int key,int x, int y) {
		this.meta.get(key).updateMinMax(x, y);
	}

}
