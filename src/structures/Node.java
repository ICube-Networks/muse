// Copyright 2021 Technology & Strategy / ICube, GNU Public License.

package structures;

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

public class Node {
	public double latitude;
	public double longitude;
	public char label;
	public int cell;
	
	public Node(double latitude, double longitude, char label) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.label = label;
		this.cell = -1;
	}
	
	public Node(double latitude, double longitude, char label, int cell) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.label = label;
		this.cell = cell;
	}
}
