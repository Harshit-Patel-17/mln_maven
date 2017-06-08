package iitd.data_analytics.mln.mln;

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
      if(!_vals.get(i).equalsIgnoreCase("")) {
        vals.addMapping(i, _vals.get(i));
      }
    }
  }
  
  public void addVal(String val) {
    vals.addMapping(vals.size(), val);
  }
  
  public Symbols getVals() {
    return vals;
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
  
  public String getValSymbolFromId(int id) {
    return vals.getSymbolFromId(id);
  }
  
  public boolean exist(int id) {
    return vals.exist(id);
  }
  
  public boolean exist(String symbol) {
    return vals.exist(symbol);
  }
  
  public int size() {
    return vals.size();
  }
  
  //Display on stdout
  @Override
  public String toString() {
    String str = "id:" + domainId;
    str += " Name:" + domainName + " ";
    str += vals.toString() + "\n";
    return str;
  }
  
  public void display() {
    System.out.print(this);
  }
  
}
