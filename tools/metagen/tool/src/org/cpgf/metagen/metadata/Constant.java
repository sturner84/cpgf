package org.cpgf.metagen.metadata;

import java.util.*;

public class Constant extends Item {
	private String value;

	public Constant(String name, String value) {
		super(EnumCategory.Constant, name);
		
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	

}
