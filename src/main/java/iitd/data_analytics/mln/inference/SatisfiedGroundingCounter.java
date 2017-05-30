package iitd.data_analytics.mln.inference;

import iitd.data_analytics.mln.gpu.GpuConfig;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.State;

import static jcuda.driver.JCudaDriver.*;
import jcuda.driver.*;

public class SatisfiedGroundingCounter {
  
  public static long[] count(Formula[] formulas, State state, int totalGpus) throws InterruptedException {
    long[] counts = new long[formulas.length];
    int formulasPerThread = (int)Math.ceil(1.0 * formulas.length / totalGpus);
    CountingThread[] countingThreads = new CountingThread[totalGpus];
    Thread[] threads = new Thread[totalGpus];
    
    for(int i = 0; i < totalGpus; i++) {
      int startIdx = i * formulasPerThread;
      int endIdx = Math.min((i+1) * formulasPerThread, formulas.length) - 1;
      countingThreads[i] = new CountingThread(GpuConfig.context[i], state, formulas, startIdx, endIdx);
      threads[i] = new Thread(countingThreads[i]);
    }
    
    //Launch threads
    for(Thread thread : threads) {
      thread.start();
    }
    
    //Join threads
    for(Thread thread : threads) {
      thread.join();
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

class CountingThread implements Runnable{

  CUcontext context;
  private State state;
  private Formula[] formulas;
  private long[] counts;
  
  public CountingThread(CUcontext _context, State _state, Formula[] _formulas, int startIdx, int endIdx) {
    context = _context;
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
    cuDeviceGet(device, gpuNo);
    CUcontext context = new CUcontext();
    cuCtxCreate(context, 0, device);*/
    cuCtxSetCurrent(context);
    
    for(int i = 0; i < formulas.length; i++) {
      counts[i] = formulas[i].countSatisfiedGroundingsNoDb(state);
    }
  }
  
}
