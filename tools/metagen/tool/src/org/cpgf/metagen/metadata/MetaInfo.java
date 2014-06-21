package org.cpgf.metagen.metadata;

import java.util.*;

import org.cpgf.metagen.*;
import org.cpgf.metagen.doxyxmlparser.FileMap;
import org.cpgf.metagen.metawriter.OperatorNameMap;
import org.cpgf.metagen.metawriter.OutputCallbackClassMap;


public class MetaInfo {
	private Config config;
    private List<CppClass> classList; // top level classes
	private TypeSolver typeSolver;
    private List<CppClass> allClassList; // top level + inner classes
    private List<TemplateInstance> templateInstanceList;
    private OutputCallbackClassMap callbackClassMap;
    private OperatorNameMap operatorNameMap;
    private List<String> namespaces;

    public MetaInfo(Config config) {
    	this.config = config;
    	
        this.classList = new ArrayList<CppClass>();
		this.typeSolver = new TypeSolver(this, this.config.classTraits);
		
		this.templateInstanceList = new ArrayList<TemplateInstance>();
		
		this.callbackClassMap = new OutputCallbackClassMap(this.config);
		
		this.operatorNameMap = new OperatorNameMap();
		
		namespaces = new ArrayList<String>();
    }
    
    public Config getConfig() {
    	return this.config;
    }

    public List<CppClass> getClassList() {
        return this.classList;
    }

	public OutputCallbackClassMap getCallbackClassMap() {
		return callbackClassMap;
	}
	
	public OperatorNameMap getOperatorNameMap() {
		return this.operatorNameMap;
	}
	
	// ----------------------------------------------------------
	/**
	 * Gets the list of all namespaces
	 * @return List of all namespaces
	 */
	public List<String> getNamespaces() {
	    return namespaces;
	}
	
	// ----------------------------------------------------------
	/**
	 * Determines if this namespace exists
	 * @param namespace Namespace to check
	 * @return true if it exists
	 */
	public boolean doesNamespaceExist(String namespace) {
	    for (String ns : namespaces) {
	        if (ns.equals(namespace)) {
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	// ----------------------------------------------------------
	/**
	 * Adds a namespace to the list of namespaces.
	 * null is ignored.
	 * @param namespace Namespace to add
	 */
	public void addNamespace(String namespace) {
	    if (namespace != null && !doesNamespaceExist(namespace)) {
	        namespaces.add(namespace);
	    }
	}
	
	// ----------------------------------------------------------
    /**
     * Adds a namespace (as a list of separate namespaces) to 
     * the list of namespaces.
     * null is ignored.
     * @param namespace Namespace to add
     */
    public void addNamespace(List<String> namespace) {
        if (namespace != null) {

            String nsStr = Util.joinStringList("::", namespace);
           
            if (!doesNamespaceExist(nsStr)) {
                namespaces.add(nsStr);
            }

        }
    }
	

	private String getNamespaceFromClassName(String className) {
	    if (className == null || className.equals("")) {
	        return "";
	    }

	    String namespace = className;
	    
	    if (doesNamespaceExist(namespace)) {
	        return namespace;
	    }
	    
	    int pos = className.lastIndexOf("::");
	    if (pos >= 0) {
	        return getNamespaceFromClassName(className.substring(0, pos));
	    }
	    
	    return "";
	}
	
	/**
	 * Does post processing on the data to be reflected. 
	 * 
	 * Fixes globals, base and inner classes, and the relationship between
	 * classes.
	 * 
	 *  @param fileMap Files mapped to their classes
	 */
    public void fixup(FileMap fileMap) {
    	this.doFixupGlobals();
    	this.doFixupBaseClasses();
    	this.doFixupInnerClasses();
    	this.doFixupOwnerClasses();
    	this.doBuildAllClassList();

    	Map<String, String> configTypeMap = null;
		if(config.parameterTypeReplacer != null) {
			configTypeMap = new HashMap<String, String>();
			for(int i = 0; i < config.parameterTypeReplacer.length ; i += 2) {
				configTypeMap.put(config.parameterTypeReplacer[i], config.parameterTypeReplacer[i + 1]);
			}
		}
		
    	for(CppClass c : this.classList) {
    		if(! c.isGlobal()) {
    			c.resolveTypesForClass();
    		}
    		if(configTypeMap != null) {
    			c.resolveParameterTypes(configTypeMap, null);
    		}
    	}
    	
    	this.doFixupTemplateInstances();
    	
    	this.callbackClassMap.build(this.getClassList(), fileMap);
    	
    	this.doFixupNamespaces();
    }
    
    private void doFixupTemplateInstances() {
    	String[] names = this.config.predefinedTemplateInstances;
    	for(int i = 0; i < names.length; i += 2) {
    		this.doFixupTemplateInstance(names[i], names[i + 1]);
    	}
    }
    
    private void doFixupTemplateInstance(String fullType, String mapName) {
    	int index = fullType.indexOf('<');
    	if(index <= 0) { // can't be first character too.
    		return;
    	}
    	
    	String name = fullType.substring(0, index).trim();
    	CppClass cppClass = this.typeSolver.getCppClass(name);
    	if(cppClass != null) {
    		this.templateInstanceList.add(new TemplateInstance(fullType, cppClass, mapName));
    	}
    }
    
    private void doBuildAllClassList() {
    	this.allClassList = new ArrayList<CppClass>();
    	
    	for(CppClass c : this.classList) {
    		this.doBuildAllClassListForSingleClass(c);
    	}
    }

    private void doBuildAllClassListForSingleClass(CppClass cppClass) {
    	if(cppClass == null) {
    		return;
    	}

    	if(! cppClass.isGlobal()) {
    		this.allClassList.add(cppClass);
    	}

    	for(DeferClass c : cppClass.getClassList()) {
    		this.doBuildAllClassListForSingleClass(c.getCppClass());
    	}
    }

    private void doFixupGlobals() {
    	HashMap<String, CppClass> globalMap = new HashMap<String, CppClass>();

    	boolean finished = false;
    	while(! finished) {
    		finished = true;

    		for(int i = 0; i < this.classList.size(); ++i) {
    			CppClass c = this.classList.get(i);
    			if(! c.isGlobal()) {
    				continue;
    			}

    			this.doFixupGlobalItems(globalMap, c.getFieldList());
    			this.doFixupGlobalItems(globalMap, c.getMethodList());
    			this.doFixupGlobalItems(globalMap, c.getOperatorList());
    			this.doFixupGlobalItems(globalMap, c.getEnumList());
    			this.doFixupGlobalItems(globalMap, c.getConstantList());
    			this.doFixupGlobalItems(globalMap, c.getTypedefList());

    			this.classList.remove(i);
    			finished = false;
    			break;
    		}
    	}

    	for(CppClass c : globalMap.values()) {
    		this.classList.add(0, c);
    	}
    }

    private <T extends Item> void doFixupGlobalItems(HashMap<String, CppClass> globalMap, List<T> itemList) {
    	for(Item item : itemList) {
    		String location = item.getLocation();
    		if(! globalMap.containsKey(location)) {
    			CppClass c = new CppClass(null);
    			c.setLocation(location);
    			globalMap.put(location, c);
    		}
    		globalMap.get(location).addItem(item);
    	}
    }
    
    private void doFixupBaseClasses() {
    	for(CppClass c : this.classList) {
    		for(DeferClass deferClass : c.getBaseClassList()) {
    			deferClass.resolve(this.classList);
    		}
    	}
    }

    private void doFixupInnerClasses() {
    	for(CppClass c : this.classList) {
    		for(DeferClass deferClass : c.getClassList()) {
    			deferClass.resolve(this.classList);
    			deferClass.getCppClass().setInner(true);
    		}
    	}
    	
    	for(int i = this.classList.size() - 1; i >= 0; --i) {
    		if(this.classList.get(i).isInner()) {
    			this.classList.remove(i);
    		}
    	}
    }

    private void doFixupOwnerClasses() {
    	for(CppClass c : this.classList) {
    		c.fixupOwners();
    	}
    }

	public TypeSolver getTypeSolver() {
		return typeSolver;
	}

	public CppClass findClassByName(String name) {
		String re = ".*\\b" + name + "$";
		
		for(CppClass cppClass : this.allClassList) {
			if(cppClass.getQualifiedName().matches(re)) {
				return cppClass;
			}
		}
		
		return null;
	}
	
	public List<TemplateInstance> findTemplateInstances(CppClass cppClass) {
		List<TemplateInstance> list = null;
		for(TemplateInstance templateInstance : this.templateInstanceList) {
			if(templateInstance.getTemplateClass() == cppClass) {
				if(list == null) {
					list = new ArrayList<TemplateInstance>();
				}
				list.add(templateInstance);
			}
		}

		return list;
	}
	
	
	private void doFixupItemNamespaces(List<Item> items, List<String> namespace) {
	    for (Item i : items) {
	        i.setNamespaces(namespace);
	    }
	}
	
	private void doFixupNamespaces() {
	    String namespace;
	    
	    for (CppClass c : allClassList) {
	        namespace = getNamespaceFromClassName(c.getLiteralName());
	        String[] allNamespaces = namespace.split("::");
	        
	        List<String> nsStrs = Arrays.asList(allNamespaces);
	        c.setNamespaces(nsStrs);
	        
	        LinkedList<Item> allItems = new LinkedList<Item>();
	        c.getAllItems(allItems);
	        
	        doFixupItemNamespaces(allItems, nsStrs);
	    }
	}
}
