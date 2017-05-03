package iitd.data_analytics.mln.gpu;

import java.util.ArrayList;
import java.util.Map;

import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.mln.*;

public class GpuFormula extends Formula {

  private int totalVars;
  private long totalGroundings;
  private int[] varDomainSizes;
  private ArrayList<GpuClause> clauses;
  private int maxThreads;
  
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
    maxThreads = GpuConfig.maxThreads;
    
    clauses = new ArrayList<GpuClause>();
    for(ArrayList<Predicate> clause : super.getClauses()) {
      clauses.add(new GpuClause(clause, totalVars, totalGroundings));
    }
  }

  @Override
  public long countSatisfiedGroundings() {
    System.out.println("Counting Satisfied Groundings");
    return 0;
  }
  
  @Override
  public String toString() {
    String str = super.toString();
    for(GpuClause clause : clauses) {
      str += clause.toString();
    }
    return str;
  }
  
  @Override
  public void display() {
    System.out.print(this);
  }

}
