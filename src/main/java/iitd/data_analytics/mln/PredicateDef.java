package iitd.data_analytics.mln;

import java.util.ArrayList;

public class PredicateDef {

  int predicateId;
  String predicateName;
  ArrayList<Domain> domains;
  Symbols vals;
  
  public PredicateDef(int _predicateId, String _predicateName,
      ArrayList<Domain> _domains, Symbols _vals) {
    predicateId = _predicateId;
    predicateName = _predicateName;
    domains = _domains;
    vals = _vals;
  }
  
  //Getters and Setters
  public int getPredicateId() {
    return predicateId;
  }
  
  public String getPredicateName() {
    return predicateName;
  }
  
  public ArrayList<Domain> getDomains() {
    return domains;
  }
  
  public Symbols getVals() {
    return vals;
  }
  
  //Display on stdout
  public void displayDomainNames() {
    System.out.print("(");
    for(Domain domain : domains) {
      System.out.print(domain.getDomainName());
      System.out.print(",");
    }
    System.out.print(")");
  }
  
  public void display() {
    System.out.print("id:" + predicateId);
    System.out.print(" Name:" + predicateName);
    displayDomainNames();
    System.out.print(" vals:");
    vals.displayAll();
  }
  
}
