package iitd.data_analytics.mln.mln;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import iitd.data_analytics.mln.exceptions.MlnParseException;
import iitd.data_analytics.mln.factory.MlnFactory;
import iitd.data_analytics.mln.gpu.GpuConfig;

import static jcuda.driver.JCudaDriver.*;
import jcuda.driver.*;
import jcuda.*;

public class App 
{
  public static void main( String[] args ) throws Exception
  {
    if(args.length == 0)
      throw new Exception("Expecting input xml file name as an argument.");
    InputParams inputParams = new InputParams(args[0]);
    
    //Create GPU context
    GpuConfig.initGpuConfig(inputParams);
    //cuCtxSetCurrent(GpuConfig.context[0]);
    /*CUdevice device = new CUdevice();
    cuDeviceGet(device, 0);
    CUcontext context = new CUcontext();
    cuCtxCreate(context, 0, device);*/
    
    MlnFactory mlnFactory = new MlnFactory();
    mlnFactory.createMln(inputParams);
    
    //Destroy GPU context
    /*cuCtxDestroy(context);*/
  }
}
