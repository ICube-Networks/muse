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

public class Segment {
	TTF ttf;
	Breakpoint bp1, bp2;
	int index;
	double a, b;
	boolean lastSegment;

	public Segment(TTF ttf) {
		this.ttf = ttf;
		this.index = 0;

		bp1 = ttf.breakpoints.get(index);
		bp2 = ttf.breakpoints.get(index+1);
		updateParameters();

		if (index >= ttf.breakpoints.size()-2) {
			lastSegment = true;
		}
		else {
			lastSegment = false;
		}
	}

	public void nextSegment() {
		if (index < ttf.breakpoints.size()-1) {
			index++;
			if (index == ttf.breakpoints.size()-2) {
				lastSegment = true;
			}
			
			bp1 = bp2;
			bp2 = ttf.breakpoints.get(index+1);
			updateParameters();
		}
	}

	private void updateParameters() {
		if (bp1.x == bp2.x) {
			a = Toolbox.INFINITY;
			b = Toolbox.INFINITY;
		}
		else {
			a = (bp2.y-bp1.y)/(bp2.x-bp1.x);
			b = bp1.y - a*bp1.x;
		}
	}
}