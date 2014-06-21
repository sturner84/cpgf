package org.cpgf.metagen.metadata;

import java.util.*;

public class CppEnum extends Item {
	private List<EnumValue> valueList;

	public CppEnum(String name) {
		super(EnumCategory.Enum, name);
		
		this.valueList = new ArrayList<EnumValue>();
	}

	public List<EnumValue> getValueList() {
		return valueList;
	}

	public void addValue(String name, String value) {
		valueList.add(new EnumValue(this, name, value));
	}

	// ----------------------------------------------------------
    /**
     * Gets a list of modifiers set for this Item
     * @return List of EnumModifiers will all of the modifiers for this Item
     */
    @Override
    public List<EnumModifier> getModifiers() {
        List<EnumModifier> list =  new LinkedList<EnumModifier>();
        
        if (isStatic()) {
            list.add(EnumModifier.Static);
        }
        
        return list;
    }
}
