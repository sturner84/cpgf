package org.cpgf.metagen.metadata;

import java.util.*;

// -------------------------------------------------------------------------
/**
 *  Represents a method/function.
 *
 *  @author  scturner (documented)
 *  @version Jun 10, 2014
 */
public class CppMethod extends CppInvokable {
	
    private boolean isExtern;
    
    // ----------------------------------------------------------
    /**
     * Creates a method/function
     * @param name Name of the method
     * @param resultType Return type
     */
    public CppMethod(String name, CppType resultType) {
		super(EnumCategory.Method, name, resultType);
		
		isExtern = false;
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
     * Gets a list of modifiers set for this Item
     * @return List of EnumModifiers will all of the modifiers for this Item
     */
    @Override
    public List<EnumModifier> getModifiers() {
        List<EnumModifier> list = super.getModifiers();
        
        if (isExtern()) {
            list.add(EnumModifier.Extern);
        }
     
        return list;
    }
}
