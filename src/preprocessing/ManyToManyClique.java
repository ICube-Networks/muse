package preprocessing;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import structures.CliqueEdge;
import structures.CostToReachSuperNodeComparator;
import structures.EdgeLabeled;
import structures.GraphMultimodal;
import structures.Supernode;
import structures.NFA;
import structures.TTF;
import structures.Toolbox;
import structures.Transition;

public class ManyToManyClique {
	private final GraphMultimodal graph;
	private final NFA nfa;
	private final int cell;

	private final List<Supernode> BORDER_SUPERNODES;

	private PriorityQueue<Supernode> queue;
	private Map<Supernode, Map<Supernode, TTF>> costs;
	private Map<Supernode, Double> keys;
	private Comparator<Supernode> comparator;

	private final double INFINITY = 1000000;

	public ManyToManyClique(GraphMultimodal graph, NFA nfa, int cell) {
		this.graph = graph;
		this.nfa = nfa;
		this.cell = cell;

		BORDER_SUPERNODES = new ArrayList<>();
		for (int bordernode : graph.borderNodes.get(cell)) {
			for (int state=0; state<nfa.numberOfStates; state++) {
				if (!isNodeAndStateCompatible(bordernode, state)) {
					continue;
				}
				BORDER_SUPERNODES.add(new Supernode(bordernode, state, 0));
			}
		}
	}

	private boolean isNodeAndStateCompatible(int v, int s) {
		char vLabel = graph.nodes.get(v).label;
		char sLabel = nfa.states.get(s).label;

		if ((vLabel == 's' && sLabel == 'p') || vLabel == sLabel) {
			return true;
		}
		return false;		
	}

	private void initialize() {
		comparator = new CostToReachSuperNodeComparator();
		queue = new PriorityQueue<>(comparator);
		costs = new HashMap<>();
		keys  = new HashMap<>();

		List<Supernode> supernodes = new ArrayList<>();
		for (int v : graph.partitions.get(cell)) {
			for (int s=0; s<nfa.numberOfStates; s++) {				
				if (isNodeAndStateCompatible(v, s)) {
					Supernode vs = new Supernode(v, s);
					supernodes.add(vs);
					keys.put(vs, INFINITY);
				}
			}
		}

		for (Supernode source : BORDER_SUPERNODES) {
			HashMap<Supernode, TTF> costsFromSource = new HashMap<>();

			source.key = 0;
			costsFromSource.put(source, new TTF(0));
			queue.add(source);
			keys.put(source, 0.0);
			
			for (Supernode supernode : supernodes) {
				if (!supernode.equals(source)) {
					costsFromSource.put(supernode, new TTF(INFINITY));
				}
			}
			costs.put(source, costsFromSource);
		}
	}

	public Map<Supernode, List<CliqueEdge>> computeClique() {
		initialize();
		boolean updated = false;

		while (!queue.isEmpty()) {
			Supernode vs = queue.poll();

			List<EdgeLabeled> neighbors = graph.outgoingEdges.get(vs.node);
			List<Transition> transitions = nfa.transitions.get(vs.state);

			for(int i=0; i<neighbors.size(); i++) {
				int w = neighbors.get(i).head;
				char edgeLabel = neighbors.get(i).label;

				if (graph.nodes.get(w).cell != cell) {
					continue;
				}

				for(int j=0; j<transitions.size(); j++) {
					int q = transitions.get(j).to_state;

					// consition1: neighbor vertex and to_state are compatible.
					// consition2: graph edge and NFA transition labels are the same.
					if (!isNodeAndStateCompatible(w, q) || transitions.get(j).label != edgeLabel) {
						continue;
					}

					Supernode wq = new Supernode(w, q);
					double minKey = keys.get(wq);

					updated = false;
					for (Supernode source : BORDER_SUPERNODES) {
						
						TTF vTTF  = costs.get(source).get(vs);
						TTF wTTF  = costs.get(source).get(wq);
						TTF vwTTF = graph.getEdge(vs.node, w).ttf;

						if (vTTF.min + vwTTF.min > wTTF.max) {
							continue;
						}
						
						wTTF = Toolbox.merge(wTTF, Toolbox.link(vTTF, vwTTF));						
						costs.get(source).put(wq, wTTF);					

						if (wTTF.min < minKey) {
							minKey = wTTF.min;
							keys.put(wq, minKey);
							updated = true;
						}
					}

					if (updated) {
						queue.remove(wq);
						wq.key = minKey;
						queue.add(wq);
					}
				}
			}
		}
		
		return buildClique();		
	}
	
	private Map<Supernode, List<CliqueEdge>> buildClique() {
		Map<Supernode, List<CliqueEdge>> clique = new HashMap<>();
		
		for (int i=0; i<BORDER_SUPERNODES.size(); i++) {
			Supernode tail = BORDER_SUPERNODES.get(i);
			List<CliqueEdge> edges = new ArrayList<>();

			for (int j=0; j<BORDER_SUPERNODES.size(); j++) {
				if (i != j) {
					Supernode head = BORDER_SUPERNODES.get(j);
					TTF ttf = costs.get(tail).get(head);
					
					if (ttf.min < INFINITY)  {
						edges.add(new CliqueEdge(head, ttf, cell));
					}
				}
			}
			clique.put(tail, edges);
		}
		return clique;
	}

}