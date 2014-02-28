package org.cpgf.metagen.metawriter;

import java.util.*; //scturner

import org.cpgf.metagen.Config;
import org.cpgf.metagen.Util;
import org.cpgf.metagen.codewriter.CppWriter;
import org.cpgf.metagen.metadata.*;
import org.cpgf.metagen.metawriter.callback.OutputCallbackData;


public class MetaClassWriter {
	private CppClass cppClass;
	private CppWriter codeWriter;
	private Config config;
	private MetaInfo metaInfo;
	
	private String define;
	private String classType;

	private OutputCallbackData callbackData;
	
	public MetaClassWriter(Config config, MetaInfo metaInfo, CppWriter codeWriter, CppClass cppClass) {
		//modified scturner
		this.initialize(config, metaInfo, codeWriter, cppClass, "_d", 
				MetaClassCodeGenerator.TEMPLATE_NAME + "::ClassType");
	}

	public MetaClassWriter(Config config, MetaInfo metaInfo, CppWriter codeWriter, CppClass cppClass, String define, String classType) {
		this.initialize(config, metaInfo, codeWriter, cppClass, define, classType);
	}

	private void initialize(Config config, MetaInfo metaInfo, CppWriter codeWriter, CppClass cppClass, String define, String classType) {
		this.cppClass = cppClass;
		this.codeWriter = codeWriter;
		this.config = config;
		this.metaInfo = metaInfo;
		this.define = define;
		this.classType = classType;
	}
	
	private String getUniqueText()
	{
		if(this.cppClass.isGlobal()) {
			return "" + Util.getUniqueID(null);
		}
		else {
			return "" + Util.getUniqueID(this.cppClass.getLocation() + this.cppClass.getFullQualifiedName());
		}
	}
	
	private String getScopePrefix() {
		return this.getScopePrefix(null);
	}
	
	private String getScopePrefix(String prefix) {
		if(prefix == null) {
			prefix = "";
		}

		if(this.cppClass.isGlobal()) {
			return "";
		}
		else {
			return prefix + this.classType + "::";
		}
	}

	private void doCallback(Item item) {
		this.callbackData = this.metaInfo.getCallbackClassMap().getData(item);
	}

	private boolean skipItem() {
		return this.callbackData.isSkipBind();
	}

	private boolean allowedMetaData(EnumCategory category) {
		for(EnumCategory c : this.config.allowedMetaData) {
			if(c == category) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean shouldSkipItem(Item item)
	{
		return this.skipItem() || ! Util.allowMetaData(this.config, item);
	}

	private boolean shouldSkipItem(ParameteredItem item)
	{
		return this.skipItem() || ! Util.allowMetaData(this.config, item) || item.isTemplate();
	}
	
	public void write() {
		if(this.allowedMetaData(EnumCategory.Constructor)) {
			this.writeConstructors();
		}
		
		if(this.allowedMetaData(EnumCategory.Field)) {
			this.writeFields();
		}
		
		if(this.allowedMetaData(EnumCategory.Method)) {
			this.writeMethods();
		}
		
		if(this.allowedMetaData(EnumCategory.Enum)) {
			this.writeEnumerators();
		}
		
		if(this.allowedMetaData(EnumCategory.Constant)) {
			this.writeConstants();
		}
		
		if(this.allowedMetaData(EnumCategory.Operator)) {
			this.writeOperators();
		}

		if(this.allowedMetaData(EnumCategory.Class)) {
			this.writeClasses();
		}
	}
	
	private void writeConstructors() {
		if(this.cppClass.isGlobal()) {
			return;
		}
		if(this.cppClass.isAbstract()) {
			return;
		}
		
		this.writeConstructorsBind();
	}

	public void writeConstructorsBind() {
		//scturner
		String action = WriterUtil.getReflectionAction(this.define, "_constructorEx");

		//added by scturner  
		// this does not seem to be creating default constructors as it should
		// so this adds them
		List<Constructor> constructors = this.cppClass.getConstructorList(); 
		ClassTraits traits = this.cppClass.getTraits();
		if (constructors.size() == 0 && !traits.isDefaultConstructorHidden()) {
			Constructor constructor = new Constructor();
			constructor.setVisibility(EnumVisibility.Public);
			constructors.add(constructor);
		}

		for(Constructor item : constructors) {
		//for(Constructor item : this.cppClass.getConstructorList()) {
			this.doCallback(item);

//			Util.trace("Constructor: " + cppClass.getLiteralName() + " " +
//			  skipItem() + " " + ! Util.allowMetaData(this.config, item) + " " +
//			                item.getVisibility() + " " + item.isTemplate());
			if(this.shouldSkipItem(item)) {
				continue;
			}
			
//			Util.trace("Written: " + cppClass.getLiteralName());			
			this.codeWriter.write(action + "<void * (");
			WriterUtil.writeParamList(this.codeWriter, item.getParameterList(), false);
			this.codeWriter.write(")>(" + WriterUtil.getPolicyText(item, false) + "");


			codeWriter.write(getTypeList(item) + ")");

			WriterUtil.writeDefaultParams(this.codeWriter, item.getParameterList());
		}
	}

	//scturner
	private String getTypeList(Constructor item) {
		//list of types
		//leaving room for the return type even though there is not one
		String types = "\"";
		for (Parameter p : item.getParameterList()) {
			types += "," + p.getType().getLiteralType();
		}                

		types += "\"";

		return types;
	}
	
	private void writeFields() {
		String prefix = this.getScopePrefix();
		//scturner
		String action = WriterUtil.getReflectionAction(this.define, "_fieldEx");

		for(CppField item : this.cppClass.getFieldList()) {
			String name = item.getPrimaryName();
			//scturner
			String type = item.getType().getLiteralType();
			
			this.doCallback(item);
			
			if(this.shouldSkipItem(item)) {
				continue;
			}

			if(name.indexOf('@') >= 0 || name.equals("")) { // anonymous union
				continue;
			}

			if(item.isBitField()) {
				//scturner
				CppField field = item;
				if(WriterUtil.shouldGenerateBitfieldWrapper(this.config, field)) {
					this.codeWriter.writeLine(WriterUtil.getReflectionAction(this.define, "_property") + "(" + Util.quoteText(name)
							+ ", " + Util.quoteText(type)
							+ ", &" + WriterUtil.getBitfieldWrapperGetterName(field)
							+ ", &" + WriterUtil.getBitfieldWrapperSetterName(field)
							+ ", cpgf::MakePolicy<cpgf::GMetaRuleGetterExplicitThis, cpgf::GMetaRuleSetterExplicitThis>()" + ");");
				}
			}
			else {
				this.codeWriter.write(action);
				this.codeWriter.write("(" + Util.quoteText(name) + ", ");
				//scturner
				this.codeWriter.write(Util.quoteText(type) + ", ");
				this.codeWriter.writeLine("&" + prefix + name 
						+ WriterUtil.getPolicyText(item) + ");");
			}
		}
	}

	private void writeMethods() {
		String scopePrefix = this.getScopePrefix();

		HashMap<String, Integer> methodOverload = new HashMap<String, Integer>();

		for(CppMethod item : this.cppClass.getMethodList()) {
			String name = item.getPrimaryName();
			Integer count = methodOverload.get(name);
			if(count == null) {
				count = new Integer(0);
			}
			count = new Integer(count.intValue() + 1);
			methodOverload.put(name, count);
		}

		for(CppMethod item : this.cppClass.getMethodList()) {
			String name = item.getPrimaryName();
			Integer overloadCount = methodOverload.get(name);
			boolean overload = (overloadCount != null && overloadCount.intValue() > 1);

			this.doCallback(item);
			
			if(this.shouldSkipItem(item)) {
				continue;
			}
			
			overload = overload || this.cppClass.isGlobal();
			
			WriterUtil.reflectMethod(this.codeWriter, this.define, scopePrefix, item, name, name, overload);
		}
	}

	private void writeEnumerators() {
		String typePrefix = this.getScopePrefix("typename ");
		String prefix = this.getScopePrefix();
		String action = WriterUtil.getReflectionAction(this.define, "_enum");

		for(CppEnum item : this.cppClass.getEnumList()) {
			String name = item.getPrimaryName();
			
			this.doCallback(item);

			if(this.shouldSkipItem(item)) {
				continue;
			}

			String typeName = typePrefix + name;

			if(name.indexOf('@') >= 0 || name.equals("")) {
				name = "GlobalEnum_"  + this.config.projectID + "_" + this.getUniqueText();
				typeName = "long long";
			}

			this.codeWriter.writeLine(action + "<" + typeName + ">(" + Util.quoteText(name) + ")");
			this.codeWriter.incIndent();
				for(EnumValue value : item.getValueList()) {
					this.codeWriter.writeLine("._element(" + Util.quoteText(value.getName()) + ", " + prefix + value.getQualifiedName() + ")");
				}
			this.codeWriter.decIndent();
			this.codeWriter.writeLine(";");
		}
	}

	private void writeConstants() {
		String action = WriterUtil.getReflectionAction(this.define, "_enum");

		if(this.cppClass.getConstantList().size() == 0) {
			return;
		}

		this.codeWriter.writeLine(action + "<long long>(" + Util.quoteText("GlobalDefine_" + this.config.projectID + "_" + this.getUniqueText()) + ")");
		this.codeWriter.incIndent();

		for(Constant item : this.cppClass.getConstantList()) {
			this.doCallback(item);
			
			if(this.shouldSkipItem(item)) {
				continue;
			}
			
			String value = item.getValue();
			if(value == null || value.equals("")) {
				continue;
			}
			
			this.codeWriter.writeLine("._element(" + Util.quoteText(item.getPrimaryName()) + ", " + item.getPrimaryName() + ")");
		}
		
		this.codeWriter.decIndent();
		this.codeWriter.writeLine(";");
	}

	private void writeOperators() {
		for(Operator item : this.cppClass.getOperatorList()) {
			this.doCallback(item);
			
			if(this.shouldSkipItem(item)) {
				continue;
			}
			
			OperatorWriter opWriter = new OperatorWriter(this.metaInfo, item);
			opWriter.writeReflectionCode(this.codeWriter, this.define);
			
			if(WriterUtil.shouldGenerateOperatorWrapper(this.metaInfo, item)) {
				opWriter.writeNamedWrapperReflectionCode(this.codeWriter, define);
			}
		}
	}

	private void writeClasses() {
		String action = WriterUtil.getReflectionAction(this.define, "_class");

		for(DeferClass deferClass : this.cppClass.getClassList()) {
			CppClass item = deferClass.getCppClass();
			this.doCallback(item);
			
			if(this.shouldSkipItem(item)) {
				continue;
			}
			
			this.codeWriter.beginBlock();
			
			WriterUtil.defineMetaClass(this.config, this.codeWriter, item, "_nd", "declare");
			MetaClassWriter classWriter = new MetaClassWriter(
				this.config,
				this.metaInfo,
				this.codeWriter,
				item,
				"_nd",
				item.getFullQualifiedName()
			);
			classWriter.write();
			this.codeWriter.writeLine(action + "(_nd);");
			
			this.codeWriter.endBlock();
		}
	}

}
