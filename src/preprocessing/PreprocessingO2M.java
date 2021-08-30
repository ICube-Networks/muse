// Copyright 2021 Technology & Strategy / ICube, GNU Public License.

package preprocessing;

/**
 * The project contains the implementation of MUSE algorithm for the purpose 
 * of multimodal route planning. The project requires a dataset available at:
 * https://doi.org/10.5281/zenodo.5276749
 * 
 * MUSE has been sumbitted to the Transportation Science Journal in a paper
 * entitled "MUSE: Multimodal Separators for Efficient Route Planning in 
 * Transportation Networks".
 * 
 * This project also includes the implementation of the SDALT algorithm as
 * described in the paper. It allows for reproducing the stated performance
 * evaluation.
 *  
 * @author Amine Falek <a.falek@technologyandstrategy.com>
 */

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
