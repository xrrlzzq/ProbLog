package engine;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dataparser.dlParser;
import util.Fact;
import util.Literal;
import util.Rule;

class RuleTree{
	Fact val=null;
	ArrayList<RuleTree> child;
	RuleTree par=null;
	boolean canInsert=false;
	Rule r=null;
	public RuleTree(Fact val){
		this.val=val;
		child=new ArrayList<RuleTree>();
	}
	
	public String toString(){
		return val.toString();
	}
}
public class AdInference {
	public HashMap<String,Fact> database;
    dlParser parser;
    ArrayList<Rule> rules;
    boolean hasPro;
    public HashMap<String,ArrayList<Fact>> factMap;
    public ArrayList<RuleTree> trees;
    public ArrayList<Fact> preIDB;
    public ArrayList<Fact> curIDB;
    public HashMap<String,Fact> factCollections;
    public boolean useMax=false;
    public boolean useProduction=false;
    boolean semiUpdate=false;
    public AdInference(String textName,boolean hasPro) throws IOException{
 	   parser=new dlParser();
 	   parser.dataReader(textName);
 	   this.hasPro=hasPro;
 	   parser.parseData(this.hasPro);
 	   database=parser.buildMap();
 	   rules=parser.rules; 
 	   
 	   trees=new ArrayList<RuleTree>();
 	   preIDB=new ArrayList<Fact>();
 	   
 	   curIDB=new ArrayList<Fact>();
 	   factCollections=new HashMap<String,Fact>();
 	   init();
    }
    public void init(){
 	   factMap=new HashMap<String,ArrayList<Fact>>();
 	   for(Map.Entry<String,Fact> entry:database.entrySet()){
 		   if(!factMap.containsKey(entry.getValue().predicate)){
 			   ArrayList<Fact> f=new ArrayList<Fact> ();
 			   f.add(entry.getValue());
 			   factMap.put(entry.getValue().predicate, f);
 		   }
 		   else
 			   factMap.get(entry.getValue().predicate).add(entry.getValue());
 		   //preIDB.add(entry.getValue());
 	   }
 	   for(Fact f:parser.edb){
 		   String index=f.eString()+":";
 		   Fact temp=new Fact(f.predicate,f.constants);
 		   if(hasPro)
 			   temp.pro=f.pro;
 		   if(!factCollections.containsKey(index))
 			   factCollections.put(index, temp);
 	   }
    }

    public void getOnePath(RuleTree a,ArrayList<Fact> fs){
 	      while(!a.val.predicate.contains(":-")){
 	    	  fs.add(a.val);
 	    	  a=a.par;
 	      }
 	      
    }
    public void dfsTree(int depth,RuleTree a,ArrayList<Fact> fs, Rule r){
 	      if(depth==r.bodys.length)
 	      {
 	    	 
 	    	  getOnePath(a,fs);
 	      }
 	      else{
 	    	  
 	    	  for(int i=0;i<a.child.size();i++){
 	    		  //System.out.println("cur node is: "+a+" cur depth is"+depth);
 	    		  //System.out.println("child is: "+a.child);
 	    		  dfsTree(depth+1,a.child.get(i),fs,r);
 	    	  }
 	      }
    }
    public void semiDfs(int depth,RuleTree a,ArrayList<Fact> fs,Fact newFact,Rule r){
    	 if(depth<r.bodys.length)
	      {
	    	   
	    		 // System.out.println("cur node is: "+a+" cur depth is"+depth);
	    		  if(a.val.eString().equals(newFact.eString())){
//	    			  System.out.println("find out! "+newFact);
//	    			  System.out.println("the par of it is: "+a.par);
//	    			  System.out.println("the child of it is: "+a.child);
	    			  dfsTree(depth+1,a,fs,r);
	    		  }
	    		     
	    		  else{
	    			  for(int i=0;i<a.child.size();i++){
//	    				System.out.println("the cur node is: "+a);
//	    				System.out.println("the child of it is: "+a.child);
	    			    semiDfs(depth+1,a.child.get(i),fs,newFact,r);
	    			    
	    			}
	    	  }
	      }
	      
    }
    public void removeDuplicate(RuleTree a){
    	HashMap<String,RuleTree> map=new HashMap<String,RuleTree>();  
    	for(int i=0;i<a.child.size();i++){
    		if(!map.containsKey(a.child.get(i).val.toString())){
    			map.put(a.child.get(i).val.toString(), a.child.get(i));
    		}
    		else{
    			a.child.remove(i);
    		}
    	}
    }
    public void printTree(RuleTree a){

 	      for(RuleTree child:a.child){
 	    	  if(child.par!=null)
 	    		  System.out.println(child.val+" and the par is"+child.par);
 	    	  else
 	    	  System.out.println(child.val);
 	    	  printTree(child);
 	      }
    }
    public ArrayList<ArrayList<Fact>> inferFacts(ArrayList<ArrayList<Fact>> collection,Rule r){
 	   ArrayList<ArrayList<Fact>> res=new ArrayList<ArrayList<Fact>>();
// 	   System.out.println("----------------------------");
// 	   System.out.println("collecrions is: "+collection);
// 	  System.out.println("----------------------------");
 	      for(int i=0;i<collection.size();i++)
 	    	  if(!collection.get(i).isEmpty())
 	    	  res.add(inferTheFact(r,collection.get(i)));
 	      return res;
    }
    public Fact infer(Rule r,ArrayList<Fact> fs){
 	   HashMap<String,String> model=new HashMap<String,String>();
	       for(int k=0;k<r.bodys.length;k++){
	    	  Literal l=r.bodys[k];
	    	  
	    	  for(int i=0;i<l.variables.length;i++){
	    		  if(!model.containsKey(l.variables[i]))
	    			  model.put(l.variables[i], fs.get(k).constants[i]);
	    	  }
	    	 
	      }
	      Literal tHead=r.head;
	      String pre=tHead.predicate;
	      String[] cons=new String[tHead.variables.length];
	      for(int i=0;i<tHead.variables.length;i++){
	    	  if(model.containsKey(tHead.variables[i]))
	    	     cons[i]=model.get(tHead.variables[i]);
	    	  else{
	    		  cons[i]=model.get(r.bodys[r.bodys.length-1].variables[i]);
	    	  }
	      }
	      Fact f=new Fact(pre,cons);
	      if(hasPro){
	    	  double p=getMin(fs);
	    	  if(useMax)
	    		  p=getProduction(fs);
	    	  f.pro=p*r.pro;
	      }
//	      System.out.println("new fact is generate "+f+" from "+fs);
//	      System.out.println("before: "+factCollections);
	      if(updateCollection(f,fs)){
	    	  //System.out.println("change: "+factCollections);
	    	  semiUpdate=true;
	      }
	      //System.out.println("semiupdate is: "+semiUpdate);
	      return f;
    }
    public boolean updateCollection(Fact f, ArrayList<Fact> fs){
    	   boolean isUpdate=false;
    	   StringBuffer sb=new StringBuffer();
    	   Fact temp=new Fact(f.predicate,f.constants);
    	   if(hasPro)
    		   temp.pro=f.pro;
    	   sb.append(f.eString()+":");
    	   for(Fact e:fs){
    		   sb.append(e.eString());
    		   
    	   }
    	   String index=sb.toString();
    	   if(!factCollections.containsKey(index)){
    		   factCollections.put(index, temp);
    		   //System.out.println(index+" generate!");
    		   isUpdate=true;
    	   }
    	   else{
    		   if(hasPro){
    			   if(factCollections.get(index).pro<f.pro){
    				   factCollections.get(index).pro=f.pro;
    				   isUpdate=true;
    			   }
    		   }
    	   }
    	   return isUpdate;
    }
    public ArrayList<Fact> inferTheFact(Rule r, ArrayList<Fact> fs){
 	      ArrayList<Fact> res=new ArrayList<Fact>();
 	      int count=fs.size()-1;
 	      while(count>=0){
 	    	  ArrayList<Fact> temp=new ArrayList<Fact>();
 	    	  for(int i=0;i<r.bodys.length;i++){
// 	    		  System.out.println();
// 	    		  System.out.println("infer collections is: "+fs);
// 	    		  System.out.println();
 	    		  temp.add(fs.get(count-i));
 	    	  }
 	    	  res.add(infer(r,temp));
 	    	  count=count-r.bodys.length;
 	      }
 	      return res;
    }
    public ArrayList<ArrayList<Fact>> semiTree(Fact curFact,Rule r){
    	  
    	
    	  
    		   
    		   ArrayList<ArrayList<Fact>> res=new ArrayList<ArrayList<Fact>>();
    		   RuleTree root=null;
    			   for(RuleTree rt:trees){
    				   if(rt.val.predicate.equals(r.toString())){
    					   root=rt;
    					   break;
    			   }
    			   }
    			   int max=r.bodys.length-1;
    			   //printTree(root);
    			   if(curFact.predicate.equals(r.bodys[0].predicate)){
		    		   if(hasPro){
		    			   if(!database.containsKey(curFact.eString()))
		    			   doUpdate(0,curFact,root,r);
		    		   }
    				       
		    		   else
		    			   doUpdate(0,curFact,root,r);
    			   }
    			   //System.out.println("child is: "+root.child);
    			   for(RuleTree tc:root.child){
    				  
                       //System.out.println("update node: "+tc);    				   
    				   ArrayList<Fact> temp=new ArrayList<Fact>();
    				   //System.out.println(database);
    				   //System.out.println("curFact is: "+curFact);
    				   //System.out.println("the child of "+tc+" is "+tc.child);
    				   if(hasPro){
    				      if(!database.containsKey(curFact.eString()))
    				          updateTree(1,curFact,tc,max,r);
    				   }
    				   else{
    					   updateTree(1,curFact,tc,max,r);
    				   }
    				   //removeDuplicate(root);
    				   //printTree(root);
    				   //System.out.println("update successfully!");
    				   //printTree(root);
//    				   System.out.println("current fact is: "+curFact);
//    				   System.out.println("cur Node is: "+tc);
    				   //System.out.println(root.toString()+" "+root.child.size());
    				   semiDfs(0,tc,temp,curFact,r);
    				   
    				   res.add(temp);
    			   }
    			   
    			   
    		  
    		   
    		   
    	
    	   return inferFacts(res,r);
    }
    public ArrayList<ArrayList<Fact>> Tree(Rule r){
 	   String[] s=new String[1];
 	   s[0]="root";
 	   Fact f=new Fact(r.toString(),s);
 	   HashMap<String,String> model=new HashMap<String,String>();
 	   RuleTree a=new RuleTree(f);
 	   a.r=r;
 	   buildTree(0,r,model,a);
 	   trees.add(a);
 	   
 	   //System.out.println(a.child.size());
 	   //printTree(a);
 	   ArrayList<ArrayList<Fact>> collection=new ArrayList<ArrayList<Fact>>();
 	   for(int i=0;i<a.child.size();i++){
 		   ArrayList<Fact> temp=new ArrayList<Fact>();
 		   dfsTree(1,a.child.get(i),temp,r);
 		   
 		   collection.add(temp);
 	   }
 	   //System.out.println("facts collection is: "+collection+" for the rule"+r);
 	   return collection;
    }
    public void doUpdate(int depth,Fact newFact,RuleTree node,Rule r){
    	   
    	   RuleTree rt=new RuleTree(newFact);
    	   
    	   node.child.add(rt);
    	   rt.par=node;
    	   if(depth<r.bodys.length-1){
    		   Literal curGoal=r.bodys[depth];
    		   HashMap<String,String> curModel=new HashMap<String,String>();
//    		   System.out.println("curGoal is: "+curGoal);
//    		   System.out.println("curFact is: "+newFact);
    		   for(int i=0;i<newFact.constants.length;i++){
	    			  if(!curModel.containsKey(curGoal.variables[i].trim()))
	    				  curModel.put(curGoal.variables[i].trim(), newFact.constants[i].trim());
	    		  }
    		   buildTree(depth+1,r,curModel,rt);
    		   
    	   }
    }
    public void updateTree(int depth,Fact newFact,RuleTree node,int max,Rule r){
    	   
    	   if(depth<=max){
    	   
    		  
//    			   System.out.println("depth is: "+depth);
    			   Literal curGoal=r.bodys[depth];
//    			   System.out.println("curGoal is: "+curGoal);
//    			   System.out.println("curFact is: "+newFact);
    			if(curGoal.predicate.equals(newFact.predicate)){
    			   HashMap<String,String> model=new HashMap<String,String>();	   
    			   Literal lastMatch=r.bodys[depth-1];
    			   Fact lastFact=node.val;
    			   //System.out.println("curGoal is: "+curGoal+" last match is "+lastMatch+" last fact is "+lastFact);
    			   for(int i=0;i<lastMatch.variables.length;i++){
    				   if(!model.containsKey(lastMatch.variables[i].trim()))
    					   model.put(lastMatch.variables[i].trim(), lastFact.constants[i].trim());
    			   }
    			   boolean isMatch=true;
    			   for(int i=0;i<curGoal.variables.length;i++){
    				   if(model.containsKey(curGoal.variables[i].trim())&&!model.get(curGoal.variables[i].trim()).equals(newFact.constants[i].trim()))
    				   {
    					   isMatch=false;
    					   break;
    				   }
    			   }
    			   if(isMatch){
    			   //System.out.println(newFact+" is match with "+node.val);
    			   doUpdate(depth,newFact,node,r);
    			   
    			   }
    			   else{
    				   for(RuleTree t:node.child)
    				   updateTree(depth+1,newFact,t,max,r);
    			   }
    			   }
    		   
    	  
    	   }
    }
    public void buildTree(int depth,Rule r,HashMap<String,String> model, RuleTree parNode){
 	      if(depth<r.bodys.length){
 	    	  Literal curGoal=r.bodys[depth];
     	      //System.out.println(curGoal+"the depth is"+depth);
     	     
     	      String lastFact=parNode.val.toString();

     	      if(factMap.containsKey(curGoal.predicate)){
     	    	  
     	    	  
     	      
     	      for(Fact f:factMap.get(curGoal.predicate)){
     	    	  boolean canMatch=true;

     	    	  for(int i=0;i<f.constants.length;i++){
     	    		  
     	    		  if(model.containsKey(curGoal.variables[i].trim())){
     	    			  if(!model.get(curGoal.variables[i].trim()).equals(f.constants[i].trim())){
     	    				  canMatch=false;
     	    				  break;
     	    			  }
     	    		  }
	    		  
     	    	  }
     	    	  
     	    	  if(canMatch){
//     	    		  System.out.println("is match!");
//     	    		  System.out.println(lastFact+" match with: "+f);
     	    		  HashMap<String,String> curModel=new HashMap<String,String>();
     	    		  for(int i=0;i<f.constants.length;i++){
     	    			  if(!curModel.containsKey(curGoal.variables[i].trim()))
     	    				  curModel.put(curGoal.variables[i].trim(), f.constants[i].trim());
     	    		  }
//     	    		  System.out.println("curModel is: "+curModel);
     	    		  RuleTree node=new RuleTree(f);
 	    			  parNode.child.add(node);
 	    			  node.par=parNode;
 	    			  buildTree( depth+1,r,curModel,node);
 	    			  if(depth!=r.bodys.length-1&&node.child.isEmpty())
 	    				  node.canInsert=true;
     	    		  
     	    		  }
  	    	  
     	      }
 	      }
//     	      else{
//     	    	  System.out.println("can not find: "+curGoal.predicate);
//     	    	  System.out.println("because the database is: "+factMap);
//     	      }
 	      }
    }

	   public double getMin(ArrayList<Fact> f){
		      double min=1;
		      for(int i=0;i<f.size();i++){
		    	  if(f.get(i).pro<min)
		    		  min=f.get(i).pro;
		      }
		      return min;
		      
	   }
	   public double getProduction(ArrayList<Fact> f){
		      double res=1;
		      for(int i=0;i<f.size();i++){
		    	 res=res*f.get(i).pro;
		      }
		      return res;
	   }
	   public Fact[] matchFact(Rule r){
		      Fact[] res=new Fact[r.bodys.length];
		      for(int i=0;i<r.bodys.length;i++){
		    	  if(!database.containsKey(r.bodys[i]))
		    		  return null;
		    	  else
		    		  res[i]=database.get(r.bodys[i]);
		      }
		      return res;
	   }
	   public Fact combineFacts(ArrayList<Fact> fs){
		      if(fs.size()==1||!hasPro)
		    	  return fs.get(0);
		      else{
		    	  double sum=0;
		    	  for(Fact f:fs){
		    		  sum=calPro(sum,f.pro);
		    	  }
		    	  Fact res= new Fact(fs.get(0).predicate,fs.get(0).constants);
		    	  res.pro=sum;
		    	  return res;
		      }
	   }
	   public ArrayList<Fact> dealIDB(ArrayList<Fact> idb){
		      HashMap<String,ArrayList<Fact>> map=new HashMap<String,ArrayList<Fact>>();
		      for(Fact f:idb){
		    	  if(!map.containsKey(f.eString())){
		    		  ArrayList<Fact> fs=new ArrayList<Fact>();
		    		  map.put(f.eString(), fs);
		    	  }
		    	  map.get(f.eString()).add(f);
		      }
		      ArrayList<Fact> res=new ArrayList<Fact>();
		      for(Map.Entry<String,ArrayList<Fact>> entry:map.entrySet()){
		    	  res.add(combineFacts(entry.getValue()));
		      }
		      return res;
		      
		      
	   }
	   public void trim(Fact f){
		   for(Rule r:rules){
 	    	  RuleTree root=null;
 	    	  for(RuleTree rt:trees){
 	    		  if(rt.val.predicate.equals(r.toString())){
 	    			  root=rt;
 	    			  break;
 	    		  }
 	    	  }
 	    	  if(r.bodys[0].predicate.equals(f.predicate))
 	    		 doUpdate(0,f,root,r);
 	    	  
 	    	 
 	    	 for(RuleTree child:root.child)
 	    	 updateTree(1,f,child,r.bodys.length-1,r);
 	    	 }
 	     
	   }
	   public ArrayList<Fact> dupFactRemove(ArrayList<Fact> idb){
		   HashMap<String,Fact> fs=new HashMap<String,Fact>();
		   ArrayList<Fact> res=new ArrayList<Fact>();
		   idb=dealIDB(idb);
		   ArrayList<Fact> pfs=new ArrayList<Fact>();
		   for(Map.Entry<String, Fact> entry:factCollections.entrySet()){
		    	  
		    	  Fact f=new Fact(entry.getValue().predicate,entry.getValue().constants);
		    	  if(hasPro)
		    		  f.pro=entry.getValue().pro;
		    	  pfs.add(f);
		      }
		   pfs=dealIDB(pfs);
		   for(Fact f:pfs){
			   if(!fs.containsKey(f.eString()))
				   fs.put(f.eString(), f);
		   }
		   for(Fact f:idb){
			   if(fs.containsKey(f.eString()))
				   res.add(fs.get(f.eString()));
		   }
		  return res; 
	   }
	   public boolean isUpdate(ArrayList<Fact> idb,int count){
		      boolean res=false;
//		      ArrayList<Fact> pfs=new ArrayList<Fact>();
//		      
//		      for(Map.Entry<String, Fact> entry:factCollections.entrySet()){
//		    	  
//		    	  
//		    	  Fact f=new Fact(entry.getValue().predicate,entry.getValue().constants);
//		    	  if(hasPro)
//		    		  f.pro=entry.getValue().pro;
//		    	  pfs.add(f);
//		      }
//		      idb=dealIDB(idb);
//		      HashMap<String,Fact> fs=new HashMap<String,Fact>();
////		      System.out.println();
////		      System.out.println("original one is: "+idb);
////		      System.out.println("correct one is: "+dupFactRemove(idb));
////		      System.out.println(factCollections);
////		      System.out.println();
//		      //System.out.println("before pfs is: "+pfs);
//		      pfs=dealIDB(pfs);
//		      //System.out.println("after pfs is: "+pfs);
//		      for(Fact e:pfs){
//		    	  if(!fs.containsKey(e.eString()))
//		    		  fs.put(e.eString(),e);
//		      }
////		      System.out.println("doing update :"+fs);
////		      System.out.println("idb is: "+idb);
//		      idb=dupFactRemove(idb);
		      ArrayList<Fact> newF=new ArrayList<Fact>();
		      for(Fact f:idb){
		    	  if(!database.containsKey(f.eString())){
		    		  //System.out.println("update f: "+fs.get(f.eString()));
		    		  
		    		  database.put(f.eString(), f);
		    		  //newIDB.add(f);
		    		  
		    		  res=true;
		    		  newF.add(f);
		    		  
		    	      if(!factMap.containsKey(f.predicate)){
		    		      ArrayList<Fact> temp=new ArrayList<Fact>();
		    		      //System.out.println("update new f in db: "+fs.get(f.eString()));
		    		      temp.add(f);
		    		      factMap.put(f.predicate, temp);
		    		  
		    	          }
		    	     else{
		    		      factMap.get(f.predicate).add(f);
		    	          }
		      }
		    	  else{
		    		  if(hasPro){
		    			  if(database.get(f.eString()).pro!=f.pro){
//		    				  if(count==1){
		    				  //System.out.println("f is: "+f);
		    					  double p=database.get(f.eString()).pro;
		    					  //System.out.println("update "+f+" into "+fs.get(f.eString()));
		    					  database.get(f.eString()).pro=f.pro;
		    					  res=true;
		    					  //newIDB.add(f);
//		    				  }
//		    				  else if(database.get(f.eString()).pro<f.pro){
//		    				  database.get(f.eString()).pro=f.pro;
//		    				 res=true;
//		    				  }
//		    				  else
//		    					  res=false;
		    				  if(res){
		    				  for(Fact fact:factMap.get(f.predicate)){
		    					   if(fact.eString().equals(f.eString())){
		    						   fact.pro=database.get(f.eString()).pro;
		    						   break;
		    					   }
		    				  }
		    				 
		    			  }
		    			  }
		    			 
		    		  }
		    	  }
		      }
		      if(hasPro){
    		      for(Fact f:newF)
    			    trim(f);
    		  }
		     // System.out.println("curEDB is: "+factMap);
		      return res;
		      
	   }
	   public double calPro(double p1, double p2){
		   BigDecimal x1=BigDecimal.valueOf(p1);
		   BigDecimal x2=BigDecimal.valueOf(p2);
		   if(useMax)
			   return Math.max(x1.doubleValue(), x2.doubleValue());
		   else
		       return x1.add(x2).subtract(x1.multiply(x2)).doubleValue();
	   }
	   public void outPrint(){
		   for(Map.Entry<String, ArrayList<Fact>> entry:factMap.entrySet()){
			   System.out.println(entry.getKey()+" facts: ");
			   ArrayList<Fact> fs=entry.getValue();
			   for(Fact f:fs){
				   System.out.println(f);
			   }
		   }
	   }
	   public void resetIDB(){
		  for(int i=0;i<curIDB.size();i++)
			  curIDB.remove(i);
	   }
	   public boolean semi_update(){
		      boolean isChange=false;
		      ArrayList<Fact> newIDB=new ArrayList<Fact>();
		      HashMap<String,Fact> map=new HashMap<String,Fact>();
		      //System.out.println("preIDB is: "+preIDB);
		      //System.out.println("before update curIDB is: "+curIDB);
		      ArrayList<Fact> temp=new ArrayList<Fact>();
		      temp=(ArrayList<Fact>) dealIDB(curIDB).clone();
		      
		    	  
		    	  
		    	  for(Fact f:preIDB){
		    		  if(!map.containsKey(f.eString()))
		    			  map.put(f.eString(), f);
		    	  }
		    	  for(Fact f:temp){
		    		  if(!map.containsKey(f.eString())){
		    			  newIDB.add(f);
		    			  isChange=true;
		    		  }
		    		  else{
		    			  if(hasPro){
		    				  if(map.get(f.eString()).pro!=f.pro){
		    					  newIDB.add(f);
		    					  isChange=true;
		    				  }
		    					  
		    			  }
		    		  }
		    	  }
		    	  
		      
//	          System.out.println("map is: "+map); 
//		      System.out.println(preIDB+" is copied from "+curIDB);
//		      System.out.println("new facts is: "+newIDB);
		      preIDB=(ArrayList<Fact>) curIDB.clone();
		      //System.out.println("preIDB is: "+preIDB);
//		      ArrayList<Fact> pfs=new ArrayList<Fact>();
//              for(Map.Entry<String, Fact> entry:factCollections.entrySet()){
//		    	  
//		    	  
//		    	  Fact f=new Fact(entry.getValue().predicate,entry.getValue().constants);
//		    	  if(hasPro)
//		    		  f.pro=entry.getValue().pro;
//		    	  pfs.add(f);
//		      }
//              HashMap<String,Fact> fs=new HashMap<String,Fact>();
//              for(Fact e:pfs){
//		    	  if(!fs.containsKey(e.eString()))
//		    		  fs.put(e.eString(),e);
//		      }
//              ArrayList<Fact> nTemp=new ArrayList<Fact>();
//              for(Fact f:newIDB){
//            	  if(fs.containsKey(f.eString()))
//            		  nTemp.add(f);
//              }
		      curIDB=(ArrayList<Fact>) dupFactRemove(newIDB).clone();
//		      System.out.println("curIDB is: "+curIDB);
		      //System.out.println("curIDB is: "+curIDB);
		      
		     
		      return isChange;
              
	   }
	   public void semi_naive(){
		      
		      boolean isupdate=true;
		      System.out.println("edb: "+factMap);
	    	  System.out.println("rules is "+rules);
	    	  int count=1;
		      while(isupdate){
		    	  semiUpdate=false;
		    	  //System.out.println("curEDB is: "+factMap);
		    	  
			      //System.out.println("curIDB is: "+curIDB);
		    	  isupdate=false;
		    	  ArrayList<Fact> idb=new ArrayList<Fact>();
		    	  ArrayList<Fact> lastIDB=new ArrayList<Fact>();
		    	  lastIDB=(ArrayList<Fact>) curIDB.clone();
		    	  //System.out.println("preIDB is: "+lastIDB);
		    	  
		    	  if(count==1){
		    	  for(Rule r:rules){
		    		  ArrayList<ArrayList<Fact>> temp=new ArrayList<ArrayList<Fact>>();
		    		  //temp=(ArrayList<Fact>) findAllMatch(r,false).clone();
		    		  temp=inferFacts(Tree(r),r);
		    		  //System.out.println("current infer is: "+temp);
		    		  for(int i=0;i<temp.size();i++){
		    		  for(Fact f:temp.get(i)){
		    			  idb.add(f);
		    		  }
		    		  }
		    	  }
//		    	  for(Fact f:idb){
//		    		  RuleTree root=null;
//		    		  for(Rule r:rules){
//	    			   for(RuleTree rt:trees){
//	    				   if(rt.val.predicate.equals(r.toString())){
//	    					   root=rt;
//	    					   break;
//	    			   }
//	    			   }
//	    			   int max=r.bodys.length-1;
//	    			   //printTree(root);
//	    			   if(f.predicate.equals(r.bodys[0].predicate)&&!database.containsKey(f.eString()))
//			    		   doUpdate(0,f,root,r);
//	    			   for(RuleTree tc:root.child){
//	    			   if(!database.containsKey(f.eString()))
//	    					 updateTree(1,f,tc,max,r);
//	    			   }
//
//		    		  
//		    	  }
//		    	  }
		    	  }
		    	  else{
		    		  ArrayList<ArrayList<ArrayList<Fact>>> fs=new ArrayList<ArrayList<ArrayList<Fact>>>();
		    		  for(Fact f:curIDB){
		    			  for(Rule r:rules){
		    				  ArrayList<ArrayList<Fact>> temp=new ArrayList<ArrayList<Fact>>();
		    				  temp=semiTree(f,r);
		    				  fs.add(temp);
		    			  }
		    		  }
		    		  for(int i=0;i<fs.size();i++){
		    			  ArrayList<ArrayList<Fact>> temp1=fs.get(i);
		    			  for(int j=0;j<temp1.size();j++){
		    				  for(Fact f:temp1.get(j)){
		    					  idb.add(f);
		    				  }
		    			  }
		    		  }
		    	  }
		    	  idb=dupFactRemove(idb);
		    	  curIDB=(ArrayList<Fact>) idb.clone();
		    	  //System.out.println("new infer facts is: "+idb);
		    	  //resetIDB();
		    	  semi_update();
		    	  //System.out.println("facts collection is: "+factCollections);
		    	  
		    	  isupdate=isUpdate(idb,count);
		    	 // isupdate=semiUpdate;
		    	  //System.out.println("should update? "+isupdate);
		    	  
		    	  
//		    	  if(count==1)
//		    	      System.out.println("idb is: "+idb);
//		    	  else
//		    		  System.out.println("idb is: "+curIDB);
//		    	  System.out.println();
		    	  //System.out.println("---------------------------");
		    	  
		    	  count++;
		      }
		      System.out.println("The iteration time is: "+count);
	   }
	
}
