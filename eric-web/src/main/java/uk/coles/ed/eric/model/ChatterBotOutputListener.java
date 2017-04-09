package uk.coles.ed.eric.model;

/**
 * Allows external entities to listen to chatterbot output
 * @author Ed Coles
 *
 */
public interface ChatterBotOutputListener {
	public void output(String output); //Called by the ChatterBot object when the instance of ChatterBot says something
}
