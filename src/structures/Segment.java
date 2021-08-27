package structures;

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