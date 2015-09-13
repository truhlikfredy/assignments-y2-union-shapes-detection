package eu.antonkrug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import edu.princeton.cs.introcs.Stopwatch;

/**
 * This is the main class and uses union to joint all the points, can call
 * another post processing methods as well. And contains main GUI.
 * 
 * Final is used because it's not intended to extend this class and as in
 * Effective Java book if I do not design and document for inheritance then I
 * should prohibit it. It should get better performance as well because no
 * tables are used with method calls for final classes.
 * 
 * @author Anton Krug
 * 
 */

public final class MachineVision implements ChangeListener {

	public final static Boolean	VERBOSE			= true;
	public final static Boolean	FAST_START	= false;
	private final static double	QUATER_PI		= Math.PI / 4f;

	/**
	 * Kick starts the application in one common setup
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		MachineVision app = new MachineVision();

		if (FAST_START) {

			// some preselected values so you don't have to select it every time when
			// testing
			app.setThreshold(175);
			app.setFileName("images/cloud-small.jpg");
			app.loadImage(true);

			// bluring clears-out artificial independent groups because it filters out
			// the dithering used in some images
			app.blurInputImage();
			app.toBW(false);
			app.preview.repaint();

		} else {

			// dialogs for file and threshold
			if (app.chooseFile()) {
				if (VERBOSE) System.out.println("This program requires a image file to work with.");
				System.exit(1);
				// return;
			}

			app.loadImage(true);

			// bluring clears-out artificial independend groups because it filters out
			// the dithering used in some images
			app.blurInputImage();
			app.toBW(false);
			app.preview.repaint();

			JSlider slider = app.getSlider(app.optionPane);
			app.optionPane.setMessage(new Object[] { "Select thershold: ", slider });
			app.optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
			app.optionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
			JDialog dialog = app.optionPane.createDialog(app.parent, "Threshold for B&W");
			dialog.setVisible(true);
			if (VERBOSE) System.out.println("Selected threshold: " + app.getThreshold());
		}

		app.runTime = new Stopwatch();
		app.union = new WeightedQuickUnion(app.width * app.height);
		app.union.populateTree(app.pic, app.threshold, VERBOSE);
		app.union.flatten(app.pic);
		app.setGroupMinThreshold(20);
		app.disableSmallGroups();

		if (VERBOSE) {
			// print stats for each group
			app.union.groupStats();

			// print the total and average stats for all groups together
			System.out.println(app);
		}

		// app.union.generateColorsRandom();
		app.union.generateColorsBySize(app.groupSmallest, app.groupBiggest);
		app.colorizeGroups(app.preview, true, false);

		// app.colorizeBrute(app.preview);
		// app.drawGroupBoxAll(app.preview,false);
		app.preview.repaint();

		if (VERBOSE) app.union.findStatsDisplay();

		if (VERBOSE) System.out.println("Elapsed time: " + app.runTime.elapsedTime() + " seconds.");

	}

	/**
	 * internal fields
	 */

	private String							fileName;
	private int									width, height;
	private Picture							pic;
	private Picture							preview;
	private double							threshold;
	private int									groupMinThreshold, groupCount;
	private int									groupSmallest, groupBiggest, groupSizeTotal;
	private Stopwatch						runTime;
	private JOptionPane					optionPane;
	private JFrame							parent;

	private WeightedQuickUnion	union;

	/**
	 * Constructor setting up GUI objects and some default values
	 */
	public MachineVision() {
		this.width = 0;
		this.height = 0;
		this.threshold = 128;
		this.groupMinThreshold = 6;
		this.optionPane = new JOptionPane();
		this.parent = new JFrame();
		this.groupCount = 0;
	}

	/**
	 * Blur the original image
	 */
	public void blurInputImage() {
		this.pic.blur();
	}

	/**
	 * Gui file choose dialog
	 * 
	 * @return false if failed, return true if got valid image filename
	 */
	public Boolean chooseFile() {
		// Choose image file filter
		FileFilter ff = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				else if (f.getName().endsWith(".jpg"))
					return true;
				else if (f.getName().endsWith(".jpeg"))
					return true;
				else if (f.getName().endsWith(".png"))
					return true;
				else if (f.getName().endsWith(".gif"))
					return true;
				else if (f.getName().endsWith(".bmp"))
					return true;
				else return false;
			}

			public String getDescription() {
				return "All supported image files";
			}
		};

		// Choose image dialog
		final JFileChooser fc = new JFileChooser("./images");
		fc.setFileFilter(ff);
		if (fc.showOpenDialog(this.parent) == JFileChooser.APPROVE_OPTION) {
			java.io.File file = fc.getSelectedFile();
			this.setFileName(file.getPath());
			if (VERBOSE) System.out.println("File Selected :" + file.getName());
			return false;
		} else {
			if (VERBOSE) System.out.println("No file selected");
			return true;
		}
	}

	/**
	 * Display even disabled groups by iterating whole width*height
	 */
	public void colorizeBrute(Picture picture) {

		if (!this.union.isFlat()) {
			if (VERBOSE) System.out.println("For this method the image has to be flat. Flattening now.");
			this.union.flatten(this.preview);
		}

		int index = 0;
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++, index++) {
				if (this.union.tree(index) >= 0) {
					preview.setFast(x, y, this.union.getColorFlat(index));
				}
			}
		}
	}

	/**
	 * Will go through all groups and fill pixels in canvas by group colors. Can
	 * draw rectangles around and estimate if content is a circle shape
	 * 
	 * @param picture
	 *          , Boolean Target canvas
	 * @param drawBox
	 *          Draw red rectangle box around the groups
	 */
	public void colorizeGroups(Picture picture, Boolean drawBox, Boolean identifyCircle) {
		picture.clear();

		for (Map.Entry<Integer, UnionGroup> entry : this.union.getKeyValueGroups()) {
			UnionGroup group = entry.getValue();
			if (!group.isDisabled()) {
				for (int y = group.getMinY(); y <= group.getMaxY(); y++) {
					int index = group.getMinX() + y * this.width;
					for (int x = group.getMinX(); x <= group.getMaxX(); x++, index++) {
						if (this.union.tree(index) == entry.getKey()) {
							picture.setFast(x, y, group.getColor());
						}
					}
				}

				int w = group.getMaxX() - group.getMinX();
				int h = group.getMaxY() - group.getMinY();
				
				//get aspect ration
				double ar = (double)(w)/h;
				
				//guess a circle just by its area, just big enough and not too much squashed
				if (identifyCircle && group.getSize()>300 && ar>0.6 && ar<1.66) {
					// area = ( w/2 * h/2 ) * pi
					// faster area = ( w*h ) * pi/4

					int elipseArea = (int) ((double) ( w * h ) * QUATER_PI);

					double error = Math.abs((double) (group.getSize() - elipseArea) / elipseArea);

					if (error < 0.08) {
						picture.drawString(group.getMinX(), group.getMinY(), "BALL !");
					} else if (error < 0.12) {
						picture.drawString(group.getMinX(), group.getMinY(), "Maybe a ball?");
					}
				}

				if (drawBox) this.drawGroupBox(picture, group);

			}
		}
	}

	/**
	 * Disable groups which size is smaller than threshold. They will be just
	 * flaged to be not shown, but still keept in memory. So this process can bre
	 * reverted if desired.
	 */
	public void disableSmallGroups() {
		this.groupCount = 0;
		this.groupBiggest = -1;
		this.groupSmallest = Integer.MAX_VALUE;
		this.groupSizeTotal = 0;

		for (UnionGroup group : this.union.getAllGroups()) {
			if (group.getSize() < this.groupMinThreshold) {
				group.setDisabled(true);
			} else {
				group.setDisabled(false);

				if (group.getSize() > this.groupBiggest) this.groupBiggest = group.getSize();
				if (group.getSize() < this.groupSmallest) this.groupSmallest = group.getSize();

				this.groupSizeTotal += group.getSize();

				this.groupCount++;
			}
		}
	}

	/**
	 * Draw rectangle around a group <b>g</b> into canvas <b>picture</b>.
	 * 
	 * @param picture
	 * @param g
	 */
	public void drawGroupBox(Picture picture, UnionGroup g) {
		picture.rectange(g.getMinX(), g.getMaxX(), g.getMinY(), g.getMaxY());
	}

	/**
	 * Draw rectangle around all groups
	 * 
	 * @param picture
	 *          destination canvas
	 * @param showAll
	 *          show even small (disabled) group
	 */
	public void drawGroupBoxAll(Picture picture, Boolean showAll) {
		for (UnionGroup group : this.union.getAllGroups())
			if (showAll || !group.isDisabled()) this.drawGroupBox(picture, group);
	}

	/**
	 * Get the image file name
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Get the size of biggest blob
	 * 
	 * @return
	 */
	public int getGroupBiggest() {
		return groupBiggest;
	}

	/**
	 * Get number of blobs detected
	 * 
	 * @return
	 */
	public int getGroupCount() {
		return groupCount;
	}

	/**
	 * Get back the minimal threshold for small groups
	 * 
	 * @return
	 */
	public int getGroupMinThreshold() {
		return groupMinThreshold;
	}

	/**
	 * Return volume of all blobs detected
	 * 
	 * @return
	 */
	public int getGroupSizeTotal() {
		return groupSizeTotal;
	}

	/**
	 * Get the size of the smallest blob
	 * 
	 * @return
	 */
	public int getGroupSmallest() {
		return groupSmallest;
	}

	/**
	 * Get height of the origin image
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Get back the source image object (use carefuly)
	 * 
	 * @return
	 */
	public Picture getImage() {
		return this.pic;
	}

	/**
	 * Return preview windows image object (use carefuly)
	 * 
	 * @return
	 */
	public Picture getPreview() {
		return this.preview;
	}

	/**
	 * Returns the JSlider object for the threshold
	 * 
	 * @param optionPane
	 * @return Get the slider for thershold
	 */
	private JSlider getSlider(final JOptionPane optionPane) {
		JSlider slider = new JSlider();
		slider.setMajorTickSpacing(25);
		slider.setPaintTicks(true);
		slider.setPaintLabels(false);

		slider.addChangeListener(this);
		return slider;
	}

	/**
	 * Get the threshold for B/W binarization
	 * 
	 * @return
	 */
	public double getThreshold() {
		return this.threshold;
	}

	/**
	 * Return union class object (use carefuly)
	 * 
	 * @return
	 */
	public WeightedQuickUnion getUnion() {
		return union;
	}

	/**
	 * Get width of the origin image
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Print out stats about groups, their sizes and dimensions.
	 */
	public void groupSizesStats() {
		System.out.println(this);
	}

	/**
	 * Load up image and prepare other scafolding so the functions can work
	 * properly
	 * 
	 * @param show
	 *          if set true it will popup window showing content of the image
	 */
	public void loadImage(Boolean show) {
		this.pic = new Picture(this.fileName);
		this.preview = new Picture(this.pic);

		this.width = pic.width();
		this.height = pic.height();

		if (show) {
			this.preview.show();
			// will exit application when the preview window is closed
			this.preview.getJFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}

	/**
	 * When given image just prepare other scafolding so the functions can work
	 * properly
	 * 
	 * @image source image
	 * @param show
	 *          if set true it will popup window showing content of the image
	 */
	public void loadImage(BufferedImage image, Boolean show) {
		this.pic = new Picture(image);
		this.preview = new Picture(this.pic);

		this.width = pic.width();
		this.height = pic.height();

		if (show) {
			this.preview.show();
			// will exit application when the preview window is closed
			this.preview.getJFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}

	/**
	 * Set the filenam
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Set threshold how big for the group to be shown has to be
	 * 
	 * @param groupMinThreshold
	 */
	public void setGroupMinThreshold(int groupMinThreshold) {
		this.groupMinThreshold = groupMinThreshold;
	}

	/**
	 * Change the source image by giving BufferedImage object (use carefuly)
	 * 
	 * @param pic
	 */
	public void setImage(BufferedImage pic) {
		this.pic = new Picture(pic);
	}

	/**
	 * Change the source image object (use carefuly)
	 * 
	 * @param pic
	 */
	public void setImage(Picture pic) {
		this.pic = pic;
	}

	/**
	 * Set thershold to new value
	 * @param value
	 */
	public void setThreshold(double value) {
		this.threshold = value;
	}

	/**
	 * Change internal union class object with new given one (use carefuly)
	 * 
	 * @param union
	 */
	public void setUnion(WeightedQuickUnion union) {
		this.union = union;
	}

	/**
	 * State changer listener for slider
	 */
	public void stateChanged(ChangeEvent e) {
		JSlider theSlider = (JSlider) e.getSource();
		if (!theSlider.getValueIsAdjusting()) {
			optionPane.setInputValue(new Integer(theSlider.getValue()));
			this.setThreshold(Double.parseDouble(this.optionPane.getInputValue().toString()) * 2.55);
			this.toBW(false);
			this.preview.repaint();
		}
	}

	/**
	 * Binarize preview image
	 */
	public void toBW(Boolean inverse) {
		this.preview.toBW(this.pic, this.threshold, inverse);
	}

	/**
	 * Binarize origin image
	 */
	public void toBWorigin(Boolean inverse) {
		this.pic.toBW(this.pic, this.threshold, inverse);
	}

	/**
	 * toString outputs all the total stats for all groups together
	 */
	public String toString() {
		if (this.groupCount > 0) {
			return String.format("Groups shown = %5d ( smallest = %6d biggest = %6d AVG = %6d)",
					this.groupCount, this.groupSmallest, this.groupBiggest, this.groupSizeTotal
							/ this.groupCount);
		} else {
			return "No objects detected";
		}
	}

}
