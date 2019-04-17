package engine;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import dataparser.dlParser;
import util.Fact;
import util.Literal;
import util.Rule;

class AnwserTree{
	  Fact curAnwser;
	  AnwserTree par=null;
	  ArrayList<AnwserTree> child;
	  public AnwserTree(Fact curAnwser){
		  this.curAnwser=curAnwser;
		  child=new ArrayList<AnwserTree>();
		 
	  }
	  public String toString(){
		  return curAnwser.toString();
	  }
}
public class Inference {
       public HashMap<String,Fact> database;
       dlParser parser;
       ArrayList<Rule> rules;
       boolean hasPro;
       public HashMap<String,ArrayList<Fact>> factMap;
       public boolean useMax=false;
       public boolean useProduction=false;
       public Inference(String textName,boolean hasPro) throws IOException{
    	   parser=new dlParser();
    	   parser.dataReader(textName);
    	   this.hasPro=hasPro;
    	   parser.parseData(this.hasPro);
    	   database=parser.buildMap();
    	   rules=parser.rules; 
    	   
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
    	   }
       }

       public void getOnePath(AnwserTree a,ArrayList<Fact> fs){
    	      while(!a.curAnwser.predicate.equals("root")){
    	    	  fs.add(a.curAnwser);
    	    	  a=a.par;
    	      }
    	      
       }
       public void dfsTree(int depth,AnwserTree a,ArrayList<Fact> fs, Rule r){
    	      if(depth==r.bodys.length)
    	      {
    	    	  
    	    	  getOnePath(a,fs);
    	      }
    	      else{
    	    	  for(int i=0;i<a.child.size();i++){
    	    		  //System.out.println("cur node is: "+a+" cur depth is"+depth);
    	    		  dfsTree(depth+1,a.child.get(i),fs,r);
    	    	  }
    	      }
       }
       public void printTree(AnwserTree a){

    	      for(AnwserTree child:a.child){
    	    	  System.out.println("CNM"+child.curAnwser);
    	    	  printTree(child);
    	      }
       }
       public ArrayList<ArrayList<Fact>> inferFacts(ArrayList<ArrayList<Fact>> collection,Rule r){
    	   ArrayList<ArrayList<Fact>> res=new ArrayList<ArrayList<Fact>>();
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
 	    	  if(useProduction)
 	    		  p=getProduction(fs);
 	    	  f.pro=p*r.pro;
 	      }
 	      //System.out.println("new fact is generate "+f+" from "+fs);
 	      return f;
       }
       public ArrayList<Fact> inferTheFact(Rule r, ArrayList<Fact> fs){
    	      ArrayList<Fact> res=new ArrayList<Fact>();
    	      int count=fs.size()-1;
    	      while(count>=0){
    	    	  ArrayList<Fact> temp=new ArrayList<Fact>();
    	    	  for(int i=0;i<r.bodys.length;i++){
    	    		  temp.add(fs.get(count-i));
    	    	  }
    	    	  res.add(infer(r,temp));
    	    	  count=count-r.bodys.length;
    	      }
    	      return res;
       }
       public ArrayList<ArrayList<Fact>> Tree(Rule r){
    	   String[] s=new String[1];
    	   s[0]="root";
    	   Fact f=new Fact("root",s);
    	   HashMap<String,String> model=new HashMap<String,String>();
    	   AnwserTree a=new AnwserTree(f);
    	   buildTree(0,r,model,a);
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
       public void buildTree(int depth,Rule r,HashMap<String,String> model, AnwserTree parNode){
    	      if(depth<r.bodys.length){
    	    	  Literal curGoal=r.bodys[depth];
        	      //System.out.println(curGoal+"the depth is"+depth);
        	     
        	      String lastFact=parNode.curAnwser.toString();
//        	      for(String str:model.keySet()){
//        	    	  curModel.put(str, model.get(str));
//        	      }   	
//        	      System.out.println("curGoal is "+curGoal);
//        	      System.out.println("curModel is "+model);
//        	      System.out.println("curNode is: "+parNode);
        	      if(factMap.containsKey(curGoal.predicate)){
        	    	  
        	    	  
        	      
        	      for(Fact f:factMap.get(curGoal.predicate)){
        	    	  boolean canMatch=true;
//        	    	  if(f.toString().equals(lastFact))
//        	    		  continue;
        	    	  for(int i=0;i<f.constants.length;i++){
        	    		  //System.out.println(f.constants[i]);
        	    		  if(model.containsKey(curGoal.variables[i].trim())){
        	    			  if(!model.get(curGoal.variables[i].trim()).equals(f.constants[i].trim())){
        	    				  canMatch=false;
        	    				  break;
        	    			  }
        	    		  }
//        	    		  if(curModel.containsKey(curGoal.variables[i].trim())){
//        	    			  curModel.put(curGoal.variables[i].trim(), f.constants[i].trim());
//        	    			  //System.out.println(curModel);
//        	    		  }
//        	    		  else{
//        	    			  if(!curModel.get(curGoal.variables[i].trim()).equals(f.constants[i].trim())){
//        	    				  canMatch=false;
//        	    				  System.out.println("zhe li bu pi pei?"+curGoal.variables[i].trim()+" and "+f.constants[i].trim());
//        	    				  System.out.println("last fact is: "+lastFact);
//        	    				  System.out.println("current fact is: "+f);
//        	    				  break;
//        	    			  }
//        	    		  }
        	    	  }
        	    	  
        	    	  if(canMatch){
//        	    		  System.out.println("is match!");
//        	    		  System.out.println(lastFact+" match with: "+f);
        	    		  HashMap<String,String> curModel=new HashMap<String,String>();
        	    		  for(int i=0;i<f.constants.length;i++){
        	    			  if(!curModel.containsKey(curGoal.variables[i].trim()))
        	    				  curModel.put(curGoal.variables[i].trim(), f.constants[i].trim());
        	    		  }
//        	    		  System.out.println("curModel is: "+curModel);
        	    		  AnwserTree node=new AnwserTree(f);
    	    			  parNode.child.add(node);
    	    			  node.par=parNode;
    	    			  buildTree( depth+1,r,curModel,node);
        	    		  
        	    		  }
     	    	  
        	      }
    	      }
//        	      else{
//        	    	  System.out.println("can not find: "+curGoal.predicate);
//        	    	  System.out.println("because the database is: "+factMap);
//        	      }
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
	   public boolean isUpdate(ArrayList<Fact> idb,int count){
		      boolean res=false;
		      idb=dealIDB(idb);
		      
		      for(Fact f:idb){
		    	  if(!database.containsKey(f.eString())){
		    		  database.put(f.eString(), f);
		    		  res=true;
		    	  
		    	      if(!factMap.containsKey(f.predicate)){
		    		      ArrayList<Fact> temp=new ArrayList<Fact>();
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
		    				  if(count==1){
		    					  double p=database.get(f.eString()).pro;
		    					  database.get(f.eString()).pro=calPro(p,f.pro);
		    					  res=true;
		    				  }
		    				  else if(database.get(f.eString()).pro<f.pro){
		    				  database.get(f.eString()).pro=f.pro;
		    				  res=true;
		    				  }
		    				  else
		    					  res=false;
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
	   public void naive(){
		      
		      boolean isupdate=true;
		      System.out.println("edb: "+factMap);
	    	  System.out.println("rules is "+rules);
	    	  int count=1;
		      while(isupdate){
		    	  //System.out.println("curEDB is: "+factMap);
		    	  isupdate=false;
		    	  ArrayList<Fact> idb=new ArrayList<Fact>();
		    	  
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
//		    	  System.out.println("idb is: "+idb);
		    	  //System.out.println("---------------------------");
		    	  
		    	  isupdate=isUpdate(idb,count);
		    	  count++;
		      }
		      System.out.println("The iteration time is: "+count);
	   }
	}

