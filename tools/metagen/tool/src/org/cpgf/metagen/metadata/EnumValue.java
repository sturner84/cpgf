package org.cpgf.metagen.metadata;

// -------------------------------------------------------------------------
/**
 *  Represents a single value for an enum.
 *
 *  @author  scturner (documented)
 *  @version Jun 6, 2014
 */
public class EnumValue {
	private CppEnum owner;
	private String name;
	private String value;
	
	// ----------------------------------------------------------
	/**
	 * Creates an EnumValue
	 * 
	 * @param owner CppEnum that owns this value
	 * @param name Name of the enum value
	 * @param value Numeric value associated with name
	 */
	public EnumValue(CppEnum owner, String name, String value) {
		this.owner = owner;
		this.name = name;
		this.value = value;
	}

	// ----------------------------------------------------------
	/**
	 * Gets the name of the value
	 * @return Name of the value
	 */
	public String getName() {
		return name;
	}
	
	// ----------------------------------------------------------
	/**
	 * Gets the full name with namespace
	 * @return Full name
	 */
	public String getQualifiedName() {
		String n = this.owner.getFullNamespace();
		if(! n.equals("")) {
			return n + "::" + this.getName();
		}
		
		return this.getName();
	}

	// ----------------------------------------------------------
	/**
	 * Gets the value associated with this name. Could be "" or null
	 * @return Value for this enum name
	 */
	public String getValue() {
		return value;
	}
	
}
