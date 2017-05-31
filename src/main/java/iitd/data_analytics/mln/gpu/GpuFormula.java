package iitd.data_analytics.mln.gpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.mln.*;

import static jcuda.driver.JCudaDriver.*;
import jcuda.*;
import jcuda.driver.*;

public class GpuFormula extends Formula {

  private int totalVars;
  private long totalGroundings;
  private int[] varDomainSizes;
  private ArrayList<GpuClause> clauses;
  private int maxThreads;
  
  public GpuFormula(int _formulaId, FirstOrderFormula<Predicate> foFormula, 
      Map<String,Domain> _varsDomain, Symbols _varsId, double _weight) {
    super(_formulaId, foFormula, _varsDomain, _varsId, _weight);
    
    totalVars = _varsId.size();
    varDomainSizes = new int[totalVars];
    totalGroundings = 1;
    for(int i = 0; i < totalVars; i++) {
      String varSymbol = _varsId.getSymbolFromId(i);
      int domainSize = _varsDomain.get(varSymbol).size();
      varDomainSizes[i] = domainSize;
      totalGroundings *= domainSize;
    }
    maxThreads = GpuConfig.maxThreads;
    
    clauses = new ArrayList<GpuClause>();
    for(ArrayList<Predicate> clause : super.getClauses()) {
      clauses.add(new GpuClause(clause, totalVars, totalGroundings, varDomainSizes));
    }
  }

  /*@Override
  public long countSatisfiedGroundings(State state)
  {
    GpuUtil gpuUtil = new GpuUtil();

    // Load the ptx file.
    CUmodule module = new CUmodule();
    cuModuleLoad(module, GpuConfig.ptxBase + "mlnCudaKernels.ptx");

    // Obtain a function pointer to the kernel function
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, module, "evalClauseKernel");

    CUdeviceptr d_satArray = new CUdeviceptr();
    CUdeviceptr d_interpretation = new CUdeviceptr();

    assert cuMemAlloc(d_satArray, totalGroundings * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_interpretation, ((CUdeviceptr[])state.getAllGroundings()).length * 
        Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    
    gpuUtil.parallelInit(d_satArray, totalGroundings, 1, maxThreads);
    assert cuMemcpyHtoD(d_interpretation, Pointer.to(((CUdeviceptr[])state.getAllGroundings())), 
        ((CUdeviceptr[])state.getAllGroundings()).length * Sizeof.POINTER) 
      == CUresult.CUDA_SUCCESS;

    for(GpuClause clause : clauses)
    { 
      CUdeviceptr d_dbIndex = new CUdeviceptr();
      CUdeviceptr d_predicates = new CUdeviceptr();
      CUdeviceptr d_valTrue = new CUdeviceptr();

      assert cuMemAlloc(d_dbIndex, clause.dbIndex.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_predicates, clause.predicates.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_valTrue, clause.valTrue.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;

      assert cuMemcpyHtoD(d_dbIndex, Pointer.to(clause.dbIndex), 
          clause.dbIndex.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_predicates, Pointer.to(clause.predicates), 
          clause.predicates.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_valTrue, Pointer.to(clause.valTrue), 
          clause.valTrue.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;

      Pointer kernelParameters = Pointer.to(
        Pointer.to(d_satArray),
        Pointer.to(d_interpretation),
        Pointer.to(d_dbIndex),
        Pointer.to(d_predicates),
        Pointer.to(d_valTrue),
        Pointer.to(new int[]{clause.totalPreds}),
        Pointer.to(new long[]{totalGroundings})
      );

      int blockSizeX = Math.min(maxThreads, (int)totalGroundings);
      int gridSizeX = ((int)totalGroundings + blockSizeX - 1) / blockSizeX;
      System.out.println("Grid size: " + gridSizeX + ", Block size: " + blockSizeX + 
          " :: evalClause");

      cuLaunchKernel(function,
        gridSizeX, 1, 1,
        blockSizeX, 1, 1,
        0, null,
        kernelParameters, null
      );
      cuCtxSynchronize();

      cuMemFree(d_dbIndex);
      cuMemFree(d_predicates);
      cuMemFree(d_valTrue);
    }

    int[] satArray = new int[(int)totalGroundings];
    assert cuMemcpyDtoH(Pointer.to(satArray), d_satArray, totalGroundings * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;

    System.out.println(Arrays.toString(satArray));

    int totalSatGroundings = gpuUtil.parallelSum(d_satArray, totalGroundings, maxThreads);

    cuMemFree(d_satArray);
    cuMemFree(d_interpretation);

    return totalSatGroundings;    
  }*/
  
  @Override
  public long countSatisfiedGroundingsNoDb(State state, int gpuNo)
  {
    GpuUtil gpuUtil = new GpuUtil();

    /*// Load the ptx file.
    CUmodule module = new CUmodule();
    cuModuleLoad(module, GpuConfig.ptxBase + "mlnCudaKernels.ptx");

    // Obtain a function pointer to the kernel function
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, module, "evalClauseWithoutDbKernel");*/
    
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, GpuConfig.mlnCudaKernels[gpuNo], "evalClauseWithoutDbKernel");

    CUdeviceptr d_satArray = new CUdeviceptr();
    CUdeviceptr d_interpretation = new CUdeviceptr();
    CUdeviceptr d_mem = new CUdeviceptr();

    assert cuMemAlloc(d_satArray, totalGroundings * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_interpretation, ((CUdeviceptr[])state.getAllGroundings(gpuNo)).length * 
        Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_mem, totalVars * totalGroundings * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    
    gpuUtil.parallelInit(d_satArray, totalGroundings, 1, maxThreads, gpuNo);
    assert cuMemcpyHtoD(d_interpretation, Pointer.to(((CUdeviceptr[])state.getAllGroundings(gpuNo))), 
        ((CUdeviceptr[])state.getAllGroundings(gpuNo)).length * Sizeof.POINTER) 
      == CUresult.CUDA_SUCCESS;

    for(GpuClause clause : clauses)
    { 
      //CUdeviceptr d_dbIndex = new CUdeviceptr();
      CUdeviceptr d_predicates = new CUdeviceptr();
      CUdeviceptr d_valTrue = new CUdeviceptr();
      CUdeviceptr d_varDomainSizes = new CUdeviceptr();
      CUdeviceptr d_predBaseIdx = new CUdeviceptr();
      CUdeviceptr d_predVarMat = new CUdeviceptr();

      /*assert cuMemAlloc(d_dbIndex, clause.dbIndex.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;*/
      assert cuMemAlloc(d_predicates, clause.predicates.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_valTrue, clause.valTrue.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_varDomainSizes, totalVars * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_predBaseIdx, clause.predBaseIdx.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_predVarMat, clause.predVarMat.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;

      /*assert cuMemcpyHtoD(d_dbIndex, Pointer.to(clause.dbIndex), 
          clause.dbIndex.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;*/
      assert cuMemcpyHtoD(d_predicates, Pointer.to(clause.predicates), 
          clause.predicates.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_valTrue, Pointer.to(clause.valTrue), 
          clause.valTrue.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_varDomainSizes, Pointer.to(varDomainSizes), 
          totalVars * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_predBaseIdx, Pointer.to(clause.predBaseIdx), 
          clause.predBaseIdx.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_predVarMat, Pointer.to(clause.predVarMat), 
          clause.predVarMat.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;

      Pointer kernelParameters = Pointer.to(
        Pointer.to(new int[]{totalVars}),
        Pointer.to(new int[]{clause.totalPreds}),
        Pointer.to(d_varDomainSizes),
        Pointer.to(d_predicates),
        Pointer.to(d_predBaseIdx),
        Pointer.to(d_valTrue),
        Pointer.to(d_predVarMat),
        Pointer.to(d_satArray),
        Pointer.to(d_interpretation),
        Pointer.to(new long[]{totalGroundings}),
        Pointer.to(d_mem)
      );

      int blockSizeX = Math.min(maxThreads, (int)totalGroundings);
      int gridSizeX = ((int)totalGroundings + blockSizeX - 1) / blockSizeX;
      /*System.out.println("Grid size: " + gridSizeX + ", Block size: " + blockSizeX + 
          " :: evalClause");*/

      cuLaunchKernel(function,
        gridSizeX, 1, 1,
        blockSizeX, 1, 1,
        0, null,
        kernelParameters, null
      );
      cuCtxSynchronize();

      //cuMemFree(d_dbIndex);
      cuMemFree(d_predicates);
      cuMemFree(d_valTrue);
      cuMemFree(d_varDomainSizes);
      cuMemFree(d_predBaseIdx);
      cuMemFree(d_predVarMat);
    }

    /*int[] satArray = new int[(int)totalGroundings];
    assert cuMemcpyDtoH(Pointer.to(satArray), d_satArray, totalGroundings * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;

    System.out.println(Arrays.toString(satArray));*/

    int totalSatGroundings = gpuUtil.parallelSum(d_satArray, totalGroundings, maxThreads, gpuNo);

    cuMemFree(d_satArray);
    cuMemFree(d_interpretation);
    cuMemFree(d_mem);

    return totalSatGroundings;    
  }

  
  /*@Override
  public long countSatisfiedGroundingsCPU(State state) {
    int[] satArray = new int[(int)totalGroundings];
    for(int i = 0; i < totalGroundings; i++)
      satArray[i] = 1;
    
    for(GpuClause clause : clauses) {
      for(int i = 0; i < totalGroundings; i++) {
        if(satArray[i] == 1) { 
          int sat = 0;
          int baseIdx = i * clause.totalPreds;
          for(int j = 0; j < clause.totalPreds; j++) {
            int trueVal = clause.valTrue[j];
            int stateVal = state.getGrounding(clause.predicates[j], clause.dbIndex[baseIdx + j]);
            sat = Math.max(sat, (trueVal == stateVal)?1:0);
          }
          satArray[i] = sat;
        }
      }
    }
    
    long count = 0;
    for(int sat : satArray) {
      count += sat;
    }
    
    return count;
  }*/
  
  @Override
  public long countSatisfiedGroundingsCPUNoDb(State state) {
    int[] satArray = new int[(int)totalGroundings];
    for(int i = 0; i < totalGroundings; i++)
      satArray[i] = 1;
    
    for(GpuClause clause : clauses) {
      for(int i = 0; i < totalGroundings; i++) {
        if(satArray[i] == 1) {
          int[] dbIndex = new int[clause.totalPreds];
          for(int j = 0; j < clause.totalPreds; j++) {
            dbIndex[j] = clause.predBaseIdx[j];
          }
          
          int n = i;
          for(int j = totalVars-1; j >= 0; j--) {
            int domainSize = varDomainSizes[j];
            int temp = n / domainSize;
            int val = n - temp * domainSize;
            n = temp;
            
            int basePredVarMatIndex = j * clause.totalPreds;
            for(int k = 0; k < clause.totalPreds; k++) {
              dbIndex[k] += clause.predVarMat[basePredVarMatIndex + k] * val;
            }
          }
          
          int sat = 0;
          for(int j = 0; j < clause.totalPreds; j++) {
            int trueVal = clause.valTrue[j];
            int stateVal = state.getGrounding(clause.predicates[j], dbIndex[j]);
            sat = Math.max(sat, (trueVal == stateVal)?1:0);
          }
          satArray[i] = sat;
        }
      }
    }
    
    long count = 0;
    for(int sat : satArray) {
      count += sat;
    }
    
    return count;
  }
  
  @Override
  public String toString() {
    String str = super.toString();
    /*for(GpuClause clause : clauses) {
      str += clause.toString();
    }*/
    return str;
  }
  
  @Override
  public void display() {
    System.out.print(this);
  }

}
