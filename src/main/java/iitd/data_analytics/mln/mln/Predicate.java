package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    assert _symbolicTerms.size() == _predicateDef.getDomains().size();
    
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
  
  void substitute(Map<Integer, Integer> varVals, Map<Integer, String> varSymbolicVals) {
    for(int i = 0; i < terms.size(); i++) {
      if(isVariable.get(i) && varVals.containsKey(terms.get(i))) {
        int varId = terms.get(i);
        terms.set(i, varVals.get(varId));
        symbolicTerms.set(i, varSymbolicVals.get(varId));
        isVariable.set(i, false);
      }
    }
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
  
  public boolean isUnifiable(int[] vals) {
    if(vals.length != terms.size()) {
      return false;
    }
    
    Map<Integer,Integer> varBindings = new HashMap<Integer,Integer>();
    for(int i = 0; i < terms.size(); i++) {
      int term = terms.get(i);
      int val = vals[i];
      boolean isVar = isVariable.get(i);
      if(isVar) {
        if(varBindings.containsKey(term)) {
          if(varBindings.get(term) != val) {
            return false;
          }
        } else {
          varBindings.put(term, val);
        }
      } else {
        if(term != val) {
          return false;
        }
      }
    }
    
    return true;
  }
  
  //display on stdout
  @Override
  public String toString() {
    String str = "";
    if(isNegated == true)
      str += "!";
    str += predicateDef.getPredicateName();
    str += Arrays.deepToString(terms.toArray());
    str += "=" + value;
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
