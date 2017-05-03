package iitd.data_analytics.mln.gpu;

import java.util.ArrayList;

import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.State;

public class GpuState extends State {

  public GpuState(ArrayList<PredicateDef> predicateDefs) {
    super(predicateDefs);
  }

  @Override
  public Object getGroundings() {
    return null;
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
