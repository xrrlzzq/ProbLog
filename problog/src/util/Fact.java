package util;

import java.util.ArrayList;

public class Fact {

	   public String predicate;
	   public String[] constants;
	   public double pro=1;
	   
	   public Fact(){
		   
	   }
	   public Fact(String predicate, String[] constants){
		   this.predicate=predicate;
		   this.constants=constants;
		   
	   }
	   
	  
	   public String toString(){
		      
		      StringBuffer sb=new StringBuffer();
		      sb.append(predicate+"(");
		      for(int i=0;i<constants.length-1;i++){
		    	  sb.append(constants[i]+",");
		      }
		      sb.append(constants[constants.length-1]+")");
		      if(pro!=2){
		    	  sb.append(" :"+String.valueOf(pro));
		      }
		      return sb.toString();
	   }
	  public String eString(){
		  StringBuffer sb=new StringBuffer();
	      sb.append(predicate+"(");
	      for(int i=0;i<constants.length-1;i++){
	    	  sb.append(constants[i]+",");
	      }
	      sb.append(constants[constants.length-1]+")");
	      return sb.toString();
	  } 
}
