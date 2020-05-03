package uk.coles.ed.eric.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Logs output to the console and, if enabled, a GUI console
 * @author Ed Coles
 * @deprecated
 *
 */
public class Logger {
	private static Logger instance = null;
	private static char prefix = '#';
	private static boolean enabled, guiEnabled = false;
	
	private JFrame loggerWindow;
	private JTextArea logs;
	private JScrollPane logsContainer;
	
	protected Logger() { logs = new JTextArea(); }
	
	/**
	 * Gets or instantiates a singleton instance of the Logger class
	 * @param 		newPrefix		The prefix to use for logs from this instance
	 * @param 		isEnabled		Determines if logs received by this class should be logged-out (true) or ignored (false)
	 * @return						The logger instance
	 */
	public static Logger getInstance(char newPrefix, boolean isEnabled) {
		if(instance == null) {
			prefix = newPrefix;
			enabled = isEnabled;
			instance = new Logger();
		}
		
		return instance;
	}
	
	/**
	 * Returns or instantiates a Logger instance using default settings
	 * @return		The logger instance
	 */
	public static Logger getInstance() {
		if(instance == null) instance = new Logger();
		return instance;
	}
	
	/**
	 * Logs out the provided output
	 * @param 		output		The log to output
	 */
	public void log(String output) { 
		if(output != null && enabled) { 
			// Only output if the output is not null and not empty and the Logger is enabled
			if(!output.equals("")) {
				if(!guiEnabled) System.out.println(prefix + " " + output);
				logs.append(prefix + " " + output + "\n");
			}
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Generates and shows the Logger GUI if the GUI is enabled for this instance
	 * @param 	newGuiEnabled	Offers the ability to override the current GUI enabled setting
	 */
	public void showGui(boolean newGuiEnabled) { 
		if(guiEnabled != newGuiEnabled) {
			if(guiEnabled) {
				loggerWindow.setVisible(false);
				
				guiEnabled = false;
			} else {
				loggerWindow = new JFrame("Chatter bot logs");
				logsContainer = new JScrollPane(logs);
				
				logs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
				
				loggerWindow.getContentPane().add(logsContainer);
				loggerWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				loggerWindow.setSize(new Dimension(500, 500));
				loggerWindow.setVisible(true);
				
				guiEnabled = true;
			}
		}
	}
}
