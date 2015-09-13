package eu.antonkrug;

/*************************************************************************
 * Used princeton class as basis:
 * http://introcs.cs.princeton.edu/31datatype
 * 
 * But changed, added and removed some methods.
 * Get pixel and set pixel is much faster, more constructor options. 
 * Constructor can take BufferedImage as well.
 * 
 * More safe for atypical image inputs (indexed, B&W) with GIF and BMP
 * 
 * Post proccessing like blur, invert. Includes luminance (which is faster than 
 * princeton class) and drawRectangle so less classes are required.
 * 
 * Final is used because it's not intended to extend this class and as 
 * in Effective Java book if I do not design and document for inheritance 
 * then I should prohibit it. It should get better performance as well because
 * no tables are used with method calls for final classes.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * @author Anton Krug
 *************************************************************************/

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class Picture implements ActionListener {
	/**
	 * Tests this <tt>Picture</tt> data type. Reads a picture specified by the
	 * command-line argument, and shows it in a window on the screen.
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			Picture pic = new Picture(args[0]);
			System.out.printf("%d-by-%d\n", pic.width(), pic.height());
			pic.show();
		} else {
			System.out.println("Arguments expected");
		}
	}

	private BufferedImage	image;
	private JFrame				frame;
	private String				filename;

	private final int			width, height;

	/**
	 * Initializes a new picture that is a deep copy of <tt>pic</tt>.
	 */
	public Picture(BufferedImage pic) {
		width = pic.getWidth();
		height = pic.getHeight();
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		filename = "/dev/null :p";
		for (int x = 0; x < width(); x++)
			for (int y = 0; y < height(); y++)
				image.setRGB(x, y, pic.getRGB(x, y));
	}

	/**
	 * Initializes a blank <tt>w</tt>-by-<tt>h</tt> picture, where each pixel is
	 * black.
	 */
	public Picture(int w, int h) {
		if (w < 0) throw new IllegalArgumentException("width must be nonnegative");
		if (h < 0) throw new IllegalArgumentException("height must be nonnegative");
		width = w;
		height = h;
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		// set to TYPE_INT_ARGB to support transparency
		filename = w + "-by-" + h;
	}

	/**
	 * Initializes a new picture that is a deep copy of <tt>pic</tt>.
	 */
	public Picture(Picture pic) {
		width = pic.width();
		height = pic.height();
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		filename = pic.filename;
		for (int x = 0; x < width(); x++)
			for (int y = 0; y < height(); y++)
				image.setRGB(x, y, pic.get(x, y).getRGB());
	}

	/**
	 * Initializes a picture by reading in a .png, .gif, or .jpg from the given
	 * filename or URL name.
	 */
	public Picture(String filename) {
		BufferedImage tmp;
		this.filename = filename;
		try {
			// try to read from file in working directory
			File file = new File(filename);
			if (file.isFile()) {
				tmp = ImageIO.read(file);
			}

			// now try to read from file in same directory as this .class file
			else {
				URL url = getClass().getResource(filename);
				if (url == null) {
					url = new URL(filename);
				}
				tmp = ImageIO.read(url);
			}
			width = tmp.getWidth(null);
			height = tmp.getHeight(null);

			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++) {
					image.setRGB(x, y, tmp.getRGB(x, y));
				}

		} catch (IOException e) {
			// e.printStackTrace();
			throw new RuntimeException("Could not open file: " + filename);
		}

	}

	/**
	 * Opens a save dialog box when the user selects "Save As" from the menu.
	 */
	public void actionPerformed(ActionEvent e) {
		FileDialog chooser = new FileDialog(frame, "Use a .png or .jpg extension", FileDialog.SAVE);
		chooser.setVisible(true);
		if (chooser.getFile() != null) {
			save(chooser.getDirectory() + File.separator + chooser.getFile());
		}
	}

	/*
	 * because of bug: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4957775
	 * this doesn't work on some images, had to rewrite constructor of this class
	 * to be sure it will be working with RGB data even when the source image will
	 * be 1bit image, or indexded GIF etc...
	 */
	/**
	 * Blur RGB canvas, using kernel matrix.
	 */
	public void blur() {
		BufferedImage imageTmp = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);

		float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f, 0.0625f };
		Kernel kernel = new Kernel(3, 3, data);
		ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		convolve.filter(image, imageTmp);
		image = imageTmp;
	}

	/**
	 * Will clear the image with 0's (in RGB that's black)
	 */
	public void clear() {
		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < this.width; x++)
				image.setRGB(x, y, 0);
	}

	/**
	 * Draws text into the canvas
	 * 
	 * @param x
	 * @param y
	 * @param text
	 */
	public void drawString(int x, int y, String text) {
		this.image.getGraphics().drawString(text, x, y);
	}

	/**
	 * Returns the color of pixel (<em>x</em>, <em>y</em>).
	 */
	public Color get(int x, int y) {
		if (x < 0 || x >= width())
			throw new IndexOutOfBoundsException("x must be between 0 and " + (width() - 1));
		if (y < 0 || y >= height())
			throw new IndexOutOfBoundsException("y must be between 0 and " + (height() - 1));
		return new Color(image.getRGB(x, y));
	}

	/**
	 * As get but no out of bound error checking, you have to know what you do to
	 * use it
	 * 
	 * @param x
	 * @param y
	 * @return Returns color of given pixel
	 */
	public Color getFast(int x, int y) {
		return new Color(image.getRGB(x, y));
	}

	/**
	 * Get buffered image object reference to get raw acccess to the data
	 * 
	 * @return BufferedImage reference
	 */
	public BufferedImage getImage() {
		return this.image;
	}

	/**
	 * Get Jframe reference so another GUI operations can be.
	 * 
	 * @return
	 */
	public JFrame getJFrame() {
		return this.frame;
	}

	/**
	 * Returns a JLabel containing this picture, for embedding in a JPanel, JFrame
	 * or other GUI widget.
	 */
	public JLabel getJLabel() {
		if (image == null) {
			return null;
		} // no image available
		ImageIcon icon = new ImageIcon(image);
		return new JLabel(icon);
	}

	/**
	 * Gets the luminance of specific pixel
	 * 
	 * @param x
	 *          Horizontal coordinate
	 * @param y
	 *          Vertical coordinate
	 * @return Luminance value
	 */
	public double getLum(int x, int y) {
		Color color = new Color(this.image.getRGB(x, y));
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return .299 * r + .587 * g + .114 * b;
	}

	/**
	 * Returns the height of the picture (in pixels).
	 */
	public int height() {
		return height;
	}

	/**
	 * Inverse colors
	 */
	public void inverse() {
		RescaleOp op = new RescaleOp(-1.0f, 255f, null);
		// BufferedImage tmp = op.filter(image, null);
		image = op.filter(image, null);
	}

	/**
	 * Draws red hollow rectangle
	 * 
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 */
	public void rectange(int minX, int maxX, int minY, int maxY) {
		Color color = Color.RED;
		for (int i = minX; i < maxX; i++) {
			image.setRGB(i, minY, color.getRGB());
			image.setRGB(i, maxY, color.getRGB());
		}

		for (int i = minY; i < maxY; i++) {
			image.setRGB(minX, i, color.getRGB());
			image.setRGB(maxX, i, color.getRGB());
		}
	}

	/**
	 * Repaint the content inside the GUI
	 */
	public void repaint() {
		frame.repaint();
	}

	/**
	 * Saves the picture to a file in a standard image format.
	 */
	public void save(File file) {
		this.filename = file.getName();
		if (frame != null) {
			frame.setTitle(filename);
		}
		String suffix = filename.substring(filename.lastIndexOf('.') + 1);
		suffix = suffix.toLowerCase();
		if (suffix.equals("jpg") || suffix.equals("png")) {
			try {
				ImageIO.write(image, suffix, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Error: filename must end in .jpg or .png");
		}
	}

	/**
	 * Saves the picture to a file in a standard image format. The filetype must
	 * be .png or .jpg.
	 */
	public void save(String name) {
		save(new File(name));
	}

	/**
	 * Sets the color of pixel (<em>x</em>, <em>y</em>) to given color.
	 */
	public void set(int x, int y, Color color) {
		if (x < 0 || x >= width())
			throw new IndexOutOfBoundsException("x must be between 0 and " + (width() - 1));
		if (y < 0 || y >= height())
			throw new IndexOutOfBoundsException("y must be between 0 and " + (height() - 1));
		if (color == null) throw new NullPointerException("can't set Color to null");
		image.setRGB(x, y, color.getRGB());
	}

	/**
	 * As set, but no out of bounce and error checking, use on your own risk
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	public void setFast(int x, int y, Color color) {
		image.setRGB(x, y, color.getRGB());
	}

	/**
	 * Displays the picture in a window on the screen.
	 */
	public void show() {

		// create the GUI for viewing the image if needed
		if (frame == null) {
			frame = new JFrame();

			JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
			menuBar.add(menu);
			JMenuItem menuItem1 = new JMenuItem(" Save...   ");
			menuItem1.addActionListener(this);
			menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			menu.add(menuItem1);
			frame.setJMenuBar(menuBar);

			frame.setContentPane(getJLabel());
			// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setTitle(filename);
			frame.setResizable(false);
			frame.pack();
			frame.setVisible(true);
		}

		// draw
		frame.repaint();
	}

	/**
	 * Calling itself with some default variables
	 */
	public void toBW() {
		this.toBW(this, 128, false);
	}

	/**
	 * Get source image, convert it into black & white and store it into this
	 * image.
	 * 
	 * @param source
	 * @param threshold
	 * @param inverse
	 *          if true the threshold will work exactly oposite than when it's
	 *          false
	 */

	public void toBW(Picture source, double threshold, Boolean inverse) {
		int over, under;

		if (inverse) {
			over = Color.BLACK.getRGB();
			under = Color.WHITE.getRGB();
		} else {
			over = Color.WHITE.getRGB();
			under = Color.BLACK.getRGB();
		}

		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				this.image.setRGB(x, y, (source.getLum(x, y) < threshold) ? under : over);
			}
		}
	}

	/**
	 * Return specific pixel in grayscaled color
	 * 
	 * @param x
	 *          Horizontal coordinate
	 * @param y
	 *          Vertical coordinate
	 * @return Grayscale color of given pixel
	 */

	public Color toGray(int x, int y) {
		int v = (int) (Math.round(this.getLum(x, y))); // floor the number down
		Color gray = new Color(v, v, v);
		return gray;
	}

	/**
	 * Returns the width of the picture (in pixels).
	 */
	public int width() {
		return width;
	}

	/**
	 * Compares 2 images
	 */
	public Boolean equals(Picture src) {
		if (this.width() != src.width() || this.height() != src.height()) {
			return false;
		}

		for (int x = 0; x < width(); x++)
			for (int y = 0; y < height(); y++) {
				if (!this.get(x, y).equals(src.get(x, y))) return false;
			}
		return true;
	}
}
