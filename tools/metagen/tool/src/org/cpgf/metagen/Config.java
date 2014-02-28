package org.cpgf.metagen;

import org.cpgf.metagen.metadata.EnumCategory;
import org.cpgf.metagen.metadata.PredefinedClassTraitsList;
import org.cpgf.metagen.metawriter.callback.IOutputCallback;

public class Config {
	// projectID is used as unique ID for meta build function names.
	// It must be unique among the meta data.
	public String projectID = "";

	// The directory to create .h files in.
	// The tool will create it and its parent directory if it doesn't yet.
	public String headerOutput = "./output";

	// The directory to create .cpp files in
	// The tool will create it and its parent directory if it doesn't yet.
	public String sourceOutput = "./output";

	// Specify the visibility of what kind of members to be built meta data.
	// Usually only allowPublic; otherwise; you can"t compile the meta data.
	public boolean allowPublic = true;
	public boolean allowProtected = false;
	public boolean allowPrivate = false;

	// Specify the meta data categories that are allowed to generate meta data
	// NOTE: current you can't change it in Javascript config file.
	public EnumCategory[] allowedMetaData = {
			EnumCategory.Constructor,
			EnumCategory.Destructor,
			EnumCategory.Class,
			EnumCategory.Constant,
			EnumCategory.Enum,
			EnumCategory.Field,
			EnumCategory.Method,
			EnumCategory.Operator
	};

	// Prefix for auto generated functions.
	// See the meta data for Box2D to see how they are used.
	public String metaClassFunctionPrefix = "buildMetaClass_";
	public String metaClassCreatePrefix = "createMetaClass_";

	public String metaClassMainRegisterPrefix = "registerMain_";

	// Prefix for .h and .cpp files.
	public String sourceFilePrefix = "meta_";

	// File name for the main entry source file.
	public String mainSourceFile = "register_meta";

	// A string for namespace for auto generated C++ code.
	// Choosing it carefully will avoid namespace clash.
	// null for no namespace.
	public String cppNamespace = null;

	// A callback called before the items are outputed.
	// See SFML generator scripts for details.
	public IOutputCallback metaItemCallback = null;

	// Header file extension.
	public String headerExtension = ".h";

	// Source file extension.
	public String sourceExtension = ".cpp";

	// A string of C++ code that will be put in front of all header files.
	// Useful if all header files must include extra headers.
	public String headerHeaderCode = null;

	// A string of C++ code that will be put in front of all source files.
	// Useful if all source files must include extra headers.
	public String sourceHeaderCode = null;

	// See SFML generator.
	public String[] sourceHeaderReplacer = null;

	public String[] parameterTypeReplacer = null;

	// A string of path that will be put in front of auto generated header.
	// See Box2D generator.
	public String metaHeaderPath = "";

	// auto register to global
	// If this property is true, .cpp source files will be generated
	// so the meta data will be auto registered after linked to the .cpp files.
	// If this property is false, only header files will be generated
	// so you need to call the reflect function manually.
	public boolean autoRegisterToGlobal = true;

	// A string of namespace for meta data.
	// All meta data will be put into the namespace (pseudo class).
	// null for no namespace.
	public String metaNamespace = null;

	public PredefinedClassTraitsList classTraits = new PredefinedClassTraitsList();

	public String[] predefinedTemplateInstances = {};

	public boolean wrapBitField = true;
	public boolean wrapOperator = true;

	// Not implemented yet. DON'T set it to true.
	public boolean wrapCallback = false;

	public String scriptClassWrapperPostfix = "Wrapper";


	//added by scturner
	/**
	 * If there exists a source file without a header file of the same name,
	 * this will automatically generate a header file for it.  Specifically,
	 * it will create stubs for all functions, classes, and global variables 
	 * (which are marked extern).  
	 * 
	 *  This is useful for testing single file programs.  
	 *  
	 *  This is done before the files are filtered (excludeSource,
	 *  excludeRegEx), so if a source file should be excluded from this, you 
	 *  need to use excludeRegEx to exclude both the source and the newly 
	 *  created header file too.
	 */
	public boolean createHeaderFiles = false;

	/**
	 * If header files are being generated, this can be used to differentiate
	 * real and generated files.
	 */
	public String createHeaderFilesPrefix = "fake_";

	/**
	 * Default value for the expression to exclude files.  
	 * 
	 */
	public static final String DEFAULT_EXCLUDE_REGEX 
	= "((.*Test.h)|(.*/runAllTests\\.cpp))"; 

	/**
	 * Determines if source files should be excluded from the generation 
	 * process. Generally this is the desired behavior.
	 * 
	 */
	public boolean excludeSource = true;

	/**
	 * Determines which files to exclude from the metadata generation. This 
	 * uses a regular expression to exclude any matching files.
	 * i.e.  
	 *     .* /Testing[^/]*           (remove the space between * and /)
	 *     .*Test.h
	 *     
	 * A value of null disables the exclusion (but does not affect
	 * excludeSource).
	 * 
	 * By default the pattern is (remove the space between the * and /)
	 * ((.*Test.h)|(.* /runAllTests.cpp))
	 * 
	 * This excludes anything ending with Test.h, files called runAllTests.cpp
	 * and any file that starts with the value of createHeaderFilesPrefix (see
	 * above).
	 * 
	 */
	public String excludeRegEx = DEFAULT_EXCLUDE_REGEX;


	/**
	 * Determines if the main method is copied so that it can be reflected and 
	 * called.  By default, this is not enabled.
	 * 
	 * Assumes that createHeaderFiles is true and that main is in a cpp file
	 * with no associated header file.
	 * 
	 */
	public boolean reflectMain = false;

	/**
	 * If the main is being reflected, this specifies the name it will be 
	 * called.  By default, this is "__student_main";
	 */
	public String reflectMainName = "__student_main";

	/**
	 * Name of the main function
	 */
	public static final String MAIN_NAME = "main";


	/**
	 * Path and file name of the version of Doxygen being used.  
	 * If null or if it is an invalid path, metagen will look in the
	 * doxygen folder where metagen.jar is located.  If there is no file
	 * name doxygen[.exe] (depending on OS), it will look for folders
	 * win, mac, linux to try to find an OS appropriate version of doxygen.
	 */
	public String doxygenFile = null;

	/**
	 * Path and file name of a default configuration file for doxygen.  
	 * If null or not found, a config file in metagen's doxygen folder will
	 * be used.  
	 * 
	 * The configuration file will be copied and modified to exclude files
	 * specified by excludeSource and excludeRegEx and to account for 
	 * createHeaderFiles.
	 * 
	 * The config file should be set to create xml files.
	 */
	public String doxygenConfigFile = null;

	/**
	 * Default value for doxygen's output directory 
	 */
	public static final String DEFAULT_DOXYGEN_OUTPUT = "./xml"; 

	/**
	 * Location for the files generated by doxygen.
	 */
	public String doxygenOutput = DEFAULT_DOXYGEN_OUTPUT;

	/**
	 * Extension added to the temporary doxygen config file
	 */
	public static final String TEMP_CONFIG_FILE_POSTFIX = ".tmp"; 

	/**
	 * The source file extensions that will be processed
	 */
	public static final String SRC_EXTENSIONS 
	= "\\.cpp|\\.C|\\.c|\\.cc|\\.cxx|\\.c++|\\.cp";

	/**
	 * The header file extensions that will be processed
	 */
	public static final String HDR_EXTENSIONS 
	= "\\.hpp|\\.H|\\.h|\\.hh|\\.hxx|\\.h++|\\.hp";
}
