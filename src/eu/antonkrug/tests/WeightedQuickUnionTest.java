package eu.antonkrug.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import eu.antonkrug.Picture;
import eu.antonkrug.WeightedQuickUnion;

/**
 * This test has hardcoded values for a cloud-small.jpg image
 * 
 * @author Anton Krug
 */
public class WeightedQuickUnionTest {
	Picture							img;
	WeightedQuickUnion	union;

	/**
	 * Hardcoded for cloud-small, do not change name of this file, or content
	 * unless you know what you are doing
	 */
	@Before
	public void init() {
		img = new Picture("images/cloud-small.jpg");
		union = new WeightedQuickUnion(img.width() * img.height());
		union.populateTree(img, 190, false);
	}

	@Test
	public void testPopulateTree() {
		assertEquals(38, union.getKeyValueGroups().size());
	}

	@Test
	public void testFind() {
		if (WeightedQuickUnion.WEIGHTED_UNION) {
			// root from different line
			assertEquals(92967, union.find(93866));

			// from different line and few pixels ahead
			assertEquals(92967, union.find(93875));
		} else {
			// root from different line
			assertEquals(136204, union.find(93866));

			// from different line and few pixels ahead
			assertEquals(136204, union.find(93875));
		}
	}

	@Test
	public void testFlatten() {
		assertEquals(false, union.isFlat());
		union.flatten(img);
		assertEquals(true, union.isFlat());
	}

	@Test
	public void testFindFlat() {
		union.flatten(img);
		if (WeightedQuickUnion.WEIGHTED_UNION) {
			assertEquals(92967, union.findFlat(93866));
			assertEquals(92967, union.findFlat(93875));
		} else {
			// root from different line
			assertEquals(136204, union.find(93866));

			// from different line and few pixels ahead
			assertEquals(136204, union.find(93875));
		}
	}

	@Test
	public void testUnionWeighted() {
		union.union(28187, 93875);
		// union.flatten(img);
		assertEquals(37, union.getKeyValueGroups().size());

		if (WeightedQuickUnion.WEIGHTED_UNION) {
			assertEquals(92967, union.find(28187));
		} else {
			assertEquals(136204, union.find(28187));

		}

		/*
		 * for (int i=0;i<img.width()*img.height();i++) { if (union.tree(i)>-1) {
		 * fail("First group found "+i); } }
		 */
	}

}
