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

import java.util.HashMap;
import java.util.Map;

public class Tests { 

	public static void main(String[] args) {
		Map<Supernode, TTF> mymap = new HashMap<>();
		
		mymap.put(new Supernode(5, 2), new TTF(5));
		mymap.put(new Supernode(10, 3), new TTF(10));
		
		Supernode v1 = new Supernode(5, 2);
		Supernode v2 = new Supernode(10, 3);
		Supernode v3 = new Supernode(5, 1);
		
		TTF ttf = mymap.get(v1);
		ttf = new TTF(50);
		
		System.out.println(ttf.toString());
		System.out.println(mymap.get(v1).toString());

	}

}
