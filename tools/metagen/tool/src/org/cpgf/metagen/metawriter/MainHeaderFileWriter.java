package org.cpgf.metagen.metawriter;

import java.util.List;

import org.cpgf.metagen.Config;
import org.cpgf.metagen.Util;
import org.cpgf.metagen.codewriter.CppWriter;
//import org.cpgf.metagen.metadata.*; //scturner

public class MainHeaderFileWriter extends CodeFileWriter {
	private List<String> creationFunctionNames;

	public MainHeaderFileWriter(Config config, List<String> creationFunctionNames) {
		super(config, null, null);

		this.creationFunctionNames = creationFunctionNames;
	}

	@Override
	public boolean shouldSkip() {
		return ! this.getConfig().autoRegisterToGlobal;
	}

	@Override
	protected String getOutputDirectory() {
		return this.getConfig().headerOutput;
	}

	@Override
	protected String getOutputFileName() {
		return this.getConfig().mainSourceFile + this.getConfig().headerExtension;
	}

	@Override
	protected void doWrite(CppWriter codeWriter) throws Exception {
		codeWriter.beginIncludeGuard(Util.normalizeSymbol(this.getOutputFileName()));

		codeWriter.include("cpgf/gmetadefine.h");

		codeWriter.writeLine("");
		codeWriter.writeLine("");

		codeWriter.useNamespace("cpgf");
		codeWriter.writeLine("");

		codeWriter.beginNamespace(this.getConfig().cppNamespace);

		List<String> sortedCreateFunctionNames = Util.sortStringList(creationFunctionNames);

		for(String funcName : sortedCreateFunctionNames) {
			codeWriter.writeLine("GDefineMetaInfo " + funcName + "();");
		}

		codeWriter.writeLine("");
		codeWriter.writeLine("");

		codeWriter.writeLine("template <typename Meta>");
		codeWriter.writeLine("void " + this.getMainFunctionName() + "(Meta _d)");

		codeWriter.beginBlock();

		for(String funcName : sortedCreateFunctionNames) {
			codeWriter.writeLine("_d._class(" + funcName + "());");
		}

		codeWriter.writeLine("");
		codeWriter.writeLine("setReflectMainName(\"" 
		                + this.getConfig().reflectMainName 
		                + "\");"); 
		
		codeWriter.endBlock();

		codeWriter.writeLine("");


		//scturner
		//create a function to easily register the metadata
		codeWriter.writeLine("void registerMetaDataToGlobal();");		
		codeWriter.writeLine("");
//		//method for getting the main method's name
//		codeWriter.writeLine("char * getReflectMainName()");   
//		codeWriter.beginBlock();
//		
//		codeWriter.endBlock();
//        codeWriter.writeLine("");
		//end scturner

		codeWriter.endNamespace(this.getConfig().cppNamespace);

		codeWriter.endIncludeGuard();	
	}

	private String getMainFunctionName() {
		return this.getConfig().metaClassMainRegisterPrefix + this.getConfig().projectID;
	}

}
