package iitd.data_analytics.mln.mln;

import java.util.ArrayList;

public abstract class State {

  private PredicateGroundings[] predicateGroundings;
  
  public State(ArrayList<PredicateDef> predicateDefs) {
    predicateGroundings = new PredicateGroundings[predicateDefs.size()];
    for(PredicateDef predicateDef : predicateDefs) {
      predicateGroundings[predicateDef.getPredicateId()] = 
          new PredicateGroundings(predicateDef);
    }
  }
  
  public abstract Object getAllGroundings();
  
  public int getGrounding(int predicateId, int groundIdx) {
    return predicateGroundings[predicateId].getGroundings()[groundIdx];
  }
  
  public int[] getGroundings(int predicateId) {
    assert (predicateId >= 0) && (predicateId < predicateGroundings.length);
    return predicateGroundings[predicateId].getGroundings();
  }
  
  @Override
  public String toString() {
    String str = "";
    for(PredicateGroundings predicateGrounding : predicateGroundings) {
      str += predicateGrounding.toString();
      str += "\n";
    }    
    return str;
  }
  
  public void display() {
    System.out.print(this);
  }
  
}
