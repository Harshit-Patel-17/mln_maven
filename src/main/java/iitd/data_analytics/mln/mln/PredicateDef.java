package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Arrays;

public class PredicateDef {

  private int predicateId;
  private String predicateName;
  private ArrayList<Domain> domains;
  private Symbols vals;
  
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
  public String getDomainNames() {
    String[] domainNames = new String[domains.size()];
    for(int i = 0; i < domains.size(); i++) {
      domainNames[i] = domains.get(i).getDomainName();
    }
    return Arrays.toString(domainNames);
  }
  
  @Override
  public String toString() {
    String str = "id:" + predicateId;
    str += " Name:" + predicateName;
    str += getDomainNames();
    str += " vals:" + vals.toString() + "\n";
    return str;
  }
  
  public void display() {
    System.out.print(this);
  }
  
}
