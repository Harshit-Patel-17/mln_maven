package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.logic.Literal;

public abstract class Formula {

  private int formulaId;
  private double weight;
  private ArrayList<ArrayList<Predicate>> clauses;
  private Map<String,Domain> varsDomain;
  private Symbols varsId;
  
  public Formula(int _formulaId, FirstOrderFormula<Predicate> foFormula, 
      Map<String,Domain> _varsDomain, Symbols _varsId, double _weight) {
    formulaId = _formulaId;
    
    clauses = new ArrayList<ArrayList<Predicate>>();
    ArrayList<ArrayList<Literal<Predicate>>> _clauses = foFormula.asSetOfClauses();
    for(ArrayList<Literal<Predicate>> _clause : _clauses) {
      ArrayList<Predicate> clause = new ArrayList<Predicate>();
      for(Literal<Predicate> literal : _clause) {
        Predicate atom = new Predicate(literal.getAtom());
        atom.setIsNegated(literal.isNegated());
        clause.add(atom);
      }
      clauses.add(clause);
    }
    
    varsDomain = _varsDomain;
    varsId = _varsId;
    weight = _weight;
  }
  
  //Getters and Setters
  public int getFormulaId() {
    return formulaId;
  }
  
  public double getWeight() {
    return weight;
  }
  
  public void setWeight(double _weight) {
    weight = _weight;
  }
  
  public ArrayList<ArrayList<Predicate>> getClauses() {
    return clauses;
  }
  
  public Map<String,Domain> getVarsDomain() {
    return varsDomain;
  }
  
  public Symbols getVarsId() {
    return varsId;
  }
  
  public boolean groundAtomExist(int predicateId, int[] vals) {
    for(ArrayList<Predicate> clause : clauses) {
      for(Predicate atom : clause) {
        if(atom.getPredicateDef().getPredicateId() == predicateId && atom.isUnifiable(vals)) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  //Abstract methods
  //public abstract long countSatisfiedGroundings(State state);
  public abstract long countSatisfiedGroundingsNoDb(State state, int gpuNo);
  //public abstract long countSatisfiedGroundingsCPU(State state);
  public abstract long countSatisfiedGroundingsCPUNoDb(State state);
  
  //Display on stdout
  @Override
  public String toString() {    
    String str = "id:" + formulaId + " " + "w:" + weight + " ";
    for(ArrayList<Predicate> _clause : clauses) {
      str += Arrays.deepToString(_clause.toArray());
      str += " ";
    }
    str += "\n";
    return str;
  }
  
  public void displaySymbolic() {
    System.out.print("id:" + formulaId + "w:" + weight);
    for(ArrayList<Predicate> _clause : clauses) {
      System.out.print(" (");
      for(Predicate p : _clause) {
        p.displaySymbolic();
        System.out.print(" ");
      }
      System.out.print(")");
    }
    System.out.println("");
  }
  
  public void display() {
    System.out.print(this);
  }
}
