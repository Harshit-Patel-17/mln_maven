package iitd.data_analytics.mln.sampler;

import java.util.HashSet;
import java.util.Set;

import iitd.data_analytics.mln.inference.SatisfiedGroundingCounter;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundingIndex;
import iitd.data_analytics.mln.mln.State;

public class GibbsSampler extends Sampler {

  private Mln mln;
  private int burnInSamples;
  private boolean burntIn;
  
  public GibbsSampler(Mln _mln, int _burnInSamples) {
    mln = _mln;
    burnInSamples = _burnInSamples;
    burntIn = false;
  }
  
  private Formula[] affectedFormulas(PredicateGroundingIndex predGroundingIdx, State state) {
    Set<Formula> formulas = new HashSet<Formula>();
    
    int predicateId = predGroundingIdx.predicateId;
    int groundingId = predGroundingIdx.groundingId;
    int[] groundingVals = state.getPredicateGroundings(predicateId).getGroundingVals(groundingId);
    for(Formula formula : mln.getFormulas()) {
      if(formula.groundAtomExist(predicateId, groundingVals)) {
        formulas.add(formula);
      }
    }
    
    return formulas.toArray(new Formula[formulas.size()]);
  }
  
  private double[] getCummMarginalProb(PredicateGroundingIndex predGroundingIdx, State state) {
    int predicateId = predGroundingIdx.predicateId;
    PredicateDef predicateDef = mln.getPredicateDefs().get(predicateId);
    Set<Integer> predicateVals = predicateDef.getVals().getIds();
    double[] cummMarginalProb = new double[predicateVals.size()];
    Formula[] formulas = affectedFormulas(predGroundingIdx, state);
    
    double Z = 0;
    for(Integer val : predicateVals) {
      state.setGrounding(predGroundingIdx, val);
      double exponent = 0;
      /*for(Formula f : formulas) {
        exponent += f.getWeight() * f.countSatisfiedGroundingsCPUNoDb(state);
      }*/
      long[] satisfiedCounts = SatisfiedGroundingCounter.count(formulas, state);
      for(int i = 0; i < formulas.length; i++) {
        exponent += formulas[i].getWeight() * satisfiedCounts[i];
      }
      cummMarginalProb[val] = Math.exp(exponent);
      Z += cummMarginalProb[val];
    }
    
    assert Z != 0 : "Z should not be zero";
    for(int i = 0; i < cummMarginalProb.length; i++) {
      cummMarginalProb[i] /= Z;
    }
    
    for(int i = 1; i < cummMarginalProb.length; i++) {
      cummMarginalProb[i] += cummMarginalProb[i-1];
    }
    
    return cummMarginalProb;
  }

  public void burnIn(State state) {
    if(burntIn)
      return;
    burntIn = true;
    for(int i = 0; i < burnInSamples; i++) {
      getNextSample(state);
    }
  }
  
  public void resetBurntIn() {
    burntIn = false;
  }
  
  @Override
  public void getNextSample(State state) {
    burnIn(state);
    PredicateGroundingIndex predGroundingIdx = state.randomlySelectUnknownGrounding();
    double[] cummMarginalProb;
    cummMarginalProb = getCummMarginalProb(predGroundingIdx, state);
    double p = Math.random();
    for(int j = 0; j < cummMarginalProb.length; j++) {
      if(p < cummMarginalProb[j]) {
        state.setGrounding(predGroundingIdx, j);
        break;
      }
    }
  }

}
