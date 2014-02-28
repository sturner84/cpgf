package org.cpgf.metagen.metadata;

public class Destructor extends Item {
	//scturner added to support virtual destructors
	private boolean isVirtual;

	public Destructor() {
		super(EnumCategory.Destructor, "");
		isVirtual = false;
	}

	//scturner added to support virtual destructors
	public boolean isVirtual()
	{
		return this.isVirtual;
	}

	//scturner added to support virtual destructors
	public void setVirtual( boolean isVirtual )
	{
		this.isVirtual = isVirtual;
	}
}
