import javax.swing.*;
import java.awt.geom.*;

class LineExt extends Line2D.Double {
	private String name;
	public LineExt(PointExt a, PointExt b){
		super(a.getX(), a.getY(), b.getX(), b.getY());
	}
	public static void main(String[] args) {
		
	}
}