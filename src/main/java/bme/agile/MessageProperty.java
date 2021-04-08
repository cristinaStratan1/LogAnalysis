package bme.agile;

import java.util.ArrayList;
import java.util.List;

public class MessageProperty {
	
	private String propertyName;
	private String propertyType;
	private List<MessageProperty> listOfChildrenProperties;
	
	public MessageProperty(){
        propertyName = null;
        propertyType = null;
        listOfChildrenProperties = new ArrayList<>();
    }
	
	public MessageProperty(String name, String type){
		super();
        propertyName = name;
        propertyType = type;
        listOfChildrenProperties = new ArrayList<>();
    }
	
	public String getPropertyName() { return propertyName; }
	
	public String getPropertyType() { return propertyType; }
	
	public List<MessageProperty> listOfChildrenProperties() { return listOfChildrenProperties; }
	
	public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
	
	public void setPropertyType(String type) { this.propertyType = type; }
	
	public void addChildren(MessageProperty childProperty) { this.listOfChildrenProperties.add(childProperty); }
	
	public void printProperty() {
		System.out.println("Name: " + this.propertyName + "\n" + "Type: " + this.propertyType + "\n");
		this.listOfChildrenProperties.forEach((temp) -> {
			this.printProperty();
		});
	}
	
	public boolean compareProperties(MessageProperty prop1, MessageProperty prop2) {
		if (prop1.propertyName.equals(prop2.propertyName) 
				&& prop1.propertyType.equals(prop2.propertyType))
			return true;
		else
			return false;
	}
	
}
