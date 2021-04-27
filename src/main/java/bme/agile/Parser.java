package bme.agile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.burningwave.core.classes.AnnotationSourceGenerator;
import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.FunctionSourceGenerator;
import org.burningwave.core.classes.GenericSourceGenerator;
import org.burningwave.core.classes.TypeDeclarationSourceGenerator;
import org.burningwave.core.classes.UnitSourceGenerator;
import org.burningwave.core.classes.VariableSourceGenerator;

import static bme.agile.Constants.*;

public class Parser {	

    public static void main(String[] args) {
    	
		File logfile = new File(FILE_TEST);
		String[] filters = {SENT_ON, ENQUEUD_ON};
		List<String> messagesList = new ArrayList<String>();
		List<MessageProperty> messageProperties = new ArrayList<>();
		ArrayList<MessageProperty> currentParents = new ArrayList<>(); // Used to track the current parent property of the properties we're parsing
		
		// Creating a list of PORTEVENT messages (type SENT_ON or ENQUEUED_ON)
		messagesList = parseTxtFile(logfile, filters);
		
		messagesList.forEach((tempMessage) -> {
			Reader inputString = new StringReader(tempMessage);
	    	BufferedReader reader = new BufferedReader(inputString);
        	Integer openBracketsNb = -1; // These will be used in parseProperties method to link properly the childrenProperties to their parentProperty
        	Integer closedBracketsNb = 0;
	    	try {
	    		// Parse the properties and obtain as a result a list of MessageProperty
	    		parseProperties(reader, messageProperties, new MessageProperty(), openBracketsNb, closedBracketsNb, currentParents);
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		messageProperties.forEach((temp) -> {
			if (temp.getParentProperty().getPropertyName() != null) {
				String nameParent = temp.getParentProperty().getPropertyName();
				boolean found = false;
				Integer index = -1;
				while (!found) {
					index++;
					if (messageProperties.get(index).getPropertyName().equals(nameParent))
						found = true;
				}
				messageProperties.get(index).addChildren(temp);
			}
		});
		
		// Debug printing - delete when project development is over
		messageProperties.forEach((temp) -> {
			temp.printProperty();
		});
		
    	messageProperties.forEach((temp) -> {
    		generateClassAtRuntime(temp);
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
    public static List<MessageProperty> parseProperties(BufferedReader reader, List<MessageProperty> propertiesList, MessageProperty currentParent, Integer openBracketsNb, Integer closedBracketsNb, ArrayList<MessageProperty> currentParents) throws IOException {
    	String currentLine = reader.readLine();
    	
    	while (currentLine != null) {
    		if (openBracketsNb == closedBracketsNb && !currentParents.isEmpty()) {
    			currentParents.remove(currentParents.size() - 1);
    		}
    		if(currentLine.contains(OPEN_BRACKET))
				openBracketsNb++;
			if(currentLine.contains(CLOSED_BRACKET))
				closedBracketsNb++;
    		if (currentLine.contains(":=")) {
    			MessageProperty property = new MessageProperty (
						currentLine.trim().substring(0, currentLine.trim().indexOf(":=")),
						checkType(currentLine.substring(currentLine.indexOf(":=") + 2)),	
						(currentParents.isEmpty() ? new MessageProperty() : currentParents.get(currentParents.size() - 1)));
    			
				if (!checkIfExists(property, propertiesList)) {
					propertiesList.add(property);
				}
    			if (property.getPropertyType().equals(ARRAY_STRUCTURE)) {
    				openBracketsNb = 1;
	        		closedBracketsNb = 0;
    				currentParents.add(property);
    				currentParent = property;
    				parseProperties(reader, propertiesList, currentParent, openBracketsNb, closedBracketsNb, currentParents).forEach((temp) -> {
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
    		return ARRAY_STRUCTURE;
    	else {
			try {
		        Integer.parseInt(propertyValue.trim().replaceAll(",", ""));
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
    
    //Generate a class at runtime
    public static void generateClassAtRuntime(MessageProperty messageProperty) {
    	if (messageProperty.getPropertyType().equals(ARRAY_STRUCTURE)) {
        	UnitSourceGenerator unitSG = UnitSourceGenerator.create("generatedclasses");
    		ClassSourceGenerator tmpClass = ClassSourceGenerator.create(
					TypeDeclarationSourceGenerator.create(messageProperty.getPropertyName().trim())
				).addModifier(
						Modifier.PUBLIC
				);
    		if (!messageProperty.getListOfChildrenProperties().isEmpty()) {
    			messageProperty.getListOfChildrenProperties().forEach((temp) -> {
    				tmpClass.addField(VariableSourceGenerator.create(TypeDeclarationSourceGenerator.create(temp.getPropertyType()), temp.getPropertyName()).addModifier(Modifier.PRIVATE));
    			});
    		}
    		unitSG.addClass(tmpClass);
    		unitSG.storeToClassPath(System.getProperty("user.dir") + "/src/main/java");
    		// Debug printing - delete when project development is over
    		System.out.println("\nGenerated code:\n" + unitSG.make());
    	};
    }
}