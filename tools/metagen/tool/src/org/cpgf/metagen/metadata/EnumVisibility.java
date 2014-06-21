package org.cpgf.metagen.metadata;

// -------------------------------------------------------------------------
/**
 *  Visibility of the code item (class, method, field, etc.)
 *
 *  @author  scturner (documented)
 *  @version Jun 6, 2014
 */
public enum EnumVisibility {
	/** Public visibility */
	Public("mPublic"),
	/** Protected visibility */
    Protected("mProtected"),
    /** Private visibility */
    Private("mPrivate");
	
	private final String value;
	
	private EnumVisibility(String val) {
	    value = val;
	}
	
	/**
	 * Gets the numeric value of this enum value.  Needed generating code
	 * for cpgf so that the values match the C++ enum.
	 * 
	 * @return Numeric value of this enum.
	 */
	public String getIntValue() {
	    return value;
	}
}
