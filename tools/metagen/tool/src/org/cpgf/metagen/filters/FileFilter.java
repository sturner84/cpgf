package org.cpgf.metagen.filters;

import org.cpgf.metagen.Config;
import java.util.regex.Pattern;
//import java.util.regex.Matcher;

// -------------------------------------------------------------------------
/**
 *  Filters out files that will not have metadata generated
 *  This class filters out the files that will not have metadata generated.
 *  This is done by excluding source files (by extension 
 *  [.cpp, .C, .c, .cc, .cxx, .c++, .cp]) and by excluding by regular
 *  expression.
 *
 *  @author  scturner
 *  @version Sep 5, 2013
 */
public class FileFilter
{
//    private boolean excludeSrc;
    
    private Pattern excludePattern;
   // private Matcher excludeExp;
    
    private static final String SRC_EXCLUSION_PATTERN = ".*(?:" 
                    + Config.SRC_EXTENSIONS + ")";
    
    private static FileFilter instance = null;
    private static Config localConfig = null;
    
    private FileFilter(Config config) {
//        excludeSrc = config.excludeSource;
        
        String patternStr = "";
        
        if (config.excludeRegEx == null) {
            excludePattern = Pattern.compile("");            
        }
        else { //pattern to be used
            excludePattern = Pattern.compile(config.excludeRegEx);
        }
        
        if (localConfig.excludeSource) {
            patternStr = "(?:(?:" + SRC_EXCLUSION_PATTERN + ")|(?:" 
               + excludePattern + "))";
        }
        else {
            patternStr = "(?:" + excludePattern + ")";
        }
        
        excludePattern = Pattern.compile(patternStr);
        
       // excludeExp = new Matcher(excludePattern);
    }
    
    /**
     *  Gets an instance of a FileFilter.  
     *  
     *  It uses the configuration file to determine what files to filter.
     *  
     *  @param config Configuration file
     *  @return a FileFilter object (singleton)
     */
    public static FileFilter getFilter(Config config) {
        if (config != null | localConfig != config) {
            localConfig = config;
            instance = new FileFilter(localConfig);
        }
        
        return instance;
    }
    
    /**
     * Determines if a file should be accepted.  
     * 
     * @param filename Name of the file to filter
     * 
     * @return true if the file is acceptable, false otherwise
     */
    public boolean acceptFile(String filename) {
        return !excludePattern.matcher(filename).matches();
    }
}
