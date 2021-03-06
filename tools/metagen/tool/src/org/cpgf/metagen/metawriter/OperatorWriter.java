package org.cpgf.metagen.metawriter;

import java.util.List;
//import java.util.regex.*;

import org.cpgf.metagen.Util;
import org.cpgf.metagen.codewriter.CppWriter;
import org.cpgf.metagen.metadata.*;

// -------------------------------------------------------------------------
/**
 *  Writes the reflection code for an operator.  
 *  
 *  This class has been gutted to only support the [] operator (setting values).
 *  Operators are only reflected as methods.
 *
 *  @author  Schnook
 *  @version May 28, 2014
 */
public class OperatorWriter {
	private MetaInfo metaInfo;
	private Operator item;
//	private int realParamCount;
//	private boolean isIncOrDec;

	private List<Parameter> templateDependentParameterList;

//	private static final String CLEAN_OPNAME = "(\\s+)|(&)|(\\*)";
//	private static final String CLEAN_OPNAME_REPLACE = "($1_)($2ref)($3ptr)";
	
	// ----------------------------------------------------------
	/**
	 * @param metaInfo Info about the operator
	 * @param item Operator being reflected.
	 */
	public OperatorWriter(MetaInfo metaInfo, Operator item) {
		this.metaInfo = metaInfo;
		this.item = item;

		this.initialize();
	}

	private void initialize() {
//		boolean isStatic = item.isStatic();
//		this.realParamCount = this.item.getParameterList().size();

//		String op = item.getOperator();
//		if(! isStatic) {
//			++this.realParamCount;
//		}

//		if(op.equals("->")) {
//			this.realParamCount = 2;
//		}
//
//		this.isIncOrDec = (op.equals("++") || op.equals("--"));

		//		if(this.item.getOwner().isTemplate()) {
		//			for(Parameter templateParam : this.item.getOwner().getTemplateParameterList()) {
		//				for(Parameter param : this.item.getParameterList()) {
		//					if(param.getType().getParsedType().matchSymbol(templateParam.getName())) {
		//						if(this.templateDependentParameterList == null) {
		//							this.templateDependentParameterList = new ArrayList<Parameter>();
		//						}
		//						this.templateDependentParameterList.add(templateParam);
		//					}
		//				}
		//			}
		//		}
		if(this.item.getOwner().isTemplate()) {
			this.templateDependentParameterList = this.item.getOwner().getTemplateParameterList();
		}
	}

	// ----------------------------------------------------------
	/**
	 * 
	 * Reflects an operator. No longer being used.
	 * 
	 * @param codeWriter Writer to output the code to.
	 * @param define Name of cpgf class storing info about the method being
     *  reflected.
	 */
	public void writeReflectionCode(CppWriter codeWriter, String define) {
//		//scturner
//		String action = WriterUtil.getReflectionAction(define, "_operatorEx");
//
//		String op = item.getOperator();
//
//		boolean isTypeConvert = this.item.isTypeConverter();
//		String opText = "";
//
//		String self = item.getSelf();
//		if(self == null) {
//			self = "cpgf::GMetaSelf";
//		}
//
//		if(isTypeConvert) {
//			codeWriter.write(action + "<" + op + " (" + self + ")>(");
//			opText = "H()";
//		}
//		else {
//			codeWriter.write(action + "<" + item.getResultType().getLiteralType() + " (*)(");
//
//			boolean hasSelf = item.hasSelf();
//
//			if(hasSelf) {
//				if(item.isConst()) {
//					codeWriter.write("const " + self + " &");
//				}
//				else {
//					codeWriter.write(self);
//				}
//			}
//
//			if(this.isIncOrDec) {
//			}
//			else {
//				if(item.hasParameter() && hasSelf) {
//					codeWriter.write(", ");
//				}
//				WriterUtil.writeParamList(codeWriter, item.getParameterList(), false);
//			}
//			codeWriter.write(")>(");
//			if(item.isFunctor()) {
//				opText = "H(H)";
//			}
//			else if(op.equals("[]")) {
//				opText = "H[0]";
//			}
//			else if(isTypeConvert && this.realParamCount == 1) { // type convert T()
//				opText = "H()";
//			}
//			else {
//				if(this.realParamCount == 2) {
//					if(this.isIncOrDec) {
//						opText = "H" + op;
//					}
//					else {
//						opText = "H " + op + " H";
//					}
//				}
//				else if(this.realParamCount == 1) {
//					opText = op + "H";
//				}
//				else {
//				}
//			}
//		}
//
//		opText = opText.replaceAll("\\bH\\b", "mopHolder");
//		if(op.equals(",")) {
//			opText = "(" + opText + ")"; // one more pair of brackets to avoid compile error
//		}
//		//scturner
//		codeWriter.write(getTypeList() + ",");
//		codeWriter.write(opText);
//		codeWriter.write(WriterUtil.getPolicyText(item) + ")");
//
//		WriterUtil.writeDefaultParams(codeWriter, item.getParameterList());
	}

	private void writeSelf(CppWriter codeWriter, boolean includeName) {
		if(this.item.isConst()) {
			codeWriter.write("const ");
		}
		codeWriter.write( this.item.getOwner().getQualifiedName());
		if(this.templateDependentParameterList != null) {
			codeWriter.write("<");
			codeWriter.write(Util.getParameterText(this.templateDependentParameterList, false, true));
			codeWriter.write(">");
		}
		codeWriter.write(" *");
		if(includeName) {
			codeWriter.write(" self");
		}
	}

	private boolean shouldGenerateArraySetter() {
		return this.item.isArray() && this.item.getResultType().isNonConstValueReference();
	}

	
	private String getArrayMethodName(Operator op) {
	    String name = op.getResultType().getLiteralType() + "_array";
	  
	    for (Parameter p : op.getParameterList()) {
	        name += "_" + p.getType().getLiteralType();
	    }
	    
//	    Pattern pattern = Pattern.compile(CLEAN_OPNAME);        
//        Matcher matcher = pattern.matcher(name);	    
//        name = matcher.replaceAll( CLEAN_OPNAME_REPLACE);
	    name = name.replaceAll( "\\s+", "_").replaceAll( "&", "ref")
	                    .replaceAll( "\\*", "ptr");
	                    
	    return name;
	}
	
	
	// ----------------------------------------------------------
	/**
	 * Creates the function that performs the [ ] operator function (setting).
	 *  
	 *  
	 * @param codeWriter Writer to output the code to.
	 */
	public void writeNamedWrapperFunctionCode(CppWriter codeWriter) {
//		String op = this.item.getOperator();

//		if(this.templateDependentParameterList != null) {
//			codeWriter.write("template <");
//			codeWriter.write(Util.getParameterText(this.templateDependentParameterList, true, true));
//			codeWriter.writeLine(">");
//		}
//		codeWriter.write("inline " + this.item.getResultType().getLiteralType() + " ");
//		codeWriter.write(WriterUtil.getOperatorWraperName(this.metaInfo, this.item) + "(");
//		this.writeSelf(codeWriter, true);
//		if(this.item.hasParameter() && ! this.isIncOrDec) {
//			codeWriter.write(", ");
//			WriterUtil.writeParamList(codeWriter, this.item.getParameterList(), true);
//		}
//		codeWriter.write(") ");
//		
//		codeWriter.beginBlock();
//		if(this.item.hasResult()) {
//			codeWriter.write("return ");
//		}
//		if(this.item.isFunctor()) {
//			codeWriter.write("(*self)(");
//			codeWriter.write(Util.getParameterText(this.item.getParameterList(), false, true));
//			codeWriter.write(")");
//		}
//		else if(this.item.isArray()) {
//			codeWriter.write("(*self)[");
//			codeWriter.write(Util.getParameterText(this.item.getParameterList(), false, true));
//			codeWriter.write("]");
//		}
//		else if(op.matches("\\w") && this.realParamCount == 1) { // type convert T()
//		}
//		else {
//			if(this.realParamCount == 2) {
//				if(this.isIncOrDec) {
//					codeWriter.write("(*self)" + op);
//				}
//				else {
//					codeWriter.write("(*self) " + op + " ");
//					codeWriter.write(Util.getParameterText(this.item.getParameterList(), false, true));
//				}
//			}
//			else if(this.realParamCount == 1) {
//				codeWriter.write(op + "(*self)");
//			}
//			else {
//			}
//		}
//		codeWriter.writeLine(";");
//
//		codeWriter.endBlock();

		if(this.shouldGenerateArraySetter()) {
			if(this.templateDependentParameterList != null) {
				codeWriter.write("template <");
				codeWriter.write(Util.getParameterText(this.templateDependentParameterList, true, true));
				codeWriter.writeLine(">");
			}
			codeWriter.write("inline " + this.item.getResultType().getLiteralType() + " ");
			codeWriter.write(WriterUtil.getOperatorWraperNamePrefix(this.metaInfo, this.item) 
			    /*+ this.metaInfo.getOperatorNameMap().get(this.item, 2)*/
			    + getArrayMethodName(this.item)
			    + "(");
			this.writeSelf(codeWriter, true);
			codeWriter.write(", ");
			WriterUtil.writeParamList(codeWriter, this.item.getParameterList(), true);
			codeWriter.write(", ");
			codeWriter.write(this.getArraySetterValueType() + " OpsEt_vALue");
			codeWriter.write(") ");

			codeWriter.beginBlock();
			codeWriter.write("return (*self)[");
			codeWriter.write(Util.getParameterText(this.item.getParameterList(), false, true));
			codeWriter.writeLine("] = OpsEt_vALue;");
			codeWriter.endBlock();
		}
	}

	private String getArraySetterValueType() {
		String typename = "";
		if(this.item.getOwner().isTemplate()) {
			typename = "typename ";
		}
		return "const " + typename + "cpgf::RemoveReference<" 
		    + this.item.getResultType().getLiteralType() + " >::Result &";
	}

	// ----------------------------------------------------------
	/**
	 * 
	 * 
	 * Reflects the function created by writeNameWrapperFunctionCode.
	 * 
	 * @param codeWriter Writer to output the code to.
	 * @param define Name of cpgf class storing info about the method being
	 *  reflected.
	 */
	public void writeNamedWrapperReflectionCode(CppWriter codeWriter, String define) {
//		String methodName = WriterUtil.getOperatorWraperName(this.metaInfo, this.item);
//		if(this.templateDependentParameterList != null) {
//			methodName = methodName + "<"
//					+ Util.getParameterText(this.templateDependentParameterList, false, true)
//					+ ">"
//					;
//		}
//		String reflectionName = this.metaInfo.getOperatorNameMap().get(this.item);
//		//scturner
//		String action = WriterUtil.getReflectionAction(define, "_methodEx");
//
//		codeWriter.write(action);
//		codeWriter.write("(" + Util.quoteText(reflectionName) + ", ");
//		//scturner
//		codeWriter.write(getTypeList() + ", ");
//
//		codeWriter.write("(" + this.item.getResultType().getLiteralType() + " (*) (");
//		this.writeSelf(codeWriter, false);
//		if(this.item.hasParameter() && ! this.isIncOrDec) {
//			codeWriter.write(", ");
//			WriterUtil.writeParamList(codeWriter, this.item.getParameterList(), false);
//		}
//		codeWriter.write(")");
//		codeWriter.write(")");
//		codeWriter.write("&" + methodName);
//		item.setIsWrapping(true);
//		String ruleText = WriterUtil.getPolicyRulesText(this.item);
//		item.setIsWrapping(false);
//		if(ruleText.length() > 0) {
//			ruleText = ruleText + ", ";
//		}
//		ruleText = ruleText + "cpgf::GMetaRuleExplicitThis";
//		codeWriter.write(", cpgf::MakePolicy<" + ruleText + " >()" + ")");
//
//		WriterUtil.writeDefaultParams(codeWriter, this.item.getParameterList());

		if(this.shouldGenerateArraySetter()) {
			this.doWriteNamedWrapperReflectionCodeForArraySetter(codeWriter, define);
		}
	}

	private void doWriteNamedWrapperReflectionCodeForArraySetter(CppWriter codeWriter, String define) {
		String reflectionName = getArrayMethodName(this.item); 
		                //this.metaInfo.getOperatorNameMap().get(this.item, 2);
		String methodName = WriterUtil.getOperatorWraperNamePrefix(this.metaInfo, this.item) + reflectionName;
		if(this.templateDependentParameterList != null) {
			methodName = methodName + "<"
					+ Util.getParameterText(this.templateDependentParameterList, false, true)
					+ ">"
					;
		}

		//scturner
		String action = WriterUtil.getReflectionAction(define, "_methodEx");

		codeWriter.write(action);
		codeWriter.write("(" + Util.quoteText(methodName) + ", ");
		//scturner
		codeWriter.write(getTypeList() + ", ");
		codeWriter.write(Util.quoteText(item.getFullNamespace()) + ", ");
		codeWriter.write(EnumModifier.modifersToCpgfString(
            EnumItemType.METHOD, item.getModifiers()) + ", ");
		
        
		codeWriter.write("(" + this.item.getResultType().getLiteralType()
		    + " (*) (");
		this.writeSelf(codeWriter, false);
		codeWriter.write(", ");
		WriterUtil.writeParamList(codeWriter, this.item.getParameterList(), false);
		codeWriter.write(", ");
		codeWriter.write(this.getArraySetterValueType());
		codeWriter.write(")");
		codeWriter.write(")");
		codeWriter.write("&" + methodName);
		item.setIsWrapping(true);
		String ruleText = WriterUtil.getPolicyRulesText(this.item);
		item.setIsWrapping(false);
		if(ruleText.length() > 0) {
			ruleText = ruleText + ", ";
		}
		ruleText = ruleText + "cpgf::GMetaRuleExplicitThis";
		//scturner
		codeWriter.write(", cpgf::MakePolicy<" + ruleText + " >()" + ")");

		WriterUtil.writeDefaultParams(codeWriter, this.item.getParameterList());
	}

	//scturner
	private String getTypeList() {
		//list of types
		String types = "\"" + item.getResultType().getLiteralType();
		for (Parameter p : item.getParameterList()) {
			types += "," + p.getType().getLiteralType();
		}                

		types += "\"";

		return types;
	}

}
