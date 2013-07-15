import java.awt.geom.*;
public class PointExt extends Point2D.Double {
	public String name;
	public boolean selected = false;

	public PointExt(){
		this.name = "";
		this.setLocation(0,0);
	}
	public PointExt(String name, Point2D point){
		this.name = name;
		this.setLocation(point);
	}
	public PointExt(Point2D point){
		this.name = "temp";
		this.setLocation(point);
	}
	public PointExt(String name, double pointX, double pointY){
		this.name = name;
		this.setLocation(pointX, pointY);	
	}
	public PointExt(double pointX, double pointY){
		this.name = "temp";
		this.setLocation(pointX, pointY);	
	}

	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return this.name;
	}

	public String printPoint(){
		return "<"+this.name+" ("+this.getX()+","+this.getY()+")>";
	}

	public Object[] getObject(){
		Object[] row = {this.name, this.getX(), this.getY()};
		return row;
	}

	public boolean getSelected(){
		return this.selected;
	}

	public void changeSelected(){
		if (this.selected) {
			this.selected = false;
		}else{
			this.selected = true;
		}
	}

	public void changeSelected(boolean status){
		this.selected = status;
	}

	public static void main(String[] args) {
		
	}

}