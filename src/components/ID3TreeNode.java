package components;

public class ID3TreeNode {
	
	private boolean diagnosisValue;
	private int diagnosisID;
	private double infoGain;

	private ID3TreeNode leftChild, rightChild;
	
	
	public ID3TreeNode(boolean value, int givenDiagnosisID, double gain) {
		this.diagnosisValue = value;
		this.diagnosisID = givenDiagnosisID;
		this.infoGain = gain;
	}
	
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
	
	public int getDiagnosisID() {
		return diagnosisID;
	}

	public void setDiagnosisID(int diagnosisID) {
		this.diagnosisID = diagnosisID;
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
		return "Value: "+diagnosisValue+", diagnosisID: "+diagnosisID;
	}
}
