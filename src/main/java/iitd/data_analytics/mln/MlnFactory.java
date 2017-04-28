package iitd.data_analytics.mln;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import mln_parser.*;
import mln_parser.MlnParser.DomainContext;
import mln_parser.MlnParser.DomainblockContext;
import mln_parser.MlnParser.IntrangeContext;
import mln_parser.MlnParser.PredicateDefContext;
import mln_parser.MlnParser.SymblistContext;

public class MlnFactory {
  
  public Mln createMln(InputStream in) throws IOException {
    //New MLN object
    Mln mln = new Mln();
    
    //Create MLN lexer object
    MlnLexer l = new MlnLexer(new ANTLRInputStream(in));
    
    //Create MLN parser object
    MlnParser p = new MlnParser(new CommonTokenStream(l));
    
    //Remove default error listener
    //Add custom error listener to throw exceptions on parsing errors
    p.removeErrorListeners();
    p.addErrorListener(new BaseErrorListener() {
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, 
          int charPositionInLine, String msg, RecognitionException e) {
        String position = line + ":" + charPositionInLine;
        throw new IllegalStateException("failed to parse at line " + position + " " + msg);
      }
    });
    
    //Add event listener for syntax directed definition. 
    //Refer to Mln.g4 to understand the class MyMlnBaseListener
    p.addParseListener(new MyMlnBaseListener(p, mln));
    
    //Start parsing the file
    p.mln();
    
    System.out.println("\nDomains");
    mln.displayDomainSymbols();
    mln.displayDomains();
    
    System.out.println("\nPredicates");
    mln.displayPredicateDefs();
    
    return mln;
  }
}

class MyMlnBaseListener extends MlnBaseListener {
  //Have a look at Mln.g4 file to understand this class
  
  private MlnParser p;
  private Mln mln;
  
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
  
  public MyMlnBaseListener(MlnParser _p, Mln _mln) {
    super();
    p = _p;
    mln = _mln;
  }
  
  @Override
  public void exitDomain(DomainContext ctx) {
    super.exitDomain(ctx);
    switch(ctx.altNum) {
    case 1: 
      checkForRedeclaration(ctx.domainName1.getText());
      mln.addDomain(ctx.domainName1.getText(), parseList(ctx.vals1.getText()));
      break;
    case 2:
      checkForRedeclaration(ctx.domainName2.getText());
      mln.addDomain(ctx.domainName2.getText(), genRange(ctx.vals2.getText()));
      break;
    }
  }
  
  @Override
  public void exitPredicateDef(PredicateDefContext ctx) {
    super.exitPredicateDef(ctx);
    switch(ctx.altNum) {
    case 1:
      checkForRedeclaration(ctx.predicateName1.getText());
      mln.addPredicate(ctx.predicateName1.getText(), parseList(ctx.doms1.getText()),
          parseList(ctx.vals1.getText()));
      break;
    case 2:
      checkForRedeclaration(ctx.predicateName2.getText());
      mln.addPredicate(ctx.predicateName2.getText(), parseList(ctx.doms2.getText()),
          genRange(ctx.vals2.getText()));
      break;
    case 3:
      checkForRedeclaration(ctx.predicateName3.getText());
      mln.addPredicate(ctx.predicateName3.getText(), parseList(ctx.doms3.getText()),
          ctx.vals3.getText());
      break;
    }
  }
}