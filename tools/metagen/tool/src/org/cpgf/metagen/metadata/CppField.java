package org.cpgf.metagen.metadata;

import java.util.*;

public class CppField extends Item {
	private CppType type;
	private int bitField;
	//added by saturner
	private String initializer;
	private boolean isVolatile;
	private boolean isMutable;
	private boolean isExtern;
	private boolean isTemplate;

	public CppField(String name, CppType type) {
		super(EnumCategory.Field, name);
		
		this.type = type;
	}

	public CppType getType() {
		return type;
	}
	
	public boolean isBitField() {
		return this.bitField > 0;
	}

	public int getBitField() {
		return bitField;
	}

	public void setBitField(int bitFields) {
		this.bitField = bitFields;
	}

	//scturner
	public String getInitializer() {
		return initializer;
	}

	public void setInitializer(String init) {
		initializer = init;
	}
	

    // ----------------------------------------------------------
    /**
     * Determines if this is volatile
     * @return true if volatile
     */
    public boolean isVolatile() {
        return this.isVolatile;
    }


    // ----------------------------------------------------------
    /**
     * Sets the volatile state
     * @param isVolatile New volatile state
     */
    public void setVolatile(boolean isVolatile) {
        this.isVolatile = isVolatile;
    }


    // ----------------------------------------------------------
    /**
     * Determines if this is mutable
     * @return true if mutable
     */
    public boolean isMutable() {
        return this.isMutable;
    }



    // ----------------------------------------------------------
    /**
     * Sets the mutable state
     * @param isMutable New mutable state
     */
    public void setMutable(boolean isMutable) {
        this.isMutable = isMutable;
    }

    // ----------------------------------------------------------
    /**
     * Sets the extern state
     * @param isExtern New extern state
     */
    public void setExtern(boolean isExtern) {
        this.isExtern = isExtern;
    }
    
 // ----------------------------------------------------------
    /**
     * Determines if this is extern
     * @return true if extern
     */
    public boolean isExtern() {
        return this.isExtern;
    }

    // ----------------------------------------------------------
    /**
     * Determines if the class is a template
     * @return true if it is a template
     */
    public boolean isTemplate() {
        return isTemplate;
    }
    
    /**
     * Sets if this class is a tempalte
     * @param isTemplate True if the class is a template
     */
    public void setTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }
    
    
 // ----------------------------------------------------------
    /**
     * Gets a list of modifiers set for this Item
     * @return List of EnumModifiers will all of the modifiers for this Item
     */
    @Override
    public List<EnumModifier> getModifiers() {
        List<EnumModifier> list = super.getModifiers();
        
        if (isMutable()) {
            list.add(EnumModifier.Mutable);
        }
        
        if (isVolatile()) {
            list.add(EnumModifier.Volatile);
        }
        
        if (isExtern()) {
            list.add(EnumModifier.Extern);
        }
        
        if (isTemplate()) {
            list.add(EnumModifier.Template);
        }
             
        return list;
    }

}
