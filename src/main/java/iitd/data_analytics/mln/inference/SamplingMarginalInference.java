package iitd.data_analytics.mln.inference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import iitd.data_analytics.mln.gpu.GpuConfig;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundingIndex;
import iitd.data_analytics.mln.mln.State;
import iitd.data_analytics.mln.mln.Symbols;
import iitd.data_analytics.mln.sampler.Sampler;

public class SamplingMarginalInference extends MarginalInference {

  private int totalSamples; //After burnIn
  private Sampler sampler;
  
  public SamplingMarginalInference(int _totalSamples,
      Sampler _sampler) {
    totalSamples = _totalSamples;
    sampler = _sampler;
  }
  
  public void generateMarginals() {
    for(int i = 0; i < totalSamples; i++) {
      State state = sampler.getNextSample();
      state.increaseMarginalCounts();
    }
  }

  @Override
  public State getMarginals() {
    generateMarginals();
    return sampler.getState();
  }
  
}
