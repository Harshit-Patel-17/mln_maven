package iitd.data_analytics.mln.gpu;

import static jcuda.driver.JCudaDriver.*;

import iitd.data_analytics.mln.mln.InputParams;
import jcuda.driver.*;

public class GpuConfig {
  public static int maxThreads;
  public static String ptxBase;//"src/main/java/iitd/data_analytics/mln/gpu/";
  public static int totalGpus;
  public static int maxBatchSize;
  
  public static CUdevice[] device;
  public static CUcontext[] context;
  public static CUmodule[] mlnCudaKernels;
  public static CUmodule[] utilCudaKernels;
  
  public static void initGpuConfig(InputParams params) {
    cuInit(0);
    
    maxThreads = params.getMaxThreads();
    
    ptxBase = params.getPtxFilesPath();//"src/main/java/iitd/data_analytics/mln/gpu/";
    
    maxBatchSize = params.getMaxBatchSize();
    
    int[] _totalGpus = new int[1];
    cuDeviceGetCount(_totalGpus);
    totalGpus = _totalGpus[0];
    System.out.println("Available GPUs:" + totalGpus);
    
    //Get Devices
    device = new CUdevice[totalGpus];
    for(int i = 0; i < device.length; i++) {
      device[i] = new CUdevice();
      cuDeviceGet(device[i], i);
    }
    
    //Create contexts
    context = new CUcontext[totalGpus];
    for(int i = 0; i < context.length; i++) {
      context[i] = new CUcontext();
      cuCtxCreate(context[i], 0, device[i]);
    }
    
    //Get modules
    mlnCudaKernels = new CUmodule[totalGpus];
    utilCudaKernels = new CUmodule[totalGpus];
    for(int i = 0; i < totalGpus; i++) {
      cuCtxSetCurrent(context[i]);
      mlnCudaKernels[i] = new CUmodule();
      cuModuleLoad(mlnCudaKernels[i], GpuConfig.ptxBase + "mlnCudaKernels.ptx");
      utilCudaKernels[i] = new CUmodule();
      cuModuleLoad(utilCudaKernels[i], GpuConfig.ptxBase + "utilCudaKernels.ptx");
    }
  }
}
