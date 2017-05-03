package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Map;

import iitd.data_analytics.mln.gpu.GpuFormula;
import iitd.data_analytics.mln.logic.FirstOrderFormula;

public class Mln {

  private Symbols domainSymbols; 
  //Contains encodings for domain names. Code for a domain name becomes
  //index in ArrayList<Domain>. Helps to quickly find Domain object given its name
  
  private ArrayList<Domain> domains;
  //Domains in MLN
  
  private Symbols predicateSymbols;
  //Contains encodings for predicate names. Code for a predicate name becomes
  //index in ArrayList<PredicateDef>. Helps to quickly find PredicateDef given its name
  
  private ArrayList<PredicateDef> predicateDefs;
  //Definitions of predicates
  
  private ArrayList<Formula> formulas;
  //Contains first order formulas.
  
  private State state;
  
  public Mln() {
    domainSymbols = new Symbols();
    domains = new ArrayList<Domain>();
    predicateSymbols = new Symbols();
    predicateDefs = new ArrayList<PredicateDef>();
    formulas = new ArrayList<Formula>();
  }
  
  //Getters and Setters
  public Symbols getDomainSymbols() {
    return domainSymbols;
  }
  
  public ArrayList<Domain> getDomains() {
    return domains;
  }
  
  public Domain getDomainByName(String domainName) {
    if(!domainSymbols.exist(domainName))
      return null;
    return domains.get(domainSymbols.getIdFromSymbol(domainName));
  }
  
  public void addDomain(String domainName, ArrayList<String> vals) {
    //Size of domains is the id for next domain symbol
    int id = domains.size();
    domainSymbols.addMapping(id, domainName);
    domains.add(new Domain(id, domainName, vals));
  }
  
  public Symbols getPredicateSymbols() {
    return predicateSymbols;
  }
  
  public ArrayList<PredicateDef> getPredicateDefs() {
    return predicateDefs;
  }
  
  public PredicateDef getPredicateDefByName(String predicateName) {
    if(!predicateSymbols.exist(predicateName))
      return null;
    return predicateDefs.get(predicateSymbols.getIdFromSymbol(predicateName));
  }
  
  public void addPredicate(String predicateName, ArrayList<String> domainNames, 
      ArrayList<String> vals) {
    //Size of predicateDefs is the id for next predicate symbol
    int id = predicateSymbols.size();
    predicateSymbols.addMapping(id, predicateName);
    
    //Construct domain
    ArrayList<Domain> predicateDomains = new ArrayList<Domain>();
    for(String domainName : domainNames) {
      predicateDomains.add(domains.get(domainSymbols.getIdFromSymbol(domainName)));
    }
    
    //Construct Symbol object for vals
    Symbols valsSymbols = new Symbols();
    for(int i = 0; i < vals.size(); i++) {
      valsSymbols.addMapping(i, vals.get(i));
    }
    
    predicateDefs.add(new PredicateDef(id, predicateName, predicateDomains, valsSymbols));
  }
  
  public void addPredicate(String predicateName, ArrayList<String> domainNames,
      String valsDomain) {
    //Size of predicateDefs is the id for next predicate symbol
    int id = predicateSymbols.size();
    predicateSymbols.addMapping(id, predicateName);
    
    //Construct domain
    ArrayList<Domain> predicateDomains = new ArrayList<Domain>();
    for(String domainName : domainNames) {
      predicateDomains.add(domains.get(domainSymbols.getIdFromSymbol(domainName)));
    }
    
    Symbols valsSymbols = domains.get(domainSymbols.getIdFromSymbol(valsDomain))
        .getVals();
    predicateDefs.add(new PredicateDef(id, predicateName, predicateDomains,
        valsSymbols));
  }
  
  public ArrayList<Formula> getFormulas() {
    return formulas;
  }
  
  public void addFormula(FirstOrderFormula<Predicate> foFormula, 
      Map<String,Domain> varsDomain, Symbols varsId) {
    //Size of formulas is the id for the next formula
    int formulaId = formulas.size();
    formulas.add(new GpuFormula(formulaId, foFormula, varsDomain, varsId));
  }
  
  public void addState(State _state) {
    state = _state;
  }
  
  public State getState() {
    return state;
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
  
  public void displayPredicateSymbols() {
    predicateSymbols.displayAll();
  }
  
  public void displayPredicateDefs() {
    for(PredicateDef predicateDef : predicateDefs) {
      predicateDef.display();
    }
  }
  
  public void displayFormulasSymbolic() {
    for(Formula formula : formulas) {
      formula.displaySymbolic();
    }
  }
  
  public void displayFormulas() {
    for(Formula formula : formulas) {
      System.out.print(formula);
    }
  }
}
