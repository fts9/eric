package uk.coles.ed.eric.model.activation_network;

import java.util.regex.Pattern;

/**
 * An individual conversation node inside the activation network
 * @author Ed Coles
 *
 */
public class ActivationNetworkNode {
	private final float ACTIVATION_CEILING = 1.0f;	//Define the ceiling of the activation value
	private final float ACTIVATION_FLOOR = 0.0f; //Define the floor of the activation value
	
	private boolean startingNode = false; //If true, identifies the node as the start of a conversation tree
	private int maxUses, uses = -1; //Set the maximum uses for the node
	private float activation = ACTIVATION_FLOOR;	//Node activation
	private String nodeId, response;	//The unique name given to the node/The response that is returned when the node is activated
	
	private String[] enhancements; //Nodes that will be enhanced when activate() is called
	private String[] inhibitions; //Nodes that will be inhibited when activate() is called
	private String[] neighbours; //A list of conversation nodes that are directly next to the node in the network and where conversation will be directed when activate() is called
	
	private PatternList[] patterns; //String patterns in user input that will be searched for when preActivate() is called
	
	/**
	 * Instantiates a new instance of an ActivationNetworkNode
	 * @param 		newNodeID			The ID of the activation network node
	 * @param 		newActivation		The node's default activation
	 * @param 		newPatterns			The user input patterns list
	 * @param 		newResponse			The response text given by this node
	 * @param 		newNeighbours		A list of the node's immediate neighbour IDs
	 * @param 		newEnhancements		A list of the nodes that will have their activation enhanced when this node is activated
	 * @param 		newInhibitions		A list of the nodes that will have their activation inhibited when this node is activated
	 * @param 		isStartingNode		True if the node is the start of a conversation tree. False if not or if the node should only be activated by other nodes
	 * @param 		newMaxUses			The default maximum uses value
	 */
	public ActivationNetworkNode(String newNodeID, float newActivation, PatternList[] newPatterns, String newResponse, String[] newNeighbours, String[] newEnhancements, String[] newInhibitions, boolean isStartingNode, int newMaxUses) {
		//Initialise variables with new values
		nodeId = newNodeID;
		setActivation(newActivation);
		patterns = newPatterns;
		neighbours = newNeighbours;
		enhancements = newEnhancements;
		inhibitions = newInhibitions;
		response = newResponse;
		startingNode = isStartingNode;
		maxUses = newMaxUses;
		uses = 0;
	}
	
	/**
	 * Parses user input for the string patterns in the 'patterns' variable and then returns a boolean depending on whether any of the node's patterns were 
	 * found in the input or not. The activation of the node is adjusted according to this result.
	 * @param 		input		The input upon which to pre-activate the node
	 * @return					True if the input matched one or more patterns. False if not
	 */
	public boolean preActivate(String input) {
		boolean matchFound = false; //We only need to know if a pattern has been successfully matched or not, not which one, so declare this outside the 'for' loop...
		
		for(PatternList p : patterns) {
			boolean isActivated = false; //...On the other hand, we also need to know on a case by case basis too so if a pattern does match the node activation can be adjusted accordingly
			
			//Search through each of the patterns found in 'p'. If there are any matches found, or the WILDCARD, then set the appropriate booleans to indicate a match was found
			if(p.compare(input)) {
				matchFound = true;
				isActivated = true;
			}
			
			//If a match was found in this case, adjust the node's activation to the magnitude specified by 'weight'
			if(isActivated) setActivation(activation + ((float)p.getWeight() / 10));
		}
		
		return matchFound;
	}
	
	/**
	 * Activate the network node and return itself to update the requesting instance
	 * @return		The network node in its activated state
	 */
	public ActivationNetworkNode activate() {
		for(String eNodeId : enhancements) ActivationNetwork.getNodeById(eNodeId).enhance(); //Activate enhancements
		for(String iNodeId : inhibitions) ActivationNetwork.getNodeById(iNodeId).inhibit(); //Activate inhibitions
		
		uses++; //Note that the node has now been activated once
		
		return this;
	}
	
	/**
	 * Inhibits the node's activation by 0.1
	 */
	public void inhibit() { 
		setActivation(activation - 0.1f); 
	}
	
	/**
	 * Enhances the node's activation by 0.1
	 */
	public void enhance() { setActivation(activation + 0.1f); }
	
	/**
	 * Updates the node's activation value with the one provided. If the value exceeds either the activation ceiling or floor it will be set to the ceiling 
	 * or floor value respectively
	 * @param 	value	The node's new activation value
	 */
	private void setActivation(float value) {
		if(activation > ACTIVATION_CEILING) activation = ACTIVATION_CEILING; //If the new 'value' exceeds 'ACTIVATION_CEILING' set it to the value of 'ACTIVATION_CEILING'
		if(activation < ACTIVATION_FLOOR) activation = ACTIVATION_FLOOR; //If the new 'value' is below 'ACTIVATION_FLOOR' set it to the value of 'ACTIVATION_FLOOR'
		else activation = value; //If the 'value' falls between 'ACTIVATION_CEILING' and 'ACTIVATION_FLOOR' update the activation with the specified value
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

	public float getActivation() {
		return activation;
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
	public boolean isMaxedOut() {
		if(maxUses > -1 && uses >= maxUses) return true;
		else return false;
	}
	
	@Override
	public String toString() {
		String output = "";
		
		output += "Node ID:\t\t" + nodeId + "\n";
		output += "Activation:\t\t" + activation + "\n";
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
