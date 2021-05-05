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

import org.burningwave.core.classes.ClassSourceGenerator;
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
		ArrayList<MessageProperty> currentParents = new ArrayList<>();
		
		// Creating a list of PORTEVENT messages (type SENT_ON or ENQUEUED_ON)
		messagesList = parseTxtFile(logfile, filters);
		
		// Extracting the properties of each message
		messagesList.forEach((tempMessage) -> {
			Reader inputString = new StringReader(tempMessage);
	    	BufferedReader reader = new BufferedReader(inputString);
	    	try {
	    		currentParents.add(new MessageProperty(PORTEVENT.toLowerCase(), ARRAY_STRUCTURE, new MessageProperty()));
	    		messageProperties.add(currentParents.get(0));
	    		// Parse the properties and obtain as a result a list of MessageProperty
		    	reader.readLine();
	    		parseProperties(reader, messageProperties, currentParents);
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		// Populating the list of children of each property (if needed)
		messageProperties.forEach((temp) -> {
			String nameParent = temp.getParentProperty().getPropertyName();
			if (nameParent != null) {
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
		
		System.out.println(messageProperties.size());
		
		// Generating the classes at runtime
    	messageProperties.forEach((temp) -> {
    		if (temp.getPropertyType().equals(ARRAY_STRUCTURE)) {
    			generateClassAtRuntime(temp);
    		}
    	});
    }
    
    /**
     * Parsing a text file and returning a list of the PORTEVENT messages
     * @param logfile
     * @param filters
     * @return
     */
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
    
    /**
     * Parsing the properties and returning a list of MessageProperty
     * @param reader
     * @param propertiesList
     * @param currentParents
     * @return
     * @throws IOException
     */
    public static List<MessageProperty> parseProperties(BufferedReader reader, List<MessageProperty> propertiesList, ArrayList<MessageProperty> currentParents) throws IOException {
    	String currentLine = reader.readLine();
    	
    	while (currentLine != null) {
    		if (currentLine.contains(EQUAL)) {
    			MessageProperty property = new MessageProperty (
    				currentLine.trim().substring(0, currentLine.trim().indexOf(EQUAL)),
    				checkType(currentLine.substring(currentLine.indexOf(EQUAL) + 2)),	
    				(currentParents.get(currentParents.size() - 1)));
    		
    			Integer indexProperty = checkIfExists(property, propertiesList);
				if (indexProperty == -1)
	    			propertiesList.add(property);
				else
					if (propertiesList.get(indexProperty).getPropertyType() == OBJECT)
						propertiesList.get(indexProperty).setPropertyType(property.getPropertyType());
					
	    		if (property.getPropertyType().equals(ARRAY_STRUCTURE))
	    			currentParents.add(property);
    		}
    		
    		if (currentLine.trim().equals(OPEN_BRACKET))
    			currentParents.add(currentParents.get(currentParents.size() - 1));

    		if (currentLine.trim().contains(":={}") || currentLine.trim().equals("}") || currentLine.trim().equals("},"))
    			currentParents.remove(currentParents.size() - 1);
    		
    		currentLine = reader.readLine();
    	}
    	return propertiesList;
    }
    
    /**
     * Checking the type of a property
     * @param propertyValue
     * @return
     */
    public static String checkType(String propertyValue) {
    	if (propertyValue.contains("\""))
    		return STRING;
    	else if (propertyValue.contains(OPEN_BRACKET))
    		return ARRAY_STRUCTURE;
    	else if (propertyValue.contains("(") && propertyValue.contains(")"))
    		return INTEGER;
    	else {
			try {
		        Integer.parseInt(propertyValue.trim().replaceAll(",", ""));
		        return INTEGER;
		    } catch (NumberFormatException ex) {
		        return OBJECT;
		    }
		}
    }
    
    /**
     * Checking if a property already is in a list of properties
     * @param property
     * @param propertiesList
     * @return
     */
    public static Integer checkIfExists(MessageProperty property, List<MessageProperty> propertiesList) {
    	Boolean alreadyExists = false;
		Integer index = -1;
		while (alreadyExists == false && index < propertiesList.size() - 1) {
			index++;
			alreadyExists = property.compareProperties(property, propertiesList.get(index));
		}
		return (alreadyExists ? index : -1);
    }

    /**
     * Checking if a string 'inputStr' contains the elements of an array of string 'items'
     * @param inputStr
     * @param items
     * @return
     */
    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }
    
    /**
     * Generating a class at runtime
     * @param messageProperty
     */
    public static void generateClassAtRuntime(MessageProperty messageProperty) {
    	
        	UnitSourceGenerator unitSG = UnitSourceGenerator.create("generatedclasses"
        		).addImport(
        			"java.util.Arrays"
        		);
    		ClassSourceGenerator tmpClass = ClassSourceGenerator.create(
				TypeDeclarationSourceGenerator.create(messageProperty.getPropertyName().trim())
			).addModifier(
				Modifier.PUBLIC
			);
    		
    		if (!messageProperty.getPropertyName().equals(PORTEVENT.toLowerCase()))
    			tmpClass.expands(
					TypeDeclarationSourceGenerator.create(messageProperty.getParentProperty().getPropertyName()
				)
			);
    		
    		if (!messageProperty.getListOfChildrenProperties().isEmpty()) {
    			messageProperty.getListOfChildrenProperties().forEach((temp) -> {
    				tmpClass.addField(VariableSourceGenerator.create(TypeDeclarationSourceGenerator.create(temp.getPropertyType()), temp.getPropertyName()).addModifier(Modifier.PRIVATE));
    			});
    		}
    		
    		unitSG.addClass(tmpClass);
    		unitSG.storeToClassPath(System.getProperty("user.dir") + "/src/main/java");
    }
}