package iitd.data_analytics.mln;

import java.util.ArrayList;

public class Predicate {

  private PredicateDef predicateDef;
  ArrayList<String> symbolicTerms;
  ArrayList<Integer> terms;
  ArrayList<Boolean> isVariable;
  String symbolicValue;
  int value;
  
  public Predicate(PredicateDef _predicateDef, ArrayList<String> _symbolicTerms, 
      String _symbolicValue) {
    predicateDef = _predicateDef;
    symbolicTerms = _symbolicTerms;
    symbolicValue = _symbolicValue;
  }
  
  //display on stdout
  @Override
  public String toString() {
    String str = predicateDef.getPredicateName() + "(";
    for(String symbolicTerm : symbolicTerms) {
      str += symbolicTerm + ",";
    }
    str += ")=" + symbolicValue;
    return str;
  }
  
  public void display() {
    System.out.print(predicateDef.getPredicateName());
    System.out.print("(");
    for(String symbolicTerm : symbolicTerms) {
      System.out.print(symbolicTerm);
      System.out.print(",");
    }
    System.out.print(")=" + symbolicValue);
  }
}
