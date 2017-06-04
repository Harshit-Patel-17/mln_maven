package iitd.data_analytics.mln.learning;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import iitd.data_analytics.mln.gpu.GpuState;
import iitd.data_analytics.mln.inference.SatisfiedGroundingCounter;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.State;
import iitd.data_analytics.mln.sampler.GibbsSampler;
import iitd.data_analytics.mln.sampler.Sampler;

public class PerWeightLearningRatesLearning extends Learning {

  private double alpha; //Learning rate
  private int maxIter;
  private double eta;
  
  public PerWeightLearningRatesLearning(double _alpha, int _maxIter, double _eta) {
    alpha = _alpha;
    maxIter = _maxIter;
    eta = _eta;
  }
  
  @Override
  public void learn(Mln mln) {
    Formula[] formulas = mln.getFormulas().toArray(new Formula[mln.getFormulas().size()]);
    long[] satCountsDb = null;
    State state = new GpuState(mln.getPredicateDefs());
    
    for(Formula formula : formulas) {
      formula.setWeight(0);
      satCountsDb = SatisfiedGroundingCounter.count(formulas, mln.getDataBase());
    }
    
    for(int i = 0; i < maxIter; i++) {
      double maxWeightChange = 0;
      int totalSamples = 1000, burnInSamples = 1000;
      double[] expectedSatCounts = new double[formulas.length];
      
      Sampler sampler = new GibbsSampler(mln, burnInSamples);
      for(int j = 0; j < totalSamples; j++) {
        sampler.getNextSample(state);
        long[] satCounts = SatisfiedGroundingCounter.count(formulas, state);
        for(int k = 0; k < satCounts.length; k++) {
          expectedSatCounts[k] += satCounts[k];
        }
      }
      
      for(int j = 0; j < expectedSatCounts.length; j++) {
        expectedSatCounts[j] /= totalSamples;
      }
      
      System.out.println(Arrays.toString(satCountsDb));
      System.out.println(Arrays.toString(expectedSatCounts));
      for(int j = 0; j < formulas.length; j++) {
        double new_alpha = satCountsDb[j] == 0 ? alpha : alpha / satCountsDb[j];
        //double diff = new_alpha / Math.sqrt(i+1) * (satCountsDb[j] - expectedSatCounts[j]);
        double diff = alpha * (satCountsDb[j] - expectedSatCounts[j]);
        maxWeightChange = Math.max(maxWeightChange, Math.abs(diff));
        double new_weight = formulas[j].getWeight() + diff;
        formulas[j].setWeight(new_weight);
      }
      
      System.out.println("Iteration:" + i + " MaxChange:" + maxWeightChange);
      DecimalFormat df = new DecimalFormat("#.####");
      for(Formula formula : formulas) {
        System.out.println(df.format(formula.getWeight()));
      }
      
      if(maxWeightChange < eta) {
        break;
      }
    }
  }
  
}
