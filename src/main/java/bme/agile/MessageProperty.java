package bme.agile;

import java.util.ArrayList;
import java.util.List;

public class MessageProperty {
	
	private String propertyName;
	private String propertyType;
	private MessageProperty parentProperty;
	private ArrayList<MessageProperty> listOfChildrenProperties;
	
	public MessageProperty(){
        propertyName = null;
        propertyType = null;
        parentProperty = null;
        listOfChildrenProperties = new ArrayList<MessageProperty>();
    }
	
	public MessageProperty(String name, String type, MessageProperty parent){
		super();
        propertyName = name;
        propertyType = type;
        parentProperty = parent;
        listOfChildrenProperties = new ArrayList<MessageProperty>();
    }
	
	public String getPropertyName() { return this.propertyName; }
	
	public String getPropertyType() { return this.propertyType; }
	
	public MessageProperty getParentProperty() { return this.parentProperty; }
	
	public ArrayList<MessageProperty> getListOfChildrenProperties() { return this.listOfChildrenProperties; }
	
	public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
	
	public void setPropertyType(String type) { this.propertyType = type; }
	
	public void setParentProperty(MessageProperty parent) { this.parentProperty = parent; }
	
	public void addChildren(MessageProperty childProperty) { this.listOfChildrenProperties.add(childProperty); }
	
	public void printProperty() {
		System.out.println("Name: " + this.propertyName + "\n" + "Type: " + this.propertyType);
		System.out.println("Parent property: " + this.getParentProperty().getPropertyName());
		if (this.listOfChildrenProperties.isEmpty())
			System.out.println("No children properties\n");
		else {
			System.out.print("Children properties: ");
			this.listOfChildrenProperties.forEach((temp) -> {
				System.out.print(temp.propertyName + "  ");
			});
			System.out.println("\n");
		}
	}
	
	public boolean compareProperties(MessageProperty prop1, MessageProperty prop2) {
		if (prop1.propertyName.equals(prop2.propertyName) 
				&& prop1.propertyType.equals(prop2.propertyType))
			return true;
		else
			return false;
	}
	
}
