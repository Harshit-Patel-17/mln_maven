package iitd.data_analytics.mln.gpu;

import java.util.ArrayList;
import java.util.Arrays;

import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.Predicate;
import iitd.data_analytics.mln.mln.PredicateDef;

public class GpuClause {
  public int totalPreds;
  public int totalVars;
  public int[] predicates;
  public int[] isNegated;
  public int[] predBaseIdx;
  public int[] predVarMat;
  public int[] valTrue;
  public int[] dbIndex;
  
  public GpuClause(ArrayList<Predicate> _clause, int _totalVars, long _totalGroundings) {
    totalPreds = _clause.size();
    totalVars = _totalVars;
    predicates = new int[totalPreds];
    isNegated = new int[totalPreds];
    predBaseIdx = new int[totalPreds];
    predVarMat = new int[totalPreds * totalVars];
    valTrue = new int[totalPreds];
    dbIndex = new int[totalPreds * (int)_totalGroundings];
    
    for(int i = 0; i < _clause.size(); i++) {
      Predicate predicate = _clause.get(i);
      PredicateDef predicateDef = predicate.getPredicateDef();
      predicates[i] = predicateDef.getPredicateId();
      isNegated[i] = predicate.getIsNegated() ? 1 : 0;
      valTrue[i] = predicate.getVal();
      
      int runningWeight = 1, baseIndex = 0;
      ArrayList<Integer> terms = predicate.getTerms();
      ArrayList<Boolean> isVariable = predicate.getIsVariable();
      ArrayList<Domain> domains = predicateDef.getDomains();
      for(int j = 0; j < terms.size(); j++) {
        if(isVariable.get(j)) {
          int varId = terms.get(j);
          predVarMat[varId * totalPreds + predicates[i]] = runningWeight;
        } else {
          baseIndex += terms.get(j) * runningWeight;
        }
        runningWeight *= domains.get(j).size();
      }
      predBaseIdx[i] = baseIndex;
    }
  }
  
  @Override
  public String toString() {
    String str = "";
    str += "predicates: " + Arrays.toString(predicates) + "\n";
    str += "isNegated: " + Arrays.toString(isNegated) + "\n";
    str += "predBaseIdx: " + Arrays.toString(predBaseIdx) + "\n";
    str += "valTrue: " + Arrays.toString(valTrue) + "\n";
    str += "predVarMat: " + Arrays.toString(predVarMat) + "\n";
    return str;
  }
  
  void display() {
    System.out.print(this);
  }
}
