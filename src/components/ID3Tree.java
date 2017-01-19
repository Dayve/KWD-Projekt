package components;

public class ID3Tree {
	private ID3TreeNode root;
	
	public void addNode(boolean newNodeValue, int diagnosisID, double gain) {
		System.out.println();
		
		ID3TreeNode newNode = new ID3TreeNode(newNodeValue, diagnosisID, gain);
		
		if(root == null) {
			root = newNode;
			System.out.println(newNode + " set as root");
		}
		else {
			ID3TreeNode focusedNode = root;
			ID3TreeNode parent;
			
			while(true) {
				parent = focusedNode;
				System.out.println(parent + " is now parent");

				if (gain < focusedNode.getInfoGain()) {
					focusedNode = focusedNode.getLeftChild();

					if (focusedNode == null) {
						parent.setLeftChild(newNode);
						//System.out.println(newNode + " set as left node");
						return;
					}

				}
				else {
					focusedNode = focusedNode.getRightChild();

					if (focusedNode == null) {
						parent.setRightChild(newNode);
						//System.out.println(newNode + " set as right node");
						return;
					}
				}
			}
		}
	}
}
