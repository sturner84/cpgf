package org.cpgf.metagen.doxyxmlparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cpgf.metagen.Config;
import org.cpgf.metagen.MetaException;
import org.cpgf.metagen.Util;
//scturner
//import org.cpgf.metagen.filters.*;
import org.cpgf.metagen.metadata.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


// -------------------------------------------------------------------------
/**
 *  Parses the Doxygen XML and creates a set of metadata classes from the 
 *  data
 *
 *  @author  scturner (documented)
 *  @version Jun 6, 2014
 */
public class DoxygenXmlParser {
    private Config config;
    private MetaInfo metaInfo;
    private FileMap fileMap;
    private String xmlFileName;
    private String basePath;
    private File xmlFile;

    private Stack<CppClass> classStack;
    private List<String> namespaceStack;

    // ----------------------------------------------------------
    /**
     * Initializes the parser
     * 
     * @param config Configuration file for the program
     * @param metaInfo High level meta information
     * @param fileMap Mapping of data to individual files
     * @param xmlFileName Name of the xml file
     */
    public DoxygenXmlParser(Config config, MetaInfo metaInfo, FileMap fileMap, String xmlFileName) {
        this.config = config;
        this.metaInfo = metaInfo;
        this.fileMap = fileMap;
        this.xmlFileName = xmlFileName;
        this.xmlFile = new File(this.xmlFileName);
        this.basePath = this.xmlFile.getParent();
        this.classStack = new Stack<CppClass>();
        this.namespaceStack = new ArrayList<String>();

        Util.trace("Parsing " + xmlFileName);
    }

    // ----------------------------------------------------------
    /**
     * Starts the parsing process
     * 
     * @throws Exception If the file cannot be read
     */
    public void parseFile() throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder xmlBuilder = builderFactory.newDocumentBuilder();
        Document xmlDocument = xmlBuilder.parse(this.xmlFile);

        this.doParaseXmlDocument(xmlDocument);
        CopyFunctionList.getInstance().addClasses(metaInfo.getClassList());
    }

    private void error(String message) throws MetaException {
        throw new MetaException(message);
    }

    private CppClass enterClass(String name) {
        CppClass cppClass = new CppClass(name);

        this.classStack.push(cppClass);
        this.metaInfo.getClassList().add(cppClass);

        return cppClass;
    }

    private void leaveClass() {
        this.classStack.pop();
    }

    private CppClass getCurrentClass() {
        return this.classStack.peek();
    }

    private void enterNamepsace(String namespace) {
        this.namespaceStack.add(namespace);
        metaInfo.addNamespace(namespaceStack);
    }

    private void leaveNamespace() {
        this.namespaceStack.remove(this.namespaceStack.size() - 1);
    }

    //scturner updated
    private List<Integer> getDimensions(String arrayDim) {
        ArrayList<Integer> dims = new ArrayList<Integer>();

        Pattern pattern = Pattern.compile("(?:\\[(.*?)\\])");
        Matcher match = pattern.matcher(arrayDim);

        int start = 0;
        while (start < arrayDim.length()) {
            match.region(start, arrayDim.length());   
            if (match.find()) {
                String strVal = arrayDim.substring(match.start(1), 
                    match.end(1));
                int val = -1;

                if (!strVal.isEmpty()) {
                    try {
                        val = Integer.parseInt(strVal);
                    }
                    catch (NumberFormatException e) {
                        //try a char
                        //using a char like 'a'
                        if (strVal.charAt(0) == '\''
                                        && strVal.charAt(strVal.length() - 1) == '\'') {
                            if (strVal.length() == 3) { 
                                val = strVal.charAt(1);
                            }
                            if (strVal.length() == 4 && strVal.charAt(1) == '\\') {
                                switch (strVal.charAt(2)) {
                                    case 'b': val = '\b'; break;
                                    case 'r': val = '\r'; break;
                                    case 'n': val = '\n'; break;
                                    case 't': val = '\t'; break;
                                    case '\"': val = '\"'; break;
                                    case '\'': val = '\''; break;
                                    case '0': val = '\0'; break;
                                    case '\\': val = '\\'; break;    
                                    default: val = strVal.charAt(2);
                                }
                            }
                        }

                        //   strVal = strVal.replaceAll("\\\\(\\w)", "\b");


                    }
                }

                dims.add(val);                
                start = match.end();
            }
            else {
                start = arrayDim.length();
            }
        }           

        return dims;
    }

    //scturner updated
    private String checkInitializedArray(String arrayDim, String initStr) {
        //remove all spaces as they are not needed
        String dimStr = arrayDim.replaceAll("\\s+", "");
        if (dimStr.indexOf("[]") < 0 || initStr == null 
                        || initStr.trim().isEmpty()) {
            return dimStr;
        }
        //find other dimensions
        List<Integer> dims = getDimensions(dimStr);


        /*
         * int array1dInit[] = {1, 2, 3, 4, 5, 6};        
	            int array2dPart[][2] = {{1, 2}, {3, 4}, {5, 6}};            
         */

        //replace strings and chars
        String mInitStr = initStr.replaceAll("(?:\\\".*?\\\")|(?:\\'.*?\\')", "");
        //replace anything in ( )
        mInitStr = mInitStr.replaceAll("(?:\\(.*?\\))", "");
        //that should leave only , between the elements
        //get rid of { } and whitespace
        mInitStr = mInitStr.replaceAll("[{}\\s]", "");

        int commaCount = 0;
        for (int i = 0; i < mInitStr.length(); i++) {
            if (mInitStr.charAt(i) == ',') {
                commaCount++;
            }
        }
        //add in the last element if there is not a comma at the end
        if (mInitStr.charAt(mInitStr.length() - 1) != ',') {
            commaCount++;
        }

        for (int i : dims) {
            if (i != -1) {
                //divide commaCount by the dimension
                //to get rid of the dimension
                //it should always be even
                commaCount /= i;   
            }
        }
        String correctedDims = "";

        for (int i : dims) {
            if (i != -1) {
                correctedDims += "[" + i + "]";
            }
            else {
                correctedDims += "[" + commaCount + "]";
            }
        }

        return correctedDims;
    }


    private CppType getType(Node node) {
        String baseType = Util.getNodeText(Util.getNode(node, "type"));
        String array = Util.getNodeText(Util.getNode(node, "array"));
        //scturner updated

        //checks for a size value in the array
        String stdArray = Util.getNodeText(Util.getNode(node, "argsstring"));
        if (stdArray != null) {
            int arrayStart = stdArray.indexOf('[');
            int arrayEnd = stdArray.lastIndexOf(']');
            if (arrayStart >= 0 && arrayEnd >= 0 && arrayStart < arrayEnd) {
                stdArray = stdArray.substring(arrayStart, arrayEnd + 1);
                stdArray = checkInitializedArray(stdArray, 
                    Util.getNodeText(Util.getNode(node, "initializer")));
            }
            else {
                stdArray = "";
            }
            if (array != null) {
                array += stdArray;
            }
            else {
                array = stdArray;
            }
        }
        //checks for volatile in the type
        //Doxygen doesn't seem to pick up volatile if it is first
        // i.e. if it is declared int volatile x; type = int volatile
        //      if it is declared volatile int x; type = int
        String def = Util.getNodeText(Util.getNode(node, "definition"));
        if (def != null && def.matches("(^|\\s)*volatile\\s.*") &&
                        (!baseType.matches(".*(^|\\s)*volatile($|\\s).*"))) {
            //add volatile to the base type and let the reflection library
            //handle reordering the modifiers
            baseType = "volatile " + baseType;
        }

        return new CppType(this.metaInfo.getTypeSolver(), baseType, array);
    }

    private String getLocation(Node node) {
        Node child = Util.getNode(node, "location");
        if(child == null) {
            return "";
        }

        return Util.getAttribute(child, "file");		
    }

    private EnumVisibility getVisibility(Node node) {
        String v = Util.getAttribute(node, "prot");

        if(v.indexOf("public") >= 0) {
            return EnumVisibility.Public;
        }
        else if(v.indexOf("private") >= 0) {
            return EnumVisibility.Private;
        }
        else {
            return EnumVisibility.Protected;
        }
    }

    private void takeVisibility(Node node, Item item) {
        item.setVisibility(this.getVisibility(node));
    }

    private void takeLocation(Node node, Item item) {
        item.setLocation(this.getLocation(node));
    }

    private void resolveNamespace(Item item) {
        item.setNamespaces(this.namespaceStack);
    }

    private void doParaseXmlDocument(Document xmlDocument) throws Exception {
        NodeList nodeList = xmlDocument.getElementsByTagName("doxygen");
        if(nodeList.getLength() == 0) {
            nodeList = xmlDocument.getElementsByTagName("doxygenindex");
        }

        if(nodeList.getLength() == 0) {
            error("Invalid Doxygen XML format.");
        }

        Node root = nodeList.item(0);
        NodeList childList = root.getChildNodes();
        for(int i = 0; i < childList.getLength(); ++i) {
            Node child = childList.item(i);
            String nodeName = child.getNodeName();
            if(nodeName.equals("compound")) {
                this.doParseCompound(child);
            }
            else if(nodeName.equals("compounddef")) {
                this.doParseCompounddef(child);
            }
            //			else {
            //			}
        }
        
        //TODO
    }

    private void doParseCompound(Node node) throws Exception {
        String refid = Util.getAttribute(node, "refid");

        String fileName = (new File(this.basePath, refid + ".xml")).getAbsolutePath();
        if(fileName != null && fileName.length() > 0) {
            DoxygenXmlParser parser = new DoxygenXmlParser(this.config, this.metaInfo, this.fileMap, fileName);
            parser.parseFile();
        }
    }

    private void doParseCompounddef(Node node) {
        String kind = Util.getAttribute(node, "kind");
        String location = this.getLocation(node);

        if(kind.equals("class")) {
            this.doParseClass(node, location);
        }
        else if(kind.equals("union")) {
            // we can't handle union for now because union will cause compile error in type traits
            ///			this.doParseClass(node, location);
        }
        else if(kind.equals("struct")) {
            this.doParseClass(node, location);
        }
        else if(kind.equals("file")) {
            this.fileMap.addLocation(location, node);
            this.doParseDefFile(node, location);
        }
        else if(kind.equals("namespace")) {
            this.doParseNamespace(node, location);
        }
    }

    private void doParseClass(Node node, String location) {
        String className = Util.getNodeText(Util.getNode(node, "compoundname"));
        CppClass cppClass = this.enterClass(className);
        cppClass.setLocation(location);

        try {
            this.doParseBaseClasses(node);
            this.doParseInnerClasses(node);
            this.doParseAllSectionDef(node /*, location*/);
            this.doParseTemplateParams(node, cppClass);

            this.resolveNamespace(cppClass);
        }
        finally {
            this.leaveClass();
        }
    }

    private void doParseDefFile(Node node, String location) {
        this.enterClass(null).setLocation(location);
        try {
            this.doParseAllSectionDef(node /*, location*/);
        }
        finally {
            this.leaveClass();
        }
    }

    private void doParseNamespace(Node node, String location) {
        this.enterNamepsace(Util.getNodeText(Util.getNode(node, "compoundname")));
        try {
            this.doParseDefFile(node, location);
        }
        finally {
            this.leaveNamespace();
        }
    }

    private void doParseBaseClasses(Node node) {
        for(Node child : Util.getChildNodesByName(node, "basecompoundref")) {
            DeferClass baseClass = new DeferClass(Util.getNodeText(child),
                this.getVisibility(child), 
                "virtual".equals(Util.getAttribute(child, "virt")));
            this.getCurrentClass().getBaseClassList().add(baseClass);
        }
    }

    private void doParseInnerClasses(Node node) {
        for(Node child : Util.getChildNodesByName(node, "innerclass")) {
            DeferClass baseClass = new DeferClass(Util.getNodeText(child),
                this.getVisibility(child), false);
            this.getCurrentClass().getClassList().add(baseClass);
        }
    }

    private void doParseAllSectionDef(Node node /*, String location*/) {
        for(Node child : Util.getChildNodesByName(node, "sectiondef")) {
            this.doParseSectionDef(child /*, location*/);
        }
    }

    private void doParseSectionDef(Node node /*, String location*/) {
        for(Node child : Util.getChildNodesByName(node, "memberdef")) {
            String kind = Util.getAttribute(child, "kind");
            String name = Util.getNodeText(Util.getNode(child, "name"));

            Item item = null;

            if(kind.equals("function")) {
                item = this.doParseMethod(child, name);
            }
            else if(kind.equals("variable")) {
                item = this.doParseField(child, name);
            }
            else if(kind.equals("enum")) {
                item = this.doParseEnum(child, name);
            }
            else if(kind.equals("define")) {
                item = this.doParseConstant(child /*, name*/);
            }
            else if(kind.equals("typedef")) {
                item = this.doParseTypedef(child, name);
            }
            else if(kind.equals("enumvalue")) {
                //do nothing
            }

            if(item != null) {
                this.takeVisibility(child, item);
                this.takeLocation(child, item);
                this.resolveNamespace(item);
            }
        }
    }

    private Item doParseMethod(Node node, String nName) {
        String name = nName;

        if(! this.getCurrentClass().isGlobal()) {
            if(name.indexOf('~') >= 0 && ! name.matches("operator\\s*~")) {
                Destructor destructor = new Destructor();
                this.getCurrentClass().setDestructor(destructor);

                //scturner scturner added virtual support
                destructor.setVirtual(Util.getAttribute(node, "virt").equals("virtual"));
                destructor.setInline(Util.isValueYes(Util.getAttribute(node, "inline")));
                return destructor;
            }

            if(this.getCurrentClass().getPrimaryName().equals(name)) { // constructor
                Constructor constructor = new Constructor();
                this.doParseParams(node, constructor);
                this.doParseTemplateParams(node, constructor);
                this.getCurrentClass().getConstructorList().add(constructor);
                constructor.setExplicit(Util.isValueYes(Util.getAttribute(node, "explicit")));
                constructor.setInline(Util.isValueYes(Util.getAttribute(node, "inline")));

                return constructor;
            }
        }

        //check if it is a cast operator
        //and disallow them.
        if (name.matches("^(?:.*\\b)*operator\\s*\\b([\\w:]+)$")) {
            return null;
        }


        //Treat all operators as methods
        //allows operators marked as const be be found correctly
        // and simplifies everything over all since operators need to be
        // called as methods in LookingGlass anyway.

        //match operators and not methods that start with operator 
        //		Pattern pattern = Pattern.compile("^.*\\boperator(.*)$");
        Pattern pattern = Pattern.compile("^.*\\boperator\\s*(\\[\\s*\\])");

        Matcher matcher = pattern.matcher(name);

        if(matcher.matches()) { // operator
            String op = matcher.group(1);
            Operator operator = new Operator(
                op, 
                new CppType(this.metaInfo.getTypeSolver(), Util.getNodeText(Util.getNode(node, "type")))
                            );

            operator.setStatic(Util.isValueYes(Util.getAttribute(node, "static")));
            operator.setConst(Util.isValueYes(Util.getAttribute(node, "const")));
            operator.setVirtual(Util.getAttribute(node, "virt").equals("virtual"));
            operator.setPureVirtual(Util.getAttribute(node, "virt").equals("pure-virtual"));
            operator.setInline(Util.isValueYes(Util.getAttribute(node, "inline")));
            Node defNode = Util.getNode(node, "definition");
            operator.setExtern(Util.getNodeText( defNode).matches(
                            "(^\\s*)extern(\\s).*"));

            if(operator.getResultType().isEmpty()) { // type convertion operator, T()
                operator.setResultType(new CppType(this.metaInfo.getTypeSolver(), operator.getOperator()));
            }
            this.doParseParams(node, operator);
            this.doParseTemplateParams(node, operator);
            this.getCurrentClass().getOperatorList().add(operator);

            //fix visibilty, location
            this.takeVisibility(node, operator);
            this.takeLocation(node, operator);
            this.resolveNamespace(operator);

            //	return operator;
        }

        //modified scturner
        if (config.reflectMain && Config.MAIN_NAME.equals(name) 
                        && config.reflectMainName != null) {
            name = config.reflectMainName;
        }

        // method
        CppMethod method = new CppMethod(
            name, 
            new CppType(this.metaInfo.getTypeSolver(), Util.getNodeText(Util.getNode(node, "type")))
                        );
        method.setStatic(Util.isValueYes(Util.getAttribute(node, "static")));
        method.setConst(Util.isValueYes(Util.getAttribute(node, "const")));
        method.setVirtual(Util.getAttribute(node, "virt").equals("virtual"));
        method.setPureVirtual(Util.getAttribute(node, "virt").equals("pure-virtual"));
        method.setInline(Util.isValueYes(Util.getAttribute(node, "inline")));
        Node defNode = Util.getNode(node, "definition");
        method.setExtern(Util.getNodeText( defNode).matches(
                        "(^\\s*)extern(\\s).*"));

        this.doParseParams(node, method);
        this.doParseTemplateParams(node, method);
        this.getCurrentClass().getMethodList().add(method);

        return method;
    }

    private void doParseParams(Node node, ParameteredItem item) {
        for(Node child : Util.getChildNodesByName(node, "param")) {
            Parameter param = new Parameter(
                Util.getNodeText(Util.getNode(child, "declname")),
                this.getType(child),
                Util.getNodeText(Util.getNode(child, "defval")),
                item
                            );
            if(! param.getType().isVoid()) {
                item.getParameterList().add(param);
            }
        }
    }

    private void doParseTemplateParams(Node nNode, ParameteredItem item) {
        Node node = Util.getNode(nNode, "templateparamlist");
        if(node == null) {
            return;
        }

        item.setTemplate(true);

        for(Node child : Util.getChildNodesByName(node, "param")) {
            String baseType = Util.getNodeText(Util.getNode(child, "type"));
            String array = Util.getNodeText(Util.getNode(child, "array"));
            String name = Util.getNodeText(Util.getNode(child, "declname"));

            if(name == null) {
                name = baseType;
                name = name.replaceAll("\\btypename\\b", "");
                name = name.replaceAll("\\bclass\\b", "");
                name = name.trim();
                if(baseType.matches("\\btypename\\b.*")) {
                    baseType = "typename";
                }
                else if(baseType.matches("\\bclass\\b.*")) {
                    baseType = "class";
                }
            }

            Parameter param = new Parameter(
                name,
                new CppType(this.metaInfo.getTypeSolver(), baseType, array),
                Util.getNodeText(Util.getNode(child, "defval")),
                item
                            );
            item.getTemplateParameterList().add(param);
        }
    }

    private Item doParseField(Node node, String name) {
        CppField field = new CppField(name, this.getType(node));

        field.setStatic(Util.isValueYes(Util.getAttribute(node, "static")));
        field.setMutable(Util.isValueYes(Util.getAttribute(node, "mutable")));
        
        //scturner added saturner
        List<Node> initializers = Util.getChildNodesByName(node, "initializer");
        //should only be one
        if (initializers.size() >= 1) {
            field.setInitializer(Util.getNodeText(initializers.get(0)));
        }
        //it is a constant if there is no pointer after the const
        field.setConst(field.getType().getLiteralType().matches(
                        ".*(^|\\s|[*])const($|\\s|&)[^*]*")); 
        //		System.out.println(field.getType().getLiteralType() + " " + field.getType().getLiteralType().matches(
        //		    ".*(^|\\s|[*])const($|\\s|&)[^*]*"));
        //end addition
        field.setVolatile(field.getType().getLiteralType().matches(
                        ".*(^|\\s|[*])volatile($|\\s|&).*"));
        field.setExtern(field.getType().getLiteralType().matches(
                        "(^\\s*)extern(\\s).*"));

        Node bitFieldNode = Util.getNode(node, "bitfield");
        if(bitFieldNode != null) {
            field.setBitField(Integer.parseInt(Util.getNodeText(bitFieldNode)));
        }

        this.getCurrentClass().getFieldList().add(field);

        return field;
    }

    private Item doParseEnum(Node node, String name) {
        CppEnum cppEnum = new CppEnum(name);
        this.getCurrentClass().getEnumList().add(cppEnum);
        for(Node child : Util.getChildNodesByName(node, "enumvalue")) {
            cppEnum.addValue(
                Util.getNodeText(Util.getNode(child, "name")),
                Util.getNodeText(Util.getNode(child, "initializer"))
                            );
        }

        return cppEnum;
    }

    private Item doParseConstant(Node node /*, String name*/) {
        if(Util.getNode(node, "param") != null) {
            return null;
        }

        Constant constant = new Constant(
            Util.getNodeText(Util.getNode(node, "name")),
            Util.getNodeText(Util.getNode(node, "initializer"))
                        );

        this.getCurrentClass().getConstantList().add(constant);

        return constant;
    }

    private Item doParseTypedef(Node node, String name) {
        Typedef typedef = new Typedef(name, this.getType(node));

        this.getCurrentClass().getTypedefList().add(typedef);

        return typedef;
    }

}
