package structures;

import java.util.Comparator;

public class CostToReachSuperNodeComparator implements Comparator<Supernode> {

	@Override
	public int compare(Supernode n1, Supernode n2) {
		
		if (n1.key + n1.heuristic < n2.key + n2.heuristic) {
            return -1;
        }
        if (n1.key + n1.heuristic > n2.key + n2.heuristic) {
            return 1;
        }		
		return 0;
	}
}
