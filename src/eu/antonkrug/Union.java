package eu.antonkrug;

import java.awt.Color;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import eu.antonkrug.UnionGroup;

/**
 * Union interface if the WeigthedQuickUnion has to be replaced
 * @author Anton Krug
 *
 */
public interface Union {
	public void populateTree(Picture pic, double threshold, Boolean verbose);

	public void generateColorsRandom();

	public void generateColorsBySize(int min, int max);

	public int find(int p);

	public boolean connected(int p, int q);

	public void union(int p, int q);

	public Collection<UnionGroup> getAllGroups();

	public Set<Entry<Integer, UnionGroup>> getKeyValueGroups();

	public int tree(int i);

	public void findStatsDisplay();

	public void groupStats();
	
	// if you are not implemetning flattening then findFlat and getColor can just
	// implement or call non-flat version of find. Other methods can be almost stubs
	public void flatten(Picture pic);

	public Boolean isFlat();

	public int findFlat(int p);

	public Color getColorFlat(int index);
	
}
