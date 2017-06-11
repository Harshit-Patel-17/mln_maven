package iitd.data_analytics.mln.inference;

import iitd.data_analytics.mln.gpu.GpuConfig;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.State;

import static jcuda.driver.JCudaDriver.*;
import jcuda.driver.*;
import jcuda.runtime.JCuda;

public class SatisfiedGroundingCounter {
  
  public static long[] count(Formula[] formulas, State state) {
    long[] counts = new long[formulas.length];
    int totalGpus = Math.min(GpuConfig.totalGpus, formulas.length);
    int formulasPerThread = (int)Math.ceil(1.0 * formulas.length / totalGpus);
    CountingThread[] countingThreads = new CountingThread[totalGpus];
    //Thread[] threads = new Thread[totalGpus];
    
    for(int i = 0; i < totalGpus; i++) {
      int startIdx = i * formulasPerThread;
      int endIdx = Math.min((i+1) * formulasPerThread, formulas.length) - 1;
      countingThreads[i] = new CountingThread(i, state, formulas, startIdx, endIdx);
      //threads[i] = new Thread(countingThreads[i]);
    }
    
    //Launch threads
    for(Thread thread : countingThreads) {
      thread.start();
    }
    
    //Join threads
    for(Thread thread : countingThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        System.out.println("Error in joining threads");
        e.printStackTrace();
        System.exit(1);
      }
    }
    
    //Accumulate results
    int idx = 0;
    for(CountingThread countingThread : countingThreads) {
      for(long count : countingThread.getCounts()) {
        counts[idx] = count;
        idx++;
      }
    }
    
    return counts;
  }
  
}

class CountingThread extends Thread{

  int gpuNo;
  private iitd.data_analytics.mln.mln.State state;
  private Formula[] formulas;
  private long[] counts;
  
  public CountingThread(int _gpuNo, iitd.data_analytics.mln.mln.State _state, Formula[] _formulas, int startIdx, int endIdx) {
    gpuNo = _gpuNo;
    state = _state;
    formulas = new Formula[endIdx - startIdx + 1];
    for(int i = startIdx; i <= endIdx; i++) {
      formulas[i - startIdx] = _formulas[i];
    }
    counts = new long[endIdx - startIdx + 1];
  }
  
  public long[] getCounts() {
    return counts;
  }

  @Override
  public void run() {
    /*CUdevice device = new CUdevice();
    assert cuDeviceGet(device, 1-gpuNo) == CUresult.CUDA_SUCCESS;
    CUcontext context = new CUcontext();
    assert cuCtxCreate(context, 0, device) == CUresult.CUDA_SUCCESS;*/
    cuCtxSetCurrent(GpuConfig.context[gpuNo]);
    //cuCtxPushCurrent(GpuConfig.context[2]);
    //cudaSetDevice(gpuNo);
    
    for(int i = 0; i < formulas.length; i++) {
      //counts[i] = formulas[i].countSatisfiedGroundingsNoDb(state, gpuNo);
      counts[i] = formulas[i].countSatisfiedGroundings(state, gpuNo);
    }
    
    /*cuCtxDestroy(context);*/
  }
  
}
