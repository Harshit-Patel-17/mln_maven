package iitd.data_analytics.mln.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.Predicate;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundings;
import mln_parser.MlnParser;
import query_parser.QueryBaseListener;
import query_parser.QueryParser;
import query_parser.QueryParser.QueryItem1Context;

public class MyQueryBaseListener extends QueryBaseListener {
  
  private QueryParser p;
  private Mln mln;
  
  public MyQueryBaseListener(QueryParser _p, Mln _mln) {
    super();
    p = _p;
    mln = _mln;
  }
  
  @Override
  public void exitQueryItem1(QueryItem1Context ctx) {
    super.exitQueryItem1(ctx);
    validateAndAddQueryAtoms(ctx.predicateName.getText(), parseList(ctx.vals.getText()));
  }
  
  private ArrayList<String> parseList(String csv) {
    return new ArrayList<String>(Arrays.asList(csv.split(",")));
  }
  
  private void validateAndAddQueryAtoms(String predicateName, ArrayList<String> terms) {
    if(!mln.getPredicateSymbols().exist(predicateName)) {
      String msg = "Predicate " + predicateName + " is not defined in Mln.";
      p.notifyErrorListeners(msg);
    }
    PredicateDef predicateDef = mln.getPredicateDefByName(predicateName);
    unifyTermsWithDomains(predicateName, predicateDef.getDomains(), terms);
    addQueryAtoms(predicateName, terms, predicateDef.getDomains());
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
  
  private void addQueryAtoms(String predicateName, ArrayList<String> terms, ArrayList<Domain> domains) {
    int predicateId = mln.getPredicateSymbols().getIdFromSymbol(predicateName);
    PredicateGroundings predGroundings = mln.getState().getPredicateGroundings(predicateId);
    ArrayList<String> container = new ArrayList<String>(Arrays.asList(new String[terms.size()]));
    addQueryAtomsRec(predGroundings, terms, domains, 0, container);
  }
  
  private void addQueryAtomsRec(PredicateGroundings predGroundings, ArrayList<String> terms,
      ArrayList<Domain> domains, int currentIdx, ArrayList<String> currentTerms) {
    if(currentIdx == terms.size()) {
      predGroundings.addQuery(currentTerms);
      return;
    }
    if(terms.get(currentIdx).equalsIgnoreCase("*")) {
      Set<String> symbols = domains.get(currentIdx).getVals().getSymbols();
      for(String symbol : symbols) {
        currentTerms.set(currentIdx, symbol);
        addQueryAtomsRec(predGroundings, terms, domains, currentIdx+1, currentTerms);
      }
    } else {
      currentTerms.set(currentIdx, terms.get(currentIdx));
      addQueryAtomsRec(predGroundings, terms, domains, currentIdx+1, currentTerms);
    }
  }
}
