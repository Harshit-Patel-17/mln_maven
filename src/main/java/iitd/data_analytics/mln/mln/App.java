package iitd.data_analytics.mln.mln;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import iitd.data_analytics.mln.exceptions.MlnParseException;
import iitd.data_analytics.mln.factory.MlnFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws MlnParseException, IOException
    {
        String inputFile = "src/test/antlr4/example.mln";
        System.out.println( "Parsing Mln File..." );
        MlnFactory mlnFactory = new MlnFactory();
        File f = new File(inputFile);
        InputStream in = new FileInputStream(f);
        mlnFactory.createMln(in);
        System.out.println("Successfully Parsed Mln File.");
    }
}
