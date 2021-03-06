package antiSpamFilterControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.uma.jmetal.solution.DoubleSolution;

import antiSpamFilter.AntiSpamFilterProblem;

public class AntiSpamFilterControl {

	public static File rules = new File("rules.cf");
	public static File ham = new File("ham.log");
	public static File spam = new File("spam.log");
	private static HashMap<String, Integer> hmRulesOrder = new HashMap<String, Integer>();
	public static ArrayList<String> ruleList = new ArrayList<String>();
	private static ArrayList<Double> wList = new ArrayList<Double>(); //Weigths List
	private static ArrayList<Double> autoWList = new ArrayList<Double>(); //Weigths List
	public static int falsePositiveManual; //emails HAM sinalizados pelo filtro
	public static int falseNegativeManual; //emails SPAM não sinalizados pelo filtro
	public static int falsePositiveAuto;
	public static int falseNegativeAuto;

	
	/**
	 * Procedure that treats "Rules.cf" file and fills the HashMap "hmRulesOrder", the rules list "ruleList" and the weight lists "wList" and "autoWList"
	 * @throws FileNotFoundException
	 */
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
	
	/**
	 * Function that returns the weight of the rule selected
	 * @param rule The name of the rule 
	 * @param isManual Flag that indicates which metric is going to be consulted to return the weight
	 * @return weight value
	 */
	public static double getWeigthByRule(String rule, boolean isManual) {
		if(isManual)
			return getWeigthByRule(rule, wList);
		else
			return getWeigthByRule(rule, autoWList);
	}
	
	/**
	 * Function that returns the weight of the rule selected
	 * @param rule The name of the rule
	 * @param list List where the rule is going to be consulted to return the weight
	 * @return weight value
	 */
	public static double getWeigthByRule(String rule, ArrayList<Double> list) {
		if(hmRulesOrder.containsKey(rule)) {
			int i = hmRulesOrder.get(rule);
			return list.get(i);
		}
		return 0.0;
		
	}
	
	/**
	 * Procedure that sets a weight value to a rule
	 * @param rule The name of the rule
	 * @param weight The weight value of the rule
	 */
	public static void setWeigthByRuleManual(String rule, double weight) {
		if(hmRulesOrder.containsKey(rule)) {
				wList.set(hmRulesOrder.get(rule), weight);
		}
	}
	
	/**
	 * Procedure that saves the new configurations defined to "Rules.cf" file
	 * @param isManual Flag that indicates which kind of configuration is going to be saved
	 * @throws IOException
	 */
	public static void saveRulesFile(boolean isManual) throws IOException {
		if(isManual)
			saveRulesFile(wList);
		else	
			saveRulesFile(autoWList);
	}
	
	/**
	 * Procedure that saves the new configurations defined to "Rules.cf" file
	 * @param weigthList The weight list that is going to be saved
	 * @throws IOException
	 */
	public static void saveRulesFile(ArrayList<Double> weigthList) throws IOException {
		FileWriter rulesFile;
		File folder = new File("AntiSpamConfigurationForProfessionalMailbox");
		if(!folder.exists())
			folder.mkdir();
		rulesFile = new FileWriter(new File("AntiSpamConfigurationForProfessionalMailbox/rules.cf"), false);
		
		PrintWriter saveRules = new PrintWriter(rulesFile);
		for (int i = 0; i < ruleList.size(); i++) {
			saveRules.write(ruleList.get(i) + " " + weigthList.get(i) + "\n");
		}
		rulesFile.close();
		saveRules.close();
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
	
	/**
	 * Procedure that gets the metrics generated by Auto Configuration. Defined to select the better configuration to Professional mailbox
	 */
	public static void selectAutoConfiguration() {
		File fileMetrics = new File("experimentBaseDirectory/referenceFronts/AntiSpamFilterProblem.NSGAII.rf");
		int i = -1;
		int FP = -1;
		int FN = -1;
		if(fileMetrics.exists()){
			Scanner scanner;
			try {
				scanner = new Scanner(fileMetrics);
				int j = 0;
				int valueGet = -1;
				while(scanner.hasNext()){
					String[] parts = scanner.nextLine().split(" ");
					valueGet = (int) Double.parseDouble(parts[0]);
					if((valueGet < FP && FP != -1) || FP == -1) {
						FP = valueGet;
						FN = (int) Double.parseDouble(parts[1]);
						i = j;
					}
					j++;
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(i != -1) {
			try {
				String line = Files.readAllLines(Paths.get("experimentBaseDirectory/referenceFronts/AntiSpamFilterProblem.NSGAII.rs")).get(i);
				String[] parts = line.split(" ");
				for(int j = 0; j < parts.length; j++) {
					autoWList.set(j, Double.parseDouble(parts[j]));
				}
				falsePositiveAuto = FP;
				falseNegativeAuto = FN;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Procedure that evaluates the configuration metrics, and checks the number of False Positives and False Negatives
	 * @param isManual Flag that indicates which kind of configuration is going to be run
	 */
	public static void Evaluater(boolean isManual) {
		if(isManual)
			Evaluater(wList, isManual);
		else
			Evaluater(autoWList, isManual);
	}
	
	/**
	 * Procedure that evaluates the configuration metrics, and checks the number of False Positives and False Negatives
	 * @param weigthList The weight list that is going to be evaluated
	 * @param isManual Flag that indicates which kind of configuration is going to be run
	 */
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
		if(spam.exists()){
			Scanner scanner;
			try {
				scanner = new Scanner(spam);
				while(scanner.hasNext()){
					if(getWeigthOfRulesArray(scanner.nextLine(), weigthList) <= 5) {
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
	}
	
	public static void generateFiles(){
		try {
			Process p = Runtime.getRuntime().exec("/usr/local/bin/Rscript HV.Boxplot.R", null, new File("experimentBaseDirectory/AntiSpamStudy/R"));
			Process p2 = Runtime.getRuntime().exec("/Library/TeX/texbin/pdflatex AntiSpamStudy.tex", null, new File("experimentBaseDirectory/AntiSpamStudy/latex"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}


