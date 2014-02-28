package org.cpgf.metagen.filters;

import java.io.File;
import java.util.List;
import java.util.LinkedList;
//import java.util.Scanner;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// -------------------------------------------------------------------------
/**
 *  Looks through a file and finds a list of lines (or parts of lines) 
 *  that match a pattern (like grep). It can also search the entire file for 
 *  instances of the pattern.
 *  
 *  
 *
 *  @author  scturner
 *  @version Oct 1, 2013
 */
public class FileContentFilter extends CppFileFilter
{
    
    public FileContentFilter( File file, String filter )
    {
        super( file, filter );
    }

    public FileContentFilter( File file )
    {
        super( file );
    }

    public FileContentFilter( String fileName, String filter )
    {
        super( fileName, filter );
    }

    public FileContentFilter( String fileName )
    {
        super( fileName );
    }

    /**
     * Searches through the file for instances of the pattern.  If partial 
     * is true, then only the section of the line that matches the pattern
     * is stored.  If false, the entire line is stored.
     * 
     * @param pattern The pattern to compare against
     * @param partial True if only the matched part of the line should be 
     *  returned. False if the whole line should be.
     *  
     * @return A list of (partial) lines that match the pattern.
     */
    private List<String> find(String pattern, boolean partial) {
        List<String> matches = new LinkedList<String>();
        
        Pattern regex = Pattern.compile(pattern);
        Matcher match;
        
        for (String currentLine : buffer) {
            match = regex.matcher(currentLine);
            //matched part of the line

            if (partial) {
                int start = 0;
                while (start < currentLine.length()) {
                    match.region(start, currentLine.length());   
                    if (match.find()) {
                        matches.add(currentLine.substring(match.start(),
                            match.end()));
                        
                        start = match.end();
                    }
                    else {
                        start = currentLine.length();
                    }
                }                    
            }
            else {
                if (match.find()) {
                    matches.add(currentLine);
                }
            }

        }

        return matches;
    }
    
    /**
     * Searches through the file for instances of the pattern.  Any lines 
     * that match the pattern are returned.
     * 
     * @param pattern The pattern to compare against
     *       
     * @return A list of lines that match the pattern.
     */
    public List<String> findLines(String pattern) throws IOException {
        bufferFile(true);
        return find(pattern, false);
    }
    
    /**
     * Searches through the file for instances of the pattern.  If a line 
     * contains that pattern, the part that matches is returned. 
     * 
     *  
     * @param pattern The pattern to compare against
     *     
     * @return A list of partial lines that match the pattern.
     */
    public List<String> findPartialLines(String pattern) throws IOException {
        bufferFile(true);
        return find(pattern, true);
    }
    
    /**
     * Searches through the file for instances of the pattern.  
     * 
     * This method searches across lines and returns all matches
     * 
     *  
     * @param pattern The pattern to compare against
     *     
     * @return A list of values that match the pattern.
     */
    public List<String> find(String pattern) throws IOException {
        bufferFile(false);
        return find(pattern, true);
    }
    
    
}
