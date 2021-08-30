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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import structures.CostToReachNodeComparator;
import structures.Edge;
import structures.NodeKey;
import structures.Tree;
import structures.GraphUnimodal;

public class Avoid {
	public GraphUnimodal graph;
	public int numberOfLandmarks;
	public List<Integer> landmarks;
	private String filepath;
	private Map<Integer, List<Integer>> landmarkCosts;
	private int weights[];
	private int sizes[];
	private int costs[];
	
	public Avoid(GraphUnimodal graph, int numberOfLandmarks, String filepath) {
		this.graph = graph;
		this.numberOfLandmarks = numberOfLandmarks;
		this.filepath = filepath;
		landmarkCosts = new HashMap<>();
		weights = new int[graph.numberOfNodes];
		sizes = new int[graph.numberOfNodes];
		costs = new int[graph.numberOfNodes];
	}
	
	private int computeSizes(Tree tree, List<Integer> landmarks) {
		sizes = new int[graph.numberOfNodes];
		int v = tree.getVertex();
		
		if (tree.isLeaf()) {					
			if (landmarks.contains(v))  {
				sizes[v] = -1;
			} else {
				sizes[v] = weights[v];
			}
			return sizes[v];
		}
		
		sizes[v] = 0;
		for (Tree subtree : tree.getChildren()) {
			int size = computeSizes(subtree, landmarks);			
			if (size < 0 || sizes[v] < 0) {
				sizes[v] = -1; 
			} else {
				sizes[v] += size;
			}
		}
		return sizes[v];
	}
	
	private void computeLandmarkCosts(int l) {
		shortestPathTree(l);
		landmarkCosts.put(l, new ArrayList<>());
		
		for (int i=0; i<costs.length; i++) {
			landmarkCosts.get(l).add(costs[i]);
		}
	}
	
	private void saveLandmarks() throws IOException {
		FileWriter file = new FileWriter(filepath + "-landmarks-" + String.valueOf(numberOfLandmarks) + ".txt");
		BufferedWriter writer = new BufferedWriter(file);
		for (int l : landmarks) {
			writer.write(String.valueOf(l) + "\n");
			
		}
		writer.close();
	}
	
	public void computeLandmarks() throws IOException {
		landmarks = new ArrayList<>();
		
		Random random = new Random();
		int dist_rl = 0;
		int dist_vl = 0;
		int bestBound = 0;
		int r, v, bound;
		
		int l0 = random.nextInt(graph.numberOfNodes);
		computeLandmarkCosts(l0);
		landmarks.add(l0);
		while(landmarks.size() < numberOfLandmarks) {
			r = random.nextInt(graph.numberOfNodes);
			List<Tree> trees = shortestPathTree(r);
			
			// compute weights
			for (v = 0; v < this.graph.numberOfNodes; v++) {
				for (Integer l : landmarks) {
					dist_rl = costs[l];
					dist_vl = landmarkCosts.get(l).get(v);
					bound = Math.max(dist_rl - dist_vl, dist_vl - dist_rl);
					if (bound > bestBound) {
						bestBound = bound;
					}
				}
				weights[v] = bestBound - costs[v];
			}
			
			// compute sizes
			computeSizes(trees.get(r), landmarks);
			
			// add landmark
			int largest = 0;
			for (int i = 1; i < sizes.length; i++) {
				if (sizes[i] > sizes[largest]) {
					largest = i;
				}
			}
			Tree tree = trees.get(largest);
			while (!tree.isLeaf()) {
				largest = 0;
				for (Tree subtree : tree.getChildren()) {
					v = subtree.getVertex();
					if (sizes[v] > largest) {
						largest = sizes[v];
						tree = subtree;					
					}
				}
			}
			computeLandmarkCosts(tree.getVertex());
			landmarks.add(tree.getVertex());	
		}
		for (int l : landmarks) {
			System.out.println(l);			
		}
		//saveLandmarks();
	}
	
	private List<Tree> shortestPathTree(int r) {
		Comparator<NodeKey> comparator = new CostToReachNodeComparator();
		PriorityQueue<NodeKey> queue = new PriorityQueue<>(comparator);
		Set<NodeKey> settled = new HashSet<>();
		
		for (int v = 0; v < graph.numberOfNodes; v++) {
			costs[v] = Integer.MAX_VALUE;
		}
		costs[r] = 0;
		queue.add(new NodeKey(r, 0, 0));
		
		while(settled.size() < graph.numberOfNodes) {
			NodeKey node = queue.poll();			
			if (settled.contains(node)) {
				continue;
			}			
			settled.add(node);
			
			for (Edge edge : graph.edges.get(node.vertex)) {
				if (costs[node.vertex] + edge.cost < costs[edge.head]) {
					costs[edge.head] = costs[node.vertex] + edge.cost;
				}
				queue.add(new NodeKey(edge.head, node.vertex, costs[edge.head]));
			}
		}
		
		List<Tree> trees = new ArrayList<>();
		for (int v = 0; v < graph.numberOfNodes; v++) {
			trees.add(new Tree(v));
		}
		for (NodeKey node : settled) {
			trees.get(node.vertex).setParent(trees.get(node.parent));
		}
		return trees;
	}
	
	public static void main(String[] args) throws IOException {
		String region = args[0];
		GraphUnimodal graph = new GraphUnimodal(String.format("./src/dataset/%s/graph/%s", region, region));
		Avoid avoid = new Avoid(graph, 32, String.format("./src/dataset/%s/sdalt/%s", region, region));
		long t = System.nanoTime();
		avoid.computeLandmarks();
		double time = (System.nanoTime() - t)/1000000.0;
		System.out.printf("%s,%.3f\n", region, time);
	}
}
