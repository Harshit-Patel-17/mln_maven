package iitd.data_analytics.mln.logic;

import java.util.ArrayList;

public class FirstOrderFormula<T> {
  
  //Structures to construct formula tree
  public enum NodeType {AND, OR, NOT, IMPLY, EQUIV, XOR, ATOM};
  private Node root = null;
  
  public FirstOrderFormula() {
    
  }
  
  public FirstOrderFormula(T _atom) {
    root = new Node(NodeType.ATOM, _atom, null, null);
  }
  
  public FirstOrderFormula(NodeType _nodeType, FirstOrderFormula<T> _f1) {
    root = new Node(_nodeType, null, null, _f1.getRoot());
  }
  
  public FirstOrderFormula(NodeType _nodeType, FirstOrderFormula<T> _f1, 
      FirstOrderFormula<T> _f2) {
    root = new Node(_nodeType, null, _f1.getRoot(), _f2.getRoot());
  }
  
  public Node getRoot() {
    return root;
  }
  
  public FirstOrderFormula<T> nnf() {
    FirstOrderFormula<T> nnfFormula = new FirstOrderFormula<T>();
    nnfFormula.root = nnfRec(root);
    return nnfFormula;
  }
  
  private Node nnfRec(Node _root) {
    if(_root == null)
      return null;
    
    Node n1, n2;
    
    switch(_root.nodeType) {
    case ATOM:
      return new Node(_root);
      
    case AND:
      return new Node(NodeType.AND, null, nnfRec(_root.lchild), nnfRec(_root.rchild));
    
    case OR:
      return new Node(NodeType.OR, null, nnfRec(_root.lchild), nnfRec(_root.rchild));
    
    case IMPLY:
      n1 = nnfRec(new Node(NodeType.NOT, null, null, _root.lchild));
      n2 = nnfRec(_root.rchild);
      return new Node(NodeType.OR, null, n1, n2);
    
    case EQUIV:
      n1 = nnfRec(new Node(NodeType.IMPLY, null, _root.lchild, _root.rchild));
      n2 = nnfRec(new Node(NodeType.IMPLY, null, _root.rchild, _root.lchild));
      return new Node(NodeType.AND, null, n1, n2);
    
    case XOR:
      n2 = new Node(NodeType.EQUIV, null, _root.lchild, _root.rchild);
      return nnfRec(new Node(NodeType.NOT, null, null, n2));
    
    case NOT:
      Node newRoot = _root.rchild;
      switch(newRoot.nodeType) {
      case ATOM:
        return new Node(NodeType.NOT, null, null, nnfRec(newRoot));
      
      case AND:
        n1 = new Node(NodeType.NOT, null, null, newRoot.lchild);
        n2 = new Node(NodeType.NOT, null, null, newRoot.rchild);
        return nnfRec(new Node(NodeType.OR, null, n1, n2));
        
      case OR:
        n1 = new Node(NodeType.NOT, null, null, newRoot.lchild);
        n2 = new Node(NodeType.NOT, null, null, newRoot.rchild);
        return nnfRec(new Node(NodeType.AND, null, n1, n2));
        
      case IMPLY:
        n1 = newRoot.lchild;
        n2 = new Node(NodeType.NOT, null, null, newRoot.rchild);
        return nnfRec(new Node(NodeType.AND, null, n1, n2));
        
      case EQUIV:
        return nnfRec(new Node(NodeType.XOR, null, newRoot.lchild, newRoot.rchild));
        
      case XOR:
        return nnfRec(new Node(NodeType.EQUIV, null, newRoot.lchild, newRoot.rchild));
        
      case NOT:
        return nnfRec(newRoot.rchild);
        
      default:
        return null;
      }
      
    default:
      return null;
    }
  }
  
  public FirstOrderFormula<T> cnf() {
    FirstOrderFormula<T> cnfFormula = new FirstOrderFormula<T>();
    Node nnfRoot = nnfRec(root);
    cnfFormula.root = cnfRec(nnfRoot);
    return cnfFormula;
  }
  
  public Node cnfRec(Node _root) {
    if(_root == null)
      return null;
    
    Node n1, n2;
    
    switch(_root.nodeType) {
    case ATOM:
      return new Node(_root);
      
    case AND:
      return new Node(NodeType.AND, null, cnfRec(_root.lchild), cnfRec(_root.rchild));
      
    case OR:
      Node lroot = _root.lchild;
      Node rroot = _root.rchild;
      if(lroot.nodeType == NodeType.AND) {
        n1 = new Node(NodeType.OR, null, lroot.lchild, _root.rchild);
        n2 = new Node(NodeType.OR, null, lroot.rchild, _root.rchild);
        return cnfRec(new Node(NodeType.AND, null, n1, n2));
        
      } else if(rroot.nodeType == NodeType.AND) {
        n1 = new Node(NodeType.OR, null, _root.lchild, rroot.lchild);
        n2 = new Node(NodeType.OR, null, _root.lchild, rroot.rchild);
        return cnfRec(new Node(NodeType.AND, null, n1, n2));
        
      } else {
        n1 = cnfRec(_root.lchild);
        n2 = cnfRec(_root.rchild);
        if(isAndNodeInSubtree(_root)) {
          return cnfRec(new Node(NodeType.OR, null, n1, n2));
        } else {
          return new Node(NodeType.OR, null, n1, n2); 
        }
      }
      
    case IMPLY:
      // Cannot occur in nnf
      assert false : "IMPLY cannot be in nnf";
      return null;
      
    case EQUIV:
      // Cannot occur in nnf
      assert false : "EQUIV cannot be in nnf";
      return null;
      
    case XOR:
      // Cannot occur in nnf
      assert false : "XOR cannot be in nnf";
      return null;
      
    case NOT:
      // Next to leaves in nnf
      return new Node(NodeType.NOT, null, null, cnfRec(_root.rchild));
      
    default:
      return null;
    }
  }
  
  private boolean isAndNodeInSubtree(Node subtreeRoot) {
    if(subtreeRoot == null)
      return false;
    if(subtreeRoot.nodeType == NodeType.ATOM)
      return false;
    if(subtreeRoot.nodeType == NodeType.AND)
      return true;
    return isAndNodeInSubtree(subtreeRoot.lchild) || isAndNodeInSubtree(subtreeRoot.rchild);
  }
  
  public ArrayList<ArrayList<Literal<T>>> asSetOfClauses() {
    Node cnfRoot = cnfRec(nnfRec(root));
    return asSetOfClausesRec(cnfRoot);
  }
  
  private ArrayList<ArrayList<Literal<T>>> asSetOfClausesRec(Node _root) {
    ArrayList<ArrayList<Literal<T>>> setOfClauses = new ArrayList<ArrayList<Literal<T>>>();
    ArrayList<Literal<T>> clause;
    
    if(_root == null)
      return setOfClauses;
    
    switch(_root.nodeType) {
    case ATOM:
      clause = new ArrayList<Literal<T>>();
      clause.add(new Literal<T>(false, _root.atom)); //Add non-negated literal
      setOfClauses.add(clause);
      break;
      
    case AND:
      ArrayList<ArrayList<Literal<T>>> setOfClauses1 = asSetOfClausesRec(_root.lchild);
      ArrayList<ArrayList<Literal<T>>> setOfClauses2 = asSetOfClausesRec(_root.rchild);
      setOfClauses.addAll(setOfClauses1);
      setOfClauses.addAll(setOfClauses2);
      break;
      
    case OR:
      ArrayList<Literal<T>> literals = getLiterals(_root);
      setOfClauses.add(literals);
      break;
      
    case IMPLY:
      assert false : "IMPLY cannot be in cnf";
      break;
      
    case EQUIV:
      assert false : "EQUIV cannot be in cnf";
      break;
      
    case XOR:
      assert false : "XOR cannot be in cnf";
      break;
      
    case NOT:
      clause = new ArrayList<Literal<T>>();
      clause.add(new Literal<T>(true, _root.rchild.atom)); //Add negated literal
      setOfClauses.add(clause);
      break;
      
    default:
      return null;
    }
    
    return setOfClauses;
  }
  
  private ArrayList<Literal<T>> getLiterals(Node _root) {
    ArrayList<Literal<T>> literals = new ArrayList<Literal<T>>();
    
    if(_root == null)
      return literals;
    
    switch(_root.nodeType) {
    case ATOM:
      literals.add(new Literal<T>(false, _root.atom)); //Add non-negated atom
      break;
      
    case AND:
      literals.addAll(getLiterals(_root.lchild));
      literals.addAll(getLiterals(_root.rchild));
      break;
      
    case OR:
      literals.addAll(getLiterals(_root.lchild));
      literals.addAll(getLiterals(_root.rchild));
      break;
      
    case IMPLY:
      literals.addAll(getLiterals(_root.lchild));
      literals.addAll(getLiterals(_root.rchild));
      break;
      
    case EQUIV:
      literals.addAll(getLiterals(_root.lchild));
      literals.addAll(getLiterals(_root.rchild));
      break;
      
    case XOR:
      literals.addAll(getLiterals(_root.lchild));
      literals.addAll(getLiterals(_root.rchild));
      break;
      
    case NOT:
      literals.add(new Literal<T>(true, _root.rchild.atom)); //Add negated atom
      break;
      
    default:
      return null;
    }
    
    return literals;
  }
  
  //Output on stdout
  public void display() {
     displayInOrder(root);
     System.out.println("");
  }
  
  private void displayInOrder(Node _root) {
    if(_root == null)
      return;
    displayInOrder(_root.lchild);
    _root.display();
    System.out.print(" ");
    displayInOrder(_root.rchild);
  }
  
  private class Node {
    public NodeType nodeType = null;
    public T atom = null;
    public Node lchild = null;
    public Node rchild = null;
    
    public Node(NodeType _nodeType, T _atom, Node _lchild, Node _rchild) {
      nodeType = _nodeType;
      atom = _atom;
      lchild = _lchild;
      rchild = _rchild;
    }
    
    public Node(Node node) {
      nodeType = node.nodeType;
      atom = node.atom;
      lchild = node.lchild;
      rchild = node.rchild;
    }
    
    public void display() {
      if(nodeType == NodeType.ATOM) {
        System.out.print(atom);
      } else {
        System.out.print(nodeType);
      }
    }
  }
}
