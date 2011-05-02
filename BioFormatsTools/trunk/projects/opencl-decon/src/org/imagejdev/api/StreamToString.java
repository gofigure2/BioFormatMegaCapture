package org.imagejdev.api;

//credit http://www.kodejava.org/examples/266.html
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StreamToString {

    public static String getString( InputStream inputStream, boolean printIt ) throws Exception {
        StreamToString sts = new StreamToString();

        String fileString = sts.convertStreamToString( inputStream );
        /*
         * Call the method to convert the stream to string
         */
        if ( printIt ) System.out.println( fileString );
        
        return fileString;
    }

    public String convertStreamToString( InputStream is )
            throws IOException {
        /*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         */
        if (is != null ) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1 ) 
                {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {        
        	return "";
        }
    }
}