package iitd.data_analytics.mln.mln;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Symbols {
  
  private Map<String, Integer> symbolToId = new HashMap<String, Integer>(); 
  private Map<Integer, String> idToSymbol = new HashMap<Integer, String>();
  
  public String getSymbolFromId(int id) {
    return (String) idToSymbol.get(id);
  }
  
  public int getIdFromSymbol(String symbol) {
    return (int) symbolToId.get(symbol);
  }
  
  public Set<String> getSymbols() {
    return symbolToId.keySet();
  }
  
  public void addMapping(int id, String symbol) {
    symbolToId.put(symbol, id);
    idToSymbol.put(id, symbol);
  }
  
  public boolean exist(String symbol) {
    return symbolToId.containsKey(symbol);
  }
  
  public boolean exist(int id) {
    return idToSymbol.containsKey(id);
  }
  
  public int size() {
    return symbolToId.size();
  }
  
  public void clear() {
    symbolToId.clear();
    idToSymbol.clear();
  }
  
  @Override
  public String toString() {
    return idToSymbol.toString();
  }
  
  public void displayAll() {
    System.out.print(this);
  }
}
