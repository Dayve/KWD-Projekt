package components;

public class ID3TreeNode {

	private ID3TreeNode leftChild, rightChild;
	
	private Integer columnIndex;
	private Boolean diagnosis;
	
	
	public ID3TreeNode(Integer index, Boolean givenDiagnosis) {
		this.diagnosis = givenDiagnosis;
		this.columnIndex = index;
	}
	
	public ID3TreeNode(Integer index) {
		this.diagnosis = null;
		this.columnIndex = index;
	}
	
	public ID3TreeNode() {
		columnIndex = null;
		diagnosis = null;
	}

	
	public Boolean getDiagnosis() {
		return diagnosis;
	}
	
	public void setDiagnosis(Boolean diagnosis) {
		this.diagnosis = diagnosis;
	}
	
	public Integer getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(Integer givenIndex) {
		this.columnIndex = givenIndex;
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
		return "[NODE: (" + columnIndex + ") Diagnosis: " + diagnosis + "]";
	}
}
