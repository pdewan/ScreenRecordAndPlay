import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.nio.file.StandardCopyOption.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class Record20secs {

	private static final long FRAME_RATE = 5;
	private static Dimension screenBounds;
	private static Rectangle captureSize;
	private static IMediaWriter writer;
	private static long startTime;

	public static void main(String[] args) throws InterruptedException, IOException {

		String baseName = Record20secs.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		baseName = baseName.replace("%20", " ");		// Replace %20's with spaces
		baseName = baseName.substring(0, baseName.length() - 1 - 4);		// Remove /bin from path
		String tempFilename = baseName + "/../screencaptures/screencap-vid-temp.mp4";
		String outputFilename = baseName + "/../screencaptures/screencap-vid.mp4";
		int captureLength = 20;
		
		File screencaps = new File(baseName + "/../screencaptures");
		if(!screencaps.exists()) {
			System.err.println("screencaptures directory does not exist... creating...");
			screencaps.mkdir();
		}

		while(true){
			try {
				Scanner scanner = new Scanner(new File(baseName + "/../screencaptures/frame.txt"));
				int [] fileparams = new int [4];
				int i = 0;
				while(scanner.hasNextInt() && i < 4){
				   fileparams[i++] = scanner.nextInt();
				}
				scanner.close();
				System.out.println("Getting coordinates from file: " + baseName + "/captures/frame.txt");
				screenBounds = new Dimension(fileparams[2], fileparams[3]);
				captureSize = new Rectangle(new Point(fileparams[0], fileparams[1]), screenBounds);
			} catch (Exception e) {
				System.out.println("No coordinates provided... recording entire screen");
				screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
				captureSize = new Rectangle(screenBounds);
			}
			File tempFile = new File(tempFilename);
			File outputFile = new File(outputFilename);
			// remove the ..
//			record(tempFile.getCanonicalPath(), captureLength);
			tempFilename = tempFile.getCanonicalPath();
			outputFilename = outputFile.getCanonicalPath();
			record(tempFilename, captureLength);

			Files.move(Paths.get(tempFilename), Paths.get(outputFilename), REPLACE_EXISTING);
			System.out.println("Video saved to: " + outputFilename);
		}
	}

	public static void record(String outputFilename, int captureLength)
			throws InterruptedException {
//		outputFilename = "screencap-vid-temp.mp4";
		// let's make a IMediaWriter to write the file.
		writer = ToolFactory.makeWriter(outputFilename);

		// We tell it we're going to add one video stream, with id 0,
		// at position 0, and that it will have a fixed frame rate of
		// FRAME_RATE.
		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, screenBounds.width, screenBounds.height);

		ArrayList<ScreenTimePair> screenshots = new ArrayList<ScreenTimePair>();

		startTime = System.nanoTime();

		for (int index = 0; index < captureLength * FRAME_RATE; index++) {
			long check0 = System.currentTimeMillis();
			screenshots.add(new ScreenTimePair(getDesktopScreenshot(), System
					.nanoTime()));
			long check3 = System.currentTimeMillis();
			long sleepDuration = (1000 / FRAME_RATE - (check3 - check0));
			if (sleepDuration > 0)
				Thread.sleep(sleepDuration);
		}

		for (int index = 0; index < captureLength * FRAME_RATE; index++) {
			ScreenTimePair screenshot = screenshots.get(index);
			BufferedImage bgrScreen = convertToType(screenshot.getScreen(),
					BufferedImage.TYPE_3BYTE_BGR); // ~10ms
			writer.encodeVideo(0, bgrScreen, screenshot.getTime() - startTime,
					TimeUnit.NANOSECONDS); // ~100ms
		}

		// tell the writer to close and write the trailer if needed
		writer.close();
	}

	public static BufferedImage convertToType(BufferedImage sourceImage,
			int targetType) {

		BufferedImage image;

		// if the source image is already the target type, return the source
		// image
		if (sourceImage.getType() == targetType) {
			image = sourceImage;
		}
		// otherwise create a new image of the target type and draw the new
		// image
		else {
			image = new BufferedImage(sourceImage.getWidth(),
					sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}

		return image;
	}

	private static BufferedImage getDesktopScreenshot() {
		try {
			Robot robot = new Robot();
			return robot.createScreenCapture(captureSize);
		} catch (AWTException e) {
			e.printStackTrace();
			return null;
		}
	}
}

class ScreenTimePair {
	private BufferedImage screen;
	private long time;

	ScreenTimePair(BufferedImage screen, long time) {
		this.screen = screen;
		this.time = time;
	}

	public BufferedImage getScreen() {
		return screen;
	}

	public long getTime() {
		return time;
	}
}