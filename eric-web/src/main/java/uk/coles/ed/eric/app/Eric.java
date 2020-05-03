package uk.coles.ed.eric.app;

import com.google.gson.Gson;
import uk.coles.ed.eric.model.ChatBot;
import uk.coles.ed.eric.model.ChatterBotOutputListener;
import uk.coles.ed.eric.model.activation_network.ActivationNetwork;
import uk.coles.ed.eric.model.activation_network.ActivationNetworkNode;
import uk.coles.ed.eric.model.activation_network.PatternList;
import uk.coles.ed.eric.utils.Logger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Bootstrapping class for the Chatterbot when running locally
 * @author Ed Coles
 * @deprecated
 * 
 * Copyright 2017 Ed Coles
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software 
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class Eric {
	private final static char USER_PROMPT = '>';
	private final static char CPU_PROMPT = '$';
	private final static char LOG_PROMPT = '#';
	
	private static boolean verbose,shiftEnabled = false;
	private static boolean suppressGui = false;
	private static Logger logger;
	private static OutputListener messageListener = new OutputListener();
	private static JFrame mainWindow;
	private static JTextPane outputTextArea = new JTextPane();
	private static JScrollPane outputContainer;
	private static JButton keyboard[][] = new JButton[5][12];
	//private static Style cpuOutput = new Style("CPU_OUTPUT", null);
	//private static Style userOutput = new Style("USER_OUTPUT", null);
	private static char keys[][] = new char[][]{{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '\b'},
	{'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'},
	{'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'},
	{'\\', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', ',', '.', '\\'}, { ' ', '>' }};
	private static JTextArea input = new JTextArea();
	private static ChatBot ericCb;
	
	private static StyledDocument doc;
	private static Style cpuStyle, userStyle;

	public static void main(String[] args) {
		ActivationNetwork activationNetwork = new ActivationNetwork("eric-web/src/main/resources/nodes.xml");
		Gson gson = new Gson();
		System.out.println(gson.toJson(activationNetwork.getNodes()));
	}
	
	/**
	 * Initialises and starts the Chatterbot software
	 * @param 	args	Command line arguments supplied to the program
	 * @deprecated
	 */
	public static void init(String[] args) {
		boolean editorMode = false;		// Determines whether or not the program should boot into network editing mode, or chatterbot mode

		if(args.length > 0) {
			for(String arg : args) {
				if(arg.equals("-v") || arg.equals("--verbose")){
					// Retrieves an instance of the logger tool
					//TODO Replace with SLF4J logging
					logger = Logger.getInstance(LOG_PROMPT, true);
					verbose = true;
					log("Logging enabled!");
				} else if(arg.equals("-n") || arg.equals("--no-gui")) {
					// If the user has asked for the GUI to be suppressed, set the flag for this
					suppressGui = true;
					log("GUI suppressed. Interactive console prompt will be used");
				} else if(arg.equals("--edit")) {
					// Flag network edit mode if the user has asked for this
					editorMode = true;
				}
			}
		}
		
		logger.showGui(true);
		
		if(editorMode) {
			// If editing option is enabled, fire-up the network editor
			System.out.println("Edit option set. Loading suspended. Entering interactive ActivationNetwork editing console...");
			xmlEditor();
		}
		
		ericCb = new ChatBot();	// Create a new chatterbot instance
		messageListener = new OutputListener();		// Create a new chatterbot output listener and register it as an output listener
		ericCb.addChatterBotOutputListener(messageListener);
		
		if(!suppressGui) drawGui();	// If the user hasn't asked for the GUI to be switched off, draw the GUI
		
		log("Initialisation complete. Entering interactive shell...");
		
		ericCb.greet();	// Command the chatterbot to start one of its greeting network nodes
		
		while(true) {
			// Read console line input and pass this to the chatterbot
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.print(USER_PROMPT + " ");	// Output a prompt for user input
			try {
				String input = br.readLine();
//				ericCb.process(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Wrapper method to send logs to the logging output, if enabled
	 * @param	 output		The log text to output
	 * @deprecated
	 */
	private static void log(String output) {
		if(logger != null) logger.log(output);
	}
	
	/**
	 * Creates and draws a GUI for user input/chatterbot output
	 */
	private static void drawGui() {
		log("Drawing the GUI...");
		
		JPanel container = new JPanel(new GridLayout(2, 0));
		JPanel messageArea = new JPanel(new GridLayout(2, 0));
		JPanel keyboardArea = new JPanel(new GridLayout(5, 0));
		JPanel keyboardRows[] = new JPanel[5];
		JPanel inputArea = new JPanel(new FlowLayout());
		JButton send = new JButton("Send");
		outputContainer = new JScrollPane(outputTextArea);
		mainWindow = new JFrame();
		
		// Draw an on-screen keyboard for touch-screen friendly input
		for(int x = 0; x < keys.length; x++) {
			for(int y = 0; y < keys[x].length; y++) {
				if(keys[x][y] == '\b') {
					keyboard[x][y] = new JButton("Backspace");
					keyboard[x][y].setPreferredSize(new Dimension(100, 50));
				} else if(keys[x][y] == '\\') {
					keyboard[x][y] = new JButton("^ Shift");
					keyboard[x][y].setPreferredSize(new Dimension(100, 50));
					keyboard[x][y].addActionListener(new OnKeyboardPressListener(keys[x][y]));
				} else if(keys[x][y] == ' ') {
					keyboard[x][y] = new JButton("Space");
					keyboard[x][y].setPreferredSize(new Dimension(300, 50));
					keyboard[x][y].addActionListener(new OnKeyboardPressListener(keys[x][y]));
				} else {
					keyboard[x][y] = new JButton(String.valueOf(keys[x][y]));
					keyboard[x][y].setPreferredSize(new Dimension(50, 50));
					keyboard[x][y].addActionListener(new OnKeyboardPressListener(keys[x][y]));
				}
			}
		}
		
		// Centre all keyboard buttons
		for(int i = 0; i < keyboardRows.length; i++) keyboardRows[i] = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		// Add each keyboard row to the keyboard container
		for(int x = 0; x < keyboardRows.length; x++) {
			for(int y = 0; y < keys[x].length; y++) {
				keyboardRows[x].add(keyboard[x][y]);
			}
		}
		
		// Configure the user input area
		input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
		input.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
		input.getDocument().putProperty("filterNewlines", Boolean.TRUE);
		inputArea.add(input);
		inputArea.add(send);
		send.setPreferredSize(new Dimension(100, 50));
		send.addActionListener(new SendInputListener());
		
		messageArea.add(outputContainer);
		messageArea.add(inputArea);
		
		// Configure the chatterbot output area
		outputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
		outputTextArea.setEditable(false);
		outputTextArea.setPreferredSize(new Dimension(1000, 500));
		container.add(messageArea);
		
		// Add each keyboard row container
		for(JPanel row : keyboardRows) keyboardArea.add(row);
		container.add(keyboardArea);
		mainWindow.getContentPane().add(container);
		mainWindow.getContentPane().addHierarchyBoundsListener(new WindowSizeChangeListener());
		
		mainWindow.setVisible(true);
		
		input.requestFocus();	// Draw initial UI focus to the user input textbox
		changeShiftValue(true);	// Initially default the user input to a capital
		
		// Set-up the individual style configurations to distinguish between user input and chatterbot output in the output text area
		doc = outputTextArea.getStyledDocument();
		cpuStyle = outputTextArea.addStyle("cpuStyle", null);
		userStyle = outputTextArea.addStyle("userStyle", null);
		StyleConstants.setFontFamily(cpuStyle, Font.MONOSPACED);
		StyleConstants.setFontFamily(userStyle, Font.MONOSPACED);
		StyleConstants.setFontSize(cpuStyle, 18);
		StyleConstants.setFontSize(userStyle, 18);
		StyleConstants.setBold(cpuStyle, true);
		StyleConstants.setBold(userStyle, true);
		StyleConstants.setForeground(cpuStyle, Color.RED);
		StyleConstants.setForeground(userStyle, Color.BLUE);
		
		mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
		input.addKeyListener(new EnterPressedListener());
	}
	
	/**
	 * Processes chatterbot output
	 * @param 		output		Chatterbot output to process
	 * @param 		hide		True if the output should be hidden from console output. False if not
	 */
	private static void say(String output, boolean hide) {
		if(output != null) {
			if(!output.equals("")) {
				// Only output the response to console if the GUI is hidden or verbose mode is enabled and if the output is not hidden
				if((suppressGui || verbose) && !hide) System.out.println(CPU_PROMPT + " " + output);
				try {
					// Add the output to the GUI
					doc.insertString(doc.getLength(), CPU_PROMPT + " " + output + "\n", cpuStyle);
					outputTextArea.select(doc.getLength(), doc.getLength());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Reset the user input text area
		input.setText("");
	}
	
	/**
	 * Sends the user's input to the chatterbot back-end and resets the GUI for the next input
	 * @param 		inputFromGui		If the input originated from the GUI (true) or not (false)
	 */
	protected static void process(boolean inputFromGui) {
		if(inputFromGui && ericCb != null && mainWindow != null) {
			try {
				// Add the user's input to the GUI output
				doc.insertString(doc.getLength(), USER_PROMPT + " " + input.getText() + "\n", userStyle);
				outputTextArea.select(doc.getLength(), doc.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
//			ericCb.process(input.getText().trim());	// Send the user's input to the back-end
			input.setText("");	// Reset the user's input text box to empty
			input.requestFocus();	// Refocus UI on input text box
			changeShiftValue(true);	// Default shift to on for new input
			//input = inputDefault;
			//input.repaint();
		}
	}
	
	/**
	 * Changes the value of the SHIFT key
	 * @param 	newValue	True to set the SHIFT button to down (caps). False to set the SHIFT button to up (lower)
	 */
	private static void changeShiftValue(boolean newValue) {
		// Run through each row and key and convert the value to upper or lower case as required
		for(int x = 0; x < keyboard.length; x++) {
			for(int y = 0; y < keyboard[x].length; y++) {
				if(newValue) {
					if(keyboard[x][y] != null) keyboard[x][y].setText(keyboard[x][y].getText().toUpperCase());
					shiftEnabled = true;	// Set the SHIFT status to DOWN
				} else {
					if(keyboard[x][y] != null) keyboard[x][y].setText(keyboard[x][y].getText().toLowerCase());
					shiftEnabled = false;	// Set the SHIFT status to UP
				}
			}
		}
	}
	
	/**
	 * Initialises and displays the console activation network editor
	 * @deprecated	Not even gonna bother commenting this nonsense up. If it interests you then I'm afraid you're on your own 
	 */
	private static void xmlEditor() {
		ActivationNetwork network;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input;
		
		System.out.print("Load nodes from existing XML file? (Y/N) > ");
		try {
			input = br.readLine();
			
			if(input.toLowerCase().equals("y")) {
				System.out.print("Path to XML file > ");
				input = br.readLine();
				
				network = new ActivationNetwork(input);
			} else {
				network = new ActivationNetwork();
				
				while(true) {
					System.out.println("----");
					System.out.println("Tasks:");
					System.out.println("1\tCreate a new ActivationNetworkNode");
					System.out.println("2\tEdit an existing ActivationNetworkNode");
					System.out.println("3\tDelete an existing ActivationNetworkNode");
					System.out.println("4\tView the specifics of an existing ActivationNetworkNode");
					System.out.println("5\tWrite the current ActivationNetwork to an XML file");
					System.out.println("6\tTest the current ActivationNetwork");
					System.out.println("0\tEXIT");
					System.out.println("----");
					System.out.print("Choice > ");
					input = br.readLine();
					
					try {
						int choice = Integer.parseInt(input);
						
						System.out.println("----");
						
						switch(choice) {
						case 0:
							System.out.print("Really exit? All your changes will be lost if they're not written! (Y/N) > ");
							input = br.readLine();
							
							if(input.toLowerCase().equals("y")) System.exit(0);
							break;
						case 1:
							network.addNode(getNodeFromDescription(br, null));
							break;
						case 2:
							System.out.print("Enter the ID of the node you wish to edit > ");
							ActivationNetworkNode ann = network.getNodeById(br.readLine());
							
							if(ann != null) {
								ann.toString();
								System.out.println("If you wish to leave a value unchanged, simply leave the input space blank.");
								network.setNodeById(ann.getNodeId(), getNodeFromDescription(br, ann));
							}
							
							break;
						case 3:
							System.out.print("Enter the ID of the node you wish to delete > ");
							input = br.readLine();
							
							if(network.getNodeById(input) != null) {
								System.out.print("Are you sure you wish to delete this node? (Y/N) > ");
								if(br.readLine().toLowerCase().equals("y")) {
									network.setNodeById(input, null);
									System.out.println("Node deleted. References to this node may still exist in other nodes, however.");
								}
							} else {
								System.out.println("Could not find node!");
							}
							
							break;
						case 4:
							System.out.print("Enter the ID of the node you wish to view > ");
							ActivationNetworkNode ann1 = network.getNodeById(br.readLine());
							if(ann1 != null) {
								System.out.println(ann1.toString());
							} else {
								System.out.println("Could not find node!");
							}
							
							break;
						case 5:
							String output;
							System.out.print("Enter the file name for the XML output (default: 'output.xml'> ");
							output = br.readLine();
							
							if(output.equals("")) output = "output.xml";
							
							BufferedWriter out = new BufferedWriter(new FileWriter(output, false));
							int level = 0;
							
							out.write("<?xml version=\"1.0\"?>");
							out.newLine();
							out.write("<network>");
							out.newLine();
							ActivationNetworkNode[] anns = network.getNodes();
							
							level++;
							
							for(ActivationNetworkNode ann2 : anns) {
								int startingNode;
								if(ann2.isStartingNode()) startingNode = 1;
								else startingNode = 0;
								
								out.write(getTabs(level++) + "<node is_start_node=\"" + startingNode + "\">");
								out.newLine();
								out.write(getTabs(level) + "<node_id>" + ann2.getNodeId() + "</node_id>");
								out.newLine();
								out.write(getTabs(level) + "<response>" + ann2.getResponse() + "</response>");
								out.newLine();
								/*out.write(getTabs(level) + "<activation>" + ann2.getActivation() + "</activation>");
								out.newLine();*/
								
								PatternList[] patterns = ann2.getPatternList();
								
								for(PatternList pattern : patterns) {
									out.write(getTabs(level) + "<pattern weight=\"" + pattern.getWeight() + "\">" + pattern.getPatterns()[0] + "</pattern>");
									out.newLine();
								}
								
								String neighbourOutput = "";
								for(String neighbour : ann2.getNeighbours()) {
									neighbourOutput += "," + neighbour;
								}
								
								out.write(getTabs(level) + "<neighbours>" + neighbourOutput.replaceFirst(",", "") + "</neighbours>");
								out.newLine();
								
								String enhancementsOutput = "";
								for(String enhancement : ann2.getEnhancements()) {
									enhancementsOutput += "," + enhancement;
								}
								
								out.write(getTabs(level) + "<enhancements>" + enhancementsOutput.replaceFirst(",", "") + "</enhancements>");
								out.newLine();
								
								String inhibitionsOutput = "";
								for(String inhibition : ann2.getInhibitions()) {
									inhibitionsOutput += "," + inhibition;
								}
								
								out.write(getTabs(level--) + "<inhibitions>" + inhibitionsOutput.replaceFirst(",", "") + "</inhibitions>");
								out.newLine();
								
								out.write(getTabs(level) + "</node>");
								out.newLine();
	 						}
							
							out.write(getTabs(--level) + "</network>");
							out.newLine();
							out.close();
							break;
						}
					} catch (NumberFormatException ex) { System.out.println("Input was not a number!"); }
				}
			}
		} catch (IOException ex) { ex.printStackTrace(); }
	}
	
	/**
	 * Configures an activation network node based upon the user's console input
	 * @param 		br			Console input
	 * @param 		base		The network node from which to use default values
	 * @return					The new network node configuration
	 * @deprecated				This one's going the same way as the xmlEditor(). For the same reason it's not being commented
	 */
	private static ActivationNetworkNode getNodeFromDescription(BufferedReader br, ActivationNetworkNode base) {
		try {
			boolean isStartingNode = false;
			int maxUses = 1;
			String nodeId, response;
			String[] rawPatterns, neighbours, enhancements, inhibitions;
			float activation = 0.0f;
			ArrayList<PatternList> resolvedPatterns = new ArrayList<PatternList>();
			PatternList[] patterns;
			
			System.out.print("Node ID (String) > ");
			nodeId = br.readLine();
			if(base != null && nodeId.equals("")) { nodeId =  base.getNodeId(); }
			
			/*System.out.print("Activation (Float) > ");
			try{
				String buffer = br.readLine();
				if(!buffer.equals("")) {
					activation = Float.parseFloat(buffer);
				} else {
					if(base != null) activation = base.getActivation();
				}
			} catch(NumberFormatException ex) { activation = 0.0f; }*/
			
			rawPatterns = arrayInput("Pattern (int weight:String regex) > ");
			for(String pattern : rawPatterns) {
				String[] digest = pattern.split(":");
				
				if(digest.length == 2) {
					try { resolvedPatterns.add(new PatternList(Integer.parseInt(digest[0]), new String[]{ digest[1] })); } catch(NumberFormatException ex) { ex.printStackTrace(); }
				}
			}
			
			if(base != null && rawPatterns.length < 1) patterns = base.getPatternList();
			
			System.out.print("Response (String) > ");
			response = br.readLine();
			if(base != null && response.equals("")) response = base.getResponse();
			
			neighbours = arrayInput("Neighbours (String) > ");
			if(base != null && neighbours.length < 1) neighbours = base.getNeighbours();
			enhancements = arrayInput("Enhancements (String) > ");
			if(base != null && enhancements.length < 1) enhancements = base.getEnhancements();
			inhibitions = arrayInput("Inhibitions (String) > ");
			if(base != null && inhibitions.length < 1) inhibitions = base.getInhibitions();
			
			System.out.print("Is a starting node? (Y/N) > ");
			String buffer = br.readLine();
			if(!buffer.equals("")) {
				if(buffer.toLowerCase().equals("y")) isStartingNode = true;
			} else {
				if(base != null) isStartingNode = base.isStartingNode();
			}
			
			System.out.print("Maximum uses (int, 1 default) > ");
			buffer = br.readLine();
			if(!buffer.equals("")) {
				try { maxUses = Integer.parseInt(buffer); } catch(NumberFormatException ex) { maxUses = 1; }
			} else {
				if(base != null) maxUses = base.getMaxUses();
			}
			
			return new ActivationNetworkNode(nodeId, resolvedPatterns.toArray(new PatternList[resolvedPatterns.size()]), response, neighbours, enhancements, inhibitions, isStartingNode, maxUses);
		} catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Generates a String with the requested number of tab spaces
	 * @param 		number		The number of tabs required
	 * @return					A String with the requested number of tabs
	 */
	private static String getTabs(int number) {
		String output = "";
		for(int i = 0; i < number; i++) output += "\t";
		return output;
	}
	
	/**
	 * Allows the user to enter a String array through the console
	 * @param 		prompt		The prompt to show to the user for each line of input
	 * @return					The String array entered by the user
	 */
	private static String[] arrayInput(String prompt) {
		System.out.println("Array input started. Enter '>>EXIT' to terminate.");
		
		String input = "";
		ArrayList<String> output = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while(!input.equals(">>EXIT")) {
			if(!input.equals("")) output.add(input);
			System.out.print(prompt);
			try {input = br.readLine();} catch(IOException ex) { ex.printStackTrace(); }
		}
		
		return output.toArray(new String[output.size()]);
	}
	
	/**
	 * A wrapper method for the say function
	 * @param 		output		The String to output
	 */
	protected static void say(String output) {
		say(output, false); 
	}
	
	/**
	 * Governs the GUI's behaviour when the window is resized 
	 */
	protected static void onResize() {
		input.setPreferredSize(new Dimension(((int)mainWindow.getSize().getWidth() - 170),50));
		//mainWindow.update(mainWindow.getGraphics());
		//JOptionPane.showConfirmDialog(mainWindow, "Resized!");
	}
	
	/**
	 * Processes on-screen keyboard input from the GUI
	 * @param 		letter		The letter the user entered
	 */
	protected static void keyboardPress(char letter) {
		// Don't ask because I don't know
		if(letter == '\\') {
			changeShiftValue(!shiftEnabled);
		}
		
		if(!shiftEnabled) input.append(String.valueOf(letter).toLowerCase());
		else if(letter != '\\'){
			input.append(String.valueOf(letter));
			changeShiftValue(false);
		}
	}
}

/**
 * Implements ChatterBotOutputListener to allow the application to listen to chatterbot output
 * @author Ed Coles
 *
 */
class OutputListener implements ChatterBotOutputListener {
	public OutputListener() {}
	public void output(String output) { Eric.say(output); }
	
}

/**
 * Listens for resize events on the main GUI and passes the signal onto the main class
 * @author Ed Coles
 *
 */
class WindowSizeChangeListener implements HierarchyBoundsListener {
	@Override
	public void ancestorResized(HierarchyEvent arg0) {
		Eric.onResize();
	}

	@Override
	public void ancestorMoved(HierarchyEvent e) {
		// Do nothing
	}
	
}

/**
 * Listens for keyboard input on the main GUI and passes it on to the main class
 * @author Ed Coles
 *
 */
class OnKeyboardPressListener implements ActionListener {
	private char letter;
	
	public OnKeyboardPressListener(char associatedLetter) {
		letter = associatedLetter;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Eric.keyboardPress(letter);
	}
	
}

/**
 * Listens for events that trigger the GUI to send the user's input to the back-end
 * @author Ed Coles
 *
 */
class SendInputListener implements ActionListener {
	public SendInputListener() {}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Eric.process(true);
	}
	
}

/**
 * Listens for the user pressing 'Enter' to pass it to the back-end
 * @author Ed Coles
 *
 */
class EnterPressedListener implements KeyListener {
	public EnterPressedListener() {}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ENTER) Eric.process(true);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// Do nothing
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// Do nothing
		
	}
}
