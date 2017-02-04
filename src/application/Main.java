package application;

import components.Diagnosis;
import components.ID3TreeNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.Math;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class Main {
	private static Set<Diagnosis>	trainingDataset = new HashSet<Diagnosis>(),
									testingDataset = new HashSet<Diagnosis>();
	
	private static final String trainingSetFile = "/data/SPECT.train",
								testingSetFile = "/data/SPECT.test";
	
	
	public static void main(String[] args) {
		// Load CSV files:
		Main.loadCSVtoDiagnosisSet(System.getProperty("user.dir") + trainingSetFile, trainingDataset);
		Main.loadCSVtoDiagnosisSet(System.getProperty("user.dir") + testingSetFile, testingDataset);
		
		// Divide training set into disjoint sets:
		final int PARTITIONS_COUNT = 2;

		List<Set<Diagnosis>> trainingDataSubsets = new ArrayList<Set<Diagnosis>>(PARTITIONS_COUNT);
		
		for (int i = 0; i < PARTITIONS_COUNT; i++) {
			trainingDataSubsets.add(new HashSet<Diagnosis>());
		}

		int index = 0;
		for (Diagnosis trainingDiagnosis : trainingDataset) {
			trainingDataSubsets.get(index++ % PARTITIONS_COUNT).add(trainingDiagnosis);
		}
			
		// Perform ID3 algorithm:
		ID3TreeNode root = new ID3TreeNode();
		
		System.out.println("> Creating decision tree for full " + trainingSetFile);
		ID3(trainingDataset, root, new HashSet<Integer>());
		
		System.out.println("> Traversing decision tree - checking results for " + testingSetFile);
		traverseID3TreeForSet(root, testingDataset);
	}
	
	
	public static void ID3(Set<Diagnosis> givenSet, ID3TreeNode givenNode, Set<Integer> attributesUsed) {		
		Boolean uniformProperty = checkIfSetIsUniform(givenSet);
		if(uniformProperty != null) {
			givenNode.setDiagnosis(uniformProperty);
			return;
		}
		
		if(attributesUsed.size() == Diagnosis.NUMBER_OF_ATTRIBUTES) {
			givenNode.setDiagnosis(whichOutcomeHasMostOccurencesInSet(givenSet));
			return;
		}
		
		Integer maxIGAttributeIndex = whichAttributeHasMaxGain(givenSet, attributesUsed);
		if(maxIGAttributeIndex == null) {
			System.out.println("[ERR]: Attribute index (column) is null");
			return;
		}
		
		givenNode.setColumnIndex(maxIGAttributeIndex);
		attributesUsed.add(maxIGAttributeIndex);
		
		Set<Diagnosis> positiveSet = subsetWhereAttributeHasValue(givenSet, true, maxIGAttributeIndex);
		if(!positiveSet.isEmpty()) {
			ID3TreeNode leftChild = new ID3TreeNode();
			givenNode.setLeftChild(leftChild);
			
			ID3(positiveSet, leftChild, new HashSet<Integer>(attributesUsed));
		}
		
		Set<Diagnosis> negativeSet = subsetWhereAttributeHasValue(givenSet, false, maxIGAttributeIndex);
		if(!negativeSet.isEmpty()) {
			ID3TreeNode rightChild = new ID3TreeNode();
			givenNode.setRightChild(rightChild);
		
			ID3(negativeSet, rightChild, new HashSet<Integer>(attributesUsed));
		}
	}
	
	
	private static void traverseID3TreeForSet(ID3TreeNode rootNode, Set<Diagnosis> testingSet) {
		int rightAnswersCounter = 0, cannotClassifyCounter = 0;
				
		for(Diagnosis testedDiagnosis : testingSet) {
			ID3TreeNode node = rootNode;
			
			while(true) {
				if(node.getColumnIndex() != null) {
					if(testedDiagnosis.getNthPartialDiagnosis(node.getColumnIndex())){
						if(node.getLeftChild() != null) {
							node = node.getLeftChild();
						}
						else {
							cannotClassifyCounter++;
							break;
						}
					}
					else {
						if(node.getRightChild() != null) {
							node = node.getRightChild();
						}
						else {
							cannotClassifyCounter++;
							break;
						}
					}
				}
				
				if(node.getDiagnosis() != null) {
					if(node.getDiagnosis().equals(testedDiagnosis.getOverallDiagnosis())) {
						rightAnswersCounter++;
					}
					break;
				}
			}
		}
		
		System.out.println("  > ID3 was right in " + rightAnswersCounter 
				+ " out of " + testingSet.size() + " cases (" 
				+ (double)rightAnswersCounter/testingSet.size()*100 + " %)");
		
		int wrong = testingSet.size()-rightAnswersCounter-cannotClassifyCounter;
		
		System.out.println("  > ID3 was wrong in " + wrong
				+ " out of " + testingSet.size() + " cases (" 
				+ (double)wrong/testingSet.size()*100 + " %)");
		
		System.out.println("  > ID3 could not find a rule for " + cannotClassifyCounter 
				+ " out of " + testingSet.size() + " cases (" 
				+ (double)cannotClassifyCounter/testingSet.size()*100 + " %)");
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
			System.out.println("  > A problem occured while parsing a CSV file.");
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
