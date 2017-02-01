package application;

import components.Diagnosis;
import components.ID3TreeNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.lang.Math;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class Main {
	private static Set<Diagnosis>	trainingDataset = new HashSet<Diagnosis>(),
									testingDataset = new HashSet<Diagnosis>();
	
	
	public static void main(String[] args) {
		
		Main.loadCSVtoDiagnosisSet(System.getProperty("user.dir") + 
				"/data/SPECT.train", trainingDataset);
		Main.loadCSVtoDiagnosisSet(System.getProperty("user.dir") + 
				"/data/SPECT.test", testingDataset);
		
		ID3TreeNode root = null;
		ID3(trainingDataset, root);
	}
	
	
	public static void ID3(Set<Diagnosis> givenSet, ID3TreeNode parentNode) {
		// if(givenSet.isEmpty()) return; // Doesn't change anything
		
		Boolean uniformProperty = givenSetUniformProperty(givenSet);

		int attribWithMaxGain = whichAttributeHasMaxGain(givenSet);
		if(parentNode == null) parentNode = new ID3TreeNode(attribWithMaxGain, null);
		
		if(uniformProperty != null) {
			parentNode.setDiagnosis(uniformProperty);
			System.out.println("Dead end");
			return;
		}
		
		Set<Diagnosis> positiveSet = subsetWhereAttributeHasValue(givenSet, true, attribWithMaxGain);
		if(!positiveSet.isEmpty()) {
			int maxGainFromPositive = whichAttributeHasMaxGain(positiveSet);
			parentNode.setLeftChild(new ID3TreeNode(maxGainFromPositive, null));
		}
		
		Set<Diagnosis> negativeSet = subsetWhereAttributeHasValue(givenSet, false, attribWithMaxGain);
		if(!negativeSet.isEmpty()) {
			int maxGainFromNegative = whichAttributeHasMaxGain(negativeSet);
			parentNode.setRightChild(new ID3TreeNode(maxGainFromNegative, null));
		}
		
		if(!positiveSet.isEmpty()) ID3(positiveSet, parentNode.getLeftChild());
		if(!negativeSet.isEmpty()) ID3(negativeSet, parentNode.getRightChild());
	}
	
	
	private static int whichAttributeHasMaxGain(Set<Diagnosis> givenSet) {
		int index = 0;
		double maxInformationGain = 0.0;
		
		for(int i=0 ; i<Diagnosis.NUMBER_OF_ATTRIBUTES ; ++i) {
			double gainForCurrentAttribute = informationGain(givenSet, i);
			
			if(gainForCurrentAttribute > maxInformationGain) {
				maxInformationGain = gainForCurrentAttribute;
				index = i;
			}
		}
		
		return index;
	}
	
	
	private static double setEntropy(Set<Diagnosis> givenSet) {
		// Given set is partitioned into 2 classes of results (true and false values)
		
		int setCardinality = givenSet.size(), 
			numOfPositiveOutcomes = 0;
		
		if(setCardinality == 0 || givenSetUniformProperty(givenSet) != null) {
			return 0.0;
		}

		for(Diagnosis diagnosis : givenSet) {
			if(diagnosis.getOverallDiagnosis()) numOfPositiveOutcomes++;
		}
		
		int numOfNegativeOutcomes = setCardinality - numOfPositiveOutcomes;
		
		// Probability of a positive result: (both variable range from 0 to 1)
		double positive = ((double)numOfPositiveOutcomes)/((double)setCardinality);
		double negative = ((double)numOfNegativeOutcomes)/((double)setCardinality);

		// Ternary conditionals for preventing 0*(-infinity) being returned (as NaN):
		return -((positive != 0.0 ? positive*log2(positive) : 0.0) + (negative != 0.0 ? negative*log2(negative) : 0.0));
	}
	
	
	private static double informationGain(Set<Diagnosis> givenSet, int attributeIndex) throws IndexOutOfBoundsException {
		if(attributeIndex < 0 || attributeIndex >= Diagnosis.NUMBER_OF_ATTRIBUTES) {
			throw new IndexOutOfBoundsException("Wrong attributeIndex value");
		}
		
		double setCardinality = givenSet.size();	
		Set<Diagnosis>	positiveValues = new HashSet<Diagnosis>(),
						negativeValues = new HashSet<Diagnosis>();
		
		for(Diagnosis diagnosis : givenSet) {
			if(diagnosis.getNthPartialDiagnosis(attributeIndex)) positiveValues.add(diagnosis);
			else negativeValues.add(diagnosis);
		}
		
		return	setEntropy(givenSet) - 
				((positiveValues.size()/setCardinality)*setEntropy(positiveValues) + 
				(negativeValues.size()/setCardinality)*setEntropy(negativeValues));
	}
	
	
	private static Boolean givenSetUniformProperty(Set<Diagnosis> givenSet) {
		// Uniform for overall diagnosis value (all overall diagnosis are the same)
		
		boolean lastCheckedValue = false; // Can be initialized to anything
		boolean noneCheckedYet = true;
		
		for(Diagnosis diag : givenSet) {
			if(noneCheckedYet) {
				lastCheckedValue = diag.getOverallDiagnosis();
				noneCheckedYet = false;
				continue;
			}
			if(diag.getOverallDiagnosis() != lastCheckedValue) return null;
		}
		
		return lastCheckedValue;
	}
	
	
	private static Set<Diagnosis> subsetWhereAttributeHasValue(Set<Diagnosis> givenSet, boolean desiredValue, int atributeIndex) {
		Set<Diagnosis> resultSet = new HashSet<Diagnosis>();
		
		for(Diagnosis diag : givenSet) {
			if(diag.getNthPartialDiagnosis(atributeIndex) == desiredValue) {
				resultSet.add(diag);
			}
		}
		
		return resultSet;
	}
	
	
	private static void loadCSVtoDiagnosisSet(String CSVFilePath, Set<Diagnosis> set) {
		System.out.println("> Parsing a CSV file: (" + CSVFilePath + ")");
		File csvDataFile = new File(CSVFilePath);
		
		try {
			CSVParser parser = CSVParser.parse(csvDataFile, StandardCharsets.UTF_8, CSVFormat.newFormat(','));
			
			for (CSVRecord csvRecord : parser.getRecords()) {
				boolean overallDiagnosisResult = booleanFromString(csvRecord.get(0));
				boolean[] partialDiagnosisResults = new boolean[csvRecord.size()-1];
				
				for(int i=1 ; i<csvRecord.size() ; ++i) {
					partialDiagnosisResults[i-1] = booleanFromString(csvRecord.get(i));
				}
				
				set.add(new Diagnosis(overallDiagnosisResult, partialDiagnosisResults));
			}
			
			System.out.println("  > Data loaded successfully\n  > Number of records: " + set.size());
		} catch (IOException e) {
			System.out.println("a problem occured while parsing a CSV file.");
			e.printStackTrace();
		}
	}
	
	
	private static double log2(double n) {
		return Math.log(n)/Math.log(2.0);
	}
	
	
	private static boolean booleanFromString(String input) {
		return input.equals("1") ? true : false;
	}
}
