package org.cpgf.metagen.metadata;

public class CppField extends Item {
	private CppType type;
	private int bitField;
	//added by saturner
	private String initializer;

	public CppField(String name, CppType type) {
		super(EnumCategory.Field, name);
		
		this.type = type;
	}

	public CppType getType() {
		return type;
	}
	
	public boolean isBitField() {
		return this.bitField > 0;
	}

	public int getBitField() {
		return bitField;
	}

	public void setBitField(int bitFields) {
		this.bitField = bitFields;
	}

	//scturner
	public String getInitializer() {
		return initializer;
	}

	public void setInitializer(String init) {
		initializer = init;
	}

}
