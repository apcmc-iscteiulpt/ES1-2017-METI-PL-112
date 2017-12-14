package antiSpamFilterControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.uma.jmetal.solution.DoubleSolution;

import antiSpamFilter.AntiSpamFilterProblem;

public final class AntiSpamFilterControl {

	static File rules = new File("rules.cf");
	static File ham = new File("ham.log");
	static File spam = new File("spam.log");
	static HashMap<String, Integer> hmRulesOrder = new HashMap<String, Integer>();
	public static ArrayList<String> ruleList = new ArrayList<String>();
	static ArrayList<Double> wList = new ArrayList<Double>(); //Weigths List
	static ArrayList<Double> autoWList = new ArrayList<Double>(); //Weigths List
	static int falsePositiveManual;
	static int falseNegativeManual;
	public static int falsePositiveAuto;
	public static int falseNegativeAuto;
	public static DoubleSolution manualResults;
	public static DoubleSolution autoResults;

	
	public AntiSpamFilterControl() {
		falsePositiveManual = 0;
		falseNegativeManual = 0;
		falsePositiveAuto = 0;
		falseNegativeAuto = 0;
	}
		
	@SuppressWarnings("unchecked")
	static void treatRulesFile() throws FileNotFoundException{
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		if(rules.exists()){
			Scanner scanner = new Scanner(rules);
			String line;
			int counter = 0;
			double value = 0.0;
			while(scanner.hasNext()){
				line = scanner.nextLine();
				String[] parts = line.split(" ");
				hm.put(parts[0], counter);
				if(parts.length > 1)
					value = Double.parseDouble(parts[1]);
				else
					value = 0.0;
				
				wList.add(value);
				ruleList.add(parts[0]);
				counter += 1;
			}
			scanner.close();
		}
		hmRulesOrder = hm;
		autoWList = (ArrayList<Double>) wList.clone();
	}
	
	public static File getHamFile() {
		return ham;
	}
	
	public static File getSpamFile() {
		return spam;
	}
	
	public static void setManualResults(DoubleSolution solution) {
		manualResults = solution;
	}
	
	public static double getWeigthByRule(String rule, boolean isManual) {
		if(isManual)
			return getWeigthByRule(rule, wList);
		else
			return getWeigthByRule(rule, autoWList);
	}
	
	public static double getWeigthByRule(String rule, ArrayList<Double> list) {
		int i = hmRulesOrder.get(rule);
		return list.get(i);
	}
	
	public static void setWeigthByRule(String rule, double weight) {
		wList.set(hmRulesOrder.get(rule), weight);
	}
	
	public static int getIndexRule(String Rule) {
		
		
		return falseNegativeAuto;
		
	}
	
	public static void saveRulesFile(boolean isManual) throws IOException {
		if(isManual)
			saveRulesFile(wList);
		else	
			saveRulesFile(autoWList);
	}
	
	public static void saveRulesFile(ArrayList<Double> weigthList) throws IOException {
		FileWriter rulesFile = new FileWriter(rules, false);
		PrintWriter saveRules = new PrintWriter(rulesFile);
		for (int i = 0; i < ruleList.size(); i++) {
			saveRules.write(ruleList.get(i) + " " + weigthList.get(i) + "\n");
		}
		rulesFile.close();
		saveRules.close();
	}
	
	public static void Evaluater(boolean isManual) {
		if(isManual)
			Evaluater(wList, isManual);
		else
			Evaluater(autoWList, isManual);
	}
	
	private static double getWeigthOfRulesArray(String line, ArrayList<Double> weigthList) {
		Double counter = 0.0;
		String[] parts = line.split("\\t");
		for(int i = 1; i < parts.length; i++) {
			if(hmRulesOrder.containsKey(parts[i]))
				counter = counter + getWeigthByRule(parts[i], weigthList);
		}
		return counter;
	}
	
	public static void Evaluater(ArrayList<Double> weigthList, boolean isManual) {
	    if(isManual) {
		    	falseNegativeManual = 0;
		    	falsePositiveManual = 0;
	    }
	    else {
	    		falseNegativeAuto = 0;
	    		falsePositiveAuto = 0;
	    }
		
		if(ham.exists()){
			Scanner scanner;
			try {
				scanner = new Scanner(ham);
				while(scanner.hasNext()){
					if(getWeigthOfRulesArray(scanner.nextLine(), weigthList) > 5) {
						if(isManual)
							falseNegativeManual += 1;
						else
							falseNegativeAuto += 1;
					}
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	    }
	    
	    if(spam.exists()){
			Scanner scanner;
			try {
				scanner = new Scanner(spam);
				while(scanner.hasNext()){
					if(getWeigthOfRulesArray(scanner.nextLine(), weigthList) <= 5) {
						if(isManual)
							falsePositiveManual += 1;
						else
							falsePositiveAuto += 1;
					}
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	    }
		
	}
	
	
	
}


