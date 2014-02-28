package org.cpgf.metagen.filters;

import java.io.*;
import java.util.*;
import java.util.regex.*;

abstract public class CppFileFilter
{
    /** File to search */
    protected File searchFile;
    /** List of data from the file (as lines or as a single String */
    protected LinkedList<String> buffer;
    /** Pattern used to filter the file before searching */
    protected String filterPattern;
    
    /** regex that matches c++ comments */
    protected static final String COMMENTS_REGEX = "(?://.*\\n)|" +
                    "(?ms:\\/\\*.*?\\*\\/)"; 
    
    /**
     * Creates a FileContentFilter with a File. Comments are filtered out.
     * 
     * This does not throw an error if the file does not exist.
     * 
     * @param file File to search
     */
    public CppFileFilter(File file) {
        searchFile = file;
        buffer = new LinkedList<String>();
        filterPattern = COMMENTS_REGEX;
    }
    
    /**
     * Creates a FileContentFilter with a String.  Comments are filtered out.
     * 
     * This does not throw an error if the file does not exist.
     * 
     * @param file Name of the file to search
     */
    public CppFileFilter(String fileName) {
        searchFile = new File(fileName);
        buffer = new LinkedList<String>();
        filterPattern = COMMENTS_REGEX;
    }
    
    /**
     * Creates a FileContentFilter with a File. The filter is applied to the
     * contents before any searches are done and the matching text is removed.
     * 
     * This does not throw an error if the file does not exist.
     * 
     * @param file File to search
     * @param filter Pattern to apply to the contents of the file before
     *  searching
     */
    public CppFileFilter(File file, String filter) {
        searchFile = file;
        buffer = new LinkedList<String>();
        filterPattern = filter;
    }
    
    /**
     * Creates a FileContentFilter with a String. The filter is applied to the
     * contents before any searches are done and the matching text is removed.
     * 
     * This does not throw an error if the file does not exist.
     * 
     * @param file Name of the file to search
     * @param filter Pattern to apply to the contents of the file before
     *  searching
     */
    public CppFileFilter(String fileName, String filter) {
        searchFile = new File(fileName);
        buffer = new LinkedList<String>();
        filterPattern = filter;
    }
    
    /**
     * Read the file and stores it in buffer
     * 
     * @param byLines Divides the file into lines (if true). Otherwise, it
     *  is read in as a single String
     */
    protected void bufferFile(boolean byLines)
                    throws IOException {
        
        if (buffer.isEmpty()) { //nothing buffered, so add the file contents
            Scanner input = new Scanner(searchFile);
            
            StringBuilder content = new StringBuilder();
            while (input.hasNextLine()) {
                content.append(input.nextLine());
                content.append("\n");
            }
           String contentStr = content.toString();
           
           if (filterPattern != null) {
               Pattern regex = Pattern.compile(filterPattern);
               Matcher match= regex.matcher(contentStr); 
               contentStr = match.replaceAll("");            
           }
            
            if (byLines) {
                String[] lines = contentStr.split("\\n");
                for (String line : lines) {
                    buffer.add(line);
                }
            }
            else {                
                buffer.add(contentStr);
            }
            input.close();
        }
    }
    

    
    
}
