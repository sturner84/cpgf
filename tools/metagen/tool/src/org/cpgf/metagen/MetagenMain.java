package org.cpgf.metagen;

import java.io.*;   //scturner
import java.util.*;

import org.cpgf.metagen.doxyxmlparser.DoxygenXmlParser;
import org.cpgf.metagen.doxyxmlparser.FileMap;
import org.cpgf.metagen.doxyxmlparser.*; //scturner
//import org.cpgf.metagen.filters.*;
import org.cpgf.metagen.metadata.MetaInfo;
import org.cpgf.metagen.metawriter.MetaWriter;
//import org.cpgf.metagen.metadata.CppClass;  //scturner

// -------------------------------------------------------------------------
/**
 *  Starts metagen and processes the command line
 *
 *  @author  scturner
 *  @version Mar 4, 2014
 */
public class MetagenMain {
	private List<String> xmlFileNameList;
	private List<String> configFileNameList;
	//added by scturner
	private boolean autoXML;
	private List<String> srcFileNameList;

	private void usage() {
		System.out.println("Usage:");
		//modified by scturner
		System.out.println("  megagen [--autoxml] " +
				"[--src Src1 [Src2 Src3...]]" +
				" [--xml Xml1 [Xml2 Xml3...]] " +
				"[--config Config1 [Config2 Config3...]]");
		System.out.println("Either --autoxml or --xml (or both) is required. " +
				"--autoxml uses doxygen to create the needed xml " +
				"files before generating the metadata.\n" +
				"--src specifies the source files/folders to " +
				"search for source files. It defaults to the current directory. " +
				"This is used only with --autoxml. If the doxygen option " +
				"RECURSIVE = YES, then folders are searched recursively.\n" +
				"--xml specifies doxygen xml files that have already " +
				"been created.\n" +
				"--config specifies configuration file(s) being used by " +
				"metagen.\n\n");
		//System.out.println("  megagen --xml Xml1 [Xml2 Xml3...] --config Config1 [Config2 Config3...]");

		System.exit(1);
	}

	private void error(String message) {
		System.out.println(message);
		System.exit(1);
	}

	private void parseCommandLine(String[] args) throws Exception {
		this.xmlFileNameList = new ArrayList<String>();
		this.configFileNameList = new ArrayList<String>();
		//added by scturner
		this.autoXML = false;
		this.srcFileNameList = new ArrayList<String>();

		if(args.length == 0) {
			this.usage();
		}

		List<String> currentList = null;
		for(String arg : args) {
			if(arg.indexOf('-') == 0) {
				if(arg.equals("--xml") || arg.equals("-xml")) {
					currentList = this.xmlFileNameList;
				}
				else if(arg.equals("--config") || arg.equals("-config")) {
					currentList = this.configFileNameList;
				}
				//added by scturner
				else if(arg.equals("--autoxml") || arg.equals("-autoxml")) {
					this.autoXML = true;
				}
				else if(arg.equals("--src") || arg.equals("-src")) {
					currentList = this.srcFileNameList;
				}
			}
			else {
				if(currentList == null) {
					error("Need option before " + arg);
				}
				else {
					currentList.add(arg);
				}
			}
		}

		//modified by scturner
		//if(this.xmlFileNameList.size() == 0) {
		if(this.xmlFileNameList.size() == 0 && !autoXML) {
			error("No XML file is specified.");
		}

//		if(this.configFileNameList.size() == 0) {
//			error("No config file is specified.");
//		}
		//added by scturner
		if(this.srcFileNameList.size() == 0 && autoXML) {
			//default to the current directory
			srcFileNameList.add("./");
		}
	}

	
	// ----------------------------------------------------------
	/**
	 * Runs the program
	 * @param args command line parameters (see usage)
	 * @throws Exception if a file cannot be read
	 */
	public void run(String[] args) throws Exception {
		this.parseCommandLine(args);

		Config config = new Config();

		List<JavascriptConfigLoader> configLoaderList = new ArrayList<JavascriptConfigLoader>();
		try {
			for(String configFileName : this.configFileNameList) {
				JavascriptConfigLoader configLoader = new JavascriptConfigLoader(config);
				configLoaderList.add(configLoader);
				configLoader.load(configFileName);
			}

			//added by scturner
			if (autoXML) {
				autoGenXML(config);
			}

			MetaInfo metaInfo = new MetaInfo(config);
			FileMap fileMap = new FileMap();

			for(String xmlFileName : this.xmlFileNameList) {
				(new DoxygenXmlParser(config, metaInfo, fileMap, xmlFileName)).parseFile();
			}

			metaInfo.fixup(fileMap);

			//Added by scturner
			// exclude files, create headers as needed, and point the 
			//   fileMap from the cpp files to the new h files
			fileMap.createFakeHeaders(config, metaInfo);


			fileMap.filterFiles(config);
			MetaWriter metaWriter = new MetaWriter(config, metaInfo, fileMap);
			metaWriter.write();

			RunStats.report();
		}
		finally {
			for(JavascriptConfigLoader configLoader : configLoaderList) {
				configLoader.free();
			}
		}
	}


	//added by scturner
	private void autoGenXML(Config config) {  
		DoxygenCaller doxygenCall = new DoxygenCaller(config);

		doxygenCall.setExtendedInfo(true);
		File indexFile = doxygenCall.run(this.srcFileNameList);

		//show errors and quit
		if (doxygenCall.hasError()) {
			error(doxygenCall.getError());
		}

		//print any warnings
		if (doxygenCall.hasWarning()) {
			System.out.println(doxygenCall.getWarning());
		}

		//print the output from calling doxgyen
		System.out.println(doxygenCall.getOutput());

		//add to file list
		this.xmlFileNameList.add(indexFile.getPath());

		//error messge if still nothing is found
		if(this.xmlFileNameList.size() == 0) {
			error("--autoxml was used but no XML files " +
					"were found/generated.");
		}
	}



}
