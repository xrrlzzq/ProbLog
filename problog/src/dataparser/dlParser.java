package dataparser;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import util.Fact;
import util.Literal;
import util.Rule;

public class dlParser {
    
    ArrayList<String> dataSentence=new ArrayList<String>();
    public ArrayList<Fact> edb=new ArrayList<Fact>();
    public ArrayList<Rule> rules=new ArrayList<Rule>();
	public void dataReader(String textName) throws IOException{
		
		BufferedReader br=new BufferedReader(new FileReader(System.getProperty("user.dir")+textName));
		String line="";
		while((line=br.readLine())!=null)
			dataSentence.add(line);
		br.close();
		
	}
	public HashMap<String,Fact> buildMap(){
		
		HashMap<String,Fact> res=new HashMap<String,Fact>();
		for(Fact f:edb){
			if(!res.containsKey(f.eString()))
				res.put(f.eString(), f);
		}
		return res;
	}
	public boolean isFact(String str){
		   
		   int left,right;
		   left=str.indexOf("(");
		   right=str.indexOf(")");
		   if(left==-1||right==-1)
			   return false;
		   String pre=str.substring(0, left);
		   String val=str.substring(left, right+1);
		   if(!val.contains(","))
			   return false;
		   for(int i=0;i<val.length();i++){  // check edge(1,2)
			   if(val.charAt(i)>='A'&&val.charAt(i)<='Z')
				   return false;
			   if(val.substring(i, i+1).equals(" "))
				   return false;
		   }
		   return true;
		   
	}
	public boolean isLiteral(String str){
		 int left,right;
		   left=str.indexOf("(");
		   right=str.indexOf(")");
		   if(left==-1||right==-1)
			   return false;
		   String pre=str.substring(0, left);
		   String val=str.substring(left, right+1);
		   if(!val.contains(","))
			   return false;
		   String[] token=val.substring(1,val.length()-2).split(",");
		   for(int i=0;i<token.length;i++){
			   if(token[i].length()>1||(token[i].charAt(0)<='A'&&token[i].charAt(0)>='Z'))
				   return false;
		   }
		   return true;
	}
	public boolean isRule(String str){
		if(!str.contains(":-"))
			return false;
		String[] divide=str.split(":-");
		if(divide.length!=2)
			return false;
		String head=divide[0].trim();
		String body=divide[1].trim();
		//System.out.println(head);
		//System.out.println(body);
		if(!isLiteral(head))
			return false;
		String[] bodys=body.split(", "); 
		for(int i=0;i<bodys.length;i++){
			if(!isLiteral(bodys[i])&&!isFact(bodys[i])){
				System.out.println(bodys[i]+" is wrong");
				return false;
			}
				
		}
		return true;
	}
	public Literal generateLiteral(String str){
		 
		  int hl=str.indexOf("("), hr=str.indexOf(")");
		  String pre=str.substring(0, hl);
		  String[] val=str.substring(hl+1,hr).split(",");
		  Literal l=new Literal(pre,val);
		  return l;
		  
	}
   public void parseData(boolean hasPro){
	   int line=0;   
	   for(String s:dataSentence){
		      if(!s.contains("."))
		    	  continue;
		      if(s.contains(":-")){
		    	  try{
		    		  String[] str=s.split(":-");
		    		  String head=str[0].trim();
		    		  String body=str[1].trim();
		    		  Literal h=generateLiteral(head); // generate a literal from string
		    		  int last=body.indexOf(".");
		    		  double pro=1;
		    		  if(hasPro){
		    			  last=body.indexOf(":");
		    			  pro=Double.parseDouble(body.substring(last+1, body.indexOf(".")+2).trim());
		    		  }
		    		  String[] bodys=body.substring(0, last).split(", "); // split body
		    		  Literal[] ls=new Literal[bodys.length];
		    		  for(int i=0;i<bodys.length;i++){
		    			  //System.out.println(bodys[i]);
		    			  Literal l=generateLiteral(bodys[i]);
		    			  ls[i]=l;
		    		  }
		    		  Rule r=new Rule(line,h,ls,pro);
		    		  line++;
		    		  rules.add(r);
		    	  }catch(Exception e){
		    		  System.out.println("this is not a legal syntax rule");
		    		  e.printStackTrace();
		    	  }
		      }
		      else{
		    	  try{
		    		  s=s.trim();
		    		  int left=s.indexOf("(");
		    		  int right=s.indexOf(")");
		    		  int last=s.indexOf(".");
		    		  String pre=s.substring(0, left);
		    		  String[] constants=s.substring(left+1, right).split(",");
		    		 
		    		  boolean isLegal=true;
//		    		  for(int i=0;i<constants.length;i++){  // check edge(1,2)
//		   			     String temp=constants[i];
//		   			     if(constants[i].length()==1&&constants[i].charAt(0)>='A'&&constants[i].charAt(0)<='Z'){
//		   			    	 isLegal=false;
//		   			    	 break;
//		   			     }
//		   			     else{
//		   			    	 for(int k=0;k<constants[i].length();k++){
//		   			    		 
//		   			    	 }
//		   			     }
//		   		   }
		    		  Fact f=new Fact(pre,constants);
		    		  
		    		  if(hasPro){
		    		  int third=s.indexOf(":");
		    		  double pro=Double.parseDouble(s.substring(third+1, last+2).trim());
		    		  //System.out.println(s.substring(third, last).trim());
		    		  f.pro=pro;
		    		  }
		    		  edb.add(f);
		    	  }catch(Exception e){
		    		  System.out.println("this is not a legal fact");
		    	  }
	          }
//	    	  if(isFact(s)){
//	    		  s=s.trim();
//	    		  int left=s.indexOf("(");
//	    		  int right=s.indexOf(")");
//	    		  int last=s.indexOf(".");
//	    		  String pre=s.substring(0, left);
//	    		  String[] constans=s.substring(left+1, right).split(",");
//	    		  Fact f=new Fact(pre,constans);
//	    		  
//	    		  if(hasPro){
//	    		  int third=s.indexOf(":");
//	    		  double pro=Double.parseDouble(s.substring(third+1, last));
//	    		  f.pro.add(pro);
//	    		  }
//	    		  edb.add(f);
//	    		  
//	    	  }
//	    	  if(isRule(s)){
//	    		  String[] str=s.split(":-");
//	    		  String head=str[0].trim();
//	    		  String body=str[1].trim();
//	    		  Literal h=generateLiteral(head);
//	    		  int last=body.indexOf(".");
//	    		  double pro=1;
//	    		  if(hasPro){
//	    			  last=body.indexOf(":");
//	    			  pro=Double.parseDouble(body.substring(last+1, body.indexOf(".")));
//	    		  }
//	    		  String[] bodys=body.substring(0, last).split(", ");
//	    		  Literal[] ls=new Literal[bodys.length];
//	    		  for(int i=0;i<bodys.length;i++){
//	    			  Literal l=generateLiteral(bodys[i]);
//	    			  ls[i]=l;
//	    		  }
//	    		  Rule r=new Rule(line,h,ls,pro);
//	    		  line++;
//	    		  rules.add(r);
//	    		  
//	    	  }
	    	  
	      }
   }
   public void print(){
	   for(Rule r:rules){
		   System.out.println(r);
	   }
	   for(Fact f:edb){
		   System.out.println(f);
	   }
   }
	
}
