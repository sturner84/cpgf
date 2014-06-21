package org.cpgf.metagen.metadata;

import java.util.*;


public class Operator extends CppInvokable {
	private String self;
	private boolean isWrapping;
	private boolean isExtern;
    	
	public Operator(String operator, CppType resultType) {
		super(EnumCategory.Operator, operator, resultType);
	}

	public String getOperator() {
		return this.getLiteralName();
	}

	public boolean isFunctor() {
		return this.getOperator().equals("()");
	}
	
	public boolean isArray() {
		return this.getOperator().equals("[]");
	}
	
	public boolean isTypeConverter() {
		return this.getOperator().matches(".*\\w+.*");
	}
	
	public boolean hasSelf() {
		return !this.isFunctor() && !this.isStatic();
	}

	@Override
	protected int getParameterPolicyRuleStartIndex() {
		if(this.hasSelf()) {
			return 1;
		}
		else {
			// When isWrapping, for functor, we must start from 1 because the first parameter is the explicit this.
			if(this.isWrapping && this.isFunctor()) {
				return 1;
			}
			return 0;
		}
	}

	public String getSelf() {
		return self;
	}

	public void setSelf(String self) {
		this.self = self;
	}

	public void setIsWrapping(boolean isWrapping) {
		this.isWrapping = isWrapping;
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
