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

public class PreprocessingM2M {

	private static String REGION;
	private static int PARTITION;
	private static int NFA_ID;

	public static void main(String[] args) throws IOException {
		
		REGION = args[0];
		PARTITION = Integer.valueOf(args[1]);
		NFA_ID = Integer.valueOf(args[2]);
		
		final String graph_filepath = String.format("./src/dataset/%s/graph/%s", REGION, REGION);
		final String data_filepath  = String.format("./src/dataset/%s/graph/%s_transit_data.txt", REGION, REGION);

		final GraphMultimodal graph = new GraphMultimodal(graph_filepath);
		graph.initializeTravelTimeFunctions(data_filepath);

		final String partition_filepath = String.format("./src/dataset/%s/graph/%s_nodes.%d", REGION, REGION, PARTITION);

		graph.loadPartition(partition_filepath, PARTITION);
		graph.splitNodesByCell();
		graph.computeBorderNodes();

		final String nfa_filepath = String.format("./src/dataset/%s/nfa/", REGION);
		final NFA nfa = new NFA(nfa_filepath, NFA_ID);

		for (int cell=0; cell<graph.partitionSize; cell++) {
			ManyToManyClique preprocessing = new ManyToManyClique(graph, nfa, cell);
			Map<Supernode, List<CliqueEdge>> clique;
			
			try {
				clique = preprocessing.computeClique();
			} catch (Exception e) {
				System.out.println("error");
				continue;
			}

			for (Supernode tail : clique.keySet()) {
				List<CliqueEdge> cliqueEdges = clique.get(tail);
				for (CliqueEdge edge : cliqueEdges) {
					System.out.printf("%d;%d;%d;%d;%d;%s\n", edge.cell, tail.node, tail.state, edge.head.node, edge.head.state, edge.ttf.toString());
				}
			}
		}
	}

}
