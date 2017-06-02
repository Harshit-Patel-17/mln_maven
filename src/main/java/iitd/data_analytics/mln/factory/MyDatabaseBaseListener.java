package iitd.data_analytics.mln.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import database_parser.DatabaseBaseListener;
import database_parser.DatabaseParser;
import database_parser.DatabaseParser.DatabaseItem1Context;
import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundings;

public class MyDatabaseBaseListener extends DatabaseBaseListener {

  private DatabaseParser p;
  private Mln mln;
  
  public MyDatabaseBaseListener(DatabaseParser _p, Mln _mln) {
    super();
    p = _p;
    mln = _mln;
  }
  
  @Override
  public void exitDatabaseItem1(DatabaseItem1Context ctx) {
    super.exitDatabaseItem1(ctx);
    validateAndAddDatabaseAtoms(ctx.predicateName.getText(), parseList(ctx.vals.getText()), ctx.val.getText());
  }
  
  private ArrayList<String> parseList(String csv) {
    return new ArrayList<String>(Arrays.asList(csv.split(",")));
  }
  
  private void validateAndAddDatabaseAtoms(String predicateName, ArrayList<String> terms, String val) {
    if(!mln.getPredicateSymbols().exist(predicateName)) {
      String msg = "Predicate " + predicateName + " is not defined in Mln.";
      p.notifyErrorListeners(msg);
    }
    PredicateDef predicateDef = mln.getPredicateDefByName(predicateName);
    unifyTermsWithDomains(predicateName, predicateDef.getDomains(), terms);
    if(!predicateDef.getVals().exist(val)) {
      String msg = "Value of predicate " + predicateName + " doesn't match with any valid "
          + "values in definition.";
      p.notifyErrorListeners(msg);
    }
    addDatabaseAtoms(predicateName, terms, predicateDef.getDomains(), val);
  }
  
  private void unifyTermsWithDomains(String predicateName, ArrayList<Domain> domains,
      ArrayList<String> terms) {
    if(domains.size() != terms.size()) {
      String msg = "Number of terms of predicate " + predicateName + " doesn't "
          + "matches with its definition.";
      p.notifyErrorListeners(msg);
    }
    
    for(int i = 0; i < domains.size(); i++) {
      Domain domain = domains.get(i);
      String term = terms.get(i);
      if(!term.equalsIgnoreCase("*")) {
        if(!domain.exist(term)) {
          String msg = "Value " + term + " doesn't exist in domain " + domain.getDomainName();
          p.notifyErrorListeners(msg);
        }
      }
    }
  }
  
  private void addDatabaseAtoms(String predicateName, ArrayList<String> terms, ArrayList<Domain> domains, String val) {
    int predicateId = mln.getPredicateSymbols().getIdFromSymbol(predicateName);
    //PredicateGroundings predGroundings = mln.getDataBase().getPredicateGroundings(predicateId);
    ArrayList<String> container = new ArrayList<String>(Arrays.asList(new String[terms.size()]));
    addDatabaseAtomsRec(predicateId, terms, domains, 0, container, val);
  }
  
  private void addDatabaseAtomsRec(int predicateId, ArrayList<String> terms,
      ArrayList<Domain> domains, int currentIdx, ArrayList<String> currentTerms, String val) {
    if(currentIdx == terms.size()) {
      mln.getDataBase().addEvidence(predicateId, currentTerms, val);;
      return;
    }
    if(terms.get(currentIdx).equalsIgnoreCase("*")) {
      Set<String> symbols = domains.get(currentIdx).getVals().getSymbols();
      for(String symbol : symbols) {
        currentTerms.set(currentIdx, symbol);
        addDatabaseAtomsRec(predicateId, terms, domains, currentIdx+1, currentTerms, val);
      }
    } else {
      currentTerms.set(currentIdx, terms.get(currentIdx));
      addDatabaseAtomsRec(predicateId, terms, domains, currentIdx+1, currentTerms, val);
    }
  }
  
}
