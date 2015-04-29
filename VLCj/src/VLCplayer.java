// Setup Tutorials: http://www.capricasoftware.co.uk/projects/vlcj/index.html
// API: http://caprica.github.io/vlcj/javadoc/2.1.0/
// https://github.com/caprica/vlcj/blob/master/src/test/java/uk/co/caprica/vlcj/test/screen/ScreenRecorder.java
// NOTE: On Mac OS X, must use JRE version 6
import java.io.File;
import java.util.Scanner;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class VLCplayer {
	
    private final EmbeddedMediaPlayer player;
    private static String source = "/Users/nicholasdillon/Documents/UNC/Research/participant16.wmv";

	public static void main (final String[] args) {
		
		// Overwrite source with demo video
		String baseName = VLCplayer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		baseName = baseName.replace("%20", " ");		// Replace %20's with spaces
		baseName = baseName.substring(0, baseName.length() - 1 - 4);		// Remove /bin from path
		source = baseName + "/../screencaptures/screencap-vid.mp4";
		
		File video = new File(source);
		if(!video.exists()) {
			System.err.println("Specified video does not exist... aborting");
			System.exit(0);
		}
		
	    // Instantiates VLC
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "/Applications/VLC.app/Contents/MacOS/lib/");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
        // SCREEN VIEWING UILITIES
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
				new VLCplayer();
            }
        });
    }
	
	// Constant loop waiting for commands from standard in
	// Seek to time point and wait for play command 
	
	// Creates swing player with embedded VLC
	VLCplayer() {
	    Scanner keyboard = new Scanner(System.in);
	    JFrame frame = new JFrame("VLC Java Player");
	    EmbeddedMediaPlayerComponent component = new EmbeddedMediaPlayerComponent();
	    
	    player = component.getMediaPlayer();
	    
	    frame.setContentPane(component);
	    frame.setLocation(50, 50);
	    frame.setSize(850, 650);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);

	    player.playMedia(source);
	    while (!player.isPlaying()) { /* Wait until video is loaded */ };
	    player.pause();
	    int length = (int) player.getLength();
	    int seconds = length/1000;
	    int minutes = seconds/60;
	    seconds %= 60;
	    
	    System.out.println("Video length: " + minutes + ":" + seconds);
	    
        while (true) {
        	String input = null;
        	System.out.println("Current time (seconds): " + player.getTime()/1000);
        	System.out.print("Enter a player command: ");
        	if(keyboard.hasNext()) input = keyboard.nextLine();
        	if(input == null || input.equalsIgnoreCase("quit")) {
        		System.exit(0);
        	}
        	if(input.equalsIgnoreCase("play")) {
        		System.out.print("Playing... ");
        		player.setPause(false);
        		continue;
        	}
        	if(input.equalsIgnoreCase("pause")) {
        		System.out.print("Pausing... ");
        		player.setPause(true);
        		continue;
        	}
        	if(input.equalsIgnoreCase("reset")) {
        		System.out.print("Pausing... ");
	        	player.setTime(0);
        		player.setPause(true);
        		continue;
        	}
        	if(input.equalsIgnoreCase("seek")) {
            	System.out.print("Enter a time: ");
            	if(keyboard.hasNext()) input = keyboard.nextLine();
        		
	        	String[] elements = input.split(":");
	        	if(elements.length != 2 || elements[1].length() != 2) {
	        		System.out.println("Invalid Time Entered");
	        		continue;
	        	}
	        	
	        	int min = Integer.parseInt(elements[0]);
	        	int sec = Integer.parseInt(elements[1]);
	        	
	        	if(min < 0 || sec >= 60) {
	        		System.out.println("Invalid Time Format Entered");
	        		continue;
	        	}
	        	
	        	int millisec = ((min*60)+sec)*1000;
	    		System.out.print("Seeking... ");
	        	player.setTime(millisec);
	        	player.setPause(false);
	        	continue;
        	}
        	if(input.equalsIgnoreCase("quit")) {
        		System.exit(0);
        	}
        	System.out.println("Invalid Command Entered");
        }
	}
}