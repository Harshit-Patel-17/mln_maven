package iitd.data_analytics.mln.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import evidence_parser.EvidenceBaseListener;
import evidence_parser.EvidenceParser;
import evidence_parser.EvidenceParser.EvidenceItem1Context;
import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundings;
import query_parser.QueryParser;

public class MyEvidenceBaseListener extends EvidenceBaseListener {

  private EvidenceParser p;
  private Mln mln;
  
  public MyEvidenceBaseListener(EvidenceParser _p, Mln _mln) {
    super();
    p = _p;
    mln = _mln;
  }
  
  @Override
  public void exitEvidenceItem1(EvidenceItem1Context ctx) {
    super.exitEvidenceItem1(ctx);
    validateAndAddEvidenceAtoms(ctx.predicateName.getText(), parseList(ctx.vals.getText()), ctx.val.getText());
  }
  
  private ArrayList<String> parseList(String csv) {
    return new ArrayList<String>(Arrays.asList(csv.split(",")));
  }
  
  private void validateAndAddEvidenceAtoms(String predicateName, ArrayList<String> terms, String val) {
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
    addEvidenceAtoms(predicateName, terms, predicateDef.getDomains(), val);
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
  
  private void addEvidenceAtoms(String predicateName, ArrayList<String> terms, ArrayList<Domain> domains, String val) {
    int predicateId = mln.getPredicateSymbols().getIdFromSymbol(predicateName);
    PredicateGroundings predGroundings = mln.getState().getPredicateGroundings(predicateId);
    ArrayList<String> container = new ArrayList<String>(Arrays.asList(new String[terms.size()]));
    addEvidenceAtomsRec(predGroundings, terms, domains, 0, container, val);
  }
  
  private void addEvidenceAtomsRec(PredicateGroundings predGroundings, ArrayList<String> terms,
      ArrayList<Domain> domains, int currentIdx, ArrayList<String> currentTerms, String val) {
    if(currentIdx == terms.size()) {
      predGroundings.addEvidence(currentTerms, val);;
      return;
    }
    if(terms.get(currentIdx).equalsIgnoreCase("*")) {
      Set<String> symbols = domains.get(currentIdx).getVals().getSymbols();
      for(String symbol : symbols) {
        currentTerms.set(currentIdx, symbol);
        addEvidenceAtomsRec(predGroundings, terms, domains, currentIdx+1, currentTerms, val);
      }
    } else {
      currentTerms.set(currentIdx, terms.get(currentIdx));
      addEvidenceAtomsRec(predGroundings, terms, domains, currentIdx+1, currentTerms, val);
    }
  }
}
