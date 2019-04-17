package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


import engine.AdInference;
import engine.Inference;
import util.Fact;

public class Main {

	public static void main(String[] args) throws IOException {
		Scanner sc=new Scanner(System.in);
		while(true){
			System.out.println("please choose naive or semi_naive, if naive, please enter (n), semi_naive is (s)");
			String way=sc.nextLine().toLowerCase();
			System.out.println("please choose file, enter: ");
			String textName=sc.nextLine();
			System.out.println("do you want probability? enter(y/n): ");
			String pro=sc.nextLine().toLowerCase();
			System.out.println("do you want max function for disjunction? enter(y?): ");
			String max=sc.nextLine().toLowerCase();
			System.out.println("do you want production function for conjunction? enter(y?): ");
			String production=sc.nextLine().toLowerCase();
			boolean hasPro=false;
			
			if(pro.equals("y"))
				hasPro=true;
			if(way.equals("s")){
				
				testSemiNaive(textName,hasPro,max,production);
			}
			else{
				testNaive(textName,hasPro,max,production);
			}
			
			System.out.println("do you want continue(y?), enter: ");
			String exit=sc.nextLine().toLowerCase();
			if(exit.equals("y"))
				continue;
			else
				break;
		}        
//		AdInference ad=new AdInference("\\2-25.cdl",true);  
//		ad.semi_naive();
//		System.out.println(ad.factMap);

	}
	public static void testNaive(String textName,boolean hasPro,String max, String production) throws IOException{
      Inference i=new Inference(File.separator+textName,hasPro); // this is naive
      if(max.equals("y"))
    	  i.useMax=true;
      if(production.equals("y"))
    	  i.useProduction=true;
      long currentTime1,currentTime2;
      currentTime1=System.currentTimeMillis();
      i.naive();
      currentTime2=System.currentTimeMillis();
      System.out.println(i.factMap);
      writeFile(i.factMap);
      System.out.println("use time: "+(currentTime2-currentTime1)+" ms");
		 int size=0;
		 for(Map.Entry<String, ArrayList<Fact>> entry:i.factMap.entrySet()){
	        	size=size+entry.getValue().size();
	        }
		 System.out.println("the siez of all facts is: "+size);
	     
	}
	public static void testSemiNaive(String textName,boolean hasPro,String max, String production) throws IOException{
		
		 AdInference ad=new AdInference(File.separator+textName,hasPro); // this is semi naive
		 if(max.equals("y"))
	    	  ad.useMax=true;
	      if(production.equals("y"))
	    	  ad.useProduction=true;
	        long currentTime1,currentTime2;
	        currentTime1=System.currentTimeMillis();
	        ad.semi_naive();
	        currentTime2=System.currentTimeMillis();
	        System.out.println(ad.factMap);
	        writeFile(ad.factMap);
	        System.out.println("use time: "+(currentTime2-currentTime1)+" ms");
	        int size=0;
	        for(Map.Entry<String, ArrayList<Fact>> entry:ad.factMap.entrySet()){
	        	size=size+entry.getValue().size();
	        }
	        System.out.println("the siez of all facts is: "+size);
	}

	public static void writeFile(HashMap<String,ArrayList<Fact>> map) throws IOException{
		BufferedWriter bw=new BufferedWriter(new FileWriter(System.getProperty("user.dir")+File.separator+"output.text"));
		for(Map.Entry<String,ArrayList<Fact>> entry:map.entrySet()){
			ArrayList<Fact> temp=entry.getValue();
			for(Fact f:temp){
				bw.write(f.toString());
				bw.newLine();
			}
		}
		bw.close();
	}
}
