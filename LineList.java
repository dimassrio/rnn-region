import java.util.*;
public class LineList extends ArrayList<LineExt> {
	public static void main(String[] args) {
		
	}
	
	public boolean intersectsLine(LineExt a){
		for (int i = 0;i<this.size() ; i++ ) {
			if (this.get(i).intersectsLine(a)) {
				return true;
			}
		}
		return false;
	}

	public boolean checkExist(LineExt a){
		for (int i=0;i<this.size();i++) {
			if (this.get(i).getName().equals(a.getName())) {
				return true;
			}
		}
		return false;
	}


	public LineExt getLineByName(String a){
		LineExt temp = null;
		for (int i=0; i<this.size(); i++ ) {
			if (this.get(i).getName().equals(a)) {
				temp = this.get(i);
			}
		}
		return temp;
	}
}