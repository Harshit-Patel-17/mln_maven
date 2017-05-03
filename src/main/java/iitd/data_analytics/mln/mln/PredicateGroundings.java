package iitd.data_analytics.mln.mln;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PredicateGroundings {

    private PredicateDef predicateDef;
    private int totalGroundings;
    private int[] groundings;
    private ArrayList<Integer> unknownGroundings;
    private Random random;
    
    public PredicateGroundings(PredicateDef _predicateDef) {
      predicateDef = _predicateDef;
      totalGroundings = 1;
      for(Domain domain : predicateDef.getDomains()) {
        totalGroundings *= domain.size();
      }
      groundings = new int[totalGroundings];
      unknownGroundings = new ArrayList<Integer>();
      random = new Random(Config.seed);
      for(int i = 0; i < totalGroundings; i++) {
        unknownGroundings.add(i);
        groundings[i] = random.nextInt(predicateDef.getVals().size());
      }
    }
    
    public int getTotalGroundings() {
      return totalGroundings;
    }
    
    public int[] getGroundings() {
      return groundings;
    }
  
    public void addEvidence(ArrayList<String> symbolicTerms, int val) {
      int idx = getGroundingIdx(symbolicTerms);
      groundings[idx] = val;
      unknownGroundings.remove(new Integer(idx));
    }
    
    @Override
    public String toString() {
      String str = "";
      str += "PredicateId:" + predicateDef.getPredicateId() + " ";
      str += Arrays.toString(groundings);
      return str;
    }
    
    public void display() {
      System.out.print(this);
    }
    
    private int getGroundingIdx(ArrayList<String> symbolicTerms) {
      ArrayList<Domain> domains = predicateDef.getDomains();
      assert symbolicTerms.size() == domains.size();
      
      int idx = 0, runningWeight = 1;
      for(int i = 0; i < domains.size(); i++) {
        String symbolicTerm = symbolicTerms.get(i);
        int term = domains.get(i).getValIdFromSymbol(symbolicTerm);
        idx += term * runningWeight;
        runningWeight *= domains.get(i).size();
      }
      return idx;
    }
}
