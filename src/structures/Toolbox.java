package structures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Toolbox {

	final static double INFINITY = 1000000;
	final static double SECONDS_IN_DAY = 86400;
	final static double EARTH_RADIUS = 6371.000;
	
	
	public static double eucledianDistance(double lat1, double lon1, double lat2, double lon2) {	 
		 double latDistance = toRad(lat2-lat1);
		 double lonDistance = toRad(lon2-lon1);
		 
		 double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		 double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		 return EARTH_RADIUS * c;
	}
	
	private static Double toRad(Double value) {
		 return value * Math.PI / 180;
	}

	/**
	 * Evaluates function ttf for a given time value.
	 *
	 * @param  ttf   a Travel-Time Function
	 * @param  time  time value
	 * @return       ttf(time)
	 */
	public static double evaluate(TTF ttf, double time) {

		if (Double.isNaN(time)) {
			throw new ArithmeticException();
		}

		if (time < ttf.breakpoints.get(0).x || time > ttf.breakpoints.get(ttf.breakpoints.size()-1).x) {
			throw new IndexOutOfBoundsException();
		}

		int lo = 0;
		int hi = ttf.breakpoints.size() - 1;
		int mid;

		while (lo <= hi) {
			mid = (hi + lo) / 2;

			if (time < ttf.breakpoints.get(mid).x) {
				hi = mid - 1;
			} else if (time > ttf.breakpoints.get(mid).x) {
				lo = mid + 1;
			} else {
				if (mid > 0 && ttf.breakpoints.get(mid-1).x == time) return ttf.breakpoints.get(mid-1).y;
				return ttf.breakpoints.get(mid).y;
			}
		}

		Breakpoint bpLeft  = ttf.breakpoints.get(lo-1);
		Breakpoint bpRight = ttf.breakpoints.get(hi+1);

		double deltaX = bpRight.x - bpLeft.x;
		double deltaY = bpRight.y - bpLeft.y;

		return ((time-bpLeft.x) * (deltaY/deltaX)) + bpLeft.y;
	}

	/**
	 * Computes the Travel-Time Function denoting the overall travel-time
	 * of two sequential edges e1 and e2 with TTFs f and g, respectively. 
	 *
	 * @param  f  first Travel-Time Function
	 * @param  g  second Travel-Time Function
	 * @return    f(t) + g[f(t) + t]
	 * @see       TTF
	 */
	public static TTF link(TTF f, TTF g) {		
		// both f and g are constant
		if (f.min == f.max && g.min == g.max) {
			return new TTF(f.min + g.min);
		}

		// g is constant, shift f upwards
		if (f.min != f.max && g.min == g.max) {
			TTF ttf = new TTF();
			for (int i=0; i<f.breakpoints.size(); i++) {
				ttf.addBreakpoint(f.breakpoints.get(i).x, f.breakpoints.get(i).y + g.min);
			}
			return ttf;
		}

		// f is constant, shift g to the left + upwards
		if (f.min == f.max && g.min != g.max) {
			TTF ttf = new TTF();
			for (int i=0; i<g.breakpoints.size(); i++) {
				ttf.addBreakpoint(g.breakpoints.get(i).x - f.min, g.breakpoints.get(i).y + f.min);
			}
			return ttf;
		}

		// both f and g are time-dependent
		else {
			return linkTTFs(f, g);
		}
	}
	
	private static TTF linkTTFs(TTF f, TTF g) {
		TTF ttf = new TTF();
		double x, x1, x2, y1, y2, h1, h2;
		double min, max, deltaX, deltaY;
		int j = 0;

		for (int i=1; i<f.breakpoints.size(); i++) {
			// piecewise segment coordinates of f(t) + t			
			x1 = f.breakpoints.get(i-1).x;
			y1 = f.breakpoints.get(i-1).y + x1;
			x2 = f.breakpoints.get(i).x;
			y2 = f.breakpoints.get(i).y + x2;
			
			deltaY = y2 - y1;
			deltaX = x2 - x1;
			
			// next f segment
			if (deltaX == 0) {
				continue;
			}
			
			// add f left and right breakpoints
			if (deltaY == 0) {				
				try {
					// h = f(x) + g[f(x) + x] at x = x1, x2
					h1 = y1 - x1 + evaluate(g, y1);
					h2 = y2 - x2 + evaluate(g, y2);
					
					ttf.addBreakpoint(x1, h1);
					ttf.addBreakpoint(x2, h2);
				} catch (Exception e) {
				}
			}
			
			if (y2 > y1) {
				max = y2;
				min = y1;
			}
			else {
				max = y1;
				min = y2;				
			}

			while (j < g.breakpoints.size()) {
				Breakpoint gBreakpoint = g.breakpoints.get(j);

				if (gBreakpoint.x < min) {
					j++;
					continue;
				}
				if (gBreakpoint.x > max || deltaY == 0) {
					break;
				}
				
				j++;
				
				// we compute x such that f(x) + x = gBreakpoint.x
				// therefore, g[f(x) + x] = g(gBreakpoint.x) = gBreakpoint.y
				// therefore, f(x) + g[f(x) + x] = (gBreakpoint.x - x) + gBreakpoint.y
				x = Math.round(((gBreakpoint.x-y1) * (deltaX/deltaY)) + x1);
				ttf.addBreakpoint(x, gBreakpoint.x - x + gBreakpoint.y);
			}
			
			if (j >= g.breakpoints.size()) {
				break;
			}
		}
		if (ttf.breakpoints.size() == 1) {
			return new TTF(INFINITY);
		}
		return ttf;
	}
	
	/**
	 * Computes the Travel-Time Function denoting the minimum travel-time
	 * of two parallel edges e1 and e2 with TTFs f and g, respectively. 
	 *
	 * @param  f  first Travel-Time Function
	 * @param  g  second Travel-Time Function
	 * @return    min{f(t), g(t)}
	 * @see       TTF
	 */
	public static TTF merge(TTF f, TTF g) {
		if (f.min >= g.max) {
			return g;
		}		
		if (g.min >= f.max) {
			return f;
		}	

		TTF ttf = new TTF();
		List<Breakpoint> intersections = computeIntersectionPointsOfTTFs(f, g);

		int i = 0;
		int j = 0;
		int k = 0;

		Breakpoint bpf = f.getBreakpoint(i);
		Breakpoint bpg = g.getBreakpoint(j);
		Breakpoint projection = null;
		Breakpoint bpfPrevious = null;
		Breakpoint bpgPrevious = null;
		Breakpoint bpi = null;
		if (!intersections.isEmpty()) {
			bpi = intersections.get(k);
		}

		while (true) {

			if (bpf == null) {
				addRemainingBreakpoints(ttf, g, j);
				break;
			}
			if (bpg == null) {
				addRemainingBreakpoints(ttf, f, i);
				break;
			}

			if (bpi != null && bpi.x < bpf.x && bpi.x < bpg.x) {
				ttf.addBreakpoint(bpi);
				try {
					bpi = intersections.get(++k);
				}
				catch (IndexOutOfBoundsException e) {
					bpi = null;
				}
				continue;
			}
			
			if (bpf.x < bpg.x) {

				if (bpgPrevious == null) {
					ttf.addBreakpoint(bpf);
				}
				
				else {
					projection = computeProjectionPoint(bpf, bpgPrevious, bpg);

					// first f breakpoint, add projection point (transition from g to f)
					if (i == 0) {
						if (bpf.y < projection.y) {
							ttf.addBreakpoint(projection);
							ttf.addBreakpoint(bpf);
						}
						else if (bpf.y == projection.y) {
							ttf.addBreakpoint(bpf);
						}
					}

					// intermediate f breakpoints
					else if (i < f.breakpoints.size()-1) {
						if (bpf.y <= projection.y) {
							ttf.addBreakpoint(bpf);
						}
					}

					// last f breakpoint, add all remaining breakpoints of g and quit
					else {
						if (bpf.y < projection.y) {
							ttf.addBreakpoint(bpf);
							ttf.addBreakpoint(projection);
						}
						else if (bpf.y == projection.y) {
							ttf.addBreakpoint(bpf);
						}
						addRemainingBreakpoints(ttf, g, j);
						break;
					}
				}

				bpfPrevious = bpf;
				bpf = f.getBreakpoint(++i);
			}

			else if (bpf.x > bpg.x) {

				if (bpfPrevious == null) {
					ttf.addBreakpoint(bpg);
				}
				
				else {
					projection = computeProjectionPoint(bpg, bpfPrevious, bpf);

					// first g breakpoint, add projection point (transition from f to g)
					if (j == 0) {
						if (bpg.y < projection.y) {
							ttf.addBreakpoint(projection);
							ttf.addBreakpoint(bpg);
						}
						else if (bpg.y == projection.y) {
							ttf.addBreakpoint(bpg);
						}
					}				

					// intermediate g breakpoints
					else if (j < g.breakpoints.size()-1) {
						if (bpg.y <= projection.y) {
							ttf.addBreakpoint(bpg);
						}
					}

					// last g breakpoint, add all remaining breakpoints of f and quit
					else {
						if (bpg.y < projection.y) {
							ttf.addBreakpoint(bpg);
							ttf.addBreakpoint(projection);
						}
						else if (bpg.y == projection.y) {
							ttf.addBreakpoint(bpg);
						}
						addRemainingBreakpoints(ttf, f, i);
						break;
					}
				}
				
				bpgPrevious = bpg;
				bpg = g.getBreakpoint(++j);
			}
			
			else {				
				if (bpf.y <= bpg.y) {
					ttf.addBreakpoint(bpf);
				}
				else {
					ttf.addBreakpoint(bpg);
				}
				
				bpfPrevious = bpf;
				bpf = f.getBreakpoint(++i);
				bpgPrevious = bpg;
				bpg = g.getBreakpoint(++j);
			}
		}

		return ttf;
	}

	private static void addRemainingBreakpoints(TTF ttf, TTF h, int index) {
		while (index < h.breakpoints.size()) {
			ttf.addBreakpoint(h.breakpoints.get(index++));
		}
	}

	private static List<Breakpoint> computeIntersectionPointsOfTTFs(TTF f, TTF g) {
		List<Breakpoint> intersections = new ArrayList<>();

		Segment fseg = new Segment(f);
		Segment gseg = new Segment(g);

		Breakpoint fbp1 = null;
		Breakpoint fbp2 = null;
		Breakpoint gbp1 = null;
		Breakpoint gbp2 = null;
		Breakpoint intersection = null;

		Segment laggingSegment = null;
		boolean checkIntersection = false;
		boolean quit = false;

		while (!quit) {

			quit = true;
			checkIntersection = false;

			// f o--------------o
			// g                   o-----o
			if (gseg.bp1.x > fseg.bp2.x) {
				laggingSegment = fseg;
			}

			// f          o--------------o
			// g o-----o
			else if (fseg.bp1.x > gseg.bp2.x) {
				laggingSegment = gseg;
			}

			// f o--------------o
			// g     o-----o
			else if (gseg.bp1.x >= fseg.bp1.x && gseg.bp2.x <= fseg.bp2.x) {
				fbp1 = computeProjectionPoint(gseg.bp1, fseg.bp1, fseg.bp2);
				fbp2 = computeProjectionPoint(gseg.bp2, fseg.bp1, fseg.bp2);
				gbp1 = gseg.bp1;
				gbp2 = gseg.bp2;
				laggingSegment = gseg;
				checkIntersection = true;
			}

			// f o--------------o
			// g     o--------------o
			else if (gseg.bp1.x >= fseg.bp1.x && gseg.bp1.x <= fseg.bp2.x && gseg.bp2.x >= fseg.bp2.x) {
				fbp1 = computeProjectionPoint(gseg.bp1, fseg.bp1, fseg.bp2);
				fbp2 = fseg.bp2;
				gbp1 = gseg.bp1;
				gbp2 = computeProjectionPoint(fseg.bp2, gseg.bp1, gseg.bp2);
				laggingSegment = fseg;
				checkIntersection = true;
			}

			// f     o--------------o
			// g o--------------o
			else if (gseg.bp1.x <= fseg.bp1.x && gseg.bp2.x >= fseg.bp1.x && gseg.bp2.x <= fseg.bp2.x) {
				fbp1 = fseg.bp1;
				fbp2 = computeProjectionPoint(gseg.bp2, fseg.bp1, fseg.bp2);
				gbp1 = computeProjectionPoint(fseg.bp1, gseg.bp1, gseg.bp2);
				gbp2 = gseg.bp2;
				laggingSegment = gseg;
				checkIntersection = true;
			}

			// f     o-----o
			// g o--------------o
			else if (gseg.bp1.x <= fseg.bp1.x && gseg.bp2.x >= fseg.bp2.x) {
				fbp1 = fseg.bp1;
				fbp2 = fseg.bp2;
				gbp1 = computeProjectionPoint(fseg.bp1, gseg.bp1, gseg.bp2);
				gbp2 = computeProjectionPoint(fseg.bp2, gseg.bp1, gseg.bp2);
				laggingSegment = fseg;
				checkIntersection = true;
			}

			// compute intersection point
			if (checkIntersection && ((fbp1.y >= gbp1.y && fbp2.y < gbp2.y) || (fbp1.y < gbp1.y && fbp2.y > gbp2.y))) {
				intersection = computeIntesection(fseg, gseg);
				if (intersection != null) {
					intersections.add(intersection);
				}
			}

			if (!fseg.lastSegment && fseg.equals(laggingSegment)) {
				fseg.nextSegment();
				quit = false;
			}
			else if (!gseg.lastSegment && gseg.equals(laggingSegment)) {
				gseg.nextSegment();
				quit = false;
			}
		}
		return intersections;
	}

	private static Breakpoint computeProjectionPoint(Breakpoint bp, Breakpoint bp1, Breakpoint bp2) {
		if (bp.x < bp1.x || bp.x > bp2.x) {
			return null;
		}

		if (bp.x == bp1.x) {
			return bp1;
		}

		if (bp.x == bp2.x) {
			return bp2;
		}

		double deltaX = bp2.x - bp1.x;
		double deltaY = bp2.y - bp1.y;

		// segment is horizontal
		if (deltaY == 0) {
			return new Breakpoint(bp.x, bp1.y);
		}
		return new Breakpoint(bp.x, ((bp.x-bp1.x) * (deltaY/deltaX)) + bp1.y);
	}

	private static Breakpoint computeIntesection(Segment fseg, Segment gseg) {		
		// f and g have different domains => no intersection
		if ((fseg.bp1.x < gseg.bp1.x && fseg.bp2.x < gseg.bp1.x) || (gseg.bp1.x < fseg.bp1.x && gseg.bp2.x < fseg.bp1.x)) {
			return null;
		}

		// f and g are vertical segments => no intersection (overlap does not count)
		if (fseg.a == INFINITY && gseg.a == INFINITY) {
			return null;
		}

		double intersectX, intersectY;
		if (fseg.a == INFINITY) {
			intersectX = fseg.bp1.x;
			intersectY = gseg.a * intersectX + gseg.b;
		}
		else if (gseg.a == INFINITY) {
			intersectX = gseg.bp1.x;
			intersectY = fseg.a * intersectX + fseg.b;
		}
		else {
			intersectX = (gseg.b - fseg.b)/(fseg.a - gseg.a);
			intersectY = fseg.a * intersectX + fseg.b;
		}
		
		if (intersectY < 0) {
			return null;
		}
		
		return new Breakpoint(intersectX, intersectY);
	}

	public static void main(String[] args) throws IOException {
		TTF f = new TTF();
		//		f.addBreakpoint(10, 8);
		//		f.addBreakpoint(20, 15);
		//		f.addBreakpoint(30, 15);
		//		f.addBreakpoint(35, 5);
		//		f.addBreakpoint(45, 25);

		f.addBreakpoint(5, 15);
		f.addBreakpoint(15, 5);
		f.addBreakpoint(15, 17);
		f.addBreakpoint(25, 7);
		f.addBreakpoint(40, 7);
		f.addBreakpoint(40, 25);
		f.addBreakpoint(60, 5);

		TTF g = new TTF();
				g.addBreakpoint(5, 12);
				g.addBreakpoint(50, 12);

//		g.addBreakpoint(5, 25);
//		g.addBreakpoint(20, 10);
//		g.addBreakpoint(20, 15);
//		g.addBreakpoint(30, 5);
//		g.addBreakpoint(30, 20);
//		g.addBreakpoint(35, 15);
//		g.addBreakpoint(35, 20);
//		g.addBreakpoint(45, 10);

//		List<Breakpoint> intersections = computeIntersectionPointsOfTTFs(f, g);
//		for (Breakpoint breakpoint : intersections) {
//			System.out.printf("(%.2f, %.2f)\n", breakpoint.x, breakpoint.y);
//		}

		TTF z = link(f, f);
		System.out.println(z.toString());
	}

}