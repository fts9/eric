package uk.coles.ed.eric.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.coles.ed.eric.model.activation_network.ActivationNetwork;
import uk.coles.ed.eric.model.activation_network.PatternList;
import uk.coles.ed.eric.model.session.ChatBotSession;
import uk.coles.ed.eric.utils.XmlReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages Chatterbot functions and is the main input/output interface for the chatterbot
 * 
 * XML manipulation code adapted from Mkyong (source: http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser , retrieved 18/11/2013)
 * @author Ed Coles
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
 *
 */
public class ChatBot {
	public static final String COMMAND_PREFIX = ">>";	//Define the String expression that proceeds command input
	public static final String COMMAND_SEPARATOR = ";;"; //Define the String expression that divides in-response commands
	/*public static final String NEGATIVE = "\\bno(ne|pe|t)\\b"; //Define a default Regex expression for positive user responses
	public static final String POSITIVE = "\\by(e(s|ah)|up)|mhmm\\b"; //Define a default Regex expression for negative user responses*/
	public static final char WILDCARD = '*'; //Define a wildcard that preceeds pre-defined expressions inside configuration files
	//Define a list of 'stop words' (extraneous words to be removed from search input prior to performing a search). Sourced from the MySQL 6.0 documentation
//	public static final String[] STOP_WORDS = {"a", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "ain't", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "aren't", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "c'mon", "c's", "came", "can", "can't", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldn't", "course", "currently", "definitely", "described", "despite", "did", "didn't", "different", "do", "does", "doesn't", "doing", "don't", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadn't", "happens", "hardly", "has", "hasn't", "have", "haven't", "having", "he", "he's", "hello", "help", "hence", "her", "here", "here's", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i'd", "i'll", "i'm", "i've", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isn't", "it", "it'd", "it'll", "it's", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "let's", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldn't", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "t's", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "that's", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "there's", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "they'd", "they'll", "they're", "they've", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasn't", "way", "we", "we'd", "we'll", "we're", "we've", "welcome", "well", "went", "were", "weren't", "what", "what's", "whatever", "when", "whence", "whenever", "where", "where's", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "who's", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "won't", "wonder", "would", "would", "wouldn't", "yes", "yet", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "zero"};

	private int maxApologies;	//	Maximum times the chatbot can fail to find a response before scouring the AN for a more suitable conversation node
	private String negativePhrases;
	private String positivePhrases;
	private List<String> stopWords;
	private List<String> sorryResponses;

	private ActivationNetwork network;
//	public int sorryCount = 0; //Counter for the amount of times the chatterbot has been forced to admit defeat
	private Logger log = LoggerFactory.getLogger(ChatBot.class);
	private static List<ChatterBotOutputListener> listeners = new ArrayList<ChatterBotOutputListener>();	//Create a list of listeners for the ChatterBotOutput event
	private PatternList highPriority[]; //A list of patterns & responses that constitute the high priority responses
	private PatternList lowPriority[];	//A list of patterns & responses that constitute the low priority responses
//	private String configuration;	//Location of the configuration directory
//	private String sorryResponses[] = { "Go-on...", "I'll remember that", "So?", "Huh?", "What does that have to do with the price of eggs?" };
	
	/**
	 * Creates a new instance of ChatterBot automatically loading the configuration from the specified directory
	 * @param 		configurationDirectory		ChatterBot configuration directory
	 */
	/*public ChatBot(String configurationDirectory) {
		log.info("Initialising chatterbot...");
		
		configuration = configurationDirectory;
		
		log.debug("Generating new Activation Network...");
		network = new ActivationNetwork(configurationDirectory + "nodes.xml");
		
		log.debug("Loading high-priority responses...");
		highPriority = loadPatternList(configurationDirectory + "hi.xml", HI_TAG);
		
		log.debug("Loading low-priority responses...");
		lowPriority = loadPatternList(configurationDirectory + "lo.xml", LO_TAG);
	}*/
	
	/**
	 * Creates a new instance of ChatterBot, loading configuration from the root directory
	 */
	public ChatBot() {
//		this("");
	}
	
	/**
	 * Directs the chatterbot to set a greeting conversation as the active node
	 */
	public void greet() {
		if(network != null) {
			network.setActiveNode("greet-1"); //Set the active node to the default greeting network node
			sendOutput(network.getActiveNode().getResponse()); //Send output to listeners
		}
	}
	
	/**
	 * Processes user input through the chatterbot
	 * @param 		input		The user input to process
	 */
	public void process(ChatBotSession session, String input) {
		AnalysisResult result = analyse(session, input.toLowerCase()); //Put the response of 'analysis()' into a temporary String container
		if(result.isResponseFound() || result.isApologiesExhausted()) {
			session.resetApologiesCount();
		}
		
		String[] response = result.getOutput().split(COMMAND_SEPARATOR); //Split 'output' by any commands which it may contain
		if(!response[0].startsWith(COMMAND_PREFIX)) sendOutput(response[0]); //If there is any user output contained in the response, this will be in the first element of the array
		
		parseCommands(result.getOutput()); //Parse any commands that may be contained within 'output'
		
		network.update(session.getAnState()); //Ensure that the new active node on the ActivationNetworkNode has sufficient neighbours or if a new conversation must be found
	}
	
	/**
	 * Analyses the user input at each level of the chatterbot's hierarchy to arrive at a response to the user
	 * @param 		input		The user input
	 * @return					The chatterbot's response to the user input
	 */
	private AnalysisResult analyse(ChatBotSession session, String input) {
		//	Stage 0: Process commands
		String commandOutput = processCommand(input);
		if(commandOutput != null) {
			log.debug("Analysis stopped at stage 0"); //Log status of processing
			return new AnalysisResult(commandOutput, true, false); //Return the output of the command
		}

		//	Stage 1: Process high-priority responses
		for(PatternList p : highPriority) { //Compare 'input' with all the patterns contained in high-priority
			if(p.compare(input)){
				log.debug("Analysis stopped at stage 1");
				return new AnalysisResult(p.getResponse(), true, false);
			}
		}

		//	Stage 2: Process response through the AN
		if(network.process(session.getAnState(), input)) {
			log.debug("Analysis stopped at stage 2");
			return new AnalysisResult(network.getActiveNode().getResponse(), true, false);
		}

		//	Stage 3: Process low-priority responses
		for(PatternList p : lowPriority) {
			if(p.compare(input)) {
				log.debug("Analysis stopped at stage 3");
				return new AnalysisResult(p.getResponse(), true, false);
			}
		}

		//	Stage 4: Dispatch apology
		//TODO Resolve issues with search functions
		boolean apologiesExhausted = session.getApologiesCount() > maxApologies;
		if(apologiesExhausted) { //If the sorryCount exceeds MAX_SORRY, then attempt a search of the Activation Network for keywords in user input as a last-ditch attempt to respond
			String searchResult = network.search(input); //Get the node ID of the best match to keywords in user input

			if(searchResult != null){ //If a match was found...
				network.setActiveNode(searchResult);

				if(network.process(session.getAnState(), input)) { //Attempt to process 'input' through the network again now a new node is active
					String response = network.getActiveNode().getResponse();

					if(response != null) {
						log.debug("Analysis stopped at stage 4");
						return new AnalysisResult(response, true, true); //Return network node's response
					}
				}
			}
			//	FIXME What happens when apologies are exhausted and a response can't be found? Could change the subject
		}

		//If the search failed or sorryCount does not exceed MAX_SORRY, however, log the fact along with some diagnostic data...
		log.debug("----");
		log.debug("Analysis failed! Sorry response dispatched. Details to follow...");
		log.debug("Computer said: \"" + network.getActiveNode().getResponse() + "\"");
		log.debug("The user said: \"" + input + "\"");
		log.debug("----");
		return new AnalysisResult(getSorryResponse(), false, apologiesExhausted); //...and admit defeat and return a sorry response

	}
	
	/**
	 * Extracts commands from the user input and executes them
	 * @param 		input		The user input to extract commands from
	 */
	private void parseCommands(String input) {
		String[] digest = input.toLowerCase().split(COMMAND_SEPARATOR);

		if(digest.length < 1) return; //If there were no commands found in 'input', then exit the sub-routine because there's nothing more to be done
		
		ArrayList<String> commands = new ArrayList<String>();

		for(String command : digest) if(command.startsWith(COMMAND_PREFIX)) commands.add(command); //Run through the split-up input and retrieve commands
		
		String[] output = new String[commands.size()]; //Create storage for each command's output
		
		for(int i = 0; i < output.length; i++) output[i] = processCommand(commands.get(i)); //Process each command

		for(String message : output) sendOutput(message); //Send the received output to output listeners
	}
	
	
	/**
	 * Executes a command and returns the response
	 * @param 		input		The command to evaluate
	 * @return					The output from the chatterbot command
	 */
	private String processCommand(String input) {
		String command = input.toLowerCase();
		if(command.equals(COMMAND_PREFIX + "exit")){ //'>>exit' exits the program
			System.exit(0);
			return null;
		} else if(command.equals(COMMAND_PREFIX + "getactivenode")) { //'>>getactivenode' returns the name of the active node in 'network'
			return "The current active node is " + network.getActiveNode().getNodeId();
		} else if(command.equals(COMMAND_PREFIX + "getnodeneighbours")) { //'>>getnodeneighbours' returns the neighbours of the active node in 'network'
			String[] neighbours = network.getActiveNode().getNeighbours();
			String output = "";
			
			for(String node : neighbours) output += node + ", ";
			
			return "The neighbours of the current active node are " + output;
		} else if(command.startsWith(COMMAND_PREFIX + "say:")) { //'>>say:(text)' has the chatterbot say the contents of 'text'
			String[] digest = command.split(":");
			if(digest.length < 2) return "I'm sorry, I couldn't understand that";
			else return digest[1];
		} else if(command.startsWith(COMMAND_PREFIX + "setactivenode:")) { //'>>setactivenode:(newNode)' sets the active node in network to the node requested, if available
			String[] digest = command.split(":");
			if(digest.length < 2) return "I'm sorry, I couldn't understand that";
			else {
				if(network.setActiveNode(digest[1])) {
					return network.getActiveNode().getResponse();
				} else return "Sorry, I couldn't find the node called " + digest[1];
			}
		} else if(command.equals(COMMAND_PREFIX + "showmap")) { //'>>showmap' brings-up the campus map
			//	TODO Get rid of this
			log.error("showmap command is disabled");
			/*JFrame map = new JFrame("Map");
			try {
				BufferedImage mapGraphic = ImageIO.read(new File("campus_map.png"));
				JLabel mapContainer = new JLabel(new ImageIcon(mapGraphic.getScaledInstance(750, 600, Image.SCALE_SMOOTH)));
				map.add(mapContainer);
				map.setSize(new Dimension(750, 600));
				map.setAlwaysOnTop(true);
				map.setVisible(true);
				
				return "The map should be up now";
			} catch (IOException ex) {
				log.error("Couldn't find 'campus_map.png' in the execution directory!");
				
				return "I'm sorry, I couldn't find the map";
			}*/
		} else if(command.equals(COMMAND_PREFIX + "activenodetostring")) { //'>>activenodetostring' converts the active node in network to String output
			return network.getActiveNode().toString();
		} else if(command.equals(COMMAND_PREFIX + "reset")) { //'>>reset' resets the Activation Network in 'network'
			//	TODO Get rid of this
			log.error("reset command is disabled");
			/*network = new ActivationNetwork(configuration + "nodes.xml");
			greet();
			return "\n\n\n\n";*/
		} else if(command.equals(COMMAND_PREFIX + "newConversation")) { //'>>newconversation' forces network to find a new conversation tree
			log.error("newConversation command is disabled");
			//return network.getNewConversation();
		}
		return null; //If no valid commands were found in 'input', return null to indicate failure
	}
	
	/**
	 * Selects a random sorry response
	 * @return		A randomly selected sorry response
	 */
	private String getSorryResponse() {
		Random randomGenerator = new Random();
		
		/*if(sorryCount > maxApologies) { //If the sorryCount exceeds MAX_SORRY...
			sorryCount = 0; //Reset sorryCount to 0
			return sorryResponses[randomGenerator.nextInt(sorryResponses.length)] + ";;>>newConversation"; //Dispatch sorry response and force the network to start a new conversation
		} else {
			sorryCount++; //Otherwise, increment sorryCount*/
			return sorryResponses.get(randomGenerator.nextInt(sorryResponses.size())); //Dispatch sorry response
//		}
	}
	
	/**
	 * Processes an XML configuration of pattern lists for high and low priority responses
	 * @param 		listLocation		The XML configuration for the high/low priority responses
	 * @param 		tagName				The tag ID to retrieve configuration from
	 * @return							An array of pattern lists containing the high/low priority responses requested
	 */
	private PatternList[] loadPatternList(String listLocation, String tagName) {		
		ArrayList<PatternList> patternList = new ArrayList<PatternList>();
		
		try {
			XmlReader hiXml = new XmlReader(listLocation);
			
			NodeList nList = hiXml.getDocument().getElementsByTagName(tagName);
			
			for(int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				
				if(nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					int maxUses = 0;
					boolean isStrict = false;
					
					if(eElement.getAttribute("patterns_are_strict").equals("1")) isStrict = true;
					
					try {if(!eElement.getAttribute("max_uses").equals("")) maxUses = Integer.parseInt(eElement.getAttribute("max_uses"));}
					catch (NumberFormatException ex) { ex.printStackTrace(); }
					
					ArrayList<String> patterns = new ArrayList<String>(); 
							
					for(int x = 0; x < eElement.getElementsByTagName("pattern").getLength(); x++) patterns.add(eElement.getElementsByTagName("pattern").item(x).getTextContent()); 
					String response = eElement.getElementsByTagName("response").item(0).getTextContent();
					
					patternList.add(new PatternList(patterns.toArray(new String[patterns.size()]), response, maxUses, isStrict));
				}
			}
		} catch (ParserConfigurationException ex) { ex.printStackTrace(); }
		catch (IOException ex) { ex.printStackTrace(); }
		catch (SAXException ex) { ex.printStackTrace(); }
		
		return patternList.toArray(new PatternList[patternList.size()]);
	}
	
	/**
	 * Adds a listener to the list of chatterbot output listeners that will be alerted when the chatterbot generates a new response
	 * @param 		newListener		The listener to add to the chatterbot output listeners
	 */
	public void addChatterBotOutputListener(ChatterBotOutputListener newListener) { listeners.add(newListener); }
	
	/**
	 * Sends output to all registered chatterbot output listeners
	 * @param 		message		Output to send to chatterbot listeners
	 */
	public static void sendOutput(String message) { 
		for(ChatterBotOutputListener listener : listeners) listener.output(message);
	}
}
