package org.cpgf.metagen.metawriter;

import java.util.*; //scturner

import org.cpgf.metagen.Config;
import org.cpgf.metagen.Util;
import org.cpgf.metagen.codewriter.CppWriter;
import org.cpgf.metagen.metadata.*;
import org.cpgf.metagen.metawriter.callback.OutputCallbackData;

//TODO
//const int metaModifierStatic = 1 << 0;
//const int metaModifierNoFree = 1 << 1;
//const int metaModifierVirtual = 1 << 2;
//const int metaModifierPureVirtual = 1 << 3;
//const int metaModifierTemplate = 1 << 4;
//const int metaModifierConst = 1 << 5;
//const int metaModifierVolitile = 1 << 6;


//TODO remove need for using namespaces
// use the namespace:: for the values

// -------------------------------------------------------------------------
/**
 *  Creates the cpgf code to register a CppClass with cpgf.
 *
 *  @author  scturner (documented)
 *  @version Jun 6, 2014
 */
public class MetaClassWriter {
    private CppClass cppClass;
    private CppWriter codeWriter;
    private Config config;
    private MetaInfo metaInfo;

    private String define;
    private String classType;

    private OutputCallbackData callbackData;

    // ----------------------------------------------------------
    /**
     * Creates the writer.
     * 
     * @param nConfig Configuration for the program
     * @param nMetaInfo High level metadata
     * @param nCodeWriter Writer for the output
     * @param nCppClass Class to output
     */
    public MetaClassWriter(Config nConfig, MetaInfo nMetaInfo, 
        CppWriter nCodeWriter, CppClass nCppClass) {
        //modified scturner
        this.initialize(nConfig, nMetaInfo, nCodeWriter, nCppClass, "_d", 
            MetaClassCodeGenerator.TEMPLATE_NAME + "::ClassType");
    }

    // ----------------------------------------------------------
    /**
     * Creates the writer.
     * 
     * @param nConfig Configuration for the program
     * @param nMetaInfo High level metadata
     * @param nCodeWriter Writer for the output
     * @param nCppClass Class to output
     * @param nDefine name of the variable used in the definition used by cpgf
     * @param nClassType Templated class type used by cpgf
     */
    public MetaClassWriter(Config nConfig, MetaInfo nMetaInfo, 
        CppWriter nCodeWriter, CppClass nCppClass, String nDefine, 
        String nClassType) {
        this.initialize(nConfig, nMetaInfo, nCodeWriter, nCppClass,
            nDefine, nClassType);
    }

    private void initialize(Config nConfig, MetaInfo nMetaInfo, 
        CppWriter nCodeWriter, CppClass nCppClass, String nDefine, 
        String nClassType) {
        this.cppClass = nCppClass;
        this.codeWriter = nCodeWriter;
        this.config = nConfig;
        this.metaInfo = nMetaInfo;
        this.define = nDefine;
        this.classType = nClassType;
    }

    private String getUniqueText()
    {
        if(this.cppClass.isGlobal()) {
            return "" + Util.getUniqueID(null);
        }

        return "" + Util.getUniqueID(this.cppClass.getLocation() 
            + this.cppClass.getFullQualifiedName());
    }

    private String getScopePrefix() {
        return this.getScopePrefix(null);
    }

    private String getScopePrefix(String prefix) {
        if(this.cppClass.isGlobal()) {
            return "";
        }
        if(prefix == null) {
            return this.classType + "::";
        }

        return prefix + this.classType + "::";		
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

    /**
     * Writes all of the metadata to the code so these classes can be 
     * registered.
     */
    public void write() {
        if(this.allowedMetaData(EnumCategory.Constructor)) {
            this.writeConstructors();
        }

        if(this.allowedMetaData(EnumCategory.Field)) {
            this.writeFields();
        }

        if(this.allowedMetaData(EnumCategory.Method)) {
            this.writeMethods();

            if (CopyFunctionList.getInstance().containsClass(this.cppClass)) {
                this.writeCopyMethod(); 
            }
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
        
       this.writeNonReflectedBaseClasses();
       this.writeDestructor();
    }

    private void writeConstructors() {
        if(this.cppClass.isGlobal()) {
            return;
        }
//      if(this.cppClass.isAbstract()) {
//          return;
//      }

        this.writeConstructorsBind();
    }

    // ----------------------------------------------------------
    /**
     * Writes the cpgf code for the constructors
     */
    public void writeConstructorsBind() {
        //scturner
        String action = WriterUtil.getReflectionAction(this.define, "_constructorEx");

        //added by scturner  
        // this does not seem to be creating default constructors as it should
        // so this adds them
        List<Constructor> constructors = this.cppClass.getConstructorList(); 
//        ClassTraits traits = this.cppClass.getTraits();
        if (constructors.size() == 0 /*&& !traits.isDefaultConstructorHidden()*/) {
            Constructor constructor = new Constructor();
            constructor.setVisibility(EnumVisibility.Public);
            constructors.add(constructor);
            String[] ns = cppClass.getFullNamespace().split("::");
            constructor.setNamespaces(Arrays.asList(ns));
        }

        for(Constructor item : constructors) {

            this.doCallback(item);
          
            if(this.shouldSkipItem(item) || this.cppClass.isAbstract()) {
                //add as non-reflected
                this.codeWriter.write(WriterUtil.getReflectionAction(
                    this.define, "_constructorNR"));
                this.codeWriter.write("(" 
                    + Util.quoteText(getConstructorSignature(item)) + ", ");

                this.codeWriter.writeLine(item.getVisibility().getIntValue() 
                    + ", " + Util.quoteText(item.getFullNamespace()) 
                    + ", " + EnumModifier.modifersToCpgfString(
                        EnumItemType.CONSTRUCTOR, item.getModifiers())
                    + ");");
            }
            else {
                		
                this.codeWriter.write(action + "<void * (");
                WriterUtil.writeParamList(this.codeWriter, 
                    item.getParameterList(), false);
                this.codeWriter.write(")>(" 
                    + WriterUtil.getPolicyText(item, false) + "");


                codeWriter.write(getTypeList(item) + ", " 
                                + Util.quoteText(item.getFullNamespace())
                                + ", " + EnumModifier.modifersToCpgfString(
                                    EnumItemType.CONSTRUCTOR, 
                                    item.getModifiers())
                                + ")"); 

                WriterUtil.writeDefaultParams(this.codeWriter, 
                    item.getParameterList());
            }
        }
    }

    private String getConstructorSignature(Constructor constr) {
        String str = constr.getLiteralName() + "(";
        
        for (int i = 0; i < constr.getParameterCount(); i++) {
            if (i != 0) {
                str += ", ";
            }
            str += constr.getParameterAt(i).getType().getLiteralType();
        }
        
        return str + ")";
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
                //add as non-reflected
                this.codeWriter.write(WriterUtil.getReflectionAction(
                    this.define, "_fieldNR"));
                this.codeWriter.write("(" + Util.quoteText(name) + ", ");
                //scturner
                this.codeWriter.writeLine(Util.quoteText(type + " " + name) 
                    + ", " + item.getVisibility().getIntValue() 
                    + ", " + Util.quoteText(item.getFullNamespace()) 
                    + ", " + EnumModifier.modifersToCpgfString(
                        EnumItemType.FIELD, item.getModifiers()) + ");");
            }
            else {
                // anonymous union
                if(name.indexOf('@') >= 0 || name.equals("")) { 
                    continue;
                }

                if(item.isBitField()) {
                    //scturner
                    CppField field = item;
                    if(WriterUtil.shouldGenerateBitfieldWrapper(this.config,
                        field)) {
                        this.codeWriter.writeLine(WriterUtil.
                            getReflectionAction(this.define, "_property") 
                            + "(" + Util.quoteText(name)
                            + ", " + Util.quoteText(type)
                            + ", &" + WriterUtil.getBitfieldWrapperGetterName(
                                field)
                            + ", &" + WriterUtil.getBitfieldWrapperSetterName(
                                field)
                            + ", cpgf::MakePolicy<" +
                            "cpgf::GMetaRuleGetterExplicitThis, " +
                            "cpgf::GMetaRuleSetterExplicitThis>()" + ");");
                    }
                }
                else {
                    this.codeWriter.write(action);
                    this.codeWriter.write("(" + Util.quoteText(name) + ", ");
                    //scturner
                    this.codeWriter.write(Util.quoteText(type) + ", " 
                                    + Util.quoteText(item.getFullNamespace())
                                    + ", " + EnumModifier.modifersToCpgfString(
                                        EnumItemType.FIELD, item.getModifiers())
                                        + ", ");
                    this.codeWriter.writeLine("&" + prefix + name 
                        + WriterUtil.getPolicyText(item) + ");");
                }
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
            boolean overload = (overloadCount != null 
                            && overloadCount.intValue() > 1);

            this.doCallback(item);

            if(this.shouldSkipItem(item)) {
              //add as non-reflected
                this.codeWriter.write(WriterUtil.getReflectionAction(
                    this.define, "_methodNR"));
                this.codeWriter.write("(" + Util.quoteText(name) + ", ");
                //scturner
                this.codeWriter.writeLine(Util.quoteText(
                    methodToSignature(item)) 
                    + ", " + item.getVisibility().getIntValue() 
                    + ", " + Util.quoteText(item.getFullNamespace()) 
                    + ", " + EnumModifier.modifersToCpgfString(
                        EnumItemType.METHOD, item.getModifiers())
                    + ");");
            }
            else {
                overload = overload || this.cppClass.isGlobal();

                WriterUtil.reflectMethod(this.codeWriter, this.define, 
                    scopePrefix, item, name, name, overload);
            }
        }
    }
    
    
    private String methodToSignature(CppMethod m) {
        String methodStr = m.getResultType().getLiteralType() + " "
                        + m.getPrimaryName() + "(";
              
        for (int i = 0; i < m.getParameterCount(); i++) {
            if (i != 0) {
                methodStr += ", ";
            }
            
            methodStr += m.getParameterAt(i).getType().getLiteralType();
        }
        
        methodStr += ")";
        
        return methodStr;
    }
    
    

    private void writeEnumerators() {
        String typePrefix = this.getScopePrefix("typename ");
        String prefix = this.getScopePrefix();
        String action = WriterUtil.getReflectionAction(this.define, "_enum");

        for(CppEnum item : this.cppClass.getEnumList()) {
            String name = item.getPrimaryName();

            this.doCallback(item);

            if(this.shouldSkipItem(item)) {
                //add as non-reflected
                this.codeWriter.write(WriterUtil.getReflectionAction(
                    this.define, "_enumNR"));
                this.codeWriter.write("(" + Util.quoteText(name) + ", ");
                
                this.codeWriter.writeLine(Util.quoteText(enumToSignature(item)) 
                    + ", " + item.getVisibility().getIntValue() 
                    + ", " + Util.quoteText(item.getFullNamespace()) 
                    + ", " + EnumModifier.modifersToCpgfString(
                        EnumItemType.ENUM, item.getModifiers())
                    + ");");
            }
            else {
                String typeName = typePrefix + name;

                if(name.indexOf('@') >= 0 || name.equals("")) {
                    name = "GlobalEnum_"  + this.config.projectID + "_" 
                                    + this.getUniqueText();
                    typeName = "long long";
                }

                this.codeWriter.writeLine(action + "<" + typeName + ">(" 
                                + Util.quoteText(name) + ", " 
                                + Util.quoteText(item.getFullNamespace())
                                + ", " + EnumModifier.modifersToCpgfString(
                                        EnumItemType.ENUM, item.getModifiers())
                                + ")");
                this.codeWriter.incIndent();
                for(EnumValue value : item.getValueList()) {
                    this.codeWriter.writeLine("._element(" 
                                    + Util.quoteText(value.getName()) + ", " 
                                    + prefix + value.getQualifiedName() + ")");
                }
                this.codeWriter.decIndent();
                this.codeWriter.writeLine(";");
            }
        }
    }

    
    private String enumToSignature(CppEnum e) {
        String enumStr = e.getPrimaryName() + " {";
        boolean first = true;
        
        for (EnumValue val : e.getValueList()) {
            if (!first) {
                first = false;
                enumStr += ", ";
            }
            
            enumStr += val.getName();
            if (val.getValue() != null && !val.getValue().isEmpty()) {
                enumStr += val.getValue() ;
            }
            enumStr += ",";
        }
        
        enumStr += "}";
        
        return enumStr;
    }
    
    
    private void writeConstants() {
        String action = WriterUtil.getReflectionAction(this.define, "_enum");

        if(this.cppClass.getConstantList().size() == 0) {
            return;
        }

        this.codeWriter.writeLine(action + "<long long>(" 
                        + Util.quoteText("GlobalDefine_" 
                        + this.config.projectID + "_" 
                        + this.getUniqueText()) + ")");
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

            this.codeWriter.writeLine("._element(" 
                            + Util.quoteText(item.getPrimaryName()) 
                            + ", " + item.getPrimaryName() + ")");
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



    private void writeCopyMethod() {
        CopyFunctionWriter copyWriter = new CopyFunctionWriter(cppClass);
        copyWriter.writeNamedWrapperReflectionCode(this.codeWriter, define);
    }

    private void writeClasses() {
        String action = WriterUtil.getReflectionAction(this.define, "_class");

        for(DeferClass deferClass : this.cppClass.getClassList()) {
            CppClass item = deferClass.getCppClass();
            this.doCallback(item);

            if(this.shouldSkipItem(item)) {
                //add as non-reflected
                this.codeWriter.write(WriterUtil.getReflectionAction(
                    this.define, "_innerClassNR"));
                this.codeWriter.write("(" 
                    + Util.quoteText(item.getLiteralName()) + ", ");               
                this.codeWriter.writeLine(item.getVisibility().getIntValue() 
                    + ", " + Util.quoteText(item.getFullNamespace()) 
                    + ", " + EnumModifier.modifersToCpgfString(
                        EnumItemType.CLASS, item.getModifiers())
                    + ");");
            }
            else {
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
    
    
    private void writeNonReflectedBaseClasses() {
        
        for(DeferClass deferClass : cppClass.getBaseClassList()) {
            if (deferClass.getVisibility() != EnumVisibility.Public
                            || deferClass.getCppClass() == null) {
                String baseName = deferClass.getName();
                
               //TODO handle templates             
                this.codeWriter.write(WriterUtil.getReflectionAction(
                    this.define, "_baseNR"));
                this.codeWriter.write("(" 
                    + Util.quoteText(baseName) + ", ");               
                this.codeWriter.writeLine(deferClass.getVisibility().getIntValue() 
                    + ", " + Util.quoteText("")
                    + ", " + EnumModifier.modifersToCpgfString(
                        EnumItemType.BASECLASS, deferClass.getModifiers())
                    + ");");                   
            }
        }           
    }
    
    
    
    private void writeDestructor() {
        if (this.cppClass.getDestructor() != null) {
            Destructor destruct = this.cppClass.getDestructor();
            String name = "~" + this.cppClass.getLiteralName();
            
          //add as non-reflected
            this.codeWriter.write(WriterUtil.getReflectionAction(
                this.define, "_methodNR"));
            this.codeWriter.write("(" + Util.quoteText(name) + ", ");
            //scturner
            this.codeWriter.writeLine(Util.quoteText(
                "void " + name + "()") 
                + ", " + destruct.getVisibility().getIntValue() 
                + ", " + Util.quoteText(destruct.getFullNamespace()) 
                + ", " + EnumModifier.modifersToCpgfString(
                        EnumItemType.DESTRUCTOR, destruct.getModifiers())
                + ");");                 
        }
    }
    

}
