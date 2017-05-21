package iitd.data_analytics.mln.mln;

public class PredicateGroundingIndex {

  public int predicateId;
  public int groundingId;
  
  @Override
  public String toString() {
    String str = "PredicateId:" + predicateId;
    str += " groundingId:" + groundingId;
    return str;
  }
}
