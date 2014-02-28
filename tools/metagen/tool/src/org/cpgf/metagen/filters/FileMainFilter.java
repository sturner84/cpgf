package org.cpgf.metagen.filters;

import java.io.*;

public class FileMainFilter extends CppBlockFilter
{

    private static final String MAIN_REGEX = "(?:^|\\s|;)int\\s+main[^{]*?\\{";
    
    public FileMainFilter( File file )
    {
        super( file );
    }

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
     */
    public String findMain() throws IOException {
        return findPatternBlock(MAIN_REGEX);        
    }

}
