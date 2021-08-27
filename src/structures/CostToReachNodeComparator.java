package structures;

import java.util.Comparator;

public class CostToReachNodeComparator implements Comparator<NodeKey> {
	@Override
	public int compare(NodeKey n1, NodeKey n2) {
		
		if (n1.key < n2.key) {
            return -1;
        }
        if (n1.key > n2.key) {
            return 1;
        }		
		return 0;
	}
}
