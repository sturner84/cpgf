package org.cpgf.metagen.doxyxmlparser;


import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
//import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Stack;
//import java.util.regex.Pattern;
//import java.util.regex.Matcher;
//import java.util.Scanner;
//import java.util.HashMap;

import org.cpgf.metagen.*;
import org.cpgf.metagen.metadata.*;
import org.cpgf.metagen.codewriter.CodeWriter;
import org.cpgf.metagen.filters.FileContentFilter;
import org.cpgf.metagen.filters.FileClassFilter;
import org.cpgf.metagen.filters.FileMainFilter;

//-------------------------------------------------------------------------
/**
*  Creates a fake header file for .cpp files that do not have a .h of their
*  own (i.e. foo.cpp but no foo.h).  This allows single file programs to 
*  be graded by generating the header file for compilation.
*
*  @author  scturner
*  @version Sep 6, 2013
*/
public class FakeHeaderWriter
{
    private String headerName;
    private String sourceName;
    private MetaInfo metaInfo;
    private Stack<String> namespaces;
    private String lastVisibility;
    private FileContentFilter filter;
    private LinkedList<CppClass> classes;
    private FileClassFilter classFilter;
    private Config config;
    
    //helps correct for a cygwin path.  Cygwin wants /cygdrive/ but others
    //do not.
    private static final String CYGWIN_PATH = "/cygdrive/";
    
    //reg exp that has the basic format for a class definition
    //the first %s takes the name of the class and the second 
    //takes the value being looked for in the definition (like a base class 
    //name).
//    private static final String CLASS_HEADER_REGEX =
//                    "(?ms:(?:\\s|^)class\\s(?:[^{]*?\\s+)*?%s" +
//                    "\\s(?:[^{]*?\\s+)*?(?:[^{]*::)*?%s" +
//                    "[\\s,](?:[^{]*?\\s+)*?\\{)";
//    
//    private static final String CLASS_HEADER_BASIC_REGEX 
//        = "(?ms:(?:\\s|^)class\\s.*?\\{)";
    
    private static final String INCLUDE_REGEX = 
                    "(?:\\n|^)\\s*#include\\s+[\"<].*[\">]";
    
    private static final String USING_REGEX = "(?:\\n|^)\\s*using\\s+[^;]*;";
    private static final String COMMENTS_REGEX = "(?://.*\\n)|" +
                    "(?ms:\\/\\*.*?\\*\\/)"; //"(?:\\/\\*(?!\\*\\/)*?\\*\\/)";
    
  
//    private static final String MAIN_NAME = "main";
    
    
    /**
     * Constructs a fake header with the name of the header, the name of the
     * source file and the metadata for the source file.
     * 
     * @param hName New header file name
     * @param sName Source file name
     * @param info Metadata for the source file.
     * @param cfg Configuration to use
     */
    public FakeHeaderWriter(String hName, String sName, MetaInfo info, 
        Config cfg) {
        headerName = hName;
        sourceName = sName;
        metaInfo = info;
        namespaces = new Stack<String>();
        lastVisibility = "";
        classes = new LinkedList<CppClass>();
        classFilter = null;         
        filter = null;
        config = cfg;
    }
    
    
    private List<String> getPatternFromFile(String pattern) {
       List<String> values = new LinkedList<String>();
       
       try
       {
           values = filter.find(pattern);
       }
       catch ( IOException e )
       {
           // If the file cannot be read, how did we get the metadata created?
           e.printStackTrace();
       }
              
       return values;
    }
    
    
    //get the #include and using statements
    private void writeIncludes(CodeWriter writer) {
        
        List<String> includes;
        List<String> usings;
        
        includes = getPatternFromFile(INCLUDE_REGEX);
        usings = getPatternFromFile(USING_REGEX);
        
        writer.writeLine("\n");
        
        for (String s : includes) {
            writer.writeLine(s.trim());
        }
        
        writer.writeLine("\n");
        
        for (String s : usings) {
            writer.writeLine(s.trim());
        }
        
        writer.writeLine("\n");
    }
    
    
    private void checkNamespace(CodeWriter writer, String namespace) {
        //pop off namespaces until a match is found or it is empty
        String lastNamespace;
        
        if (!namespaces.isEmpty()) {
            lastNamespace = namespaces.peek();
            while (!namespaces.isEmpty() && !namespace.startsWith(lastNamespace)) {
                namespaces.pop();
                writer.decIndent();
                writer.writeLine("}");

                if (!namespaces.isEmpty()) {
                    lastNamespace = namespaces.peek();
                }
            }
        }

        if (!namespace.isEmpty()) {
            //make sure the namespace does not match the current one
            lastNamespace = "";
            if (!namespaces.isEmpty()) {
                lastNamespace = namespaces.peek();
            }
            
            if (!namespace.equals(lastNamespace)) {
                namespaces.push(namespace);
                
                String lastPart = namespace;

                if (lastPart.lastIndexOf("::") >= 0) {
                    lastPart = lastPart.substring(lastPart.lastIndexOf("::") 
                        + 2);
                }

                writer.writeLine("namespace " + lastPart);
                writer.writeLine("{");
                writer.incIndent();
            }
        }
        
    }
    
    private void checkVisibility(CodeWriter writer, EnumVisibility visibility) {
        if (!lastVisibility.equals(visibility.toString())) {
            writer.decIndent();
            writer.writeLine(visibility.toString().toLowerCase() + ":");
            writer.incIndent();
            lastVisibility = visibility.toString();
        }
    }
    
    private void writeField(CodeWriter writer, CppField field, boolean isGlobal) {

     //   List<String> constStrs = null;
        String externStr = "";
        String staticStr = "";
        String initializer = "";
        
        String name = field.getLiteralName();
        
        String type = field.getType().getLiteralType();
        
//TODO missing the static on some variables
        if (isGlobal) {
            if (field.isConst() && field.getInitializer() != null) {
                
                initializer = " " + field.getInitializer();
            }
            else {
                externStr = "extern ";
            }
        }
        else {
            if (field.isStatic()) {
                staticStr = "static ";
            }
        }
        String array = "";
        String namespace = field.getFullNamespace();
        if (type.indexOf('[') >= 0) {
            array = type.substring(type.indexOf('['));
            type = type.substring(0, type.indexOf('['));
        }

        checkVisibility(writer, field.getVisibility());
        checkNamespace(writer, namespace);

        writer.writeLine(externStr + staticStr +  type + " " + name + 
            array + initializer + ";");

    }
    
    
    private void writePublicVariables(CodeWriter writer, CppClass cpp) {
        List<CppField> globals = cpp.getFieldList();
        lastVisibility = EnumVisibility.Public.toString();
        
        for (CppField var : globals) {
            writeField(writer, var, true);
        }
        
        writer.writeLine("\n");        
    }
    
    
    private void createMain(String mainName) {
        //find main in file
        BufferedWriter writer = null;
        FileMainFilter mainFilter = new FileMainFilter(fixCygwinPath(sourceName));
        try {
            String mainCode = mainFilter.findMain();
            if (mainCode != null) {
                mainCode = mainCode.replaceFirst(Config.MAIN_NAME, mainName);
                //create new .cpp file (same name has header)            
                writer = new BufferedWriter(
                    new FileWriter(fixCygwinPath(headerName + ".cpp")));
            
                //write to new file
                writer.write("\n\n#include \"" + headerName + "\"\n\n\n");
                writer.write(mainCode);
                writer.write("\n\n");
            }
        }
        catch (IOException e) {
            //this should be in there because Doxygen found it
            e.printStackTrace();            
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace(); 
                }
            }
        }
    }
    
    private void writeMethod(CodeWriter writer, CppMethod method, boolean isGlobal) {
        String signature = "";
        Parameter param;
        
        String methodName = method.getLiteralName();
        if (isGlobal && methodName.equals(config.reflectMainName)) {
            if (config.reflectMain) {
                if (config.reflectMainName != null) {
                    methodName = config.reflectMainName;                    
                } 
                //copy main method into new file
                createMain(methodName);
            }
            else { //don't bother doing anything, you cannot call it anyway
                return;
            }
        }
        
        checkNamespace(writer, method.getFullNamespace());
        checkVisibility(writer, method.getVisibility());
        
        
        
        if (method.isStatic() && !isGlobal) {
            signature += "static ";
        }
        
        if (method.isVirtual() || method.isPureVirtual()) {
            signature += "virtual ";
        }
        
        signature += method.getResultType().getLiteralType()
           + " " + methodName + "(";
        
        for (int i = 0; i < method.getParameterCount(); i++) {
            param = method.getParameterAt(i);
            if (param.getType().getLiteralType().equals("...")) {
                signature += "..."; 
            }
            else {
                signature += param.getType().getLiteralType() + " " 
                    + param.getName();
            }
            
            if (i < method.getParameterCount() - 1) {
                signature += ", ";
            }           
        }
        
        signature += ")";
        
        if (method.isConst()) {
            signature += " const";
        }
        
        if (method.isPureVirtual()) {
            signature += " = 0";
        }
        
        signature += ";";
        writer.writeLine(signature);        
    }
    
    
//    private void writeConstructor(CodeWriter writer, String className, 
//        Constructor constr) {
//        String signature = "";
//        Parameter param;
//       
//        // templates - need this?? this should be in a header file anyway
//        // constant??? 
//        // static - test
//       //
//        checkNamespace(writer, constr.getFullNamespace());
//        checkVisibility(writer, constr.getVisibility());
//        
//        if (constr.isExplicit()) {
//            signature += "explicit ";
//        }
//        signature += className + "(";
//        
//        for (int i = 0; i < constr.getParameterCount(); i++) {
//            param = constr.getParameterAt(i);
//            signature += param.getType().getLiteralType() + " " 
//               + param.getName();
//            
//            if (i < constr.getParameterCount() - 1) {
//                signature += ", ";
//            }           
//        }
//        
//        signature += ")";
//        
//        signature += ";";
//        writer.writeLine(signature);
//    }
//    
//    
//    private void writeDestructor(CodeWriter writer, CppClass cpp) {
//        String signature = "";
//        
//        Destructor destr = cpp.getDestructor();
//        
//        if (destr != null) {
//            checkNamespace(writer, destr.getFullNamespace());
//            checkVisibility(writer, destr.getVisibility());
//            
//            if (destr.isVirtual()) {
//                signature += "virtual ";
//            }
//            signature += "~" + cpp.getLiteralName() + "();";
//            writer.writeLine(signature);
//        }
//    }

    private void writeEnum(CodeWriter writer, CppEnum enumeration)
    {
        writer.writeLine("enum " + enumeration.getLiteralName());
        writer.writeLine("{");
        writer.incIndent();
        
        List<EnumValue> enums = enumeration.getValueList();
        
        for (int i = 0; i < enums.size(); i++) {
            EnumValue val = enums.get(i);
            String comma = "";
            String value;
            if (val.getValue() == null) {
                value = "";
            }
            else {
                value = " " + val.getValue();
            }
            
            if (i != enums.size() - 1) {
                comma = ",";
            }
            
            writer.writeLine(val.getName() + value + comma);
        }
        
        writer.decIndent();
        writer.writeLine("};");
    }
    
    private void writeEnums(CodeWriter writer, CppClass cpp)
    {
        List<CppEnum> enums = cpp.getEnumList();
        
        lastVisibility = EnumVisibility.Public.toString();
        
        for (CppEnum e : enums) {
            writeEnum(writer, e);
        }
        
        writer.writeLine("\n");
    }
    
    private void writeFunctions(CodeWriter writer, CppClass cpp) {
        List<CppMethod> functions = cpp.getMethodList();
        
        lastVisibility = EnumVisibility.Public.toString();
        
        for (CppMethod func : functions) {
            writeMethod(writer, func, true);
        }
        
        writer.writeLine("\n");
    }
    
    
//    private boolean isVirtualBase(String className, String baseName) {
//        //check if a base class is virtual
//        List<String> results;
//        results = getPatternFromFile(CLASS_HEADER_BASIC_REGEX);
//        
//        for (String r : results) {
//            //remove spaces before or after ::
//            r = r.replaceAll("\\s*::\\s*", "::");
//            
//            if (r.matches(String.format(CLASS_HEADER_REGEX, className, 
//                baseName))) {
//                
//                if (r.matches("(?ms:.*\\svirtual\\s+(?:(?:(public)|" +
//                		"(protected)|(private))\\s+)?" + baseName + ".*)")) {
////                    System.out.println("Match: " + className + "\n" + baseName + r);
//                    return true;
//                }
//            }
//            
//           
//        }
//        
//        return false;
//    }
    
//    private String getPrefix(String className, String baseName) {
//        List<String> results;
//        results = getPatternFromFile(CLASS_HEADER_BASIC_REGEX);
//        
//        for (String r : results) {
//            //remove spaces before or after ::
//            r = r.replaceAll("\\s*::\\s*", "::");
//            if (r.matches(String.format(CLASS_HEADER_REGEX, className, 
//                baseName))) {
//               // System.out.println("Match: " + className + "\n" + baseName + r);
//                Pattern regex = Pattern.compile("(?:(\\S*::)+(?:" + baseName + "))");
//                Matcher match = regex.matcher(r); 
//                if (match.find()) {
////                    System.out.println("Match: " + className + "\n" + baseName + r.substring(match.start(1), match.end(1)));
//                    return r.substring(match.start(1), match.end(1));
//                }
//            }
//        }
//        return "";
//    }
    
//    private void writeBaseClasses(CodeWriter writer, CppClass cpp) {
//        List<DeferClass> baseClass = cpp.getBaseClassList();
//        
//        if (baseClass.size() > 0) {
//            writer.write(" : ");
//            DeferClass c;
//            
//            
//            for (int i = 0; i < baseClass.size(); i++) {
//                c = baseClass.get(i);
//               
//                //check if a base class is virtual
//                if (isVirtualBase(cpp.getLiteralName(), c.getName())) {
//                    writer.write("virtual ");
//                }
//                String prefix = getPrefix(cpp.getLiteralName(), c.getName());  
//
//                writer.write(c.getVisibility().toString().toLowerCase()
//                    + " " + prefix + c.getName());
//                
//                if (i < baseClass.size() - 1) {
//                    writer.write(", ");
//                }
//            }
//        }
//    }

    private void writeClass(CodeWriter writer, CppClass cpp) {
        //get class def from file
        try {
            String classDef = classFilter.find(cpp.getLiteralName());
            writer.writeLine(classDef);
        }
        catch (IOException e) {
            //should always be able to read the file
            e.printStackTrace();
        }             
    }
    
    private void writeClassDecl(CodeWriter writer, CppClass cpp) {
        
        checkNamespace(writer, cpp.getFullNamespace());
        writer.write( "class "  + cpp.getLiteralName());
//        writeBaseClasses(writer, cpp);
//        writer.writeLine("");
//        
//        writer.writeLine("{");
//        
//        lastVisibility = "";
//        
//        //fields
//        for (CppField field : cpp.getFieldList()) {
//            writeField(writer, field, false);
//        }
//        
//        //constructors
//        for (Constructor constructor : cpp.getConstructorList()) {
//            writeConstructor(writer, cpp.getLiteralName(), constructor);
//        }
//        //destructor
//        writeDestructor(writer, cpp);
//        
//        //methods
//        for (CppMethod method : cpp.getMethodList()) {
//            writeMethod(writer, method, false);
//        }
//        
//        //enums
//        for (CppEnum e : cpp.getEnumList()) {
//            writeEnum(writer, e);
//        }
//        
//        for (DeferClass c : cpp.getClassList()) {
//            if (c.getCppClass() != null) {
//                writeClass(writer, c.getCppClass());
//            }
//        }
//        
//        writer.decIndent();
//        writer.writeLine("};");
        writer.writeLine(";");
       // writer.writeLine("");
    }
    
    private String getHeaderContent() {        
        CodeWriter writer = new CodeWriter();
        
        String defineName = headerName.toUpperCase().
                        replaceAll( "[/\\\\.:]", "_");
        writer.writeLine("#ifndef " + defineName);
        writer.writeLine("#define " + defineName + "");
        
        writeIncludes(writer);
        
        for (CppClass cpp : metaInfo.getClassList()) {
            
            if (sourceName.equals(cpp.getLocation())) {
                if (cpp.isGlobal()) {
                    //global variables
                    writePublicVariables(writer, cpp);
                    //functions
                    writeFunctions(writer, cpp);
                    //enums
                    writeEnums(writer, cpp);
                }
                else {
                    //classes
                    //this is just a forward declaration
                    //the class comes later
                    writeClassDecl(writer, cpp);
                    classes.add(cpp);
                }
            }
        }
        
        //copy each class definition from the source file
        for (CppClass cpp : classes) {
            writeClass(writer, cpp);
        }
        
        writer.write("\n#endif //" + defineName);
        
        return writer.getText();
    }
    
    /**
     * Searches the file name for /cygdrive/ and replaces it with a standard
     * Windows drive letter.
     * 
     * 
     * @param name Name of the file
     * @return The name of the file corrected
     */
    private String fixCygwinPath(String name) {
        String newName = name;
        try {
            FileReader input = new FileReader(newName);
//            while (input.hasNextLine()) {
//            System.out.println(input.nextLine());
//            }
            input.close();
        }
        catch (Exception e) {
            //probably means there is a problem with the path
            if (newName.startsWith(CYGWIN_PATH)) {
                newName = newName.substring(CYGWIN_PATH.length());
                newName = newName.substring(0, 1).toUpperCase() + ":" 
                                + newName.substring(1);  
            }
        }
        
        return newName;        
    }
    
    
    /**
     * This goes through all of the classes and updates references to the 
     * source file to point to the new (fake) header file;
     */
    private void updateLocation() {
        LinkedList<Item> allItems = new LinkedList<Item>();
        for (CppClass cpp : metaInfo.getClassList()) {
            if (sourceName.equals(cpp.getLocation())) {
//                Util.trace(cpp.getLiteralName() + " " + cpp.getLocation());
                cpp.getAllItems(allItems);
                allItems.add(cpp);
            }
        }
        
        for (Item item : allItems) {
            if (sourceName.equals(item.getLocation())) {
                item.setLocation(headerName);
            }
        }
    }
    
    
    /**
     * Takes the source file and metadata and creates a fake header file.
     * @throws IOException 
     * 
     * @throws IOException If the file is invalid or the metadata is invalid.
     */
    public void write() throws IOException {
        if (headerName == null || sourceName == null) {
            throw new IOException("File name is invalid.");
        }
        if (metaInfo == null) {
            throw new IOException("Metadata to write is invalid.");
        }
        
       // File file = new File(fixCygwinPath(headerName));
        BufferedWriter writer = new BufferedWriter(
            new FileWriter(fixCygwinPath(headerName)));
        filter = new FileContentFilter(fixCygwinPath(sourceName), 
            COMMENTS_REGEX);
        classFilter = new FileClassFilter(fixCygwinPath(sourceName));
        try {
            writer.write(getHeaderContent());
            //System.out.println(getHeaderContent());
            updateLocation();
        }
        finally {
            writer.close();
        }
        
        
    }
}
