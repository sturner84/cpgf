package org.cpgf.metagen.metadata;

import java.util.*;

import org.cpgf.metagen.cppparser.*;

// -------------------------------------------------------------------------
/**
 *  Contains a list of classes that should have a copy function generated for 
 *  them.
 *
 *  @author  Schnook
 *  @version May 29, 2014
 */
public class CopyFunctionList
{
    private static CopyFunctionList instance;
    
    private LinkedList<CppClass> classes;
    
    // ----------------------------------------------------------
    /**
     * Gets a singleton to this class
     * @return Instance of this class
     */
    public static CopyFunctionList getInstance() {
        if (instance == null) {
            instance = new CopyFunctionList();
        }
        
        return instance;
    }
        
    private CopyFunctionList()
    {
        classes = new LinkedList<CppClass>();
    }
    
    
    private Set<String> getMethodReturnTypes(
        List<? extends CppInvokable> invokables) {
        HashSet<String> returnTypes = new HashSet<String>();
        ParsedType p;
        
        for (CppInvokable i : invokables) {
            p = i.getResultType().getParsedType();
            
            if (!p.isPointerOrReference() && !p.isArray()) {
                returnTypes.add(p.getBaseType());
            }
        }
        
        return returnTypes;
    }
    
    
    private Set<String> getReturnTypes(CppClass c) {
        HashSet<String> returnTypes = new HashSet<String>();
        
        if (c != null) {
            returnTypes.addAll(getMethodReturnTypes(c.getMethodList()));
            returnTypes.addAll(getMethodReturnTypes(c.getOperatorList()));
            
            for (DeferClass d : c.getClassList()) {
                returnTypes.addAll(getReturnTypes(d.getCppClass()));
            }
        }
                
        return returnTypes;
    }
    
    
    private Set<String> getReturnTypes(List<CppClass> allClasses) {
        HashSet<String> returnTypes = new HashSet<String>();
        
        for (CppClass c : allClasses) {
            returnTypes.addAll(getReturnTypes(c));           
        }
                
        return returnTypes;
    }
    
    
    // ----------------------------------------------------------
    /**
     * Looks at the list of classes and adds everyone that appears as a return
     * value in a method/function.
     * 
     * @param allClasses List of all reflected classes.
     */
    public void addClasses(List<CppClass> allClasses) {
        Set<String> returnTypes = getReturnTypes(allClasses);
        
        for (CppClass c : allClasses) {
            //TODO use qualified name?
            if (returnTypes.contains(c.getQualifiedName())) {
                classes.add(c);
            }
        }
    }
    
    // ----------------------------------------------------------
    /**
     * Determines if this class should have a copy function created for it.
     * @param c Class to check
     * @return true if it is in the list of classes to create a copy function
     * for.
     */
    public boolean containsClass(CppClass c) {
        return containsClass(c.getQualifiedName());
    }
    
    
    // ----------------------------------------------------------
    /**
     * Determines if this class should have a copy function created for it.
     * @param qName Name of the class to check
     * @return true if it is in the list of classes to create a copy function
     * for.
     */
    public boolean containsClass(String qName) {
        for (CppClass iClass : classes) {
            if (iClass.getQualifiedName().equals(qName)) {
                return true;
            }
        }
        
        return false;
    }
   
   
}
