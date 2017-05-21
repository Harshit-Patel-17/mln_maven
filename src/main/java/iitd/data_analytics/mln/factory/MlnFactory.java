package iitd.data_analytics.mln.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import evidence_parser.EvidenceLexer;
import evidence_parser.EvidenceParser;
import evidence_parser.EvidenceParser.EvidenceContext;
import iitd.data_analytics.mln.exceptions.MlnParseException;
import iitd.data_analytics.mln.exceptions.QueryParseException;
import iitd.data_analytics.mln.gpu.GpuState;
import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.mln.Config;
import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.InputParams;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.Predicate;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundingIndex;
import iitd.data_analytics.mln.mln.State;
import iitd.data_analytics.mln.mln.Symbols;
import mln_parser.*;
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
import mln_parser.MlnParser.MlnContext;
import mln_parser.MlnParser.PredicateContext;
import mln_parser.MlnParser.PredicateDef1Context;
import mln_parser.MlnParser.PredicateDef2Context;
import mln_parser.MlnParser.PredicateDef3Context;
import query_parser.QueryLexer;
import query_parser.QueryParser;
import query_parser.QueryParser.QueryContext;

public class MlnFactory {
  
  public Mln createMln(InputParams inputParams) throws MlnParseException, IOException, InterruptedException, QueryParseException {
    
    Mln mln = parseMlnFile(inputParams.getMlnFile());
    mln.addState(new GpuState(mln.getPredicateDefs()));
    parseQueryFile(inputParams.getQueryFile(), mln);
    parseEvidenceFile(inputParams.getEvidenceFile(), mln);
    
    State state = mln.getState();

    long startTime = System.nanoTime();
    if(inputParams.useGpu()) {
      for(int i = 0; i < 1; i++) {
        //System.out.println("GPU: " + mln.getFormulas().get(0).countSatisfiedGroundings(state));
        System.out.println("GPU NoDb: " + mln.getFormulas().get(0).countSatisfiedGroundingsNoDb(state));
      }
    } else {
      for(int i = 0; i < 1; i++) {
        //System.out.println("CPU: " + mln.getFormulas().get(0).countSatisfiedGroundingsCPU(state));
        System.out.println("CPU NoDb: " + mln.getFormulas().get(0).countSatisfiedGroundingsCPUNoDb(state));
      }
    }
    long endTime = System.nanoTime();
    System.out.println("Time: " + (endTime - startTime)/1e9);
    /*state.display();
    System.out.println(mln.getFormulas().get(0).countSatisfiedGroundings(state));
    System.out.println(mln.getFormulas().get(1).countSatisfiedGroundings(state));
    System.out.println(mln.getFormulas().get(2).countSatisfiedGroundings(state));
    System.out.println(mln.getFormulas().get(0).countSatisfiedGroundingsCPU(state));
    System.out.println(mln.getFormulas().get(1).countSatisfiedGroundingsCPU(state));
    System.out.println(mln.getFormulas().get(2).countSatisfiedGroundingsCPU(state));*/
    
    for(int i = 0; i < 10; i++) {
      PredicateGroundingIndex predicateGroundingIndex = state.randomlySelectUnknownGrounding();
      Random random = new Random(Config.seed);
      int vals = mln.getPredicateDefs().get(predicateGroundingIndex.predicateId).getVals().size();
      state.setGrounding(predicateGroundingIndex, random.nextInt(vals));
      System.out.println("GPU NoDb: " + mln.getFormulas().get(0).countSatisfiedGroundingsNoDb(state));
      System.out.println("CPU NoDb: " + mln.getFormulas().get(0).countSatisfiedGroundingsCPUNoDb(state));
      System.out.println("");
    }

    return mln;
  }
  
  private Mln parseMlnFile(String mlnFileName) throws IOException, MlnParseException {
    //New MLN object
    Mln mln = new Mln();
    
    //Create input stream
    File f = new File(mlnFileName);
    InputStream in = new FileInputStream(f);
    
    //Create MLN lexer object
    MlnLexer l = new MlnLexer(CharStreams.fromStream(in));
    
    //Create MLN parser object
    MlnParser p = new MlnParser(new CommonTokenStream(l));
    
    //Parse file
    MlnContext mlnContext = p.mln();
    if(p.getNumberOfSyntaxErrors() != 0)
      throw new MlnParseException("Error in parsing MLN file");
    
    //File successfully parsed. Remove default error listener
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
    ParseTreeWalker walker = new ParseTreeWalker();
    MyMlnBaseListener listener = new MyMlnBaseListener(p, mln);
    
    //Walk parse tree
    walker.walk(listener, mlnContext);

    return mln;
  }
  
  private void parseQueryFile(String queryFile, Mln mln) throws IOException, QueryParseException {
    //Create input stream
    File f = new File(queryFile);
    InputStream in = new FileInputStream(f);
    
    //Create MLN lexer object
    QueryLexer l = new QueryLexer(CharStreams.fromStream(in));
    
    //Create MLN parser object
    QueryParser p = new QueryParser(new CommonTokenStream(l));
    
    //Parse file
    QueryContext queryContext = p.query();
    if(p.getNumberOfSyntaxErrors() != 0)
      throw new QueryParseException("Error in parsing MLN file");
    
    //File successfully parsed. Remove default error listener
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
    ParseTreeWalker walker = new ParseTreeWalker();
    MyQueryBaseListener listener = new MyQueryBaseListener(p, mln);
    
    //Walk parse tree
    walker.walk(listener, queryContext);
  }
  
  private void parseEvidenceFile(String evidenceFile, Mln mln) throws IOException, QueryParseException {
    //Create input stream
    File f = new File(evidenceFile);
    InputStream in = new FileInputStream(f);
    
    //Create MLN lexer object
    EvidenceLexer l = new EvidenceLexer(CharStreams.fromStream(in));
    
    //Create MLN parser object
    EvidenceParser p = new EvidenceParser(new CommonTokenStream(l));
    
    //Parse file
    EvidenceContext evidenceContext = p.evidence();
    if(p.getNumberOfSyntaxErrors() != 0)
      throw new QueryParseException("Error in parsing MLN file");
    
    //File successfully parsed. Remove default error listener
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
    ParseTreeWalker walker = new ParseTreeWalker();
    MyEvidenceBaseListener listener = new MyEvidenceBaseListener(p, mln);
    
    //Walk parse tree
    walker.walk(listener, evidenceContext);
  }
}