import java.util.*;
import java.awt.geom.*;

class PointList extends ArrayList<PointExt> {

	public PointList(PointExt[] a){
		for (int i=0;i<a.length ;i++ ) {
			this.add(a[i]);
		}
	}
	public PointList(){
		super();
	}

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

	public void printPoint(boolean test){
		if (test) {
			for (int i=0;i<this.size();i++) {
				System.out.println(this.get(i).getName()+" ("+this.get(i).getX()+","+this.get(i).getY()+") ");
			}
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
			if (this.get(i).getName().equals(a)) {
				temp = this.get(i);
			}
		}
		return temp;
	}

	public void add(String name, Point2D p){
		PointExt data = new PointExt(name, p);
		super.add(data);
	}

	public PointExt[] toArray(){
		PointExt[] p = new PointExt[this.size()];
		for (int i = 0;i<this.size();i++) {
			p[i] = this.get(i);
		}
		return p;
	}
	public PointExt getNext(PointList visited){
		PointExt result = new PointExt();
		result = this.get(0);
		for ( PointExt p : this ) {
			if (!visited.checkExist(p)) {
				result = p;
				break;
			}
		}
		return result;
	}
	public boolean checkExist(PointExt p){
		boolean stat = false;
		if (this.contains(p)) {
			stat = true;
		}else if (this.indexOf(p) != -1) {
			stat = true;
		}else {
			for (PointExt z : this) {
				if (p.getName().equals(z.getName())) {
					stat = true;
					break;
				}
				if (z.getX()==p.getX()&&z.getY()==p.getY()) {
					stat = true;
					break;
				}
			}
		}

		return stat;
	}
}
