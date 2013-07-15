import java.util.*;

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
}
