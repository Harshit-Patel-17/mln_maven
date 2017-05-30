package iitd.data_analytics.mln.gpu;

import static jcuda.driver.JCudaDriver.*;
import jcuda.driver.*;

public class GpuConfig {
  public static int maxThreads;
  public static String ptxBase;//"src/main/java/iitd/data_analytics/mln/gpu/";
  public static int totalGpus;
  public static CUdevice[] device;
  public static CUcontext[] context;
  
  public static void initGpuConfig() {
    cuInit(0);
    
    maxThreads = 1024;
    
    ptxBase = "";
    
    int[] _totalGpus = new int[1];
    cuDeviceGetCount(_totalGpus);
    totalGpus = _totalGpus[0];
    System.out.println("Available GPUs:" + totalGpus);
    
    device = new CUdevice[totalGpus];
    for(int i = 0; i < device.length; i++) {
      device[i] = new CUdevice();
      cuDeviceGet(device[i], i);
    }
    
    context = new CUcontext[totalGpus];
    for(int i = 0; i < context.length; i++) {
      context[i] = new CUcontext();
      cuCtxCreate(context[i], 0, device[i]);
    }
  }
}
