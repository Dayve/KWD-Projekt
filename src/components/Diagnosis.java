package components;

public class Diagnosis {
	public static final int NUMBER_OF_ATTRIBUTES = 22;
	
	private boolean overallDiagnosis;
	private boolean[] partialDiagnosis = new boolean[NUMBER_OF_ATTRIBUTES];
	
	
	public boolean getOverallDiagnosis() {
		return overallDiagnosis;
	}
	
	
	public boolean getNthPartialDiagnosis(int n) {
		return partialDiagnosis[n];
	}

	
	public Diagnosis(boolean overallDiagnosis, boolean[] partialDiagnosis) {
		this.overallDiagnosis = overallDiagnosis;
		this.partialDiagnosis = partialDiagnosis;
	}

	
	@Override
	public String toString() {
		String result = "Diagnosis: " + (overallDiagnosis ? "1" : "0");
		//for(int i=0 ; i<partialDiagnosis.length ; ++i) {
		//	result += "," + (partialDiagnosis[i]  ? "1" : "0");
		//}
		return result;
	}
}
