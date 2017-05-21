package iitd.data_analytics.mln.gpu;

import java.util.ArrayList;

import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundingIndex;
import iitd.data_analytics.mln.mln.State;

import static jcuda.driver.JCudaDriver.*;
import jcuda.*;
import jcuda.driver.*;

public class GpuState extends State {

  private int totalPredicates;
  private CUdeviceptr[] allGroundings;
  
  public GpuState(ArrayList<PredicateDef> predicateDefs) {
    super(predicateDefs);
    
    totalPredicates = predicateDefs.size();
    allGroundings = new CUdeviceptr[totalPredicates];
    for(PredicateDef predicateDef : predicateDefs) {
      int predicateId = predicateDef.getPredicateId();
      allGroundings[predicateId] = new CUdeviceptr();
      initGroundings(predicateId, super.getGroundings(predicateId));
    }
  }
  
  private void initGroundings(int predicateId, int[] groundings) {
    assert (predicateId >= 0) && (predicateId < totalPredicates);
    assert cuMemAlloc(allGroundings[predicateId], groundings.length * Sizeof.INT) == 
        CUresult.CUDA_SUCCESS;
    assert cuMemcpyHtoD(allGroundings[predicateId], Pointer.to(groundings), 
        groundings.length * Sizeof.INT) == CUresult.CUDA_SUCCESS;
  }
  
  @Override
  public PredicateGroundingIndex randomlySelectUnknownGrounding() {
    return super.randomlySelectUnknownGrounding();
  }
  
  @Override
  public void setGrounding(PredicateGroundingIndex predGroundingIdx, int val) {
    super.setGrounding(predGroundingIdx, val);
    
    int predicateId = predGroundingIdx.predicateId;
    int groundingId = predGroundingIdx.groundingId;
    CUdeviceptr updateLocation = allGroundings[predicateId].withByteOffset(groundingId * Sizeof.INT);
    assert cuMemcpyHtoD(updateLocation, Pointer.to(new int[]{val}), Sizeof.INT) == CUresult.CUDA_SUCCESS;
  }

  @Override
  public Object getAllGroundings() {
    return allGroundings;
  }
  
  @Override
  public String toString() {
    return super.toString();
  }
  
  @Override
  public void display() {
    super.display();
  }
}
