package iitd.data_analytics.mln.sampler;

import iitd.data_analytics.mln.mln.State;

public abstract class Sampler {

  public abstract State getNextSample();
  public abstract State getState();
  
}
