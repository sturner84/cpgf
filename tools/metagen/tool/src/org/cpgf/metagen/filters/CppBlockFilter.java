package org.cpgf.metagen.filters;

import java.io.*;
import java.util.regex.*;

abstract public class CppBlockFilter extends CppFileFilter
{

    public CppBlockFilter( File file )
    {
        super( file );
    }


    public CppBlockFilter( String fileName )
    {
        super( fileName );
    }


    public CppBlockFilter( File file, String filter )
    {
        super( file, filter );
    }


    public CppBlockFilter( String fileName, String filter )
    {
        super( fileName, filter );
    }
    
    /**
     * Searches the file for the block of code that matches the pattern 
     * (i.e. a class or a function)
     * 
     * @param pattern Pattern to find
     * 
     * @return The block (class/function) that matches the pattern 
     */
    protected String findPatternBlock(String pattern) throws IOException {
        bufferFile(false); //it would be bad to breakt this into lines
        
        int start = -1;
        int end = -1;
        
        Pattern regex = Pattern.compile(pattern);
        Matcher match;
        String contents = buffer.get(0); //File should be in the first position.
        
        match = regex.matcher(contents);
        if (match.find()) {
            //found it, so extract it
            start = match.start();
            boolean foundCurly = false;
            int curlyCount = 0;
            int slashCount = 0;
            boolean inChar = false;
            boolean inString = false;
            
            //starting at the beginning of the match, continue through the 
            //rest of the file counting opening and closing { }'s.  After the
            //first { is encountered, the next time curlyCount is at 0, is the 
            //end of the class.
            for (int i = start; i < contents.length() && 
                            (curlyCount != 0 || !foundCurly); i++) {
                switch (contents.charAt(i)) { 
                    case '{':
                        if (!inString && !inChar) {
                            foundCurly = true;
                            curlyCount++;
                        }
                        break;
                    case '}':
                        if (!inString && !inChar) {
                            curlyCount--;
                        }
                        break;
                    case '\'':
                        if (slashCount % 2 == 0 && !inString) {
                            inChar = !inChar;
                        }
                        break;
                    case '"':
                        if (slashCount % 2 == 0 && !inChar) {
                            inString = !inString;
                        }
                        break;                    
                }
                
                if (contents.charAt(i) == '\\') {
                    slashCount++;
                }
                else {
                    slashCount = 0;
                }                
             
                if (foundCurly && curlyCount == 0) {
                    end = i;
                }
                    
            }
            
        }
        if (end >= 0) {
            return contents.substring(start, end + 1);
        }
        
        return null;
    }

}
