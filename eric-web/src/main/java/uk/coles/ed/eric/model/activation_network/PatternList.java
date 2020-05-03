package uk.coles.ed.eric.model.activation_network;

import uk.coles.ed.eric.data.service.ChatBotConfigurationDataService;
import uk.coles.ed.eric.data.service.ChatBotConfigurationDataServiceImpl;
import uk.coles.ed.eric.model.ChatBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PatternList stores a list of regex values used to determine a node's activation or to filter responses for high and low priority responses
 * @author Ed Coles
 *
 */
public class PatternList {
	private final int ANN_MODE = 0; //Defines the value indicating the PatternList instance belongs to an ActivationNetworkNode
	private final int INDEPENDENT_MODE = 1; //Defines the value indicating the PatternList instance belongs to a high or low priority response
	
	private int maxUses; //Stores the maximum amount of times the response can be used
	private int mode; //Indicates the context of the use of PatternList
	private int uses; //Stores the frequency with which the pattern has been used
	private int weight;	//Weight to be applied to the ActivationNetworkNode should comparison succeed
	
	private Pattern[] patterns;	//Patterns to be matched against user input
	private String response; //Text response to dispatch if requested
	private boolean strict = false; //Determines the matching style employed (if true, this means all patterns must match in order for the comparison to be a success)
	
	private boolean maxedOut;

	private ChatBotConfigurationDataService chatBotConfigurationDataService = new ChatBotConfigurationDataServiceImpl();

	/**
	 * Creates a new instance of the Pattern in its ActivationNetworkNode (ANN) pattern handling mode. This mode stores a list of regex patterns against along
	 * with a weight value that, when user input matches the patterns, determines the change in the node's activation value when a match is found
	 * @param 		newWeight		If the patterns given match when a node is preactivated, the change in the node's activation
	 * @param 		newPatterns		The list of regex patterns used that will be applied to the user's input to determine if this pattern weighting will affect
	 * 								the parent node's activation value
	 */
	public PatternList(int newWeight, String[] newPatterns) {
		weight = newWeight;
		
		patterns = new Pattern[newPatterns.length];
		for(int i = 0; i < patterns.length; i++) {
			//If the '*POS' wildcard has been employed, replace the wildcard with the positive response definition defined in ChatterBot
			if(newPatterns[i].equals(ChatBot.WILDCARD + "POS")) patterns[i] = Pattern.compile(chatBotConfigurationDataService.getPositiveResponseRegex());
			//If the '*NEG' wildcard has been employed, replace the wildcard with the negative response definition defined in ChatterBot
			else if(newPatterns[i].equals(ChatBot.WILDCARD + "NEG")) patterns[i] = Pattern.compile(chatBotConfigurationDataService.getNegativeResponseRegex());
			//Otherwise, compile the regex pattern
			else patterns[i] = Pattern.compile(newPatterns[i]);
		}
		
		mode = ANN_MODE;
	}
	
	
	/**
	 * Instantiates an instance of pattern list in its stand alone mode, used by high and low priority responses
	 * @param 		newPatterns			The regex patterns to apply to user input
	 * @param 		newResponse			The response given if the regex pattern matches user input successfully
	 * @param 		newMaxUses			The maximum amount of times the response can be used
	 * @param 		isStrict			Determines whether or not the matching is strict. If set to true, then user input must match all of the patterns
	 * 									in the list provided before being determined as a match. If set to false, then the user input must only match one
	 * 									of the regex patterns in the list provided in order to be determined a match
	 */
	public PatternList(String[] newPatterns, String newResponse, int newMaxUses, boolean isStrict) {
		patterns = new Pattern[newPatterns.length];
		strict = isStrict;
		for(int i = 0; i < patterns.length; i++) {
			//If the '*POS' wildcard has been employed, replace the wildcard with the positive response definition defined in ChatterBot
			if(newPatterns[i].equals(ChatBot.WILDCARD + "POS")) { patterns[i] = Pattern.compile(chatBotConfigurationDataService.getPositiveResponseRegex()); }
			//If the '*NEG' wildcard has been employed, replace the wildcard with the negative response definition defined in ChatterBot
			if(newPatterns[i].equals(ChatBot.WILDCARD + "NEG")) { patterns[i] = Pattern.compile(chatBotConfigurationDataService.getNegativeResponseRegex()); }
			//Otherwise, compile the regex pattern
			else { patterns[i] = Pattern.compile(newPatterns[i]); }
		}
		
		//Initialise the property variables
		response = newResponse;
		maxUses = newMaxUses;
		uses = 0;
		mode = INDEPENDENT_MODE;
	}
	
	/**
	 * Performs a comparison of the user input and regex patterns stored in this list
	 * @param 		input		The user input to compare to the patterns
	 * @return					True if the input is a match to the user input. False if not
	 */
	public boolean compare(String input) {
		for(Pattern p : patterns) {
			Matcher m = p.matcher(input);
			boolean match = m.find();
			
			if(match && !strict) return true; //If there was a 'match' and matching isn't 'strict' then we can stop here rather than wasting time continuing the comparison
			else if(strict && !match) return false; //However, if there wasn't a 'match' and matching is 'strict' then we can also stop here rather than waste time
		}
		
		if(strict) return true; //If matching is 'strict' and this code has been reached then return true
		else return false; //Otherwise, return false
	}
	
	public int getWeight() {
		return weight;
	}
	
	public Pattern[] getPatterns() {
		return patterns;
	}
	
	public String getResponse() {
		if(mode == INDEPENDENT_MODE && maxUses != -1) { //Verify that this instance hasn't exceeded it's maximum uses
			uses++;
			if(uses > maxUses) maxedOut = true;
		}
		
		return response;
	}
	
	public boolean isMaxedOut() {
		return maxedOut;
	}
}
