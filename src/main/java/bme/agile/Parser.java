package bme.agile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.burningwave.core.classes.AnnotationSourceGenerator;
import org.burningwave.core.classes.ClassFactory;
import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.FunctionSourceGenerator;
import org.burningwave.core.classes.GenericSourceGenerator;
import org.burningwave.core.classes.TypeDeclarationSourceGenerator;
import org.burningwave.core.classes.UnitSourceGenerator;
import org.burningwave.core.classes.VariableSourceGenerator;

import static bme.agile.Constants.*;

public class Parser {	

    public static void main(String[] args) {
    	
		File logfile = new File(FILE_1);
		String[] filters = {SENT_ON, ENQUEUD_ON};
		List<String> messagesList = new ArrayList<String>();
		List<MessageProperty> messageProperties = new ArrayList<>();
		
		// Creating a list of PORTEVENT messages (type SENT_ON or ENQUEUED_ON)
		messagesList = parseTxtFile(logfile, filters);
		
		messagesList.forEach((tempMessage) -> {
			Reader inputString = new StringReader(tempMessage);
	    	BufferedReader reader = new BufferedReader(inputString);
        	Integer openBracketsNb = -1; // These will be used in parseProperties method to link properly the childrenProperties to their parentProperty
        	Integer closedBracketsNb = 0;
	    	try {
	    		// Parse the properties and obtain as a result a list of MessageProperty
	    		parseProperties(reader, messageProperties, new MessageProperty(), openBracketsNb, closedBracketsNb);
				messageProperties.forEach((temp) -> {
					temp.printProperty();
				});
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
    }
    
    // Parsing a text file and returning a list of the PORTEVENT messages
    public static ArrayList<String> parseTxtFile(File logfile, String[] filters){
    	BufferedReader reader;
    	ArrayList<String> messagesList = new ArrayList<String>();
    	try {
			reader = Files.newBufferedReader(logfile.toPath(), StandardCharsets.UTF_8);
	        reader.lines().forEach((temp) -> {
	        	if(temp.contains(PORTEVENT) && stringContainsItemFromList(temp, filters)) {
	        		Integer openBracketsNb = 1;
	        		Integer closedBracketsNb = 0;
	        		String message = OPEN_BRACKET + NEW_LINE;
	        		String currentLine;
	        		while (openBracketsNb != closedBracketsNb) { // A message contains as many "{" as “}"
	        			try {
							currentLine = reader.readLine();
							if(currentLine.contains(OPEN_BRACKET))
								openBracketsNb++;
							if(currentLine.contains(CLOSED_BRACKET))
								closedBracketsNb++;
							message+=currentLine + NEW_LINE;
						} catch (IOException e) {
							e.printStackTrace();
						}
	        		}
	        		messagesList.add(message);
	        	}
	        });
	        reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return messagesList;
    }
    
    // Parsing the properties and returning a list of MessageProperty
    public static List<MessageProperty> parseProperties(BufferedReader reader, List<MessageProperty> propertiesList, MessageProperty currentParent, Integer openBracketsNb, Integer closedBracketsNb) throws IOException {
    	String currentLine = reader.readLine();
    	Boolean noParent = false; // Used to know if the property is a 'main' one (without any parent property)
    	
    	while (currentLine != null) {
    		if (openBracketsNb == closedBracketsNb)
    			noParent = true;
    		if(currentLine.contains(OPEN_BRACKET))
				openBracketsNb++;
			if(currentLine.contains(CLOSED_BRACKET))
				closedBracketsNb++;
    		if (currentLine.contains(":=")) {
    			MessageProperty property = new MessageProperty (
						currentLine.trim().substring(0, currentLine.trim().indexOf(":=")),
						checkType(currentLine.substring(currentLine.indexOf(":=") + 1)),	
						(noParent ? new MessageProperty() : currentParent));
    			
				if (!checkIfExists(property, propertiesList)) {
					propertiesList.add(property);
				}
    			if (property.getPropertyType().equals(COMPLEX_STRUCTURE)) {
    				currentParent = property;
    				parseProperties(reader, propertiesList, currentParent, openBracketsNb, closedBracketsNb).forEach((temp) -> {
        				if (!checkIfExists(property, propertiesList)) {
        					propertiesList.add(property);
        				}
    				});
    			}
    		}
			currentLine = reader.readLine();
    	}
    	return propertiesList;
    }
    
    //Checking the type of a property
    public static String checkType(String propertyValue) {
    	if (propertyValue.contains("\""))
    		return STRING;
    	else if (propertyValue.contains(OPEN_BRACKET))
    		if (propertyValue.contains(CLOSED_BRACKET))
    			return EMPTY_STRUCTURE;
    		else 
    			return COMPLEX_STRUCTURE;
    	else {
			try {
		        Integer.parseInt(propertyValue);
		        return INTEGER;
		    } catch (NumberFormatException ex) {
		        return UNKNOWN;
		    }
		}
    }
    
    // Check if a property already is in a list of properties
    public static boolean checkIfExists(MessageProperty property, List<MessageProperty> propertiesList) {
    	Boolean alreadyExists = false;
		Integer index = 0;
		while (alreadyExists == false && index < propertiesList.size()) {
			alreadyExists = property.compareProperties(property, propertiesList.get(index));
			index++;
		}
		return alreadyExists;
    }

    // Checking if a string 'inputStr' contains the elements of an array of string 'items'
    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }
}