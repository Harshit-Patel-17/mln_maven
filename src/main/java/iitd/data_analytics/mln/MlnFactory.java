package iitd.data_analytics.mln;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import mln_parser.*;
import mln_parser.MlnParser.DomainContext;
import mln_parser.MlnParser.DomainblockContext;
import mln_parser.MlnParser.IntrangeContext;
import mln_parser.MlnParser.ListContext;
import mln_parser.MlnParser.SymblistContext;

public class MlnFactory {
  public MlnFactory() {
    
  }
  
  //TODO: Return MLN instead of void
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
    
    mln.displayDomainSymbols();
    mln.displayDomains();
    
    return mln;
  }
}

class MyMlnBaseListener extends MlnBaseListener {
  //Have a look at Mln.g4 file to understand this class
  
  private MlnParser p;
  private Mln mln;
  private ArrayList<String> list = new ArrayList<String>();
  
  public MyMlnBaseListener(MlnParser _p, Mln _mln) {
    super();
    p = _p;
    mln = _mln;
  }
  
  @Override
  public void exitDomain(DomainContext ctx) {
    super.exitDomain(ctx);
    mln.addDomain(ctx.domainName.getText(), list);
  }
  
  @Override
  public void enterList(ListContext ctx) {
    super.enterList(ctx);
    list.clear();
  }
  
  @Override
  public void exitSymblist(SymblistContext ctx) {
    super.exitSymblist(ctx);
    list.add(ctx.val.getText());
  }
  
  @Override
  public void exitIntrange(IntrangeContext ctx) {
    super.exitIntrange(ctx);
    int startVal = Integer.parseInt(ctx.valStart.getText());
    int endVal = Integer.parseInt(ctx.valEnd.getText());
    if(startVal > endVal) {
      String msg = "Start value cannot be greater than end value in range declaration";
      p.notifyErrorListeners(msg);
    }
    for(int i = startVal; i <= endVal; i++) {
      list.add(Integer.toString(i));
    }
  }
}