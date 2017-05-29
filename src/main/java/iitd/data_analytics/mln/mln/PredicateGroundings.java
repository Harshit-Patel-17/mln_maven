package iitd.data_analytics.mln.mln;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PredicateGroundings {

    private PredicateDef predicateDef;
    private int totalGroundings;
    private int distinctVals;
    private int[] groundings;
    private int[][] marginalCount;
    private boolean[] isQuery;
    private boolean[] isEvidence;
    private ArrayList<Integer> unknownGroundings;
    private Random random;
    
    public PredicateGroundings(PredicateDef _predicateDef) {
      predicateDef = _predicateDef;
      totalGroundings = 1;
      for(Domain domain : predicateDef.getDomains()) {
        totalGroundings *= domain.size();
      }
      groundings = new int[totalGroundings];
      distinctVals = predicateDef.getVals().size();
      marginalCount = new int[totalGroundings][];
      for(int i = 0; i < totalGroundings; i++) {
        marginalCount[i] = new int[distinctVals];
      }
      isQuery = new boolean[totalGroundings];
      isEvidence = new boolean[totalGroundings];
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
    
    public int getPredicateId() {
      return predicateDef.getPredicateId();
    }
    
    public ArrayList<Integer> getUnknownGroundings() {
      return unknownGroundings;
    }
    
    public void setGrounding(int idx, int val) {
      assert predicateDef.getVals().exist(val) : "Illegal value has been assigned to a grounding";
      groundings[idx] = val;
    }
    
    public void addQuery(ArrayList<String> symbolicTerms) {
      int idx = getGroundingIdx(symbolicTerms);
      isQuery[idx] = true;
    }
  
    public void addEvidence(ArrayList<String> symbolicTerms, String symbolicVal) {
      int idx = getGroundingIdx(symbolicTerms);
      groundings[idx] = predicateDef.getVals().getIdFromSymbol(symbolicVal);
      unknownGroundings.remove(new Integer(idx));
      isEvidence[idx] = true;
    }
    
    public void increaseMarginalCounts() {
      //assert idx >= 0 && idx < totalGroundings && val >= 0 && val < distinctVals;
      for(int i = 0; i < totalGroundings; i++) {
        marginalCount[i][groundings[i]]++;
      }
    }
    
    public void setMarginalCount(int idx, int val, int count) {
      //assert idx >= 0 && idx < totalGroundings && val >= 0 && val < distinctVals;
      marginalCount[idx][val] = count;
    }
    
    public void resetMarginalCounts() {
      for(int i = 0; i < totalGroundings; i++) {
        for(int j = 0; j < distinctVals; j++) {
          marginalCount[i][j] = 0;
        }
      }
    }
    
    @Override
    public String toString() {
      String str = "";
      str += "State of " + predicateDef.getPredicateName() + " ";
      str += Arrays.toString(groundings);
      return str;
    }
    
    public void display() {
      System.out.print(this);
    }
    
    public int[] getGroundingVals(int idx) {
      ArrayList<Domain> domains = predicateDef.getDomains();
      int[] groundingVals = new int[domains.size()];
      
      for(int i = 0; i < domains.size(); i++) {
        int domainSize = domains.get(i).size();
        int temp = idx / domainSize;
        groundingVals[i] = idx - temp * domainSize;
        idx = temp;
      }
      return groundingVals;
    }
    
    public int getGroundingIdx(ArrayList<String> symbolicTerms) {
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
    
    public void outputMaxMarginals(PrintWriter writer) {
      for(int i = 0; i < totalGroundings; i++) {
        if(isQuery[i]) {
          int max = 0, argMax = 0;
          double Z = 0;
          for(int j = 0; j < distinctVals; j++) {
            int count = marginalCount[i][j];
            Z += count;
            if(count > max) {
              max = count;
              argMax = j;
            }
          }
          printGroundPredicate(i, max / Z, argMax, writer);
        }
      }
    }
    
    private void printGroundPredicate(int idx, double prob, int val, PrintWriter writer) {
      int[] groundingVals = getGroundingVals(idx);
      ArrayList<Domain> domains = predicateDef.getDomains();
      Symbols vals = predicateDef.getVals();
      
      String str = prob + "::" + predicateDef.getPredicateName();
      str += "(" + domains.get(0).getValSymbolFromId(groundingVals[0]);
      for(int i = 1; i < domains.size(); i++) {
        str += "," + domains.get(i).getValSymbolFromId(groundingVals[i]);
      }
      str += ") = " + vals.getSymbolFromId(val);
      writer.println(str);
    }
}
