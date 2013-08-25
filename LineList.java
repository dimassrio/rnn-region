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

	public void printPoint(){
		for (LineExt line : this ) {
			System.out.println(line.getName()+" "+line.vertex);
		}
	}
}