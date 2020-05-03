package uk.coles.ed.eric.model.activation_network;

import uk.coles.ed.eric.model.session.AnnActivation;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * An individual conversation node inside the activation network
 * @author Ed Coles
 *
 */
public class ActivationNetworkNode {
	private boolean startingNode = false; //If true, identifies the node as the start of a conversation tree
	private int maxUses; //Set the maximum uses for the node
	private String nodeId, response;	//The unique name given to the node/The response that is returned when the node is activated
	
	private String[] enhancements; //Nodes that will be enhanced when activate() is called
	private String[] inhibitions; //Nodes that will be inhibited when activate() is called
	private String[] neighbours; //A list of conversation nodes that are directly next to the node in the network and where conversation will be directed when activate() is called
	
	private PatternList[] patterns; //String patterns in user input that will be searched for when preActivate() is called
	
	/**
	 * Instantiates a new instance of an ActivationNetworkNode
	 * @param 		nodeId			The ID of the activation network node
	 * @param 		patterns			The user input patterns list
	 * @param 		response			The response text given by this node
	 * @param 		neighbours		A list of the node's immediate neighbour IDs
	 * @param 		enhancements		A list of the nodes that will have their activation enhanced when this node is activated
	 * @param 		inhibitions		A list of the nodes that will have their activation inhibited when this node is activated
	 * @param 		startingNode		True if the node is the start of a conversation tree. False if not or if the node should only be activated by other nodes
	 * @param 		maxUses			The default maximum uses value
	 */
	public ActivationNetworkNode(String nodeId, PatternList[] patterns, String response, String[] neighbours, String[] enhancements, String[] inhibitions, boolean startingNode, int maxUses) {
		//Initialise variables with new values
		this.nodeId = nodeId;
		this.patterns = patterns;
		this.neighbours = neighbours;
		this.enhancements = enhancements;
		this.inhibitions = inhibitions;
		this.response = response;
		this.startingNode = startingNode;
		this.maxUses = maxUses;
	}
	
	/**
	 * Parses user input for the string patterns in the 'patterns' variable and then returns a boolean depending on whether any of the node's patterns were 
	 * found in the input or not. The activation of the node is adjusted according to this result.
	 * @param 		input		The input upon which to pre-activate the node
	 * @return					True if the input matched one or more patterns. False if not
	 */
	public boolean preActivate(AnnActivation annActivation, String input) {
		boolean matchFound = false; //We only need to know if a pattern has been successfully matched or not, not which one, so declare this outside the 'for' loop...
		
		for(PatternList p : patterns) {
			//Search through each of the patterns found in 'p'. If there are any matches found, or the WILDCARD, then set the appropriate booleans to indicate a match was found
			if(p.compare(input)) {
				matchFound = true;

				//If a match was found in this case, adjust the node's activation to the magnitude specified by 'weight'
				annActivation.preActivate((float)p.getWeight() / 10);
			}
		}
		
		return matchFound;
	}
	
	/**
	 * Activate the network node and return itself to update the requesting instance
	 * @return		The network node in its activated state
	 */
	public ActivationNetworkNode activate(Map<String, AnnActivation> anState) {
		for(String enhancedNodeId : enhancements) {
			anState.get(enhancedNodeId).enhance();	//	Activate enhancements
		}
		for(String inhibitedNodeId : inhibitions) {
			anState.get(inhibitedNodeId).inhibit();	//	Activate inhibitions
		}
		
		anState.get(nodeId).incrementActivationCount();	//	Note that the node has now been activated once
		
		return this;
	}

	public String getResponse() {
		return response;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	
	public String[] getNeighbours() {
		return neighbours;
	}
	
	public boolean isStartingNode() {
		return startingNode;
	}
	
	public PatternList[] getPatternList() {
		return patterns;
	}

	public String[] getEnhancements() {
		return enhancements;
	}
	
	public String[] getInhibitions() {
		return inhibitions;
	}

	public int getMaxUses() {
		return maxUses;
	}
	
	/**
	 * Determines if the node has exceeded its maximum uses or not
	 * @return		True if the node has exceeded its maximum uses. False if not
	 */
	public boolean isMaxedOut(AnnActivation annActivation) {
		return maxUses > -1 && annActivation.getActivationCount() >= maxUses;
	}
	
	@Override
	public String toString() {
		String output = "";
		
		output += "Node ID:\t\t" + nodeId + "\n";
		output += "Starting node:\t\t" + startingNode + "\n";
		output += "Patterns:\n";
		for(PatternList p : patterns) {
			output += "\t\t" + p.getWeight() + ":";
			
			for(Pattern pattern : p.getPatterns()) output += pattern.toString() + ",";
			
			output += "\n";
		}
		
		output += "Response:\t\t" + response;
		
		output += "\nEnhancements:\t\t";
		for(String eNodeId : enhancements) output += eNodeId + ",";
		
		output += "\nInhibitions:\t\t";
		for(String iNodeId : inhibitions) output += iNodeId + ",";
		
		output += "\nNeighbours:\t\t";
		for(String neighbourId : neighbours) output += neighbourId + ",";
		
		output += "\nMaximum uses:\t\t" + maxUses;
		
		return output;
	}
}
