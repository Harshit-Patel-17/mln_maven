package iitd.data_analytics.mln;

import java.util.ArrayList;

public class Domain {

  int domainId;
  String domainName;
  Symbols vals;
  
  public Domain(int _domainId, String _domainName, ArrayList<String> _vals) {
    domainId = _domainId;
    domainName = _domainName;
    vals = new Symbols();
    addVals(_vals);
  }
  
  //Getter and Setters
  public void addVals(ArrayList<String> _vals) {
    for(int i = 0; i < _vals.size(); i++) {
      vals.addMapping(i, _vals.get(i));
    }
  }
  
  public int getDomainId() {
    return domainId;
  }
  
  public String getDomainName() {
    return domainName;
  }
  
  public int getValIdFromSymbol(String symbol) {
    return vals.getIdFromSymbol(symbol);
  }
  
  public String getValNameFromId(int id) {
    return vals.getSymbolFromId(id);
  }
  
  //Display on stdout
  public void display() {
    System.out.print("id:" + domainId);
    System.out.print(" Name:" + domainName + " ");
    vals.displayAll();
  }
  
}
