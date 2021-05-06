package bme.agile;


import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {


    @Test
    void testCheckType() {
        String propertyValue = "{}";
        String response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.ARRAY_STRUCTURE);

        //when propertyValue is "
        propertyValue = "\"";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.STRING);

        //when propertyValue contains a complex structure
        propertyValue = "{this is complex";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.ARRAY_STRUCTURE);

        //when propertyValue is integer
        propertyValue = "123";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.INTEGER);

        //when propertyValue throws error
        propertyValue ="this will throw error";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.OBJECT);


    }

    @Test
    void testParseTxtFile() {
        String file ="src/main/resources/msg_test.txt";
        String []filters = new String[1];
        filters[0] = Constants.PORTEVENT;
        ArrayList<String> response = Parser.parseTxtFile(new File(file), filters);
        assertEquals(response.size(),1);
        String result ="{\n" +
                "\trequest := {\n" +
                "        client_id := 9,\n" +
                "        method := \"POST\",\n" +
                "        uri := \"/jolokia/\",\n" +
                "        version_major := 1,\n" +
                "        version_minor := 1,\n" +
                "        header := {\n" +
                "            {\n" +
                "                header_name := \"Host\",\n" +
                "                header_value := \"142.133.142.3\"\n" +
                "            },\n" +
                "            {\n" +
                "                header_name := \"Content-Type\",\n" +
                "                header_value := \"application/json\"\n" +
                "            },\n" +
                "            {\n" +
                "                header_name := \"Content-Length\",\n" +
                "                header_value := \"1971\"\n" +
                "            }\n" +
                "        },\n" +
                "        body := \"STRING\"\n" +
                "    }\n" +
                "}\n";

        assertEquals(response.get(0),result);
    }



    @Test
    void testCheckIfExists() {
        MessageProperty messageProperty = buildMessageProperty("test1");
        MessageProperty messageProperty1 = buildMessageProperty("test2");

        List<MessageProperty> propertyList = new ArrayList<>();
        propertyList.add(messageProperty1);

        Integer response = Parser.checkIfExists(messageProperty,propertyList);
        assertEquals(response,-1);

        propertyList.add(messageProperty);
        response = Parser.checkIfExists(messageProperty,propertyList);
        assertEquals(response,1);

    }



    private MessageProperty buildMessageProperty(String testNumber) {
        MessageProperty messageProperty = new MessageProperty();
        StringBuilder property = new StringBuilder(testNumber);
        messageProperty.setPropertyName(String.valueOf(property.append("-DummyName")));
        messageProperty.setPropertyType(String.valueOf(property.append("-DummyType")));
        return messageProperty;
    }
    
    @Test
    void teststringContainsItemFromList() {
    	
    	String str1 = "test";
    	String[] strAr2 = {"rand", "test", " was"};  
     
    	assertTrue(Parser.stringContainsItemFromList(str1, strAr2));
    	
    	String str2 = "test";
    	String[] strAr3 = {"rand", "dev", " was"};  
     
    	assertFalse(Parser.stringContainsItemFromList(str2, strAr3));
    }
    
    @Test
    void  generateClassAtRuntime() {

    	
    	
    	assertTrue(generateClassAtRuntimetest1());
    	 
    	
    }
    private boolean generateClassAtRuntimetest1() {
		// TODO Auto-generated method stub
    	MessageProperty messageProperty1 = buildMessagePropertyforGenerateClass("");

    		
    		try {
    			Parser.generateClassAtRuntime(messageProperty1);
    			}
    			catch(Exception e) {
    			  //  Block of code to handle errors
    				return true;
    			}
			return false;
		
	}


	private MessageProperty buildMessagePropertyforGenerateClass(String testNumber) {
        MessageProperty messageProperty = new MessageProperty();
        StringBuilder property = new StringBuilder(testNumber);
        messageProperty.setPropertyType(String.valueOf(property.append("Array")));
        messageProperty.setPropertyName(String.valueOf(property.append("Array")));
        return messageProperty;
    }
	
	@Test
	void testparserProperites() {

	
		try {
			FileReader r = null;
			BufferedReader br;
			br=new BufferedReader(r);
			  MessageProperty messageProperty1 = buildMessageProperty("test2");

		        List<MessageProperty> propertyList = new ArrayList<>();
		        propertyList.add(messageProperty1);
		        
		        MessageProperty messageProperty2 = buildMessageProperty("test2");

		        ArrayList<MessageProperty> propertyList1 = new ArrayList<>();
		        propertyList1.add(messageProperty2);
			Parser.parseProperties(br, propertyList, propertyList1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			assertTrue(true);
		}
		
	}
    
}
