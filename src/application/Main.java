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
	
	private static final String trainingSetFile = "/data/SPECT.train";
	private static final String testingSetFile = "/data/SPECT.test";
	
	
	public static void main(String[] args) {
		
		Main.loadCSVtoDiagnosisSet(System.getProperty("user.dir") + trainingSetFile, trainingDataset);
		Main.loadCSVtoDiagnosisSet(System.getProperty("user.dir") + testingSetFile, testingDataset);
		
		ID3TreeNode root = new ID3TreeNode();
		
		System.out.println("> Creating decision tree for " + trainingSetFile);
		ID3(trainingDataset, root, new HashSet<Integer>());
		
		int rightAnswerByID3Counter = 0;
		
		System.out.println("> Traversing decision tree - checking results for " + testingSetFile);
		
		for(Diagnosis testedDiagnosis : testingDataset) {
			ID3TreeNode node = root;
			
			while(true) {
				//System.out.println(node);
				
				if(node.getColumnIndex() != null) {
					//System.out.println("Switching nodes");
					
					if(testedDiagnosis.getNthPartialDiagnosis(node.getColumnIndex())){
						if(node.getLeftChild() != null) {
							node = node.getLeftChild();
						}
					}
					else {
						if(node.getRightChild() != null) {
							node = node.getRightChild();
						}
					}
				}
				
				if(node.getDiagnosis() != null) {
					//System.out.println("ID3 diagnosis: " + node.getDiagnosis());
					//System.out.println("Real diagnosis: " + testedDiagnosis.getOverallDiagnosis());
					if(node.getDiagnosis().equals(testedDiagnosis.getOverallDiagnosis())) {
						rightAnswerByID3Counter++;
					}
					break;
				}
				
				if(node.getDiagnosis() == null && node.getColumnIndex() == null) {
					//System.out.println("END NODE");
					break;
				}
			}
		}
		
		System.out.println("> ID3 was right in " + rightAnswerByID3Counter 
				+ " out of " + testingDataset.size() + " cases (" 
				+ (double)rightAnswerByID3Counter/testingDataset.size()*100 + " %)");
	}
	
	
	public static void ID3(Set<Diagnosis> givenSet, ID3TreeNode givenNode, Set<Integer> attributesUsed) {		
		Boolean uniformProperty = checkIfSetIsUniform(givenSet);
		if(uniformProperty != null) {
			//System.out.println("Given set is uniform: (by " + uniformProperty + ") " + givenSet);
			givenNode.setDiagnosis(uniformProperty);
			return;
		}
		
		if(attributesUsed.size() == Diagnosis.NUMBER_OF_ATTRIBUTES) {
			//System.out.println("We've used all the attributes");
			givenNode.setDiagnosis(whichOutcomeHasMostOccurencesInSet(givenSet));
			return;
		}
		
		Integer maxIGAttributeIndex = whichAttributeHasMaxGain(givenSet, attributesUsed);
		if(maxIGAttributeIndex == null) {
			System.out.println("[ERR]: Attribute index (column) is null for:");
			System.out.println("Attributes used: " + attributesUsed);
			System.out.println("Happened for set: " + givenSet);
			return;
		}
		
		givenNode.setColumnIndex(maxIGAttributeIndex);
		//System.out.println("Node label: " + maxIGAttributeIndex);
		attributesUsed.add(maxIGAttributeIndex);
		
		Set<Diagnosis> positiveSet = subsetWhereAttributeHasValue(givenSet, true, maxIGAttributeIndex);
		ID3TreeNode leftChild = new ID3TreeNode();
		givenNode.setLeftChild(leftChild);
		
		Set<Diagnosis> negativeSet = subsetWhereAttributeHasValue(givenSet, false, maxIGAttributeIndex);
		ID3TreeNode rightChild = new ID3TreeNode();
		givenNode.setRightChild(rightChild);
		
		if(!positiveSet.isEmpty()) ID3(positiveSet, leftChild, new HashSet<Integer>(attributesUsed));
		if(!negativeSet.isEmpty()) ID3(negativeSet, rightChild, new HashSet<Integer>(attributesUsed));
	}
		
	
	private static boolean whichOutcomeHasMostOccurencesInSet(Set<Diagnosis> givenSet) {
		int numOfTrue = 0, numOfFalse = 0;
		
		for(Diagnosis diag : givenSet) {
			if(diag.getOverallDiagnosis()) {
				numOfTrue++;
			}
			else {
				numOfFalse++;
			}
		}
		
		// If there are equal numbers of occurences, returns true:
		return numOfTrue >= numOfFalse;
	}
	
	
	private static Integer whichAttributeHasMaxGain(Set<Diagnosis> givenSet, Set<Integer> excludedIndexes) {
		if(givenSet.isEmpty()) {
			System.out.println("[ERR]: Empty set.");
		}
		
		if(excludedIndexes.size() == Diagnosis.NUMBER_OF_ATTRIBUTES) {
			System.out.println("[ERR]: All attributes used.");
			for(Integer i : excludedIndexes) {
				System.out.println("Excluded: " + i);
			}
		}
		
		Integer index = null;
		double maxInformationGain = 0.0;
		
		for(int i=0 ; i<Diagnosis.NUMBER_OF_ATTRIBUTES ; ++i) {
			double gainForCurrentAttribute = informationGain(givenSet, i);

			// It's ">=" below because of the case, when there is no information gain for any column.
			// In that case, they all give zero information gain, and it will take the last one:
			if(gainForCurrentAttribute >= maxInformationGain && !excludedIndexes.contains(i)) {
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
		
		if(setCardinality == 0 || checkIfSetIsUniform(givenSet) != null) {
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
	
	
	private static Boolean checkIfSetIsUniform(Set<Diagnosis> givenSet) {
		// Uniform for overall diagnosis value (all overall diagnosis are the same)
		// Returns true if all are true, false if all are false and null otherwise.
		
		Boolean lastCheckedValue = null;
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
