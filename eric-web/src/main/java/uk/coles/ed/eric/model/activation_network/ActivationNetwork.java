package uk.coles.ed.eric.model.activation_network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.coles.ed.eric.model.ChatBot;
import uk.coles.ed.eric.model.session.AnnActivation;
import uk.coles.ed.eric.utils.NlpUtilities;
import uk.coles.ed.eric.utils.XmlReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a network of Activation Nodes which manages the state of the current conversation
 * @author Ed Coles
 *
 */
public class ActivationNetwork {
	//Define dummy node as a place holder to avoid a NullReferenceException when the active node is unavailable
	public final ActivationNetworkNode DUMMY_NODE = new ActivationNetworkNode("dummyNode", new PatternList[]{ new PatternList(0, new String[]{ ChatBot.COMMAND_PREFIX + "dummyNode" }) }, "This is a dummy node.", new String[] { "dummyNode" }, new String[] { "dummyNode" }, new String[] { "dummyNode" }, false, -1);
	
	private ArrayList<ActivationNetworkNode> network = new ArrayList<ActivationNetworkNode>(); //Array list to hold network nodes
	private ActivationNetworkNode activeNode; //A place holder for the current active node
	private Logger log = LoggerFactory.getLogger(ActivationNetwork.class); //Grab an instance of the logger

	private NlpUtilities nlpUtilities = new NlpUtilities();
	
	/**
	 * Initialises the activation network reading XML configuration information from path provided
	 * @param 		pathToConfig	Location of Activation Network XML configuration
	 */
	public ActivationNetwork(String pathToConfig) {		
		try {
			XmlReader nodesInput = new XmlReader(pathToConfig); //Load ActivationNetwork configuration XML
			NodeList nList = nodesInput.getDocument().getElementsByTagName("node"); //Get network node tags from the XML document
			
			log.info("Creating activation network nodes...");
			for(int i = 0; i < nList.getLength(); i++) { //Go through each 'node' XML tag and create ActivationNetworkNode objects from them
				Node nNode = nList.item(i); //Get the 'node' tag 'i'
				
				if(nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode; //Load node details
					
					//Create some place holder variables for the ActivationNetworkNode's properties extracted from the XML
					boolean isStartNode = false;
					float activation = 0.0f;
					int maxUses = 1;
					ArrayList<PatternList> patterns = new ArrayList<PatternList>();
					String nodeId, response, neighbours, enhancements, inhibitions;
					
					if(eElement.getAttribute("is_start_node").equals("1")) isStartNode = true;
					
					try {
						String stringMaxUses = eElement.getAttribute("max_uses");
						if(!stringMaxUses.equals("")) maxUses = Integer.parseInt(stringMaxUses);
					} catch(NumberFormatException ex) { ex.printStackTrace(); }
					
					nodeId =  eElement.getElementsByTagName("node_id").item(0).getTextContent();
					
					
					try {
						activation = Float.parseFloat(eElement.getElementsByTagName("activation").item(0).getTextContent());
					} catch(NumberFormatException ex) { ex.printStackTrace(); }
					
					
					try{
						for(int x = 0; x < eElement.getElementsByTagName("pattern").getLength(); x++) {
							String unprocessedPatterns = eElement.getElementsByTagName("pattern").item(x).getTextContent();

							int weight = Integer.parseInt(eElement.getElementsByTagName("pattern").item(x).getAttributes().getNamedItem("weight").getTextContent());
							
							patterns.add(new PatternList(weight, new String[] { unprocessedPatterns }));
						}
					} catch (NumberFormatException ex) { ex.printStackTrace(); }
					
					response = eElement.getElementsByTagName("response").item(0).getTextContent();
					enhancements = eElement.getElementsByTagName("enhancements").item(0).getTextContent();
					inhibitions = eElement.getElementsByTagName("inhibitions").item(0).getTextContent();
					neighbours = eElement.getElementsByTagName("neighbours").item(0).getTextContent();
					
					//Add a new ActivationNetworkNode to the ActivationNetwork based upon the properties extracted from the XML
					network.add(new ActivationNetworkNode(nodeId, patterns.toArray(new PatternList[patterns.size()]), response, XmlReader.parseList(neighbours), XmlReader.parseList(enhancements), XmlReader.parseList(inhibitions), isStartNode, maxUses));
				}
			}
			
			log.info(String.valueOf(network.size()) + " nodes loaded!"); //Report the number of successfully loaded network nodes
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		activeNode = DUMMY_NODE; //Place DUMMY_NODE as the current active node as the network has only just been initialised
	}

	/**
	 * As well as being an IOException generator, returns an ActivationNetwork instance with default values 
	 */
	public ActivationNetwork() {
		this("");
	}
	
	/**
	 * Adds a new Activation Network Node to the activation network
	 * @param 		newNode		The new network node
	 */
	public void addNode(ActivationNetworkNode newNode) { network.add(newNode); }
	
	/**
	 * Returns the network's currently active node
	 * @return		The current Active Node of the network
	 */
	public ActivationNetworkNode getActiveNode() { return activeNode; }
	
	/**
	 * Processes the user input provided through the currently active node's neighbours and reports back on whether or not a match was successfully found among them
	 * @param 		input		The user's input
	 * @return					True if a match was found based on the input provided amongst the active node's neighbours. False if not or if the active node is not currently initialised
	 */
	public boolean process(Map<String, AnnActivation> anState, String input) {
		if(activeNode == null || activeNode.equals(DUMMY_NODE)) return false; //Automatically return false if there is no active node
		
		ActivationNetworkNode topActive = null; //Create placeholder while the new 'activeNode' value is determined
		boolean matchFound = false; //Signals whether or not a match was successfully found external to the 'for' loops
		
		String[] neighbours = activeNode.getNeighbours(); //Get the neighbours of the 'activeNode'
		
		for(String ann : neighbours) {
			if(getNodeById(ann).preActivate(anState.get(ann), input)) {
				matchFound = true; //Preactivate neighbour nodes based upon the user input. Flag if the preActivate function returns true, indicating a successful match between user input and the node's patterns
			}
		}
		
		//Run through the neighbour nodes now they've been preactivated
		for(String ann : neighbours) {
			if(topActive != null) { //If there's already an 'activeNode' candidate...
				//...then compare their activation values. If the current node's activation is greater than the current 'topActive's activation, then replace the 'topActive' with the current one...
				if(anState.get(ann).getActivation() > anState.get(topActive.getNodeId()).getActivation()) {
					topActive = getNodeById(ann);
				}
			} else {
				topActive = getNodeById(ann); //...If there isn't a candidate in the placeholder however, put one there
			}
		}
		
		if(topActive != null && matchFound) {
			activeNode = topActive.activate(anState); //If there's a topActive candidate AND a match found, then update and activate the 'activeNode'
		}
		if(topActive == null) {
			activeNode = DUMMY_NODE; //Otherwise, if the 'topActive' candidate remains null replace the 'activeNode' with the 'DUMMY_NODE'
		}
		
		return matchFound; //Return whether or not there was a successful match
	}
	
	/**
	 * Searches the network for a node whose regex most closely matches that provided in the search and returns the node ID of the closest match, if available
	 * @param 		search		The user input to conduct the search with
	 * @return					The closest matching network node to the search terms. Null if no best match was found
	 */
	public String search(String search) {
		String searchTerms[] = search.toLowerCase().split("\\s+"); //Split the search terms along whitespace characters
		
		for(int i = 0; i < searchTerms.length; i++) searchTerms[i] = searchTerms[i].replaceAll("[^\\w]", ""); //Remove any punctuation marks
		
		ArrayList<String> sanitisedSearchTerms = new ArrayList<String>();
		
		//Process and remove any search terms which match the 'STOP_WORDS' provided by ChatterBot
		for(String word : searchTerms) {
			if(nlpUtilities.isStopWord(word)) {
				sanitisedSearchTerms.add(word); //If the search term at 'i' was not a 'STOP_WORD', then include this in the list of sanitised search terms
			}
		}
		searchTerms = sanitisedSearchTerms.toArray(new String[sanitisedSearchTerms.size()]); //Convert the sanitised search terms to a regular array
		
		if(searchTerms.length < 1) return null; //If there are no search terms left over from the sanitising process, automatically return null
		
		Pattern needles[] = new Pattern[searchTerms.length];
		for(int i = 0; i < searchTerms.length; i++) needles[i] = Pattern.compile(searchTerms[i]); //Convert the 'searchTerms' into matchable regex expressions
		
		int matchFrequency[] = new int[network.size()]; //Create an integer array to store the match frequencies of each needle against the nodes in the ActivationNetwork

		//Attempt to match each search term against node responses in the ActivationNetwork
		//	TODO Review this algorithm for finding candidate ANN
		for(Pattern term : needles) {
			for(int i = 0; i < network.size(); i++) {
				Matcher hayStack = term.matcher(network.get(i).getResponse());
				if(hayStack.find()) matchFrequency[i]++; //If there was a successful match between the 'needle' and the 'haystack' then increment the 'matchFrequency' of the node at 'i'
			}
		}
		
		int winner = -1; //Stores the match frequency of the winning node
		String winnerNodeId = null; //Stores the 'nodeId' of the winning node
		
		//Go through the 'matchFrequency' array to find the most popular result
		for(int i = 0; i < matchFrequency.length; i++) {
			if(matchFrequency[i] > winner && matchFrequency[i] > 0) {
				//If the 'matchFrequency' at 'i' is larger than the current winning node, replace the current winner with the new winner
				winner = matchFrequency[i];
				winnerNodeId = network.get(i).getNodeId();
			}
		}

		return winnerNodeId;
	}
	
	/**
	 * Updates the network's active node with the ID provided
	 * @param 		nodeId		The ID of the new active node
	 * @return					True on active node update success. False on failure (e.g. the node ID provided could not be found)
	 */
	public boolean setActiveNode(String nodeId) {
		ActivationNetworkNode newActiveNode = getNodeById(nodeId); //Attempt to locate the requested node
		
		if( newActiveNode != null) { //If successful...
			activeNode = newActiveNode; //...update the 'activeNode' to the new node and return true
			log.debug("Active node set to " + nodeId);
			return true;
		} else { //Otherwise return false
			log.error("Failed to set active node to " + nodeId);
			return false;
		}
	}
	
	/**
	 * Retrieves an ActivationNetworkNode object by looking-up its ID
	 * @param 		nodeId		ID of the network node to retrieve
	 * @return					The activation network node requested. On failure, the dummy network node will be returned
	 */
	public ActivationNetworkNode getNodeById(String nodeId) {
		for(ActivationNetworkNode ann : network) if(ann.getNodeId().equals(nodeId)) return ann; //Search the network and return the node if it exists
		
		log.error("Could not find a node with the ID: " + nodeId);
		return DUMMY_NODE; //Otherwise, return the 'DUMMY_NODE'
	}
	
	/**
	 * Updates a network node by looking up its ID value 
	 * @param 		nodeId		The node ID to look-up
	 * @param 		newNode		The new value of the node
	 */
	public void setNodeById(String nodeId, ActivationNetworkNode newNode) {
		if(getNodeById(nodeId) != null) {
			for(int i = 0; i < network.size(); i++) {
				if(network.get(i).getNodeId().equals(nodeId)) network.set(i, newNode);
			}
		}
	}
	
	public ActivationNetworkNode[] getNodes() {
		return network.toArray(new ActivationNetworkNode[network.size()]);
	}

	/**
	 * Checks to see if the 'activeNode' has any neighbours. If not, it forces the network to find a new conversation
	 */
	public void update(Map<String, AnnActivation> anState) {
		if(activeNode.getNeighbours().length < 1) {
			ChatBot.sendOutput(getNewConversation(anState));
		}
	}
	
	/**
	 * Randomly selects a new start of conversation node, sets it as the active node, and then returns the new active node's response text
	 * @return		The response for the start of the new conversation
	 */
	public String getNewConversation(Map<String, AnnActivation> anState) {
		ArrayList<ActivationNetworkNode> starters = new ArrayList<ActivationNetworkNode>();
		
		for(ActivationNetworkNode ann : network) {
			if(ann.isStartingNode() && !ann.isMaxedOut(anState.get(ann.getNodeId()))) {
				starters.add(ann);
			}
		}
		
		if(starters.size() < 1) {
			log.error("ERIC is out of usable ActivationNetworkNodes! ERIC whited out!");
			activeNode = DUMMY_NODE;	//TODO Not really good enough response to being out of conversations
			return null;
		} else {
			Random random = new Random();
			
			activeNode = starters.get(random.nextInt(starters.size()));
			
			return activeNode.getResponse();
		}
	}
}
