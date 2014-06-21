package org.cpgf.metagen.metadata;

import java.util.*;


// -------------------------------------------------------------------------
/**
 *  C++ Modifiers that might apply to a function/field/method/function/class.
 *  
 *  This enum provides a conversion between the Doxygen xml and the modifiers
 *  stored in the cpgf classes.
 *
 *  @author  scturner
 *  @version Jun 7, 2014
 */
public enum EnumModifier
{
    /** Static keyword */
    Static("metaModifierStatic", EnumItemType.VARIABLE.getValue() 
        | EnumItemType.FUNCTION.getValue() 
        | EnumItemType.FIELD.getValue() 
        | EnumItemType.METHOD.getValue() 
        | EnumItemType.OPERATOR.getValue()),
    /** virtual keyword */
    Virtual("metaModifierVirtual", EnumItemType.METHOD.getValue() 
        | EnumItemType.OPERATOR.getValue() 
        | EnumItemType.DESTRUCTOR.getValue() 
        | EnumItemType.BASECLASS.getValue()),
    /** virtual keyword but a pure virtual method*/
    PureVirtual("metaModifierPureVirtual", EnumItemType.METHOD.getValue() 
        | EnumItemType.OPERATOR.getValue()),
    /** Uses a template */
    Template("metaModifierTemplate", EnumItemType.VARIABLE.getValue() 
        | EnumItemType.FUNCTION.getValue() 
        | EnumItemType.FIELD.getValue() 
        | EnumItemType.METHOD.getValue()
        | EnumItemType.CONSTRUCTOR.getValue() 
        | EnumItemType.OPERATOR.getValue() 
        | EnumItemType.CLASS.getValue() 
        | EnumItemType.BASECLASS.getValue()),
    /** const keyword */
    Const("metaModifierConst", EnumItemType.VARIABLE.getValue() 
        | EnumItemType.FIELD.getValue() 
        | EnumItemType.METHOD.getValue() 
        | EnumItemType.OPERATOR.getValue()),
    /** volatile keyword */
    Volatile("metaModifierVolatile", EnumItemType.VARIABLE.getValue() 
        | EnumItemType.FIELD.getValue()),
    /** inline keyword */
    Inline("metaModifierInline", EnumItemType.FUNCTION.getValue() 
        | EnumItemType.METHOD.getValue() 
        | EnumItemType.CONSTRUCTOR.getValue() 
        | EnumItemType.DESTRUCTOR.getValue()),
    /** explicit keyword */
    Explicit("metaModifierExplicit", EnumItemType.CONSTRUCTOR.getValue()),
    /** extern keyword */
    Extern("metaModifierExtern", EnumItemType.VARIABLE.getValue() 
        | EnumItemType.FUNCTION.getValue()),
    /** mutable keyword */
    Mutable("metaModifierMutable", EnumItemType.VARIABLE.getValue() 
        | EnumItemType.FIELD.getValue());

    private String value;
    private int items;
   
    private EnumModifier(String cpgfValue, int allowedItems)
    {
        value = cpgfValue;
        items = allowedItems;
    }

    /**
     * Gets the name of the constant in cpgf that matches this modifier
     * 
     * @return Name of the constant in cpgf that matches this modifier
     */
    public String getCpgfValue() {
        return value;
    }
    
 
    
    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for the item type
     * @param type ItemType (variable, method, class, etc.)
     * @return true if the modifier is permitted with the type
     */
    public boolean isAllowedFor(EnumItemType type) {
        return (items & type.getValue()) != 0; 
    }
    
    
    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for variables
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForVariable() {
        return isAllowedFor(EnumItemType.VARIABLE);
    }
    
    
    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for functions
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForFunction() {
        return isAllowedFor(EnumItemType.FUNCTION);
    }
    

    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for fields 
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForField() {
        return isAllowedFor(EnumItemType.FIELD);
    }

    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for methods 
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForMethod() {
        return isAllowedFor(EnumItemType.METHOD);
    }

    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for constructors 
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForConstructor() {
        return isAllowedFor(EnumItemType.CONSTRUCTOR);
    }

    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for descructors
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForDestructor() {
        return isAllowedFor(EnumItemType.DESTRUCTOR);
    }

    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for enums 
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForEnum() {
        return isAllowedFor(EnumItemType.ENUM);
    }


    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for operators 
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForOperator() {
        return isAllowedFor(EnumItemType.OPERATOR);
    }

    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for classes 
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForClass() {
        return isAllowedFor(EnumItemType.CLASS);
    }

    // ----------------------------------------------------------
    /**
     * Determines if this modifier is allowed for base classes 
     * @return true if the modifier is permitted
     */
    public boolean isAllowedForBaseClass() {
        return isAllowedFor(EnumItemType.BASECLASS);
    }


    // ----------------------------------------------------------
    /**
     * Converts a list of modifiers to a String of cpgf constants separated by
     * |
     * @param mods List of modifiers
     * @return String of cpgf constants
     */
    static public String modifersToCpgfString(List<EnumModifier> mods) {
        String str = "";
        
        for (EnumModifier m : mods) {
            if (!str.isEmpty()) {
                str += " | ";
            }
            str += m.getCpgfValue();
        }
        
        if (str.isEmpty()) {
            str = "metaModifierNone";
        }
        
        
        return str;
    }
    
    
 // ----------------------------------------------------------
    /**
     * Converts a list of modifiers to a String of cpgf constants separated by
     * |
     * @param type Type of item these modifiers are for
     * @param mods List of modifiers
     * @return String of cpgf constants
     */
    static public String modifersToCpgfString(EnumItemType type, 
        List<EnumModifier> mods) {
        String str = "";
        
        for (EnumModifier m : mods) {
            if (m.isAllowedFor(type)) {
                if (!str.isEmpty()) {
                    str += " | ";
                }
                str += m.getCpgfValue();
            }
        }

        if (str.isEmpty()) {
            str = "metaModifierNone";
        }
        
        return str;
    }
    
    
}
