package org.cpgf.metagen.metadata;

import java.util.*;

public class Constructor extends CppInvokable {
	private boolean isExplicit;

	public Constructor() {
		super(EnumCategory.Constructor, "", null);
	}

	public boolean isExplicit() {
		return isExplicit;
	}

	public void setExplicit(boolean isExplicit) {
		this.isExplicit = isExplicit;
	}
	
	public boolean isDefaultConstructor() {
		return ! this.hasParameter();
	}

	public boolean isCopyConstructor() {
		if(this.getParameterList().size() == 1 && this.getOwner() != null) {
			CppType type = this.getParameterList().get(0).getType();
			return type.isConstValueReference() && type.getLiteralType().indexOf(this.getOwner().getPrimaryName()) >= 0;
		}
		
		return false;		
	}
	
	public boolean isTypeConverter() {
		if(! this.isCopyConstructor()) {
			if(this.getNonDefaultParameterCount() == 1 && this.getOwner() != null) {
				return true;
			}
		}

		return false;
	}
	
	// ----------------------------------------------------------
    /**
     * Gets a list of modifiers set for this Item
     * @return List of EnumModifiers will all of the modifiers for this Item
     */
    @Override
    public List<EnumModifier> getModifiers() {
        List<EnumModifier> list =  new LinkedList<EnumModifier>();
        
        if (isExplicit()) {
            list.add(EnumModifier.Explicit);
        }
        
        if (isTemplate()) {
            list.add(EnumModifier.Template);
        }
        
        if (isInline()) {
            list.add(EnumModifier.Inline);
        }
        
        return list;
    }
}
