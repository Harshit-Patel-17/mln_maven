package iitd.data_analytics.mln.gpu;

import java.util.Map;

import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.mln.*;

public class GpuFormula extends Formula {

  private int totalVars;
  long totalGroundings;
  private int[] varDomainSizes;
  //Set<GpuClause> clauses;
  int maxThreads;
  
  public GpuFormula(int _formulaId, FirstOrderFormula<Predicate> foFormula, 
      Map<String,Domain> _varsDomain, Symbols _varsId) {
    super(_formulaId, foFormula, _varsDomain, _varsId);
    
    totalVars = _varsId.size();
    varDomainSizes = new int[totalVars];
    totalGroundings = 1;
    for(int i = 0; i < totalVars; i++) {
      String varSymbol = _varsId.getSymbolFromId(i);
      int domainSize = _varsDomain.get(varSymbol).size();
      varDomainSizes[i] = domainSize;
      totalGroundings *= domainSize;
    }
  }

  @Override
  public long countSatisfiedGroundings() {
    System.out.println("Counting Satisfied Groundings");
    return 0;
  }

}
