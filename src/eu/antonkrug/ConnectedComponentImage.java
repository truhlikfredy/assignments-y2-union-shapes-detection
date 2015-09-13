package eu.antonkrug;

import eu.antonkrug.Picture;

/*************************************************************************
 * The <tt>ConnectedComponentImage</tt> class
 * <p>
 * 
 * @author Anton Krug
 *************************************************************************/
public class ConnectedComponentImage {

	private MachineVision				app;
	private WeightedQuickUnion	union;

	/**
	 * Initialise fields
	 * 
	 * @param fileLocation
	 */
	public ConnectedComponentImage(String fileLocation) {
		this.app = new MachineVision();

		this.app.setThreshold(70);
		this.app.setFileName(fileLocation);
		this.app.loadImage(false);
		this.app.blurInputImage();
		this.app.toBW(false);

		this.union = new WeightedQuickUnion(this.app.getWidth() * this.app.getHeight());
		this.app.setUnion(union);

		this.union.populateTree(this.app.getImage(), this.app.getThreshold(), false);
		this.union.flatten(this.app.getImage());

		this.app.setGroupMinThreshold(30);
		this.app.disableSmallGroups();
	}

	/**
	 * Returns the number of components identified in the image.
	 * 
	 * @return the number of components (between 1 and N)
	 */
	public int countComponents() {
		return this.app.getGroupCount();
	}

	/**
	 * Returns the original image with each object bounded by a red box.
	 * 
	 * @return a picture object with all components surrounded by a red box
	 */
	public Picture identifyComonentImage() {
		// this.union.generateColorsRandom();
		this.union.generateColorsBySize(app.getGroupSmallest(), app.getGroupBiggest());
		this.app.colorizeGroups(this.app.getPreview(), false, false);
		
		this.app.drawGroupBoxAll(this.app.getPreview(), false);

		return this.app.getPreview();
	}

	/**
	 * Returns a picture with each object updated to a random colour.
	 * 
	 * @return a picture object with all components coloured.
	 */
	public Picture colourComponentImage() {
		// this.union.generateColorsRandom();
		this.union.generateColorsBySize(app.getGroupSmallest(), app.getGroupBiggest());

		this.app.colorizeGroups(this.app.getPreview(), false, false);

		return this.app.getPreview();

	}

	/**
	 * Get preview object (most likely if you need call redraw or send it as
	 * argument to other classes)
	 * 
	 * @return
	 */
	public Picture getPicture() {
		return this.app.getPreview();
	}

	/**
	 * Returns a binarised version of the original image
	 * 
	 * @return a picture object with all components surrounded by a red box
	 */
	public Picture binaryComponentImage() {
		this.app.toBW(false);
		return this.getPicture();
	}

}
