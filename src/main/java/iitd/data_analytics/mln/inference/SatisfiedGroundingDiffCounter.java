package iitd.data_analytics.mln.inference;

import static jcuda.driver.JCudaDriver.cuCtxSetCurrent;

import iitd.data_analytics.mln.gpu.GpuConfig;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.PredicateGroundingIndex;
import iitd.data_analytics.mln.mln.State;

public class SatisfiedGroundingDiffCounter {
  
  public static long[] countDiff(Formula[] formulas, State state, PredicateGroundingIndex predGroundingIdx,
      int oldVal, int newVal, long[] oldCounts) {
    long[] counts = new long[formulas.length];
    int totalGpus = Math.min(GpuConfig.totalGpus, formulas.length);
    int formulasPerThread = (int)Math.ceil(1.0 * formulas.length / totalGpus);
    CountingDiffThread[] countingDiffThreads = new CountingDiffThread[totalGpus];
    //Thread[] threads = new Thread[totalGpus];
    
    for(int i = 0; i < totalGpus; i++) {
      int startIdx = i * formulasPerThread;
      int endIdx = Math.min((i+1) * formulasPerThread, formulas.length) - 1;
      countingDiffThreads[i] = new CountingDiffThread(i, predGroundingIdx, state, formulas, oldVal, newVal, oldCounts,
          startIdx, endIdx);
      //threads[i] = new Thread(countingThreads[i]);
    }
    
    //Launch threads
    for(Thread thread : countingDiffThreads) {
      thread.start();
    }
    
    //Join threads
    for(Thread thread : countingDiffThreads) {
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
    for(CountingDiffThread countingDiffThread : countingDiffThreads) {
      for(long count : countingDiffThread.getCounts()) {
        counts[idx] = count;
        idx++;
      }
    }
    
    return counts;
  }
}

class CountingDiffThread extends Thread{

  int gpuNo;
  private PredicateGroundingIndex predGroundingIdx;
  private iitd.data_analytics.mln.mln.State state;
  private Formula[] formulas;
  private int oldVal;
  private int newVal;
  private long[] oldCounts;
  private long[] counts;
  
  public CountingDiffThread(int _gpuNo, PredicateGroundingIndex _predGroundingIdx, iitd.data_analytics.mln.mln.State _state, 
      Formula[] _formulas, int _oldVal, int _newVal, long[] _oldCounts, int startIdx, int endIdx) {
    gpuNo = _gpuNo;
    predGroundingIdx = _predGroundingIdx;
    state = _state;
    formulas = new Formula[endIdx - startIdx + 1];
    for(int i = startIdx; i <= endIdx; i++) {
      formulas[i - startIdx] = _formulas[i];
    }
    oldVal = _oldVal;
    newVal = _newVal;
    oldCounts = _oldCounts;
    counts = new long[endIdx - startIdx + 1];
  }
  
  public long[] getCounts() {
    return counts;
  }

  @Override
  public void run() {
    cuCtxSetCurrent(GpuConfig.context[gpuNo]);
    
    for(int i = 0; i < formulas.length; i++) {
      int formulaId = formulas[i].getFormulaId();
      if(oldCounts[formulaId] == -1) {
        counts[i] = formulas[i].countSatisfiedGroundings(state, gpuNo);
      } else {
        if(oldVal == newVal) {
          counts[i] = oldCounts[formulaId];
        } else {
          counts[i] = oldCounts[formulaId] + formulas[i].countSatisfiedGroundingDiff(predGroundingIdx, state, oldVal, newVal, gpuNo);
        }
      }
    }
    
    /*cuCtxDestroy(context);*/
  }
  
}
