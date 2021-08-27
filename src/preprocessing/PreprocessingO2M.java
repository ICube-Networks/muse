package preprocessing;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import structures.CliqueEdge;
import structures.GraphMultimodal;
import structures.NFA;
import structures.Supernode;

public class PreprocessingO2M {

	private static String REGION;
	private static int PARTITION;
	private static final int [] NFAS = {1, 2, 3, 4, 5};

	public static void main(String[] args) throws IOException {
		
		REGION = args[0];
		PARTITION = Integer.valueOf(args[1]);
		
		final String graph_filepath = String.format("./src/dataset/%s/graph/%s", REGION, REGION);
		final String data_filepath  = String.format("./src/dataset/%s/graph/%s_transit_data.txt", REGION, REGION);

		final GraphMultimodal graph = new GraphMultimodal(graph_filepath);
		graph.initializeTravelTimeFunctions(data_filepath);

		final String partition_filepath = String.format("./src/dataset/%s/graph/%s_nodes.%d", REGION, REGION, PARTITION);

		graph.loadPartition(partition_filepath, PARTITION);
		graph.splitNodesByCell();
		graph.computeBorderNodes();

		for (int nfa_id : NFAS) {
			final String nfa_filepath = String.format("./src/dataset/%s/nfa/", REGION);
			final NFA nfa = new NFA(nfa_filepath, nfa_id);

			for (int cell=0; cell<graph.partitionSize; cell++) {
				OneToManyClique preprocessing = new OneToManyClique(graph, nfa, cell);

				long t1 = System.nanoTime();
				Map<Supernode, List<CliqueEdge>> clique = preprocessing.computeClique();
				long t2 = System.nanoTime();

				System.out.printf("%d,%d,%d,%d,%d,%.2f\n", nfa.nfa_id, graph.partitionSize, cell, graph.borderNodes.get(cell).size(), graph.partitions.get(cell).size(), (t2-t1)/1000000.0);
			}
		}
	}

}
