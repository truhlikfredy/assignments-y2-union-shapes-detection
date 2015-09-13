# Union shapes detection
Second year college project to process images and webcam stream. Original assigment required students to work with static images. But I wanted to do bit of extra and this project handles video stream from webcam and there is simple condition to detect circle/ball shapes.

Author: Anton Krug. License: GPL

Webcam part of this project uses https://github.com/sarxos/webcam-capture code to access the camera.

# Downloads

# Features
* WebCam and MachineVission classes containing main. Both behave differently.
 * WebCam can detect shapes like circle/balls. (when ball detection is enabled, the image is inverted)
 * MachineVission contains dialog based GUI and allows change thersholds, but work only with static images.
* Ready to import Eclipse project (tested on Eclipse Kepler and Mars).
* Improved princeton Picture class.
 * Removed, added and modified methods.
 * Get/Set pixel is faster
 * Constructor accepts BufferedImage as well
 * More safer handling for less common inputs with GIF and BMP (8-bit indexed files, black & white).
 * Post-proccessing like blur,invert added.
 * Luminance calculations done faster than original princeton.
* Console outputs verbose information (verbosity can be adjusted in source code)
* Colorizes union groups by size or random color.
* Few simple JUint tests.
* Tested on Windows/Linux.

# Notes

Lot of behaviour is based on how their static defined variables are setup.
 Of cource it can be setup to misbehave or be slower (when verbose + profiling is
e nabled etc...).

MachineVision has option to fast start when its preloades some fixed image, 
when fast start is disabled it will show dialogs prompting for these options.


The included 3rd party webcam library may output lot of warnings, but still can work nicely.
