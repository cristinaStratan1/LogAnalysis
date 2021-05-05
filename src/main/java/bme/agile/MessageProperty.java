package bme.agile;

import java.util.ArrayList;

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
	
	public boolean compareProperties(MessageProperty prop1, MessageProperty prop2) {
		return prop1.propertyName.equals(prop2.propertyName);
	}
	
}
