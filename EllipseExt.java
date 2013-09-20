import java.awt.geom.Ellipse2D;
public class EllipseExt extends Ellipse2D.Double {
	private String name;
	public static void main(String[] args) {
		
	}

	public EllipseExt(PointExt a, PointExt b){
		super(a.getX()-(a.distance(b)), a.getY()-(a.distance(b)), (a.distance(b)*2), (a.distance(b)*2));
		this.setName(a.getName());
	}

	public EllipseExt(){}

	public void setName(String a){
		this.name = a;
	}
	public String getName(){
		return name;
	}

	 public boolean contains(double x, double y) {
		// Normalize the coordinates compared to the ellipse
		// having a center at 0,0 and a radius of 0.5.
		double ellw = getWidth();
      		if (ellw <= 0.0) {
      		return false;
		}
      	double normx = (x - getX()) / ellw - 0.5;
      	double ellh = getHeight();
		if (ellh <= 0.0) {
      		return false;
      	}
      	double normy = (y - getY()) / ellh - 0.5;
      	return (normx * normx + normy * normy) <= 0.25;
	}
	/**
	a = pointContainer
	q = query Point
	x = exclusion
	*/
	public PointList getMember(PointList a, PointExt q, PointList x){
		PointList result = new PointList();
		for (PointExt b : a) {
			if (this.contains(b)&&!b.getName().equals(q.getName())&&!x.checkExist(b)) {
				result.add(b);
			}
		}
		return result;
	}

	public PointList getMember(PointList a, PointExt q){
		PointList result = new PointList();
		for (PointExt b : a) {
			if (this.contains(b)&&!b.getName().equals(q.getName())) {
				if (!result.checkExist(b)) {
					result.add(b);
				}
			}
		}
		return result;
	}
}