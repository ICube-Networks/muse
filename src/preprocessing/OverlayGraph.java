package preprocessing;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

import structures.CliqueEdge;
import structures.GraphMultimodal;
import structures.Supernode;
import structures.NFA;

public class OverlayGraph {

	private static final String REGION = "idf";
	private static final int [] PARTITIONS = {200};
	private static final int [] NFAS = {3};

//	private static final int MYTHREADS = Runtime.getRuntime().availableProcessors();
	private static Map<Supernode, List<CliqueEdge>> overlay;

	private static void saveOverlay(String region, int nfa_id, int partitionSize) throws IOException {
		final String OVERLAY_FILEPATH = String.format("./src/dataset/%s/overlay/%s_overlay_%d_%d.txt", REGION, REGION, partitionSize, nfa_id);
		FileWriter file = new FileWriter(OVERLAY_FILEPATH);
		BufferedWriter writer = new BufferedWriter(file);
		for (Supernode tail : overlay.keySet()) {
			for (CliqueEdge edge : overlay.get(tail)) {
				writer.write(String.format("%d;%d;%d;%d;%d;%s\n", edge.cell, tail.node, tail.state, edge.head.node, edge.head.state, edge.ttf.toString()));
			}
		}
		writer.close();
	}

	public static void main(String[] args) throws IOException {

		final String graph_filepath = String.format("./src/dataset/%s/graph/%s", REGION, REGION);
		final String data_filepath  = String.format("./src/dataset/%s/graph/%s_transit_data.txt", REGION, REGION);

		final GraphMultimodal graph = new GraphMultimodal(graph_filepath);
		graph.initializeTravelTimeFunctions(data_filepath);

		for (int partitionSize : PARTITIONS) {
			final String partition_filepath = String.format("./src/dataset/%s/graph/%s_nodes.%d", REGION, REGION, partitionSize);

			graph.loadPartition(partition_filepath, partitionSize);
			graph.splitNodesByCell();
			graph.computeBorderNodes();

			for (int nfa_id : NFAS) {
				final String nfa_filepath = String.format("./src/dataset/%s/nfa/", REGION);
				final NFA nfa = new NFA(nfa_filepath, nfa_id);

				overlay = new HashMap<>();
				
				for (int cell=0; cell<graph.partitionSize; cell++) {
					ManyToManyClique preprocessing = new ManyToManyClique(graph, nfa, cell);
					
					long t1 = System.nanoTime();
					Map<Supernode, List<CliqueEdge>> clique = preprocessing.computeClique();
					long t2 = System.nanoTime();
					
					System.out.printf("%d,%d,%d,%d,%d,%.2f\n", nfa.nfa_id, graph.partitionSize, cell, graph.borderNodes.get(cell).size(), graph.partitions.get(cell).size(), (t2-t1)/1000000.0);
					
					for (Supernode supernode : clique.keySet()) {
						if (!overlay.containsKey(supernode)) {
							overlay.put(supernode, new ArrayList<>());
						}
						overlay.get(supernode).addAll(clique.get(supernode));
					}
				}				
				saveOverlay(REGION, nfa_id, partitionSize);
			}
		}
	}

	public static class MyRunnable implements Runnable  {
		private final GraphMultimodal graph;
		private final NFA nfa;
		private final int cell;

		MyRunnable(GraphMultimodal graph, NFA nfa, int cell) {
			this.graph = graph;
			this.nfa   = nfa;
			this.cell  = cell;
		}

		@Override
		public void run() {
			OneToManyClique preprocessing = new OneToManyClique(graph, nfa, cell);

			long t1 = System.nanoTime();
			Map<Supernode, List<CliqueEdge>> clique = preprocessing.computeClique();
			long t2 = System.nanoTime();

			System.out.printf("%d,%d,%d,%d,%d,%.2f\n", nfa.nfa_id, graph.partitionSize, cell, graph.borderNodes.get(cell).size(), graph.partitions.get(cell).size(), (t2-t1)/1000000.0);

			for (Supernode key : clique.keySet()) {
				if (!overlay.containsKey(key)) {
					overlay.put(key, new ArrayList<>());
				}
				overlay.get(key).addAll(clique.get(key));
			}
		}
	}
}