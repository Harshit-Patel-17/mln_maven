package iitd.data_analytics.mln.mln;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public abstract class State {

  private PredicateGroundings[] predicateGroundings;
  private PredicateGroundingIndex currentUnknownGrounding; //Useful for GibbsSampling
  private Random random;
  
  public State(ArrayList<PredicateDef> predicateDefs) {
    predicateGroundings = new PredicateGroundings[predicateDefs.size()];
    for(PredicateDef predicateDef : predicateDefs) {
      predicateGroundings[predicateDef.getPredicateId()] = new PredicateGroundings(predicateDef);
    }
    random = new Random(Config.seed);
    currentUnknownGrounding = new PredicateGroundingIndex();
    for(int i = 0; i < predicateGroundings.length; i++) {
      if(predicateGroundings[i].getUnknownGroundings().size() != 0) {
        currentUnknownGrounding.predicateId = predicateGroundings[i].getPredicateDef().getPredicateId();
        currentUnknownGrounding.groundingId = 0; 
        break;
        //It is not the actual groundId but the index of groundId in array of unknown groundings
      }
    }
  }
  
  public abstract Object getAllGroundings(int gpuNo);
  
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
  
  public PredicateGroundingIndex getNextUnknownGrounding() {
    PredicateGroundingIndex predGroundingIdx = new PredicateGroundingIndex();
    int predicateId = currentUnknownGrounding.predicateId;
    int groundingId = currentUnknownGrounding.groundingId;
    
    predGroundingIdx.predicateId = predicateId;
    predGroundingIdx.groundingId = predicateGroundings[predicateId].getUnknownGroundings().get(groundingId);
    
    //Update currentUnknownGrounding 
    if(predicateGroundings[predicateId].getUnknownGroundings().size() == groundingId + 1) {
      //Next grounding belongs to some other predicate
      int totalPreds = predicateGroundings.length;
      for(int i = (predicateId + 1) % totalPreds; i != predicateId; i = (i+1) % totalPreds) {
        if(predicateGroundings[i].getUnknownGroundings().size() != 0) {
          currentUnknownGrounding.predicateId = i;
          break;
        }
        //It is not the actual groundId but the index of groundId in array of unknown groundings
      }
      currentUnknownGrounding.groundingId = 0;
    } else {
      currentUnknownGrounding.groundingId += 1;
      //It is not the actual groundId but the index of groundId in array of unknown groundings
    }
    
    return predGroundingIdx;
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
  
  public void increaseMarginalCounts() {
    for(PredicateGroundings predGroundings : predicateGroundings) {
      predGroundings.increaseMarginalCounts();
    }
  }
  
  public void resetMarginalCounts() {
    for(PredicateGroundings predGroundings : predicateGroundings) {
      predGroundings.resetMarginalCounts();
    }
  }
  
  public void addQuery(int predicateId, ArrayList<String> symbolicTerms) {
    PredicateGroundings predGroundings = predicateGroundings[predicateId];
    predGroundings.addQuery(symbolicTerms);
  }
  
  public void addEvidence(int predicateId, ArrayList<String> symbolicTerms, String symbolicVal) {
    PredicateGroundings predGroundings = predicateGroundings[predicateId];
    predGroundings.addEvidence(symbolicTerms, symbolicVal);
  }
  
  public void destroy() {}
  
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
  
  public void outputMarginals(String outputFile) throws FileNotFoundException {
    PrintWriter writer = new PrintWriter(outputFile);
    for(PredicateGroundings predGroundings : predicateGroundings) {
      predGroundings.outputMarginals(writer);
    }
    writer.close();
  }
  
  public void outputMaxMarginals(String outputFile) throws FileNotFoundException {
    PrintWriter writer = new PrintWriter(outputFile);
    for(PredicateGroundings predGroundings : predicateGroundings) {
      predGroundings.outputMaxMarginals(writer);
    }
    writer.close();
  }
}
