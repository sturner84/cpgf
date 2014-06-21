package org.cpgf.metagen.metadata;

import java.util.*;

import org.cpgf.metagen.Util;


public class DeferClass {
	private String name;
	private EnumVisibility visibility;
	private CppClass cppClass;
	private boolean isVirtual;
	private boolean isTemplate;
	
	public DeferClass(String name, EnumVisibility visibility, boolean virtual) {
		this.name = name;
		this.visibility = visibility;
		this.isVirtual = virtual;
	}
	
	public DeferClass(CppClass cppClass) {
		this.cppClass = cppClass;
		this.visibility = EnumVisibility.Public;
	}
	
	public void resolve(List<CppClass> classList) {
		if(this.cppClass == null) {
			this.cppClass = (CppClass)(Util.findItemByName(classList, this.name));
		}

		if(this.cppClass != null) {
			this.cppClass.setVisibility(this.getVisibility());
		}
	}

	public CppClass getCppClass() {
		return cppClass;
	}

	public EnumVisibility getVisibility() {
		return visibility;
	}

	public String getName() {
		return name;
	}

	
	//scturner added to support virtual base classes
    // ----------------------------------------------------------
    /**
     * Determines if the class is virtual
     * @return true if it is virtual
     */
    public boolean isVirtual()
    {
        return this.isVirtual;
    }

    //scturner added to support virtual base classes
    // ----------------------------------------------------------
    /**
     * Sets if this class is virtual
     * @param isVirtual True if the class should be virtual
     */
    public void setVirtual( boolean isVirtual )
    {
        this.isVirtual = isVirtual;
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
    public List<EnumModifier> getModifiers() {
        LinkedList<EnumModifier> list = new LinkedList<EnumModifier>();
        
        if (isVirtual()) {
            list.add(EnumModifier.Virtual);
        }
        
        if (isTemplate()) {
            list.add(EnumModifier.Template);
        }
        
        
        return list;
    }
}
