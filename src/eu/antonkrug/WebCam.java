package eu.antonkrug;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.github.sarxos.webcam.Webcam;

import edu.princeton.cs.introcs.Stopwatch;

/**
 * Streams images from webcam and detects blobs in it
 * 
 * @author Anton Krug
 * 
 */

public final class WebCam {

	static final Boolean	DETECT_BALL			= false;
	static final Boolean	COLORS_BY_SIZE	= true;  //if not by size, then they will be random
	

	public static void main(String[] args) throws IOException {
		MachineVision app;
		WeightedQuickUnion union;
		Stopwatch runTime;
		int frameCount = 1;
		double timeTaken = 0;
		double camTimeTaken = 0;

		// automatically open if webcam is closed
		Webcam.setAutoOpenMode(true);

		app = new MachineVision();

		// different thersholds if detecting ball
		if (DETECT_BALL) {
			app.setThreshold(128);
		} else {
			app.setThreshold(175);
		}

		// get first image to setup GUI window
		app.loadImage(Webcam.getDefault().getImage(), true);

		while (true) {
			runTime = new Stopwatch();

			BufferedImage web = Webcam.getDefault().getImage();

			// will not crash on null pointer when the window is closed
			if (web != null) {
				app.setImage(web);
				camTimeTaken += runTime.elapsedTime();

				// app.blurInputImage();

				// will inverse image when detecting ball
				app.toBWorigin(DETECT_BALL);

				union = new WeightedQuickUnion(app.getWidth() * app.getHeight());
				app.setUnion(union);

				union.populateTree(app.getImage(), app.getThreshold(), false);
				union.flatten(app.getImage());

				app.setGroupMinThreshold(40);
				app.disableSmallGroups();

				if (COLORS_BY_SIZE) {
					union.generateColorsBySize(app.getGroupSmallest(), app.getGroupBiggest());
				} else {
					union.generateColorsRandom();					
				}

				app.colorizeGroups(app.getPreview(), true, DETECT_BALL);

				app.getPreview().repaint();

				timeTaken += runTime.elapsedTime();

				// display stats every 10frames
				if ((frameCount % 10) == 0) {
					System.out.printf("FPS: %3d, timeSpendWithCam=%4d%% %s\n", (int) (10 / timeTaken),
							(int) ((100f * (camTimeTaken)) / timeTaken), app);
					timeTaken = 0;
					camTimeTaken = 0;

				}

				frameCount++;
			} else {
				System.exit(0);
			}
		}

	}
}
