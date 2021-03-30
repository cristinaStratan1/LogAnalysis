package bme.agile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
	
	// CONSTANTS
	public static final String SENT_ON = "Sent on";
	public static final String ENQUEUD_ON = "Message enqueud on";
	public static final String PORTEVENT = "PORTEVENT";
	public static final String OPEN_BRACKET = "{";
	public static final String CLOSED_BRACKET = "}";
	public static final String NEW_LINE = "\n";

    public static void main(String[] args) {
    	
        BufferedReader reader;
		File logfile = new File("src/main/resources/WCG100140020.txt");
		String[] filters = {SENT_ON, ENQUEUD_ON};
		List<String> messagesList = new ArrayList<String>();
		
		try {
			reader = Files.newBufferedReader(logfile.toPath(), StandardCharsets.UTF_8);
	        reader.lines().forEach((temp) -> {
	        	if(temp.contains(PORTEVENT) && stringContainsItemFromList(temp, filters)) {
	        		Integer openBracketsNb = 1;
	        		Integer closedBracketsNb = 0;
	        		String message = OPEN_BRACKET + NEW_LINE;
	        		String currentLine;
	        		while (openBracketsNb != closedBracketsNb) {
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
	        		System.out.println(message);
	        	}
	        });
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    // Checks if a string 'inputStr' is contained in an array of string 'items'
    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }
}