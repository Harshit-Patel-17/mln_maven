package iitd.data_analytics.mln.gpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.Predicate;
import iitd.data_analytics.mln.mln.PredicateDef;

import static jcuda.driver.JCudaDriver.*;
import jcuda.*;
import jcuda.driver.*;

public class GpuClause {
  public int totalPreds;
  public int totalVars;
  public long totalGroundings;
  public int[] predicates;
  //public int[] varDomainSizes;
  public int[] isNegated;
  public int[] predBaseIdx;
  public int[] predVarMat;
  public int[] valTrue;
  //public int[] dbIndex;
  
  public GpuClause(ArrayList<Predicate> _clause, int _totalVars, long _totalGroundings,
      int[] _varDomainSizes, Map<Integer,Integer> oldVarIdToNewVarId) {
    totalPreds = _clause.size();
    totalVars = _totalVars;
    totalGroundings = _totalGroundings;
    predicates = new int[totalPreds];
    //varDomainSizes = _varDomainSizes;
    isNegated = new int[totalPreds];
    predBaseIdx = new int[totalPreds];
    predVarMat = new int[totalPreds * totalVars];
    valTrue = new int[totalPreds];
    //dbIndex = new int[totalPreds * (int)_totalGroundings];
    
    for(int i = 0; i < _clause.size(); i++) {
      Predicate predicate = _clause.get(i);
      PredicateDef predicateDef = predicate.getPredicateDef();
      predicates[i] = predicateDef.getPredicateId();
      isNegated[i] = predicate.getIsNegated() ? 1 : 0;
      valTrue[i] = predicate.getVal();
      
      int runningWeight = 1, baseIndex = 0;
      ArrayList<Integer> terms = predicate.getTerms();
      ArrayList<Boolean> isVariable = predicate.getIsVariable();
      ArrayList<Domain> domains = predicateDef.getDomains();
      for(int j = 0; j < terms.size(); j++) {
        if(isVariable.get(j)) {
          int varId = oldVarIdToNewVarId.get(terms.get(j));
          predVarMat[varId * totalPreds + i] = runningWeight;
        } else {
          baseIndex += terms.get(j) * runningWeight;
        }
        runningWeight *= domains.get(j).size();
      }
      predBaseIdx[i] = baseIndex;
    }
    
    //initDbIndex(); //TODO: Uncomment it later
  }
  
  /*private void initDbIndex()
  {
    // Load the ptx file.
    CUmodule module = new CUmodule();
    cuModuleLoad(module, GpuConfig.ptxBase + "mlnCudaKernels.ptx");

    // Obtain a function pointer to the kernel function
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, module, "initDbIndexKernel");


    CUdeviceptr d_varDomainSizes = new CUdeviceptr();
    CUdeviceptr d_predBaseIdx = new CUdeviceptr();
    CUdeviceptr d_predVarMat = new CUdeviceptr();
    CUdeviceptr d_dbIndex = new CUdeviceptr();

    assert cuMemAlloc(d_varDomainSizes, totalVars * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_predBaseIdx, predBaseIdx.length * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_predVarMat, predVarMat.length * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_dbIndex, dbIndex.length * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;

    assert cuMemcpyHtoD(d_varDomainSizes, Pointer.to(varDomainSizes), 
        totalVars * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_predBaseIdx, Pointer.to(predBaseIdx), 
        predBaseIdx.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_predVarMat, Pointer.to(predVarMat), 
        predVarMat.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_dbIndex, Pointer.to(dbIndex), 
        dbIndex.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;

    Pointer kernelParameters = Pointer.to(
      Pointer.to(new int[]{totalVars}),
      Pointer.to(new int[]{totalPreds}),
      Pointer.to(d_varDomainSizes),
      Pointer.to(d_predBaseIdx),
      Pointer.to(d_predVarMat),
      Pointer.to(d_dbIndex),
      Pointer.to(new long[]{totalGroundings})
    );

    int blockSizeX = Math.min(GpuConfig.maxThreads, (int)totalGroundings);
    int gridSizeX = ((int)totalGroundings + blockSizeX - 1) / blockSizeX;
    System.out.println("Grid size: " + gridSizeX + ", Block size: " + blockSizeX + 
        " :: initDbIndex");

    cuLaunchKernel(function,
      gridSizeX, 1, 1,
      blockSizeX, 1, 1,
      0, null,
      kernelParameters, null
    );
    cuCtxSynchronize();

    assert cuMemcpyDtoH(Pointer.to(dbIndex), d_dbIndex, dbIndex.length * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    
    cuMemFree(d_varDomainSizes);
    cuMemFree(d_predBaseIdx);
    cuMemFree(d_predVarMat);
    cuMemFree(d_dbIndex);
  }*/
  
  @Override
  public String toString() {
    String str = "";
    str += "predicates: " + Arrays.toString(predicates) + "\n";
    str += "isNegated: " + Arrays.toString(isNegated) + "\n";
    str += "predBaseIdx: " + Arrays.toString(predBaseIdx) + "\n";
    str += "valTrue: " + Arrays.toString(valTrue) + "\n";
    str += "predVarMat: " + Arrays.toString(predVarMat) + "\n";
    return str;
  }
  
  void display() {
    System.out.print(this);
  }
}
