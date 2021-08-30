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

import java.util.ArrayList;
import java.util.List;

public class TTF {
	public List<Breakpoint> breakpoints;
	public int size;
	public double min;	
	public double max;
	private double slope;
	
	public TTF() {
		min = Toolbox.INFINITY;
		max = 0;
		breakpoints = new ArrayList<>();
		size = 0;
	}
	
	public TTF(double cost) {		
		min = cost;
		max = cost;
		breakpoints = new ArrayList<>();
		
		breakpoints.add(new Breakpoint(0, cost));
		breakpoints.add(new Breakpoint(Toolbox.SECONDS_IN_DAY, cost));	
		size = 2;
		slope = 0;
	}
	
	public Breakpoint getBreakpoint(int index) {
		if (index >= size) {
			return null;
		}
		return breakpoints.get(index);		
	}
	
	public void addBreakpoint(double time, double cost) {
		
		if (Double.isNaN(time) || Double.isNaN(cost)) {
			return;
		}
		
		if (size > 1 && time < breakpoints.get(size-1).x) {
			return;
		}
		
		if (size > 1 && breakpoints.get(size-1).x == time && breakpoints.get(size-1).y == cost) {
			return;
		}
		
		if (size > 1 && breakpoints.get(size-1).x == time && breakpoints.get(size-2).x == time) {
			return;
		}

		if (cost >= Toolbox.INFINITY) {
			cost = Toolbox.INFINITY;
		}
		
		if (time <= 0) {
			breakpoints.clear();
			breakpoints.add(new Breakpoint(time, cost));
			size = 1;
		}
		
		else {
			
			if (size >= 2) {
				double newSlope = (cost - breakpoints.get(size-1).y) / (time - breakpoints.get(size-1).x);
				if (newSlope == slope) {
					breakpoints.set(size-1, new Breakpoint(time, cost));
				}
				else {
					breakpoints.add(new Breakpoint(time, cost));
					size++;
					slope = newSlope;
				}
			}
			
			else if (size == 1) {
				slope = (cost - breakpoints.get(0).y) / (time - breakpoints.get(0).x);
				
				if (breakpoints.get(0).x < 0) {
					breakpoints.clear();
					breakpoints.add(new Breakpoint(0, cost - slope*time));
					breakpoints.add(new Breakpoint(time, cost));
				}
				else {
					slope = (cost - breakpoints.get(0).y) / (time - breakpoints.get(0).x);
					breakpoints.add(new Breakpoint(time, cost));
				}
				
				size = 2;
			}
			
			else {
				breakpoints.add(new Breakpoint(time, cost));
				size++;
			}
			
			if (cost < min) {
				min = cost;
			}
			if (cost > max) {
				max = cost;
			}			
		}
	}
	
	public void addBreakpoint(Breakpoint bp) {
		addBreakpoint(bp.x, bp.y);
	}
	
	@Override
    public String toString() {
		String str = "";
		for (int i=0; i<size; i++) {
			str += String.format("%d,%d;", (int) breakpoints.get(i).x, (int) breakpoints.get(i).y);
		}
		return str;
    }
}
