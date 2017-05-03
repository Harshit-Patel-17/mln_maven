package iitd.data_analytics.mln.mln;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import iitd.data_analytics.mln.exceptions.MlnParseException;
import iitd.data_analytics.mln.factory.MlnFactory;

import static jcuda.driver.JCudaDriver.*;
import jcuda.driver.*;

/**
 * Hello world!
 *
 */
public class App 
{
  public static void main( String[] args ) throws MlnParseException, IOException
  {
    //Create GPU context
    cuInit(0);
    CUdevice device = new CUdevice();
    cuDeviceGet(device, 0);
    CUcontext context = new CUcontext();
    cuCtxCreate(context, 0, device);
    
    String inputFile = "src/test/antlr4/example.mln";
    System.out.println("Parsing Mln File...");
    MlnFactory mlnFactory = new MlnFactory();
    File f = new File(inputFile);
    InputStream in = new FileInputStream(f);
    mlnFactory.createMln(in);
    System.out.println("Successfully Parsed Mln File.");
    
    //Destroy GPU context
    cuCtxDestroy(context);
  }
}
