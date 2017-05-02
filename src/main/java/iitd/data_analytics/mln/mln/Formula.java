package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Map;

import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.logic.Literal;

public abstract class Formula {

  private int formulaId;
  private ArrayList<ArrayList<Predicate>> clauses;
  private Map<String,Domain> varsDomain;
  private Symbols varsId;
  
  public Formula(int _formulaId, FirstOrderFormula<Predicate> foFormula, 
      Map<String,Domain> _varsDomain, Symbols _varsId) {
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
  }
  
  //Getters and Setters
  public int getFormulaId() {
    return formulaId;
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
  
  //Abstract methods
  public abstract long countSatisfiedGroundings();
  
  //Display on stdout
  public void display() {
    System.out.print("id:" + formulaId);
    for(ArrayList<Predicate> _clause : clauses) {
      System.out.print(" (");
      for(Predicate p : _clause) {
        System.out.print(p + " ");
      }
      System.out.print(")");
    }
    System.out.println("");
  }
  
  public void displayEncoded() {
    System.out.print("id:" + formulaId);
    for(ArrayList<Predicate> _clause : clauses) {
      System.out.print(" (");
      for(Predicate p : _clause) {
        p.displayEncoded();
        System.out.print(" ");
      }
      System.out.print(")");
    }
    System.out.println("");
  }
}
