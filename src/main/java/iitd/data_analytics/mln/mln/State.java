package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Random;

public abstract class State {

  private PredicateGroundings[] predicateGroundings;
  private Random random;
  
  public State(ArrayList<PredicateDef> predicateDefs) {
    predicateGroundings = new PredicateGroundings[predicateDefs.size()];
    for(PredicateDef predicateDef : predicateDefs) {
      predicateGroundings[predicateDef.getPredicateId()] = 
          new PredicateGroundings(predicateDef);
    }
    random = new Random(Config.seed);
  }
  
  public abstract Object getAllGroundings();
  
  public int getGrounding(int predicateId, int groundIdx) {
    return predicateGroundings[predicateId].getGroundings()[groundIdx];
  }
  
  public int[] getGroundings(int predicateId) {
    assert (predicateId >= 0) && (predicateId < predicateGroundings.length);
    return predicateGroundings[predicateId].getGroundings();
  }
  
  public PredicateGroundings getPredicateGroundings(int predicateId) {
    assert (predicateId >= 0) && (predicateId < predicateGroundings.length);
    return predicateGroundings[predicateId];
  }
  
  public PredicateGroundingIndex randomlySelectUnknownGrounding() {
    PredicateGroundingIndex predGroundingIdx = new PredicateGroundingIndex();
    
    // Determine total number of unknown groundings
    int totalUnknownGroundings = 0;
    for(PredicateGroundings _predicateGroundings : predicateGroundings) {
      totalUnknownGroundings += _predicateGroundings.getUnknownGroundings().size();
    }
    
    // Uniformly select an unknown grounding
    int selectedGrounding = random.nextInt(totalUnknownGroundings);
    totalUnknownGroundings = 0;
    for(PredicateGroundings _predicateGroundings : predicateGroundings) {
      totalUnknownGroundings += _predicateGroundings.getUnknownGroundings().size();
      if(selectedGrounding < totalUnknownGroundings) {
        int unknownGroundingIndex = selectedGrounding - (totalUnknownGroundings - 
            _predicateGroundings.getUnknownGroundings().size());
        predGroundingIdx.predicateId = _predicateGroundings.getPredicateId();
        predGroundingIdx.groundingId = _predicateGroundings.getUnknownGroundings().get(unknownGroundingIndex);
        break;
      }
    }
    return predGroundingIdx;
  }
  
  public void setGrounding(PredicateGroundingIndex predGroundingIdx, int val) {
    int predicateId = predGroundingIdx.predicateId;
    int groundingId = predGroundingIdx.groundingId;
    predicateGroundings[predicateId].setGrounding(groundingId, val);
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
