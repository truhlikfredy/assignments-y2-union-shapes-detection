package eu.antonkrug;

import java.awt.Color;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Based on: http://algs4.cs.princeton.edu/15uf 4th Edition</i> by Robert
 * Sedgewick and Kevin Wayne.
 * 
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * @author Anton Krug
 */
public final class WeightedQuickUnion implements Union {
	public final static Boolean	FIND_STATS				= false;
	public final static Boolean	FIND_FLAT_ON_FLY	= false;
	public final static Boolean	WEIGHTED_UNION		= true;

	private int[]								tree;
	private TreeMeta						treeMeta;

	private int									findStatsCalls;
	private int									findStatsIterations;
	private Boolean							flat;

	/**
	 * Initializes an empty union-find data structure with N isolated components
	 * -1 through N-1.
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *           if N < 0
	 * @param N
	 *          the number of objects
	 */
	public WeightedQuickUnion(int N) {
		this.flat = false;
		this.findStatsCalls = 0;
		this.findStatsIterations = 0;

		this.treeMeta = new TreeMeta();
		this.tree = new int[N];
		for (int i = 0; i < N; i++)
			this.tree[i] = -1;
	}

	/**
	 * Are the two sites <tt>p</tt> and <tt>q</tt> in the same component?
	 * 
	 * @param p
	 *          the integer representing one site
	 * @param q
	 *          the integer representing the other site
	 * @return <tt>true</tt> if the two sites <tt>p</tt> and <tt>q</tt> are in the
	 *         same component, and <tt>false</tt> otherwise
	 * @throws java.lang.IndexOutOfBoundsException
	 *           unless both 0 <= p < N and 0 <= q < N
	 */
	public boolean connected(int p, int q) {
		return find(p) == find(q);
	}

	/**
	 * Returns the component identifier for the component containing site, be
	 * careful to not send a -1 <tt>p</tt>.
	 * 
	 * @param p
	 *          the integer representing one site
	 * @return the component identifier for the component containing site
	 *         <tt>p</tt>
	 * @throws java.lang.IndexOutOfBoundsException
	 *           unless 0 <= p < N
	 */
	public int find(int p) {
		if (FIND_FLAT_ON_FLY) {
			// will update root with every find operation
			int orig = p;

			if (FIND_STATS) findStatsCalls++;

			while (p != this.tree[p]) {
				p = this.tree[p];

				if (FIND_STATS) findStatsIterations++;
			}

			this.tree[orig] = p;

		} else {
			// will not update root

			if (FIND_STATS) findStatsCalls++;

			while (p != this.tree[p]) {
				p = this.tree[p];

				if (FIND_STATS) findStatsIterations++;
			}
		}
		return p;
	}

	/**
	 * As find but it assumes the tree is flat, this makes it a lot simpler and
	 * more likely inlined by compiler. So it should be a faster then regular
	 * find.
	 */
	public int findFlat(int p) {
		if (FIND_STATS) findStatsCalls++;
		return this.tree[p];
	}

	/**
	 * Display profiling stats when verbose debuging is enabled for find call. 
	 * 
	 */
	public void findStatsDisplay() {
		if (FIND_STATS)
			System.out.println("Find stats, calls:" + findStatsCalls + " looped:" + findStatsIterations
					+ " loops/call:" + (float) (findStatsIterations) / findStatsCalls);
	}

	/**
	 * Will flatten all groups into weight 1.
	 */
	public void flatten(Picture pic) {
		// because of the horizontal pre-grouping here it will often encounter same
		// group which will belong to same root, therefore i will cache 1 pixel of
		// the root loockup to reuse it. This will work even between the gaps if the
		// group will be same. So flatening should go pretty quick and
		// guarantee that root is 1 iteration away for another fast image processing
		int cacheGroup = -1;
		int cacheRoot = -1;
		int index = 0;
		for (int y = 0; y < pic.height(); y++) {
			for (int x = 0; x < pic.width(); x++, index++) {
				if (this.tree[index] >= 0) {
					if (cacheGroup != this.tree[index]) {
						cacheGroup = this.tree[index];
						cacheRoot = find(cacheGroup);
					}
					this.tree[index] = cacheRoot;
					this.treeMeta.updateMinMax(cacheRoot, x, y);
				}
			}
		}
		this.flat = true;
	}

	/**
	 * Generate colors by group size
	 */
	public void generateColorsBySize(int min, int max) {
		this.treeMeta.generateColorsBySize(min, max);
	}

	/**
	 * Generate colors randomly
	 */
	public void generateColorsRandom() {
		// Generate colors for each group
		this.treeMeta.generateColors();
	}

	/**
	 * Get collection of all groups
	 */
	public Collection<UnionGroup> getAllGroups() {
		return this.treeMeta.getAllGroups();
	}
	
	/**
	 * As getColor but assuming the tree is flattened
	 */
	public Color getColorFlat(int index) {
		return this.treeMeta.color(findFlat(index));
	}

	/**
	 * Return collection of keys and values for each group
	 */
	public Set<Entry<Integer, UnionGroup>> getKeyValueGroups() {
		return this.treeMeta.getKeyValueGroups();
	}
	
	/**
	 * Print stats of all groups
	 */
	public void groupStats() {
		for (UnionGroup g : this.treeMeta.getAllGroups()) {
			if (!g.isDisabled()) System.out.println(g);
		}
	}

	/**
	 * True if the tree was flattened
	 */
	public Boolean isFlat() {
		return flat;
	}

	/**
	 * Will populate the tree data from given picture, union class is not best
	 * place for this method but given optimalisations something would have to be
	 * sacrified. It needs raw access to the structure. And using getters and
	 * setter could waste a lot perfomance and the goal of this implementation is
	 * to be faster. It's more messier and compromises had to be made, but should
	 * be worth it.
	 * 
	 * This made even the interface much more complicated, but on bright side,
	 * thanks of this mess the need to replace this implementation is smaller,
	 * because it's alredy working pretty nice.
	 * 
	 * The checks are not done in + shape (top, bottom, left, right) but just in
	 * _| shape (left and top), there is no point checking them twice when we are
	 * going trough all of them anyway and the order is not-important.
	 * 
	 * From 4 check this is reduced to 2 checks per pixel, where first check
	 * (left) is done in pre-pass and produces pretty quickly flat tree.
	 * 
	 * In second pass it check for top and just joins the flat groups together.
	 */
	public void populateTree(Picture pic, double threshold, Boolean verbose) {

		int index = 0;

		// will merge on line basis so at least it will do less unions in final pass
		// overhead in this stage is minimal, but saving for second stage is huge,
		// because it generates flat groups and in second stage just first pixels
		// will touch and they roots are joined, so rest of the line will just be
		// processed quickly because they will be already joined into same root and
		// no unecesarry unions are called. so it is practicaly caching 1 root
		// loockup
		// interesting how just 32bits of cached data can change how the algorithm
		// will behave. features like webcam recognition woudn't be possible without
		// this cached data

		// ********** binarize & pre-group pass ***************
		for (int y = 0; y < pic.height(); y++) {
			int leftRoot = -1;
			for (int x = 0; x < pic.width(); x++, index++) {
				if (pic.getLum(x, y) > threshold) {

					// will check if element left to current one is in some root,
					// if so will add current one to it as well.
					if (leftRoot >= 0) {

						// adding to existing group
						this.tree[index] = leftRoot;

						// we don't need to increase the group weight, because it's more
						// about height and with this aproach it will stay flat anyway
						// but for other statistics is it not bad
						this.treeMeta.inc(leftRoot);
					} else {

						// creating new group
						this.tree[index] = index;
						leftRoot = index;
						this.treeMeta.inc(index);
					}

				} else {
					this.tree[index] = -1;
					leftRoot = -1;
				}

			}
		}

		if (verbose) System.out.println("Pre-pass groups: " + this.treeMeta.getGroups());

		// ********* second final pass *********

		// this part could be done as linear loop but, then it would require more
		// conditional checking per each loop cycle, which would make it even slowe
		// than it's now

		// you don't have to process first line (because it will not do any
		// difference)
		index = pic.width();
		for (int y = 1; y < pic.height(); y++) {

			// get pointer 1 pixel above the current
			int top = (y - 1) * pic.width();
			for (int x = 0; x < pic.width(); x++, index++, top++)
				if (this.tree[index] >= 0 && this.tree[top] >= 0) this.union(top, index);

		}

		if (verbose) System.out.println("Final pass groups: " + this.treeMeta.getGroups());

	}

	/**
	 * Return content of index i
	 */
	public int tree(int i) {
		return tree[i];
	}

	/**
	 * Merges the component containing site<tt>p</tt> with the component
	 * containing site <tt>q</tt>.
	 * 
	 * @param p
	 *          the integer representing one site
	 * @param q
	 *          the integer representing the other site
	 * @throws java.lang.IndexOutOfBoundsException
	 *           unless both 0 <= p < N and 0 <= q < N
	 */
	public void union(int p, int q) {
		int rootP = find(p);
		int rootQ = find(q);

		// they are the same, exit
		if (rootP == rootQ) return;

		// make smaller root point to larger one
		if (WEIGHTED_UNION) {
			if (this.treeMeta.size(rootP) < this.treeMeta.size(rootQ)) {
				this.tree[rootP] = rootQ;
				this.treeMeta.merge(rootQ, rootP);
			} else {
				this.tree[rootQ] = rootP;
				this.treeMeta.merge(rootP, rootQ);
			}
		} else {
			this.tree[rootP] = rootQ;
			this.treeMeta.merge(rootQ, rootP);
		}
	}

}
