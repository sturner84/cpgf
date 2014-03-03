package org.cpgf.metagen.filters;

import java.io.*;

// -------------------------------------------------------------------------
/**
 *  Searches a file for the main method.  
 *  
 *  If a main method is found, the entire method is extracted and returned.
 *
 *  @author  Schnook
 *  @version Feb 28, 2014
 */
public class FileMainFilter extends CppBlockFilter
{

    private static final String MAIN_REGEX = "(?:^|\\s|;)int\\s+main[^{]*?\\{";
    
    /**
     * Creates a filter with the supplied file
     * 
     * @param file file to filter
     */    
    public FileMainFilter( File file )
    {
        super( file );
    }

    /**
     * Creates a filter with the supplied file name
     * 
     * @param fileName file name to filter
     */
    public FileMainFilter( String fileName )
    {
        super( fileName );
    }
    
    /**
     * Searches through the file for the definition of a class with the given
     * class name.  If found, the entire class definition is returned.
     * 
     * @param className The name of the class to find
     *  
     * @return A String with the class definition in it or null.
     * @throws IOException If file is invalid or cannot be read
     */
    public String findMain() throws IOException {
        return findPatternBlock(MAIN_REGEX);        
    }

}
