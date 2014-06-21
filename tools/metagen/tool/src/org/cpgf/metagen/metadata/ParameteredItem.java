package org.cpgf.metagen.metadata;

import java.util.*;

public class ParameteredItem extends Item {
	private List<Parameter> parameterList;
	private List<Parameter> templateParameterList;
	private boolean isVirtual;
	private boolean isPureVirtual;
	private boolean isTemplate;
	private boolean isInline;

	public ParameteredItem(EnumCategory category, String name) {
		super(category, name);

		this.parameterList = new ArrayList<Parameter>();
		this.templateParameterList = new ArrayList<Parameter>();
	}

	public boolean hasParameter() {
		return this.parameterList.size() > 0;
	}
	
	public int getParameterCount() {
		return this.parameterList.size();
	}
	
	public Parameter getParameterAt(int index) {
		return this.parameterList.get(index);
	}

	public List<Parameter> getParameterList() {
		return parameterList;
	}

	public List<Parameter> getTemplateParameterList() {
		return templateParameterList;
	}
	
	public boolean isVirtual() {
		return isVirtual || this.isPureVirtual();
	}

	public void setVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	public boolean isPureVirtual() {
		return isPureVirtual;
	}

	public void setPureVirtual(boolean isPureVirtual) {
		this.isPureVirtual = isPureVirtual;
	}

	public boolean isTemplate() {
		return isTemplate || this.templateParameterList.size() > 0;
	}
	
	public void setTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}
	
	public boolean isInline() {
        return isInline;
    }

    public void setInline(boolean isInline) {
        this.isInline = isInline;
    }


	public int getNonDefaultParameterCount() {
		int count = 0;
		
		for(Parameter param : this.parameterList) {
			if(param.hasDefaultValue()) {
				break;
			}
			else {
				++count;
			}
		}
		
		return count;
	}

	public boolean removeParameterDefaultValue() {
		for(Parameter param : this.parameterList) {
			if(param.hasDefaultValue()) {
				param.clearDefaultValue();
				
				return true;
			}
		}

		return false;
	}

	public void removeParameter(String paramName) {
		for(int i = 0; i < this.parameterList.size(); ++i) {
			Parameter param = this.parameterList.get(i);
			if(param.getName().equals(paramName)) {
				this.parameterList.remove(i);
				
				break;
			}
		}
	}

	public void removeParameterAt(int index) {
		this.parameterList.remove(index);
	}

	public void setParameterDefaultValue(String paramName, String value) {
		for(int i = 0; i < this.parameterList.size(); ++i) {
			Parameter param = this.parameterList.get(i);
			if(param.getName().equals(paramName)) {
				param.setDefaultValue(value);
				
				break;
			}
		}
	}

	public void setParameterDefaultValueAt(int index, String value) {
		if(index < 0) {
			index += this.parameterList.size();
		}
		Parameter param = this.parameterList.get(index);
		param.setDefaultValue(value);
	}
	
	@Override
	public void replaceInType(String pattern, String substitute)
	{
		super.replaceInType(pattern, substitute);
		
		for(Parameter param : this.parameterList) {
			param.getType().setLiteralType(param.getType().getLiteralType().replaceAll(pattern, substitute));
		}
	}
	
	public boolean hasCallbackParameter() {
		for(Parameter param : this.parameterList) {
			if(param.getCallback() != null) {
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
        List<EnumModifier> list = super.getModifiers();
        
        if (isVirtual()) {
            list.add(EnumModifier.Virtual);
        }
        
        if (isPureVirtual()) {
            list.add(EnumModifier.PureVirtual);
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
