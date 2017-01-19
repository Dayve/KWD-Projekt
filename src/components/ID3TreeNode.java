package components;

public class ID3TreeNode {
	
	//private boolean diagnosisValue;
	private int columnIndex;
	private Boolean diagnosis;
	//private double infoGain;

	public Boolean getDiagnosis() {
		return diagnosis;
	}
	public void setDiagnosis(Boolean diagnosis) {
		this.diagnosis = diagnosis;
	}

	private ID3TreeNode leftChild = null, rightChild = null;
	
	
	public ID3TreeNode(/*boolean value, */int index, Boolean givenDiagnosis) {
		this.diagnosis = givenDiagnosis;
		this.columnIndex = index;
		//this.infoGain = gain;
	}
	/*
	public boolean getValue() {
		return diagnosisValue;
	}

	public void setValue(boolean value) {
		this.diagnosisValue = value;
	}
	
	public double getInfoGain() {
		return infoGain;
	}

	public void setInfoGain(double infoGain) {
		this.infoGain = infoGain;
	}
	*/
	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int diagnosisID) {
		this.columnIndex = diagnosisID;
	}
	
	public ID3TreeNode getLeftChild() {
		return leftChild;
	}

	public void setLeftChild(ID3TreeNode leftChild) {
		this.leftChild = leftChild;
	}

	public ID3TreeNode getRightChild() {
		return rightChild;
	}

	public void setRightChild(ID3TreeNode rightChild) {
		this.rightChild = rightChild;
	}
	
	@Override
	public String toString() {
		return "Column index: " + columnIndex;
	}
}
