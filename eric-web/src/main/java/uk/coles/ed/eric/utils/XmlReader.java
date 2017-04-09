package uk.coles.ed.eric.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A utility class for reading XML files
 * @author Ed Coles
 *
 */
public class XmlReader {
	private static final char LIST_DELIMITER = ','; //Define the list element delimiting character
	
	private Document doc; //Create Document object to host the XML file
	
	/**
	 * Reads the given XML file
	 * @param 		pathToXml						The location of the XML file to read
	 * @throws 		ParserConfigurationException	
	 * @throws 		SAXException
	 * @throws 		IOException
	 */
	public XmlReader(String pathToXml) throws ParserConfigurationException, SAXException, IOException {
		File networkConfig = new File(pathToXml); //Loads the XML file requested
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); //Grab an instance of the DocumentBuilderFactory to process the XML document
		DocumentBuilder dBuilder; //Create an instance of DocumentBuilder
		
		dBuilder = dbFactory.newDocumentBuilder(); //Get a new instance of DocumentBuiler
		
		doc = dBuilder.parse(networkConfig); //Process the XML information
		doc.getDocumentElement().normalize(); //Check for errors in XML notations or syntax
	}
	
	/**
	 * Retrieves the Document parsed from the XML
	 * @return		Document object parsed from the XML
	 */
	public Document getDocument() { return doc; }
	
	/**
	 * Splits a list into a String array
	 * @param 		list		String to split into a list
	 * @return					The String split into a list
	 */
	public static String[] parseList(String list) {
		return list.split(String.valueOf(LIST_DELIMITER));
	}
}
