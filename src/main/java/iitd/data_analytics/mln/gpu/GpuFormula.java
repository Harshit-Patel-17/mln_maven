package iitd.data_analytics.mln.gpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.mln.*;

import static jcuda.driver.JCudaDriver.*;
import jcuda.*;
import jcuda.driver.*;

public class GpuFormula extends Formula {

  private int totalVars;
  private int[] newVarIdToOldVarId;
  Map<Integer,Integer> oldVarIdToNewVarId;
  private long totalGroundings;
  private int[] varDomainSizes;
  private ArrayList<GpuClause> clauses;
  private int maxThreads;
  
  private void renameVariables() {
    Symbols varsId = super.getVarsId();
    newVarIdToOldVarId = new int[varsId.size()];
    oldVarIdToNewVarId = new HashMap<Integer,Integer>();
        
    int newVarId = 0;
    for(int varId : varsId.getIds()) {
      newVarIdToOldVarId[newVarId] = varId;
      oldVarIdToNewVarId.put(varId, newVarId);
      newVarId += 1;
    }
  }
  
  public GpuFormula(int _formulaId, FirstOrderFormula<Predicate> foFormula, 
      Map<String,Domain> _varsDomain, Symbols _varsId, double _weight) {
    super(_formulaId, foFormula, _varsDomain, _varsId, _weight);
    
    totalVars = _varsId.size();
    renameVariables();
    varDomainSizes = new int[totalVars];
    totalGroundings = 1;
    for(int i = 0; i < totalVars; i++) {
      int varId = newVarIdToOldVarId[i];
      String varSymbol = _varsId.getSymbolFromId(varId);
      int domainSize = _varsDomain.get(varSymbol).size();
      varDomainSizes[i] = domainSize;
      totalGroundings *= domainSize;
    }
    maxThreads = GpuConfig.maxThreads;
    
    clauses = new ArrayList<GpuClause>();
    for(ArrayList<Predicate> clause : super.getClauses()) {
      clauses.add(new GpuClause(clause, totalVars, totalGroundings, varDomainSizes, oldVarIdToNewVarId));
    }
  }
  
  public GpuFormula(Formula gf) {
    super(gf);
    
    totalVars = super.getVarsId().size();
    renameVariables();
    varDomainSizes = new int[totalVars];
    totalGroundings = 1;
    for(int i = 0; i < totalVars; i++) {
      int varId = newVarIdToOldVarId[i];
      String varSymbol = super.getVarsId().getSymbolFromId(varId);
      int domainSize = super.getVarsDomain().get(varSymbol).size();
      varDomainSizes[i] = domainSize;
      totalGroundings *= domainSize;
    }
    maxThreads = GpuConfig.maxThreads;
    
    clauses = new ArrayList<GpuClause>();
    for(ArrayList<Predicate> clause : super.getClauses()) {
      clauses.add(new GpuClause(clause, totalVars, totalGroundings, varDomainSizes, oldVarIdToNewVarId));
    }
  }
  
  @Override
  public void substitute(Map<Integer, Integer> varVals) {
    super.substitute(varVals);
    
    totalVars = super.getVarsId().size();
    renameVariables();
    varDomainSizes = new int[totalVars];
    totalGroundings = 1;
    for(int i = 0; i < totalVars; i++) {
      int varId = newVarIdToOldVarId[i];
      String varSymbol = super.getVarsId().getSymbolFromId(varId);
      int domainSize = super.getVarsDomain().get(varSymbol).size();
      varDomainSizes[i] = domainSize;
      totalGroundings *= domainSize;
    }
    maxThreads = GpuConfig.maxThreads;
    
    clauses = new ArrayList<GpuClause>();
    for(ArrayList<Predicate> clause : super.getClauses()) {
      clauses.add(new GpuClause(clause, totalVars, totalGroundings, varDomainSizes, oldVarIdToNewVarId));
    }
  }
  
  @Override
  public void setTotalGroundings() {
    super.setTotalGroundings();
    totalGroundings = super.getTotalGroundings();
  }
  
  @Override
  public long getTotalGroundings() {
    return super.getTotalGroundings();
  }
  
  @Override
  public long countSatisfiedGroundingsNoDb(State state, int gpuNo)
  {
    GpuUtil gpuUtil = new GpuUtil();
    
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, GpuConfig.mlnCudaKernels[gpuNo], "evalClauseWithoutDbKernel");
    
    long totalBatches = (totalGroundings + GpuConfig.maxBatchSize - 1) / GpuConfig.maxBatchSize;
    long totalSatGroundings = 0;
    
    CUdeviceptr d_interpretation = new CUdeviceptr();
    assert cuMemAlloc(d_interpretation, ((CUdeviceptr[])state.getAllGroundings(gpuNo)).length * 
        Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_interpretation, Pointer.to(((CUdeviceptr[])state.getAllGroundings(gpuNo))), 
        ((CUdeviceptr[])state.getAllGroundings(gpuNo)).length * Sizeof.POINTER) 
      == CUresult.CUDA_SUCCESS;
    
    for(long i = 0; i < totalBatches; i++) {
      long offset = i * GpuConfig.maxBatchSize;
      int batchGroundings = (int)Math.min(totalGroundings - offset, GpuConfig.maxBatchSize);
      
      CUdeviceptr d_satArray = new CUdeviceptr();
      CUdeviceptr d_mem = new CUdeviceptr();
  
      assert cuMemAlloc(d_satArray, batchGroundings * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_mem, totalVars * batchGroundings * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      
      gpuUtil.parallelInit(d_satArray, batchGroundings, 1, maxThreads, gpuNo);
  
      for(GpuClause clause : clauses)
      { 
        CUdeviceptr d_predicates = new CUdeviceptr();
        CUdeviceptr d_negated = new CUdeviceptr();
        CUdeviceptr d_valTrue = new CUdeviceptr();
        CUdeviceptr d_varDomainSizes = new CUdeviceptr();
        CUdeviceptr d_predBaseIdx = new CUdeviceptr();
        CUdeviceptr d_predVarMat = new CUdeviceptr();
  
        assert cuMemAlloc(d_predicates, clause.predicates.length * Sizeof.INT) == 
            CUresult.CUDA_SUCCESS;
        assert cuMemAlloc(d_negated, clause.isNegated.length * Sizeof.INT) == 
            CUresult.CUDA_SUCCESS;
        assert cuMemAlloc(d_valTrue, clause.valTrue.length * Sizeof.INT) == 
            CUresult.CUDA_SUCCESS;
        assert cuMemAlloc(d_varDomainSizes, totalVars * Sizeof.INT) == 
            CUresult.CUDA_SUCCESS;
        assert cuMemAlloc(d_predBaseIdx, clause.predBaseIdx.length * Sizeof.INT) == 
            CUresult.CUDA_SUCCESS;
        assert cuMemAlloc(d_predVarMat, clause.predVarMat.length * Sizeof.INT) == 
            CUresult.CUDA_SUCCESS;
  
        assert cuMemcpyHtoD(d_predicates, Pointer.to(clause.predicates), 
            clause.predicates.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
        assert cuMemcpyHtoD(d_negated, Pointer.to(clause.isNegated), 
            clause.isNegated.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
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
          Pointer.to(d_negated),
          Pointer.to(d_predBaseIdx),
          Pointer.to(d_valTrue),
          Pointer.to(d_predVarMat),
          Pointer.to(d_satArray),
          Pointer.to(d_interpretation),
          Pointer.to(new long[]{batchGroundings}),
          Pointer.to(new long[]{offset}),
          Pointer.to(d_mem)
        );
  
        int blockSizeX = Math.min(maxThreads, (int)batchGroundings);
        int gridSizeX = ((int)batchGroundings + blockSizeX - 1) / blockSizeX;
  
        cuLaunchKernel(function,
          gridSizeX, 1, 1,
          blockSizeX, 1, 1,
          0, null,
          kernelParameters, null
        );
        cuCtxSynchronize();
  
        cuMemFree(d_predicates);
        cuMemFree(d_negated);
        cuMemFree(d_valTrue);
        cuMemFree(d_varDomainSizes);
        cuMemFree(d_predBaseIdx);
        cuMemFree(d_predVarMat);
      }
  
      totalSatGroundings += gpuUtil.parallelSum(d_satArray, batchGroundings, maxThreads, gpuNo);
  
      cuMemFree(d_satArray);
      cuMemFree(d_mem);
    }
    
    cuMemFree(d_interpretation);

    return totalSatGroundings;    
  }
  
  @Override
  public long countSatisfiedGroundings(State state, int gpuNo)
  {
    GpuUtil gpuUtil = new GpuUtil();
    
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, GpuConfig.mlnCudaKernels[gpuNo], "evalCNFKernel");
    
    long totalBatches = (totalGroundings + GpuConfig.maxBatchSize - 1) / GpuConfig.maxBatchSize;
    long totalSatGroundings = 0;
    
    //Load Interpretation in GPU
    CUdeviceptr d_interpretation = new CUdeviceptr();
    assert cuMemAlloc(d_interpretation, ((CUdeviceptr[])state.getAllGroundings(gpuNo)).length * 
        Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_interpretation, Pointer.to(((CUdeviceptr[])state.getAllGroundings(gpuNo))), 
        ((CUdeviceptr[])state.getAllGroundings(gpuNo)).length * Sizeof.POINTER) 
      == CUresult.CUDA_SUCCESS;
    
    //Load variable domain sizes
    CUdeviceptr d_varDomainSizes = new CUdeviceptr();
    assert cuMemAlloc(d_varDomainSizes, totalVars * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_varDomainSizes, Pointer.to(varDomainSizes), 
        totalVars * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    
    //Load clauses in GPU
    int totalClauses = clauses.size();
    int[] totalPredsInClause = new int[totalClauses];
    CUdeviceptr[] d_predicates_per_clause = new CUdeviceptr[totalClauses];
    CUdeviceptr[] d_negated_per_clause = new CUdeviceptr[totalClauses];
    CUdeviceptr[] d_valTrue_per_clause = new CUdeviceptr[totalClauses];
    CUdeviceptr[] d_predBaseIdx_per_clause = new CUdeviceptr[totalClauses];
    CUdeviceptr[] d_predVarMat_per_clause = new CUdeviceptr[totalClauses];
    
    for(int i = 0; i < totalClauses; i++) {
      GpuClause clause = clauses.get(i);
      totalPredsInClause[i] = clause.totalPreds;
      d_predicates_per_clause[i] = new CUdeviceptr();
      d_negated_per_clause[i] = new CUdeviceptr();
      d_valTrue_per_clause[i] = new CUdeviceptr();
      d_predBaseIdx_per_clause[i] = new CUdeviceptr();
      d_predVarMat_per_clause[i] = new CUdeviceptr();
      
      assert cuMemAlloc(d_predicates_per_clause[i], clause.predicates.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_negated_per_clause[i], clause.isNegated.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_valTrue_per_clause[i], clause.valTrue.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_predBaseIdx_per_clause[i], clause.predBaseIdx.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_predVarMat_per_clause[i], clause.predVarMat.length * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      
      assert cuMemcpyHtoD(d_predicates_per_clause[i], Pointer.to(clause.predicates), 
          clause.predicates.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_negated_per_clause[i], Pointer.to(clause.isNegated), 
          clause.isNegated.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_valTrue_per_clause[i], Pointer.to(clause.valTrue), 
          clause.valTrue.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_predBaseIdx_per_clause[i], Pointer.to(clause.predBaseIdx), 
          clause.predBaseIdx.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemcpyHtoD(d_predVarMat_per_clause[i], Pointer.to(clause.predVarMat), 
          clause.predVarMat.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    }
    
    CUdeviceptr d_predicates = new CUdeviceptr();
    CUdeviceptr d_negated = new CUdeviceptr();
    CUdeviceptr d_valTrue = new CUdeviceptr();
    CUdeviceptr d_predBaseIdx = new CUdeviceptr();
    CUdeviceptr d_predVarMat = new CUdeviceptr();
    
    assert cuMemAlloc(d_predicates, d_predicates_per_clause.length * Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_negated, d_negated_per_clause.length * Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_valTrue, d_valTrue_per_clause.length * Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_predBaseIdx, d_predBaseIdx_per_clause.length * Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    assert cuMemAlloc(d_predVarMat, d_predVarMat_per_clause.length * Sizeof.POINTER) == CUresult.CUDA_SUCCESS;
    
    assert cuMemcpyHtoD(d_predicates, Pointer.to(d_predicates_per_clause), d_predicates_per_clause.length * Sizeof.POINTER) 
      == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_negated, Pointer.to(d_negated_per_clause), d_negated_per_clause.length * Sizeof.POINTER) 
    == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_valTrue, Pointer.to(d_valTrue_per_clause), d_valTrue_per_clause.length * Sizeof.POINTER) 
    == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_predBaseIdx, Pointer.to(d_predBaseIdx_per_clause), d_predBaseIdx_per_clause.length * Sizeof.POINTER) 
    == CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_predVarMat, Pointer.to(d_predVarMat_per_clause), d_predVarMat_per_clause.length * Sizeof.POINTER) 
    == CUresult.CUDA_SUCCESS;
    
    
    //Load total predicate count per clauses
    CUdeviceptr d_totalPredsInClauses = new CUdeviceptr();
    assert cuMemAlloc(d_totalPredsInClauses, totalClauses * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(d_totalPredsInClauses, Pointer.to(totalPredsInClause), 
        totalClauses * Sizeof.INT) == CUresult.CUDA_SUCCESS;
    
    
    for(long i = 0; i < totalBatches; i++) {
      long offset = i * GpuConfig.maxBatchSize;
      int batchGroundings = (int)Math.min(totalGroundings - offset, GpuConfig.maxBatchSize);
      
      CUdeviceptr d_satArray = new CUdeviceptr();
      CUdeviceptr d_mem = new CUdeviceptr();
  
      assert cuMemAlloc(d_satArray, batchGroundings * Sizeof.INT) == CUresult.CUDA_SUCCESS;
      assert cuMemAlloc(d_mem, totalVars * batchGroundings * Sizeof.INT) == 
          CUresult.CUDA_SUCCESS;
      
      Pointer kernelParameters = Pointer.to(
          Pointer.to(new int[]{totalVars}),
          Pointer.to(new int[]{totalClauses}),
          Pointer.to(d_totalPredsInClauses),
          Pointer.to(d_varDomainSizes),
          Pointer.to(d_predicates),
          Pointer.to(d_negated),
          Pointer.to(d_predBaseIdx),
          Pointer.to(d_valTrue),
          Pointer.to(d_predVarMat),
          Pointer.to(d_satArray),
          Pointer.to(d_interpretation),
          Pointer.to(new long[]{batchGroundings}),
          Pointer.to(new long[]{offset}),
          Pointer.to(d_mem)
        );
      
      int blockSizeX = Math.min(maxThreads, (int)batchGroundings);
      int gridSizeX = ((int)batchGroundings + blockSizeX - 1) / blockSizeX;

      cuLaunchKernel(function,
        gridSizeX, 1, 1,
        blockSizeX, 1, 1,
        0, null,
        kernelParameters, null
      );
      cuCtxSynchronize();
  
      totalSatGroundings += gpuUtil.parallelSum(d_satArray, batchGroundings, maxThreads, gpuNo);
  
      cuMemFree(d_satArray);
      cuMemFree(d_mem);
    }
    
    //Free Interpretation in GPU
    cuMemFree(d_interpretation);
    
    //Free variable domain sizes in GPU
    cuMemFree(d_varDomainSizes);
    
    //Free clauses in GPU
    for(int c = 0; c < clauses.size(); c++) {
      cuMemFree(d_predicates_per_clause[c]);
      cuMemFree(d_negated_per_clause[c]);
      cuMemFree(d_valTrue_per_clause[c]);
      cuMemFree(d_predBaseIdx_per_clause[c]);
      cuMemFree(d_predVarMat_per_clause[c]);
    }
    cuMemFree(d_predicates);
    cuMemFree(d_negated);
    cuMemFree(d_valTrue);
    cuMemFree(d_predBaseIdx);
    cuMemFree(d_predVarMat);
    
    //Free total predicate count per clauses
    cuMemFree(d_totalPredsInClauses);

    return totalSatGroundings;    
  }
  
  @Override
  public long countSatisfiedGroundingsCPUNoDb(State state) {
    long totalBatches = (totalGroundings + GpuConfig.maxBatchSize - 1) / GpuConfig.maxBatchSize;
    long totalSatGroundings = 0;
    
    for(long batch = 0; batch < totalBatches; batch++) {
      long offset = batch * GpuConfig.maxBatchSize;
      int batchGroundings = (int)Math.min(totalGroundings - offset, GpuConfig.maxBatchSize);
      
      int[] satArray = new int[batchGroundings];
      for(int i = 0; i < batchGroundings; i++)
        satArray[i] = 1;
      
      for(GpuClause clause : clauses) {
        for(int i = 0; i < batchGroundings; i++) {
          if(satArray[i] == 1) {
            int[] dbIndex = new int[clause.totalPreds];
            for(int j = 0; j < clause.totalPreds; j++) {
              dbIndex[j] = clause.predBaseIdx[j];
            }
            
            long n = i + offset;
            for(int j = totalVars-1; j >= 0; j--) {
              int domainSize = varDomainSizes[j];
              long temp = n / domainSize;
              int val = (int)(n - temp * domainSize);
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
              if(clause.isNegated[j] == 0)
                sat = Math.max(sat, (trueVal == stateVal)?1:0);
              else
                sat = Math.max(sat, (trueVal != stateVal)?1:0);
            }
            satArray[i] = sat;
          }
        }
      }
      
      for(int sat : satArray) {
        totalSatGroundings += sat;
      }
    }
    
    return totalSatGroundings;
  }
  
  @Override
  public String toString() {
    String str = super.toString();
    return str;
  }
  
  @Override
  public void display() {
    System.out.print(this);
  }

}
