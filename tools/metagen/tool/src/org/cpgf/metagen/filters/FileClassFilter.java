package org.cpgf.metagen.filters;

import java.io.*;
//import java.util.*;
//import java.util.regex.*;

//-------------------------------------------------------------------------
/**
*  Looks through a file and a class definition matching a class name.
*  
*  
*
*  @author  scturner
*  @version Oct 1, 2013
*/
public class FileClassFilter extends CppBlockFilter
{
    private static final String CLASS_HEADER =  
                    "(?ms:(?:(?:template\\s*<[^>{]*?>\\s*)|(?:\\s|^))" +
                    "class\\s(?:[^{]*?\\s+)*?%s" +
                    "\\s(?:[^{]*?\\{))";
    
    /**
     * Creates a FileLineFilter with a File.
     * 
     * This does not throw an error if the file does not exist.
     * 
     * @param file File to search
     */
    public FileClassFilter(File file) {
        super(file);
    }
    
    /**
     * Creates a FileLineFilter with a String.
     * 
     * This does not throw an error if the file does not exist.
     * 
     * @param file Name of the file to search
     */
    public FileClassFilter(String fileName) {
        super(fileName);
    }
    
    
    /**
     * Searches through the file for the definition of a class with the given
     * class name.  If found, the entire class definition is returned.
     * 
     * @param className The name of the class to find
     *  
     * @return A String with the class definition in it or null.
     */
    public String find(String className) throws IOException {
        return findPatternBlock(String.format(CLASS_HEADER, className)) + ";";        
    }
    
    
}
