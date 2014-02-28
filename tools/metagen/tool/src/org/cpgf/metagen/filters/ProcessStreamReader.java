package org.cpgf.metagen.filters;

import java.io.*;

// -------------------------------------------------------------------------
/**
 *  Reads from an InputStream in a separate thread.
 *  Useful when spawning a process as it can read from its output and
 *  error streams and stop the process from blocking (see the JavaDoc for
 *  the class Process).
 *
 *  @author  scturner
 *  @version Jan 18, 2014
 */
public class ProcessStreamReader extends Thread
{
    private StringBuilder msg;
    private InputStream stream;
    
    /**
     * Creates a reader with the specified input stream
     * 
     * @param iStream Input stream to read from.
     */
    public ProcessStreamReader(InputStream iStream)
    {
        stream = iStream;
        msg = new StringBuilder();
    }
    
    @Override
    public void run() {
        
        try {
            BufferedReader input = new BufferedReader(
                new InputStreamReader(stream));
            String iLine;
        
            iLine = input.readLine();
            while (iLine != null) {
                msg.append(iLine);
                msg.append("\n");
                iLine = input.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the message read from the input stream
     * @return message from the input stream
     */
    public String getMessage() {
        return msg.toString();
    }
}
