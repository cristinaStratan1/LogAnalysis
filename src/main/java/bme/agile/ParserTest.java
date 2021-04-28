package bme.agile;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {


    @Test
    void testCheckType() {
        String propertyValue = "{}";
        String response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.EMPTY_STRUCTURE);

        //when propertyValue is "
        propertyValue = "\"";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.STRING);

        //when propertyValue contains a complex structure
        propertyValue = "{this is complex";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.COMPLEX_STRUCTURE);

        //when propertyValue is integer
        propertyValue = "123";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.INTEGER);

        //when propertyValue throws error
        propertyValue ="this will throw error";
        response = Parser.checkType(propertyValue);
        assertEquals(response,Constants.UNKNOWN);


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

        boolean response = Parser.checkIfExists(messageProperty,propertyList);
        assertFalse(response);

        propertyList.add(messageProperty);
        response = Parser.checkIfExists(messageProperty,propertyList);
        assertTrue(response);

    }



    private MessageProperty buildMessageProperty(String testNumber) {
        MessageProperty messageProperty = new MessageProperty();
        StringBuilder property = new StringBuilder(testNumber);
        messageProperty.setPropertyName(String.valueOf(property.append("-DummyName")));
        messageProperty.setPropertyType(String.valueOf(property.append("-DummyType")));
        return messageProperty;
    }
}
