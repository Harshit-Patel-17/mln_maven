package iitd.data_analytics.mln.sampler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import iitd.data_analytics.mln.inference.SatisfiedGroundingCounter;
import iitd.data_analytics.mln.inference.SatisfiedGroundingDiffCounter;
import iitd.data_analytics.mln.mln.Config;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundingIndex;
import iitd.data_analytics.mln.mln.State;

public class GibbsSampler extends Sampler {

  private Mln mln;
  private State state;
  private int burnInSamples;
  private boolean burntIn;
  private int totalFormulas;
  private long[] formulaSatCounts;
  private Random random;
  
  public GibbsSampler(Mln _mln, State _state, int _burnInSamples) {
    mln = _mln;
    state = _state;
    burnInSamples = _burnInSamples;
    burntIn = false;
    totalFormulas = mln.getFormulas().size();
    formulaSatCounts = new long[totalFormulas];
    for(int i = 0; i < totalFormulas; i++) {
      formulaSatCounts[i] = -1;
    }
    random = new Random(Config.seed);
  }
  
  public void initFormulaSatCounts() {
    Formula[] formulas = mln.getFormulas().toArray(new Formula[mln.getFormulas().size()]);
    assert formulas.length == mln.getFormulas().size();
    formulaSatCounts = SatisfiedGroundingCounter.count(formulas, state);
  }
  
  public long[] getFormulaSatCounts() {
    return formulaSatCounts;
  }
  
  private Formula[] affectedFormulas(PredicateGroundingIndex predGroundingIdx) {
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
  
  private long getUnnormalizedLogProb(Formula[] formulas, PredicateGroundingIndex predGroundingIdx, 
      int newVal, long[][] satCountBuffer, long[] formulaSatCounts) {
    long exponent = 0;
    
    int predicateId = predGroundingIdx.predicateId;
    int groundingId = predGroundingIdx.groundingId;
    
    int oldVal = state.getGrounding(predicateId, groundingId);
    state.setGrounding(predGroundingIdx, newVal);
    //long[] satisfiedCounts = SatisfiedGroundingCounter.count(formulas, state);
    long[] satisfiedCountsDiff = SatisfiedGroundingDiffCounter.countDiff(formulas, state, predGroundingIdx, oldVal, 
        newVal, formulaSatCounts);
    for(int i = 0; i < formulas.length; i++) {
      formulaSatCounts[formulas[i].getFormulaId()] = satisfiedCountsDiff[i];
      satCountBuffer[i][newVal] = satisfiedCountsDiff[i];
    }
    for(int i = 0; i < formulas.length; i++) {
      exponent += formulas[i].getWeight() * satisfiedCountsDiff[i];
    }
    
    return exponent;
  }
  
  private void genNextSample(PredicateGroundingIndex predGroundingIdx) {
    int predicateId = predGroundingIdx.predicateId;
    int groundingId = predGroundingIdx.groundingId;
    PredicateDef predicateDef = mln.getPredicateDefs().get(predicateId);
    Set<Integer> predicateVals = predicateDef.getVals().getIds();
    double[] cummMarginalProb = new double[predicateVals.size()];
    Formula[] formulas = affectedFormulas(predGroundingIdx);
    long[][] satCountBuffer = new long[formulas.length][];
    for(int i = 0; i < formulas.length; i++) {
      satCountBuffer[i] = new long[predicateVals.size()];
    }
    
    double exponent = 0, Z = 0;
    int currentVal = state.getGrounding(predicateId, groundingId);
    exponent = getUnnormalizedLogProb(formulas,predGroundingIdx, currentVal, satCountBuffer, 
        formulaSatCounts);
    cummMarginalProb[currentVal] = Math.exp(exponent);
    Z += cummMarginalProb[currentVal];
    
    for(Integer val : predicateVals) {
      if(val != currentVal) {
        exponent = getUnnormalizedLogProb(formulas, predGroundingIdx, val, satCountBuffer,
            formulaSatCounts);
        cummMarginalProb[val] = Math.exp(exponent);
        Z += cummMarginalProb[val];
      } 
    }
    
    assert Z != 0 : "Z should not be zero";
    for(int i = 0; i < cummMarginalProb.length; i++) {
      cummMarginalProb[i] /= Z;
    }
    
    for(int i = 1; i < cummMarginalProb.length; i++) {
      cummMarginalProb[i] += cummMarginalProb[i-1];
    }
    
    double p = random.nextDouble();//Math.random();
    for(int i = 0; i < cummMarginalProb.length; i++) {
      if(p < cummMarginalProb[i]) {
        state.setGrounding(predGroundingIdx, i);
        for(int j = 0; j < formulas.length; j++) {
          formulaSatCounts[formulas[j].getFormulaId()] = satCountBuffer[j][i];
        }
        break;
      }
    }
  }
  
  /*private void genNextSample(PredicateGroundingIndex predGroundingIdx, State state) {
    int predicateId = predGroundingIdx.predicateId;
    int groundingId = predGroundingIdx.groundingId;
    PredicateDef predicateDef = mln.getPredicateDefs().get(predicateId);
    Set<Integer> predicateVals = predicateDef.getVals().getIds();
    double[] cummMarginalProb = new double[predicateVals.size()];
    Formula[] formulas = affectedFormulas(predGroundingIdx, state);
    
    double Z = 0;
    for(Integer val : predicateVals) {
      state.setGrounding(predGroundingIdx, val);
      long exponent = 0;
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
    
    double p = random.nextDouble();//Math.random();
    for(int i = 0; i < cummMarginalProb.length; i++) {
      if(p < cummMarginalProb[i]) {
        state.setGrounding(predGroundingIdx, i);
        break;
      }
    }
  }*/

  public void burnIn() {
    if(burntIn)
      return;
    burntIn = true;
    for(int i = 0; i < burnInSamples; i++) {
      getNextSample();
    }
    System.out.println("BurnIn completed");
  }
  
  public void resetBurntIn() {
    burntIn = false;
  }
  
  @Override
  public State getNextSample() {
    burnIn();
    //PredicateGroundingIndex predGroundingIdx = state.randomlySelectUnknownGrounding();
    PredicateGroundingIndex predGroundingIdx = state.getNextUnknownGrounding();
    genNextSample(predGroundingIdx);
    return state;
  }
  
  @Override
  public State getState() {
    return state;
  }

}
