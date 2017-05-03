package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Arrays;

public class Predicate {

  private PredicateDef predicateDef;
  private ArrayList<String> symbolicTerms;
  private ArrayList<Integer> terms;
  private ArrayList<Boolean> isVariable;
  private String symbolicValue;
  private boolean isNegated;
  private int value;
  
  public Predicate(PredicateDef _predicateDef, ArrayList<String> _symbolicTerms, 
      ArrayList<Boolean> _isVariable, String _symbolicValue, Symbols vars) {
    assert symbolicTerms.size() == predicateDef.getDomains().size();
    
    predicateDef = _predicateDef;
    symbolicTerms = _symbolicTerms;
    terms = new ArrayList<Integer>();
    isVariable = _isVariable;
    symbolicValue = _symbolicValue;
    isNegated = false;
    
    //Encode symbolicTerms and symbolicValue
    for(int i = 0; i < symbolicTerms.size(); i++) {
      String symbolicTerm = symbolicTerms.get(i);
      if(isVariable.get(i)) {
        terms.add(vars.getIdFromSymbol(symbolicTerm));
      } else {
        Domain domain = predicateDef.getDomains().get(i);
        terms.add(domain.getValIdFromSymbol(symbolicTerm));
      }
    }
    value = predicateDef.getVals().getIdFromSymbol(symbolicValue);
  }
  
  public Predicate(Predicate p) {
    symbolicTerms = new ArrayList<String>();
    terms = new ArrayList<Integer>();
    isVariable = new ArrayList<Boolean>();
    
    predicateDef = p.predicateDef;
    for(String s : p.symbolicTerms)
      symbolicTerms.add(new String(s));
    for(Integer i : p.terms)
      terms.add(new Integer(i));
    for(Boolean b : p.isVariable)
      isVariable.add(new Boolean(b));
    symbolicValue = new String(p.symbolicValue);
    isNegated = p.isNegated;
    value = p.value;
  }
  
  //Getters and Setters
  public PredicateDef getPredicateDef() {
    return predicateDef;
  }
  
  public void setIsNegated(boolean _isNegated) {
    isNegated = _isNegated;
  }
  
  public boolean getIsNegated() {
    return isNegated;
  }
  
  public int getVal() {
    return value;
  }
  
  public ArrayList<Integer> getTerms() {
    return terms;
  }
  
  public ArrayList<Boolean> getIsVariable() {
    return isVariable;
  }
  
  //display on stdout
  @Override
  public String toString() {
    String str = "";
    if(isNegated == true)
      str += "!";
    str += predicateDef.getPredicateName();
    str += Arrays.deepToString(terms.toArray());
    str += "=" + symbolicValue;
    return str;
  }
  
  public void displaySymbolic() {
    if(isNegated == true)
      System.out.print("!");
    System.out.print(predicateDef.getPredicateName());
    System.out.print(Arrays.deepToString(symbolicTerms.toArray()));
    System.out.print("=" + symbolicValue);
  }
  
  public void display() {
    System.out.print(this);
  }
}
