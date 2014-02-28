package org.cpgf.metagen.filters;

import org.cpgf.metagen.doxyxmlparser.FileMap;
import org.cpgf.metagen.Config;

import java.util.*;

// -------------------------------------------------------------------------
/**
 *  This class takes a list of source and header file names and determines
 *  which source files do not have a corresponding header file.  In this case, 
 *  corresponding means a header file with the same name (but different 
 *  extension).  The files can be located in different directories. 
 *
 *  @author  scturner
 *  @version Sep 6, 2013
 */
public class UnmatchedSourceFilter
{
    //private static final String SRC_EXTENSIONS = "\\.cpp|\\.C|\\.c|\\.cc|\\.cxx|\\.c++|\\.cp";
    //private static final String HEADER_EXTENSIONS = "\\.hpp|\\.H|\\.h|\\.hh|\\.hxx|\\.h++|\\.hp";
    
    //private FileMap map;
    private HashSet<String> fileSet;
    private String fileStr;
    private Config config;
    
    /**
     * Creates a filter for the source files
     * 
     * @param files File map with all of the files being processed
     * @param cfg Configuration for the program.
     */
    public UnmatchedSourceFilter(FileMap files, Config cfg) {
        setup(files.getFileMap().keySet(), cfg);        
    }
    
    
    /**
     * Creates a filter for the source files
     * 
     * @param files Collection with all of the files being processed
     * @param cfg Configuration for the program.
     */
    public UnmatchedSourceFilter(Collection<String> files, Config cfg) {
        setup(files, cfg);
    }
    
    
    private void setup(Collection<String> files, Config cfg) {
        fileSet = new HashSet<String>();
        fileSet.addAll(files);
        //get a string with all of the keys.  This will make it easier to 
        //search for the values
        fileStr = fileSet.toString();
        fileStr = fileStr.replaceAll("\\\\", "/");
        config = cfg;
    }
    
    private String getExtension(String fileName) {
        //find last . in file name
        if (fileName.lastIndexOf(".") >= 0) {
            //include . in the extension
            return fileName.substring( fileName.lastIndexOf(".") );
        }
        
        return "";
    }
    
    private String removeExtension(String fileName) {
      //find last . in file name
        if (fileName.lastIndexOf(".") >= 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        
        return fileName;
    }
    
    private boolean isSourceFile(String fileName) {
        return (getExtension(fileName).matches( "(?:" + Config.SRC_EXTENSIONS + ")"));
    }
    
    private String removePath(String fileName) {
        if (fileName.lastIndexOf("/") >= 0) {
            return fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        
        return fileName;
    }
    
    private String getPath(String fileName) {
        if (fileName.lastIndexOf("/") >= 0) {
            return fileName.substring(0, fileName.lastIndexOf("/") + 1);
        }
        
        return fileName;
    }
    
    private boolean hasMatchingHeader(String fileName) {
        String nameNoPath = removePath(fileName);
        
        if (isSourceFile(nameNoPath)) {
            return fileStr.matches(".*[\\s\\[/]" + removeExtension(nameNoPath) 
                + "(?:" + Config.HDR_EXTENSIONS + ").*");
        }
        //if not a source file, this doesn't matter
        return true;
    }
    
    /**
     * Gets a file name that is suitable for a header file for that source file.
     * This removes the extension and replaces it with the extension specified
     * in the configuration (headerExtension). It also prefixes the file name
     * with "fake_"
     * 
     * @param fileName Name of the file to get the header name for
     * 
     * @return A suitable name for the header file.
     */
    public String getHeaderName(String fileName) {
        return getPath(fileName) + config.createHeaderFilesPrefix
           + removeExtension(removePath(fileName)) + config.headerExtension;        
    }
    
    /**
     * Returns a list of all source files without a header file that is named
     * the same (with a different extension of .h, .hpp, etc.)
     * 
     * @return List of unmatched source files
     */
    public Set<String> getUnmatchedSourceFiles() {
        HashSet<String> unmatched = new HashSet<String>();
        
        for (String fileName : fileSet) {
            fileName = fileName.replaceAll("\\\\", "/");
            if (!hasMatchingHeader(fileName)) {
                unmatched.add( fileName );
            }
        }
        
        return unmatched;
    }
    
}
