package iitd.data_analytics.mln;

import java.util.ArrayList;

public class Mln {

  private Symbols domainSymbols; 
  //Contains encodings for domain names. Code for the domain name becomes
  //index in ArrayList<Domain>. Helps to quickly find Domain object given its name
  
  private ArrayList<Domain> domains;
  
  public Mln() {
    domainSymbols = new Symbols();
    domains = new ArrayList<Domain>();
  }
  
  //Getters and Setters
  public Symbols getDomainSymbols() {
    return domainSymbols;
  }
  
  //Manipulators
  public void addDomain(String domainName, ArrayList<String> vals) {
    //Size of map is the id for next symbol
    int id = domains.size();
    domainSymbols.addMapping(id, domainName);
    domains.add(new Domain(id, domainName, vals));
  }
  
  //Display on stdout
  public void displayDomainSymbols() {
    domainSymbols.displayAll();
  }
  
  public void displayDomains() {
    for(Domain domain : domains) {
      domain.display();
    }
  }
  
}
