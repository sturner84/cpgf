package org.cpgf.metagen.metawriter;

//import java.util.*;

import org.cpgf.metagen.*;
import org.cpgf.metagen.codewriter.*;
import org.cpgf.metagen.metadata.*;

// -------------------------------------------------------------------------
/**
 *  Creates a method that makes a copy of object of a specific type 
 *  and returns that copy as a GVariant.
 *  
 *  The problem being solved here occurs when reflected objects are being 
 *  returned from methods/functions.  If the object is passed by reference or
 *  by pointer, everything is fine.  However, if it is just returned normally,
 *  the original variable may go out of scope before it is used.
 *  
 *  Since the objects are being passed around as void * and the types of the
 *  objects are not known a priori, there needs to be a way to 
 *  safely make a copy of the object. (Since the variable is being
 *  returned normally, it is safe to assume that a copy can be made of it.)  
 *  
 *  This class creates function with a predictable signature that can be 
 *  discovered by just knowing the class name being reflected.  The method will
 *  make a copy of the object and return it as a GVariant.
 *
 *  @author  scturner
 *  @version May 28, 2014
 */
public class CopyFunctionWriter
{
    private CppClass item;
    
    private static final String COPY_METHOD_PREFIX = "cOpYwRaPpEr_";
    private static final String COPY_METHOD_TEMPLATE = "_template_";

 
    // ----------------------------------------------------------
    /**
     * @param metaInfo Info about the operator
     * @param nItem Operator being reflected.
     */
    public CopyFunctionWriter(CppClass nItem) {
      item = nItem;
    }

    private void writeSelf(CppWriter codeWriter, boolean includeName) {
//        if(this.item.isConst()) {
//            codeWriter.write("const ");
//        }
        codeWriter.write(item.getQualifiedName());
        if(item.getTemplateParameterList() != null
                        && item.getTemplateParameterList().size() > 0) {
            codeWriter.write("<");
            codeWriter.write(Util.getParameterText(
                item.getTemplateParameterList(), false, true));
            codeWriter.write(">");
        }
        codeWriter.write(" *");
        if(includeName) {
            codeWriter.write(" self");
        }
    }
    
    private String getCopyMethodName() {
        String name = COPY_METHOD_PREFIX + item.getQualifiedName();
      
        if(item.getTemplateParameterList() != null
                        && item.getTemplateParameterList().size() > 0) {
           name += COPY_METHOD_TEMPLATE 
                           + item.getTemplateParameterList().size();
        }
    
        return name;
    }
    
    
    // ----------------------------------------------------------
    /**
     * Creates the function that copies a reflected object safely.
     *  
     * @param codeWriter Writer to output the code to.
     */
    public void writeNamedWrapperFunctionCode(CppWriter codeWriter) {


        if(item.getTemplateParameterList() != null
                        && item.getTemplateParameterList().size() > 0) {
            codeWriter.write("template <");
            codeWriter.write(Util.getParameterText(
                item.getTemplateParameterList(), true, true));
            codeWriter.writeLine(">");
        }
        codeWriter.write("inline " + item.getQualifiedName() + " * ");
        codeWriter.write(getCopyMethodName() + "(");
        this.writeSelf(codeWriter, true);           
        codeWriter.write(") ");

        codeWriter.beginBlock();
//        codeWriter.writeLine("return cpgf::createVariant<true>(*self, true);");
        codeWriter.writeLine("return new " + item.getQualifiedName() 
            + "(*self);");
        codeWriter.endBlock();

    }

  
    // ----------------------------------------------------------
    /**
     * Reflects the function created by writeNamedWrapperFunctionCode.
     * 
     * @param codeWriter Writer to output the code to.
     * @param define Name of cpgf class storing info about the method being
     *  reflected.
     */
    public void writeNamedWrapperReflectionCode(CppWriter codeWriter, String define) {

        String methodName = getCopyMethodName();
        if(item.getTemplateParameterList() != null
                        && item.getTemplateParameterList().size() > 0) {
            methodName = methodName + "<"
                    + Util.getParameterText(item.getTemplateParameterList(),
                        false, true) + ">";
        }

        codeWriter.write(WriterUtil.getReflectionAction(define, "_methodEx"));
        codeWriter.write("(" + Util.quoteText(methodName) + ", ");
        //scturner
        codeWriter.write(getTypeList() + ", ");
        codeWriter.write(Util.quoteText(item.getFullNamespace()) + ", ");
        codeWriter.write(EnumModifier.modifersToCpgfString(
            EnumItemType.METHOD, item.getModifiers()) + ", ");
        
        codeWriter.write("(" + item.getQualifiedName() + " * (*) (");
        this.writeSelf(codeWriter, false);
//        codeWriter.write(", ");
//        WriterUtil.writeParamList(codeWriter, this.item.getParameterList(),
//            false);
//        codeWriter.write(", ");
//        codeWriter.write(this.getArraySetterValueType());
        codeWriter.write(")");
        codeWriter.write(")");
        codeWriter.write("&" + methodName);
//        item.setIsWrapping(true);
//        String ruleText = WriterUtil.getPolicyRulesText(this.item);
////        item.setIsWrapping(false);
//        if(ruleText.length() > 0) {
//            ruleText = ruleText + ", ";
//        }
//        ruleText = ruleText + "cpgf::GMetaRuleExplicitThis";
//        //scturner
//        codeWriter.write(", cpgf::MakePolicy<" + ruleText + " >()" + ")");
        codeWriter.writeLine(");");
//      WriterUtil.writeDefaultParams(codeWriter, this.item.getParameterList());
    }

    //scturner
    private String getTypeList() {
        //list of types
        String types = "\"" + item.getQualifiedName() + " *,"
                        + item.getQualifiedName() + " *\"";
//        for (Parameter p : item.getParameterList()) {
//            types += "," + p.getType().getLiteralType();
//        }                

//        types += "\"";

        return types;
    }


}
