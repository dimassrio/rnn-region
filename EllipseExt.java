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

}