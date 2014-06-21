package org.cpgf.metagen.metadata;

// -------------------------------------------------------------------------
/**
 *  List the types of items in c++ that are being processed in metagen
 *
 *  @author  sctuner
 *  @version Jun 7, 2014
 */
public enum EnumItemType
{
    /** A variable */
    VARIABLE(1),
    /** A function */
    FUNCTION(2),
    /** A class field */
    FIELD(4),
    /** A class method */
    METHOD(8),
    /** A constructor */
    CONSTRUCTOR(16),
    /** A desctructor */
    DESTRUCTOR(32),
    /** An enum */
    ENUM(64),
    /** An operator (which is treated as a method) */
    OPERATOR(128),
    /** A class */
    CLASS(256),
    /** Class listed as a base class */
    BASECLASS(512);
    
    private int value;
    
    private EnumItemType(int v) {
        value = v;
    }
    
    /** 
     * Gets the value associated with the type. Used for ORing these values
     * together.
     * @return Value associated with this enum value
     */
    public int getValue() {
        return value;
    }
}
