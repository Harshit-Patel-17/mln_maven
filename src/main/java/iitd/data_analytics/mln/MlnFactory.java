package iitd.data_analytics.mln;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import mln_parser.*;

public class MlnFactory {
  public MlnFactory() {
    
  }
  
  //TODO: Return MLN instead of void
  public void createMln(InputStream in) throws IOException {
    MlnLexer l = new MlnLexer(new ANTLRInputStream(in));
    MlnParser p = new MlnParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, 
          int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
      }
    });
    p.mln();
  }
  
}