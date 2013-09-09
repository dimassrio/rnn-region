import javax.swing.*;
import java.awt.geom.*;

class LineExt extends Line2D.Double {
	private String name;
	private double m;
	public int vertex = 0;
	public LineExt(PointExt a, PointExt b){
		super(a.getX(), a.getY(), b.getX(), b.getY());
		this.m = ((b.getY()-a.getY())/(b.getX()-a.getX()));
	}
	public LineExt(){

	}
	public LineExt(Point2D a, PointExt b){
		super(a.getX(), a.getY(), b.getX(), b.getY());
		this.m = ((b.getY()-a.getY())/(b.getX()-a.getX()));
	}

	public LineExt(PointExt a, Point2D b){
		super(a.getX(), a.getY(), b.getX(), b.getY());
		this.m = ((b.getY()-a.getY())/(b.getX()-a.getX()));
	}

	public static void main(String[] args) {
		
	}
	public double getM(){
		return ((this.getP2().getY()-this.getP1().getY())/(this.getP2().getX()-this.getP1().getX()));
	}
	public void setLine(PointExt a, PointExt b){
		super.setLine(a.getX(), a.getY(), b.getX(), b.getY());
		this.m = ((b.getY()-a.getY())/(b.getX()-a.getX()));
	}

	public void setLine(double x1, double y1, double x2, double y2){
		super.setLine(x1, y1, x2, y2);
		this.m = (y2-y1)/(x2-x1);
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return this.name;
	}

}