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

import database_parser.DatabaseLexer;
import database_parser.DatabaseParser;
import database_parser.DatabaseParser.DatabaseContext;
import evidence_parser.EvidenceLexer;
import evidence_parser.EvidenceParser;
import evidence_parser.EvidenceParser.EvidenceContext;
import iitd.data_analytics.mln.exceptions.DatabaseParseException;
import iitd.data_analytics.mln.exceptions.EvidenceParseException;
import iitd.data_analytics.mln.exceptions.MlnParseException;
import iitd.data_analytics.mln.exceptions.QueryParseException;
import iitd.data_analytics.mln.gpu.GpuState;
import iitd.data_analytics.mln.inference.SamplingMarginalInference;
import iitd.data_analytics.mln.inference.MarginalInference;
import iitd.data_analytics.mln.inference.SatisfiedGroundingCounter;
import iitd.data_analytics.mln.learning.Learning;
import iitd.data_analytics.mln.learning.PerWeightLearningRatesLearning;
import iitd.data_analytics.mln.logic.FirstOrderFormula;
import iitd.data_analytics.mln.mln.Config;
import iitd.data_analytics.mln.mln.Domain;
import iitd.data_analytics.mln.mln.Formula;
import iitd.data_analytics.mln.mln.InputParams;
import iitd.data_analytics.mln.mln.Mln;
import iitd.data_analytics.mln.mln.Predicate;
import iitd.data_analytics.mln.mln.PredicateDef;
import iitd.data_analytics.mln.mln.PredicateGroundingIndex;
import iitd.data_analytics.mln.mln.State;
import iitd.data_analytics.mln.mln.Symbols;
import iitd.data_analytics.mln.sampler.GibbsSampler;
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
  
  public Mln createMln(InputParams inputParams) throws MlnParseException, IOException, InterruptedException, QueryParseException, EvidenceParseException, DatabaseParseException {
    
    Mln mln = parseMlnFile(inputParams.getMlnFile());
    if(inputParams.addNewConstants()) {
      //Collect constants from database and evidence files
      System.out.println("Collecting constants from database and evidence files.");
      if(inputParams.doLearning()) {
        parseDatabaseFile(inputParams.getDatabaseFile(), mln, true);
      }
      parseEvidenceFile(inputParams.getEvidenceFile(), mln, true);
      
      //Reset totalGroundings of formulae as new constants are added
      for(Formula formula : mln.getFormulas()) {
        formula.setTotalGroundings();
      }
    }
    
    for(Domain domain : mln.getDomains()) {
      System.out.print(domain);
      System.out.println("Total:" + domain.size());
    }
    
    for(Formula formula : mln.getFormulas()) {
      System.out.print(formula);
      System.out.println("Total Groundings:" + formula.getTotalGroundings());
    }
    
    System.exit(1);
    
    mln.addStateWithEvidenceAndQuery(new GpuState(mln.getPredicateDefs()));
    mln.addDatabase(new GpuState(mln.getPredicateDefs()));
    parseQueryFile(inputParams.getQueryFile(), mln);
    parseEvidenceFile(inputParams.getEvidenceFile(), mln, false);
    
    State state = mln.getStateWithEvidenceAndQuery();
    state.resetMarginalCounts();
    
    long startTime = System.nanoTime();
    if(inputParams.doLearning()) {
      parseDatabaseFile(inputParams.getDatabaseFile(), mln, false);
      Learning learning = new PerWeightLearningRatesLearning(1e-1, 15, 1e-5);
      learning.learn(mln);
    }
    if(inputParams.doInference()) {
      GibbsSampler gibbsSampler = new GibbsSampler(mln, 1000);
      MarginalInference marginalInference = new SamplingMarginalInference(10000, gibbsSampler);
      marginalInference.getMarginals(state);
      //state.outputMaxMarginals(inputParams.getOutputFile());
      state.outputMarginals(inputParams.getOutputFile());
    }
    long endTime = System.nanoTime();
    System.out.println("Time: " + (endTime - startTime)/1e9);
    
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
        throw new IllegalStateException("failed to parse in Mln file at line " + position + " " + msg);
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
      throw new QueryParseException("Error in parsing Query file");
    
    //File successfully parsed. Remove default error listener
    //Add custom error listener to throw exceptions on parsing errors
    p.removeErrorListeners();
    p.addErrorListener(new BaseErrorListener() {
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, 
          int charPositionInLine, String msg, RecognitionException e) {
        String position = line + ":" + charPositionInLine;
        throw new IllegalStateException("failed to parse in Query file at line " + position + " " + msg);
      }
      
    });
    
    //Add event listener for syntax directed definition. 
    //Refer to Mln.g4 to understand the class MyMlnBaseListener
    ParseTreeWalker walker = new ParseTreeWalker();
    MyQueryBaseListener listener = new MyQueryBaseListener(p, mln);
    
    //Walk parse tree
    walker.walk(listener, queryContext);
  }
  
  private void parseEvidenceFile(String evidenceFile, Mln mln, boolean collectConstants) throws IOException, EvidenceParseException {
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
      throw new EvidenceParseException("Error in parsing Evidence file");
    
    //File successfully parsed. Remove default error listener
    //Add custom error listener to throw exceptions on parsing errors
    p.removeErrorListeners();
    p.addErrorListener(new BaseErrorListener() {
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, 
          int charPositionInLine, String msg, RecognitionException e) {
        String position = line + ":" + charPositionInLine;
        throw new IllegalStateException("failed to parse in Evidence file at line " + position + " " + msg);
      }
      
    });
    
    //Add event listener for syntax directed definition. 
    //Refer to Mln.g4 to understand the class MyMlnBaseListener
    ParseTreeWalker walker = new ParseTreeWalker();
    MyEvidenceBaseListener listener = new MyEvidenceBaseListener(p, mln, collectConstants);
    
    //Walk parse tree
    walker.walk(listener, evidenceContext);
  }
  
  private void parseDatabaseFile(String databaseFile, Mln mln, boolean collectConstants) throws IOException, QueryParseException, DatabaseParseException {
    //Create input stream
    File f = new File(databaseFile);
    InputStream in = new FileInputStream(f);
    
    //Create MLN lexer object
    DatabaseLexer l = new DatabaseLexer(CharStreams.fromStream(in));
    
    //Create MLN parser object
    DatabaseParser p = new DatabaseParser(new CommonTokenStream(l));
    
    //Parse file
    DatabaseContext databaseContext = p.database();
    if(p.getNumberOfSyntaxErrors() != 0)
      throw new DatabaseParseException("Error in parsing Database file");
    
    //File successfully parsed. Remove default error listener
    //Add custom error listener to throw exceptions on parsing errors
    p.removeErrorListeners();
    p.addErrorListener(new BaseErrorListener() {
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, 
          int charPositionInLine, String msg, RecognitionException e) {
        String position = line + ":" + charPositionInLine;
        throw new IllegalStateException("failed to parse in Database file at line " + position + " " + msg);
      }
      
    });
    
    //Add event listener for syntax directed definition. 
    //Refer to Mln.g4 to understand the class MyMlnBaseListener
    ParseTreeWalker walker = new ParseTreeWalker();
    MyDatabaseBaseListener listener = new MyDatabaseBaseListener(p, mln, collectConstants);
    
    //Walk parse tree
    walker.walk(listener, databaseContext);
  }
}