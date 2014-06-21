package org.cpgf.metagen.metadata;

import java.util.*;

// -------------------------------------------------------------------------
/**
 *  Represents a Descructor
 *
 *  @author  scturner
 *  @version Jun 7, 2014
 */
public class Destructor extends Item {
	//scturner added to support virtual destructors
	private boolean isVirtual;
	private boolean isInline;

	// ----------------------------------------------------------
	/**
	 * Creates destructor
	 */
	public Destructor() {
		super(EnumCategory.Destructor, "");
		isVirtual = false;
	}

	//scturner added to support virtual destructors
	// ----------------------------------------------------------
	/**
	 * Determines if the destructor is virtual
	 * @return true if it is virtual
	 */
	public boolean isVirtual()
	{
		return this.isVirtual;
	}

	//scturner added to support virtual destructors
	// ----------------------------------------------------------
	/**
	 * Sets if this destructor is virtual
	 * @param isVirtual True if the destructor should be virtual
	 */
	public void setVirtual( boolean isVirtual )
	{
		this.isVirtual = isVirtual;
	}

	/**
	 * Determines if the destructor is inline
	 * @return true if it is inline
	 */
	public boolean isInline() {
	    return isInline;
	}

	/**
	 * Sets if this destructor is inline
	 * @param isInline True if the destructor should be inline
	 */
	public void setInline(boolean isInline) {
	    this.isInline = isInline;
	}

	// ----------------------------------------------------------
    /**
     * Gets a list of modifiers set for this Item
     * @return List of EnumModifiers will all of the modifiers for this Item
     */
    @Override
    public List<EnumModifier> getModifiers() {
        List<EnumModifier> list = new LinkedList<EnumModifier>();
        
        if (isVirtual()) {
            list.add(EnumModifier.Virtual);
        }
        
        if (isInline()) {
            list.add(EnumModifier.Inline);
        }
        
        return list;
    }
}
