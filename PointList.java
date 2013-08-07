import java.util.*;
import java.awt.geom.*;

class PointList extends ArrayList<PointExt> {

	public static void main(String[] args) {
			
	}	

	public Object[][] getDataModel(){
		Object[][] data = new Object[this.size()][3];
		for (int i = 0;i<this.size() ;i++ ) {
			data[i][0] = this.get(i).getName();
			data[i][1] = this.get(i).getX();
			data[i][2] = this.get(i).getY();
		}
		return data;
	}

	public String[] getComboBox(){
		String[] data = new String[this.size()];
		for (int i = 0;i<this.size() ; i++ ) {
			data[i] = this.get(i).getName();
		}
		return data;
	}

	public void printPoint(){
		for (int i=0;i<this.size();i++) {
			System.out.println(this.get(i).getName()+" ("+this.get(i).getX()+","+this.get(i).getY()+") "+this.get(i).distance(this.get(0)));
		}
	}

	public void printLine(){
		System.out.print("[ ");
		for (int i=0;i<this.size();i++) {
			
			System.out.print(this.get(i).getName()+", ");
			
		}	
		System.out.print(" ]");
	}

	public PointExt getPointByName(String a){
		PointExt temp = null;
		for (int i=0; i<this.size(); i++ ) {
			if (this.get(i).getName()==a) {
				temp = this.get(i);
			}
		}
		return temp;
	}

	public void add(String name, Point2D p){
		PointExt data = new PointExt(name, p);
		super.add(data);
	}
}
