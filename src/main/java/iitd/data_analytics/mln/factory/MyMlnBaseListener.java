package iitd.data_analytics.mln.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.Predicate;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.Symbols;
import mln_parser.MlnBaseListener;
import mln_parser.MlnParser;
import mln_parser.MlnParser.Domain1Context;
import mln_parser.MlnParser.Domain2Context;
import mln_parser.MlnParser.Formula1Context;
import mln_parser.MlnParser.Formula2Context;
import mln_parser.MlnParser.Formula3Context;
import mln_parser.MlnParser.Formula4Context;
import mln_parser.MlnParser.Formula5Context;
import mln_parser.MlnParser.Formula6Context;
import mln_parser.MlnParser.Formula7Context;
import mln_parser.MlnParser.FormulaBody2Context;
import mln_parser.MlnParser.PredicateContext;
import mln_parser.MlnParser.PredicateDef1Context;
import mln_parser.MlnParser.PredicateDef2Context;
import mln_parser.MlnParser.PredicateDef3Context;

public class MyMlnBaseListener extends MlnBaseListener{
  //Have a look at Mln.g4 file to understand this class
  
  private MlnParser p;
  private Mln mln;
  Map<String,Domain> varsDomain;
  Symbols varsId;
  
  public MyMlnBaseListener(MlnParser _p, Mln _mln) {
    super();
    p = _p;
    mln = _mln;
    varsDomain = new HashMap<String,Domain>();
    varsId = new Symbols();
  }
  
  @Override
  public void exitDomain1(Domain1Context ctx) {
    super.exitDomain1(ctx);
    checkForRedeclaration(ctx.domainName1.getText());
    mln.addDomain(ctx.domainName1.getText(), parseList(ctx.vals1.getText()));
  }
  
  @Override
  public void exitDomain2(Domain2Context ctx) {
    super.exitDomain2(ctx);
    checkForRedeclaration(ctx.domainName2.getText());
    mln.addDomain(ctx.domainName2.getText(), genRange(ctx.vals2.getText()));
  }
  
  @Override
  public void exitPredicateDef1(PredicateDef1Context ctx) {
    super.exitPredicateDef1(ctx);
    checkForRedeclaration(ctx.predicateName1.getText());
    checkForDomainDeclaration(parseList(ctx.doms1.getText()));
    mln.addPredicate(ctx.predicateName1.getText(), parseList(ctx.doms1.getText()),
        parseList(ctx.vals1.getText()));
  }
  
  @Override
  public void exitPredicateDef2(PredicateDef2Context ctx) {
    super.exitPredicateDef2(ctx);
    checkForRedeclaration(ctx.predicateName2.getText());
    checkForDomainDeclaration(parseList(ctx.doms2.getText()));
    mln.addPredicate(ctx.predicateName2.getText(), parseList(ctx.doms2.getText()),
        genRange(ctx.vals2.getText()));
  }
  
  @Override
  public void exitPredicateDef3(PredicateDef3Context ctx) {
    super.exitPredicateDef3(ctx);
    //TODO: Handle this case
    checkForRedeclaration(ctx.predicateName3.getText());
    mln.addPredicate(ctx.predicateName3.getText(), parseList(ctx.doms3.getText()),
        ctx.vals3.getText());
  }
  
  @Override
  public void exitFormulaBody2(FormulaBody2Context ctx) {
    super.exitFormulaBody2(ctx);
    mln.addFormula(ctx.formula().foFormula, varsDomain, varsId);
    varsDomain = new HashMap<String,Domain>();
    varsId = new Symbols();
  }
  
  @Override
  public void exitFormula1(Formula1Context ctx) {
    super.exitFormula1(ctx);
    ctx.foFormula = new FirstOrderFormula<Predicate>(ctx.predicate().pred);
  }
  
  @Override
  public void exitFormula2(Formula2Context ctx) {
    super.exitFormula2(ctx);
    ctx.foFormula = new FirstOrderFormula<Predicate>(FirstOrderFormula.NodeType.NOT,
        ctx.formula().foFormula);
  }
  
  @Override
  public void exitFormula3(Formula3Context ctx) {
    super.exitFormula3(ctx);
    ctx.foFormula = new FirstOrderFormula<Predicate>(FirstOrderFormula.NodeType.AND,
        ctx.formula(0).foFormula, ctx.formula(1).foFormula);
  }
  
  @Override
  public void exitFormula4(Formula4Context ctx) {
    super.exitFormula4(ctx);
    ctx.foFormula = new FirstOrderFormula<Predicate>(FirstOrderFormula.NodeType.OR,
        ctx.formula(0).foFormula, ctx.formula(1).foFormula);
  }
  
  @Override
  public void exitFormula5(Formula5Context ctx) {
    super.exitFormula5(ctx);
    ctx.foFormula = new FirstOrderFormula<Predicate>(FirstOrderFormula.NodeType.IMPLY,
        ctx.formula(0).foFormula, ctx.formula(1).foFormula);
  }
  
  @Override
  public void exitFormula6(Formula6Context ctx) {
    super.exitFormula6(ctx);
    ctx.foFormula = new FirstOrderFormula<Predicate>(FirstOrderFormula.NodeType.EQUIV,
        ctx.formula(0).foFormula, ctx.formula(1).foFormula);
  }
  
  @Override
  public void exitFormula7(Formula7Context ctx) {
    super.exitFormula7(ctx);
    ctx.foFormula = ctx.formula().foFormula;
  }
  
  @Override
  public void exitPredicate(PredicateContext ctx) {
    super.exitPredicate(ctx);
    String predicateName = ctx.predicateName1.getText();
    ArrayList<String> terms = parseList(ctx.terms1.getText());
    String val = ctx.val1.getText();
    ctx.pred = validateAndCreatePredicate(predicateName, terms, val);
  }
  
  private ArrayList<String> parseList(String csv) {
    return new ArrayList<String>(Arrays.asList(csv.split(",")));
  }
  
  private ArrayList<String> genRange(String csv) {
    ArrayList<String> list = new ArrayList<String>();
    String[] split = csv.split(",");
    int startVal = Integer.parseInt(split[0]);
    int endVal = Integer.parseInt(split[2]);
    
    if(startVal > endVal) {
      String msg = "Start value cannot be greater than end value in range declaration";
      p.notifyErrorListeners(msg);
    }
    for(int i = startVal; i <= endVal; i++) {
      list.add(Integer.toString(i));
    }
    return list;
  }
  
  private void checkForRedeclaration(String name) {
    String msg = "";
    if(mln.getDomainSymbols().exist(name)) {
      msg = name + " was earlier used as domain name.";
      p.notifyErrorListeners(msg);
    }
    if(mln.getPredicateSymbols().exist(name)) {
      msg = name + " was earlier used as predicate name.";
      p.notifyErrorListeners(msg);
    }
  }
  
  private void checkForDomainDeclaration(ArrayList<String> domainNames) {
    for(String domainName : domainNames) {
      if(!mln.getDomainSymbols().exist(domainName)) {
        String msg = "Declaration of domain " + domainName + " not found.";
        p.notifyErrorListeners(msg);
      }
    }
  }
  
  private Predicate validateAndCreatePredicate(String predicateName,
      ArrayList<String> terms, String val) {
    PredicateDef predicateDef = mln.getPredicateDefByName(predicateName);
    ArrayList<Boolean> isVariable = new ArrayList<Boolean>();
    for(String term : terms) {
      isVariable.add(Character.isLowerCase(term.charAt(0)));
    }
    unifyTermsWithDomains(predicateName, predicateDef.getDomains(), terms, isVariable);
    if(!predicateDef.getVals().exist(val)) {
      String msg = "Value of predicate " + predicateName + " doesn't match with any valid "
          + "values in definition.";
      p.notifyErrorListeners(msg);
    }
    Predicate p = new Predicate(predicateDef, terms, isVariable, val, varsId);
    return p;
  }
  
  private void unifyTermsWithDomains(String predicateName, ArrayList<Domain> domains,
      ArrayList<String> terms, ArrayList<Boolean> isVariable) {
    if(domains.size() != terms.size()) {
      String msg = "Number of terms of predicate " + predicateName + " doesn't "
          + "matches with its definition.";
      p.notifyErrorListeners(msg);
    }
    
    for(int i = 0; i < domains.size(); i++) {
      Domain domain = domains.get(i);
      String term = terms.get(i);
      if(isVariable.get(i)) {
        //If term is variable then check for consistency of var-domain mapping
        //If variable appears for the first time then create new var-domain mapping
        if(varsDomain.containsKey(term)) {
          if(domain.getDomainId() != varsDomain.get(term).getDomainId()) {
            String msg = "Variable " + term + " doesn't have consistent "
                + "domain in the formula.";
            p.notifyErrorListeners(msg);
          }          
        } else {
          varsDomain.put(term, domain);
          varsId.addMapping(varsId.size(), term);
        }
      } else {
        if(!domain.exist(term)) {
          String msg = "Value " + term + " doesn't exist in domain " + domain.getDomainName();
          p.notifyErrorListeners(msg);
        }
      }
    }
  }
}
