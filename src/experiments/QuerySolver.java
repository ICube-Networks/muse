// Copyright 2021 Technology & Strategy / ICube, GNU Public License.

package experiments;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithms.DijkstraRegLC;
import algorithms.Muse;
import algorithms.MuseStar;
import algorithms.Sdalt;
import structures.GraphMultimodal;
import structures.NFA;
import structures.Toolbox;

public class QuerySolver {

	private static String REGION;
	private static int PARTITION;
	private static int NFA_ID;
	private static final int NUMBER_OF_LANDMARKS = 32;
	private static final int DEPARTURE_TIME = 36000;

	public static List<Query> loadQueries() throws IOException {
		List<Query> queries = new ArrayList<>();
		final String QUERIES_FILEPATH = String.format("./src/dataset/%s/queries/%s_queries.txt", REGION, REGION);

		FileReader file = new FileReader(QUERIES_FILEPATH);
		BufferedReader reader = new BufferedReader(file);

		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {

				List<String> row = Arrays.asList(sCurrentLine.split(","));

				int v = Integer.parseInt(row.get(0));
				int w = Integer.parseInt(row.get(1));	

				queries.add(new Query(v, w));
			}					
		}
		reader.close();

		return queries;
	}

	public static void main(String[] args) throws IOException {

		REGION = args[0];
		PARTITION = Integer.valueOf(args[1]);
		NFA_ID = Integer.valueOf(args[2]);

		final String GRAPH_FILEPATH = String.format("./src/dataset/%s/graph/%s", REGION, REGION);
		final String DATA_FILEPATH  = String.format("./src/dataset/%s/graph/%s_transit_data.txt", REGION, REGION);
		final String NFA_FILEPATH = String.format("./src/dataset/%s/nfa/", REGION);
		final String PARTITION_FILEPATH = String.format("./src/dataset/%s/graph/%s_nodes.%d", REGION, REGION, PARTITION);
		final String LANDMARKS_FILEPATH = String.format("./src/dataset/%s/landmarks/%s", REGION, REGION);

		final GraphMultimodal graph = new GraphMultimodal(GRAPH_FILEPATH);
		graph.initializeTravelTimeFunctions(DATA_FILEPATH);
		graph.loadPartition(PARTITION_FILEPATH, PARTITION);
		graph.splitNodesByCell();

		List<Query> queries = loadQueries();

		final NFA nfa = new NFA(NFA_FILEPATH, NFA_ID);

		final String OVERLAY_FILEPATH = String.format("./src/dataset/%s/overlay/%s_overlay_%d_%d.txt", REGION, REGION, PARTITION, NFA_ID);
		graph.loadOverlay(OVERLAY_FILEPATH);

		final DijkstraRegLC dijkstra = new DijkstraRegLC(graph, nfa);
		final Sdalt sdalt = new Sdalt(graph, nfa, NUMBER_OF_LANDMARKS);
		final Muse muse = new Muse(graph, nfa);
		final MuseStar museStar = new MuseStar(graph, nfa, 1.0);
		final MuseStar museSV1 = new MuseStar(graph, nfa, 1.2);
		final MuseStar museSV2 = new MuseStar(graph, nfa, 1.5);
		final MuseStar museSV3 = new MuseStar(graph, nfa, 1.8);
		
		sdalt.loadPreprocessing(LANDMARKS_FILEPATH);

		int index = 0;
		long t;
		double t1, t2, t3, t4, t5, t6, t7;

		for (int i=0; i<queries.size(); i++) {
			Query query = queries.get(i);

			int source = query.v;
			int target = query.w;
			int state = 0;

			int vCell = graph.nodes.get(source).cell;
			int wCell = graph.nodes.get(target).cell;

			if (vCell == wCell) {
				continue;
			}

			t = System.nanoTime();
			double cost1 = dijkstra.computeShortestPath(source, state, target, DEPARTURE_TIME);
			t1 = (System.nanoTime() - t)/1000000.0;
			
			t = System.nanoTime();
			double cost2 = sdalt.computeShortestPath(source, state, target, DEPARTURE_TIME);
			t2 = (System.nanoTime() - t)/1000000.0;

			t = System.nanoTime();
			double cost3 = muse.computeShortestPath(source, state, target, DEPARTURE_TIME);
			t3 = (System.nanoTime() - t)/1000000.0;

			t = System.nanoTime();
			double cost4 = museStar.computeShortestPath(source, state, target, DEPARTURE_TIME);
			t4 = (System.nanoTime() - t)/1000000.0;

			t = System.nanoTime();
			double cost5 = museSV1.computeShortestPath(source, state, target, DEPARTURE_TIME);
			t5 = (System.nanoTime() - t)/1000000.0;

			t = System.nanoTime();
			double cost6 = museSV2.computeShortestPath(source, state, target, DEPARTURE_TIME);
			t6 = (System.nanoTime() - t)/1000000.0;

			t = System.nanoTime();
			double cost7 = museSV3.computeShortestPath(source, state, target, DEPARTURE_TIME);
			t7 = (System.nanoTime() - t)/1000000.0;

			double settledDijkstra = dijkstra.getTouchedNodes();
			double settledSdalt = sdalt.getTouchedNodes();
			double settledMuse = muse.getTouchedNodes();
			double settledMuseStar = museStar.getTouchedNodes();
			double settledMuseSV1 = museSV1.getTouchedNodes();
			double settledMuseSV2 = museSV2.getTouchedNodes();
			double settledMuseSV3 = museSV3.getTouchedNodes();

			double distance = Toolbox.eucledianDistance(graph.nodes.get(source).latitude, graph.nodes.get(source).longitude,
					graph.nodes.get(target).latitude, graph.nodes.get(target).longitude);

			System.out.printf("%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
					PARTITION, NFA_ID, distance,
					cost1, cost2, cost3, cost4, cost5, cost6, cost7,
					t1, t2, t3, t4, t5, t6, t7,
					settledDijkstra/settledSdalt,
					settledDijkstra/settledMuse,
					settledDijkstra/settledMuseStar,
					settledDijkstra/settledMuseSV1,
					settledDijkstra/settledMuseSV2,
					settledDijkstra/settledMuseSV3);
		}
	}

}
