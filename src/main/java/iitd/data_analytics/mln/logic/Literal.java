package iitd.data_analytics.mln.logic;

public class Literal<T> {
  
  private boolean negated;
  private T atom;
  
  public Literal(boolean _negated, T _atom) {
    negated = _negated;
    atom = _atom;
  }
  
  public void setNegated(boolean _negated) {
    negated = _negated;
  }
  
  public void setAtom(T _atom) {
    atom = _atom;
  }
  
  public boolean isNegated() {
    return negated;
  }
  
  public T getAtom() {
    return atom;
  }
}
