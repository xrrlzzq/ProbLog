package util;

public class Rule {

	   public int ruleNum;
	   public Literal head;
	   public Literal[] bodys;
	   public double pro;
	   
	   public Rule(){
		   
	   }
	   
	   public Rule(int ruleNum,Literal head,Literal[] bodys, double pro ){
		   this.ruleNum=ruleNum;
		   this.head=head;
		   this.bodys=bodys;
		   this.pro=pro;
	   }
	   public String toString(){
		      
		      StringBuffer sb=new StringBuffer();
		      sb.append(head.toString()+" :-");
		      for(int i=0;i<bodys.length-1;i++){
		    	  sb.append(bodys[i].toString()+" ,");
		      }
		      sb.append(bodys[bodys.length-1]);
		      if(pro!=1){
		    	  sb.append(" :"+String.valueOf(pro));
		      }
		      return sb.toString();
	   }
}
