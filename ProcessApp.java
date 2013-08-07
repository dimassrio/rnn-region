import java.util.*;
import java.awt.geom.*;
import java.text.*;
public class ProcessApp{
	private int maxX = 0;
	private int maxY = 0;
	private PointExt queryPoint = new PointExt();
	private PointList pointContainer =  new PointList();
	private PointList sortedContainer = new PointList();
	private PointList vertexContainer = new PointList();
	private LineList perpendicularList = new LineList();
	private LineList sortedLine = new LineList();
	private LineList euclidianList = new LineList();
	private Path2D.Double polygon = new Path2D.Double();
	
	public ProcessApp(){

	}
	public ProcessApp(PointList pointContainer, PointExt queryPoint){
		this.pointContainer = pointContainer;
		this.queryPoint = queryPoint;
	}
	public ProcessApp(PointList pointContainer, PointExt queryPoint, int x, int y){
		this.pointContainer = pointContainer;
		this.queryPoint = queryPoint;
		this.maxX = x;
		this.maxY = y;
	}
	public static void main(String[] args) {
		
	}
	/**
	Encapsulation
	*/
	public LineList getLine(){return this.perpendicularList;}

	public PointList getVertex(){return this.vertexContainer;}

	public PointExt getQueryPoint(){return this.queryPoint;}

	public PointList getPointContainer(){return this.pointContainer;}

	public PointList getSortedContainer(){return this.sortedContainer;}

	public PointList getVertexContainer(){return this.vertexContainer;}

	public LineList getPerpendicularList(){return this.perpendicularList;}

	public LineList getSortedLine(){return this.sortedLine;}

	public LineList getEuclidianList(){return this.euclidianList;}

	public Path2D.Double getPolygon(){return this.polygon;}

	public void setMax(int x, int y){
		this.maxX = x;
		this.maxY = y;
	}
	public void setPointContainer(PointList pointContainer){
		this.pointContainer = pointContainer;	
	}
	public void setQueryPoint(PointExt queryPoint){
		this.queryPoint = queryPoint;
	}
	
	/**
	Print
	*/
	public void printContainer(){
		this.getPointContainer().printPoint();
	}
	public void printQueryPoint(){
		System.out.print("Query Point : "+this.getQueryPoint().printPoint());
	}
	/**
	Process
	*/
	public void sortingContainer(){
		if (this.getQueryPoint().getName() == "") {
			return;
		}
		this.getSortedContainer().add(this.getQueryPoint());
		for (int i = 0;i<this.getPointContainer().size();i++ ) {			
			if (!(this.getPointContainer().get(i).getName()==this.getQueryPoint().getName())) {
				this.getSortedContainer().add(this.getPointContainer().get(i));
			}
		}
		this.bubbleSort(this.getSortedContainer());
	}

	public void bubbleSort(PointList a){
		int n = a.size();
		boolean swapped = false;
		for (int i=0;i<n-1 ;i++ ) {
			for (int j = i+1;j<n ;j++) {
				if (a.get(i).distance(a.get(0))>a.get(j).distance(a.get(0))) {
					PointExt temp = a.get(i);
					a.set(i, a.get(j));
					a.set(j, temp);
				}
			}
		}
	}


	public LineExt createBisector(PointExt a, PointExt b){
		LineExt line = new LineExt();
		PointExt tempPoint = new PointExt(((b.getX()+a.getX())/2),((b.getY()+a.getY())/2));
			if (a.getX() == b.getX()) { 
				line.setLine(0, tempPoint.getY(), this.maxX, tempPoint.getY());
			}else if(a.getY() == b.getY()) {
				line.setLine(tempPoint.getX(), 0, tempPoint.getX(), this.maxY);
			}else{
				LineExt tempLine = null;
				if (a.getX()<b.getX()) {
					tempLine = new LineExt(a,b);
				}else{
					tempLine = new LineExt(b,a);
				}
				double m = -1 / tempLine.getM();
				double y1 = 0;
				double y2 = maxY;
				double x1 = (((y1-tempPoint.getY())+(m*tempPoint.getX()))/m);
				double x2 = (((y2-tempPoint.getY())+(m*tempPoint.getX()))/m);
				line.setLine(x1, y1, x2, y2);
			}
		return line;
	}

	public void checkVertex(){
		if (this.getPerpendicularList().size()>2) {
			for (int i=0; i<this.getPerpendicularList().size()-1; i++) {
				for(int j = i+1; j<this.getPerpendicularList().size(); j++){
					if (this.getPerpendicularList().get(i).intersectsLine(this.getPerpendicularList().get(j))) {
						this.getVertexContainer().add(this.getIntersectionPoint(this.getPerpendicularList().get(i), this.getPerpendicularList().get(j)));
					}
				}
			}
		}else{
			return;
		}	
	}


	public void createPolygon(){
		double x = (double) this.getVertexContainer().get(0).getX();
		double y = (double) this.getVertexContainer().get(0).getY();
		this.getPolygon().moveTo(x, y);
		for (int i = 1;i<this.getVertexContainer().size() ;i++ ) {
			x = (double) this.getVertexContainer().get(i).getX();
			y = (double) this.getVertexContainer().get(i).getY();
			this.getPolygon().lineTo(x, y);
		}
		this.getPolygon().closePath();

	}


	public void startProcess(){
		this.sortingContainer();
		this.getSortedContainer().printLine();
		System.out.println("Query Point : "+this.getQueryPoint());
		/*this.createLine(this.getSortedContainer());*/
		/*this.checkVertex();*/
		this.lineProcess();
		//this.createPolygon();
	}
 
	public void lineProcess(){
		PointExt a = this.getQueryPoint();
		PointExt b = null;
		LineExt tempLine = null;
		for (int i = 1; i<this.getSortedContainer().size();i++ ) {
			b = this.getSortedContainer().get(i);
			if (this.getPerpendicularList().size()<3) {
				tempLine = this.createBisector(a, b);
				tempLine.setName(b.getName());
				boolean flag = true;
				for (int j = 0;j<this.getPerpendicularList().size() ;j++ ) {
					if (this.checkParalel(tempLine, this.getPerpendicularList().get(j))) {
						flag = false;
					}
				}
				//System.out.println(flag);
				if(flag){
					this.getPerpendicularList().add(tempLine);
				}
				if(this.getPerpendicularList().size()==3){
					if (this.findClosedPolygon()) {
						this.createPolygon();	
					}	
				}
			}else{
				if (this.findClosedPolygon()) {
					this.createPolygon();
				}else{
					tempLine = this.createBisector(a, b);
					tempLine.setName(b.getName());
					boolean flag = true;
					for (int j = 0;j<this.getPerpendicularList().size() ;j++ ) {
						if (this.checkParalel(tempLine, this.getPerpendicularList().get(j))) {
							flag = false;
						}
					}
					if(flag){
						this.getPerpendicularList().add(tempLine);
					}
					if (this.findClosedPolygon()) {
						this.createPolygon();
					}
				}
			}
		}
		
		
	}

	public boolean findClosedPolygon(){
		boolean closed = false;
		if (this.checkParalel(this.getPerpendicularList().get(0), this.getPerpendicularList().get(this.getPerpendicularList().size()-1))) {
			closed = false; // check paralel atau tidaknya garis pertama dan terakhir.
		}else{
			if (this.checkInitPosition()) {
				closed = true;
			}
		}
		return closed;
	}

	public boolean checkInitPosition(){
		// Line Sort berdasarkan mana yang saling berpotongan
		LineExt tempLine = null;
		for (LineExt temp : this.getPerpendicularList()) {
			this.getSortedLine().add(temp);
		}
		for (int i=0;i<this.getSortedLine().size()-1;i++) {
			for (int j=0;j<this.getSortedLine().size();j++ ) {
				if (this.getSortedLine().get(i).intersectsLine(this.getSortedLine().get(j))) {
					tempLine = this.getSortedLine().get(j);
					this.getSortedLine().remove(j);
					this.getSortedLine().add(i, tempLine);
				}
			}
		}
		
		this.generateVertex();
		
		//System.out.println("Line sorted vertex gained : "+this.getVertexContainer().size());

		PointList flag = new PointList();
		LineExt checkLine = null;
		PointExt checkPoint = null;
		boolean flagVertex = false;
		// Debug Here
		for ( PointExt temp : this.getVertexContainer() ) {
			checkLine = new LineExt(temp, this.getQueryPoint()); // Garis antara query point dengan vertex
			checkLine.setName("l("+temp.getName()+":"+this.getQueryPoint().getName()+")");
			flagVertex = false;

			for (LineExt bisect : this.getPerpendicularList()) { // Untuk setiap bisector
				if (checkLine.intersectsLine(bisect)) { // Check apakah melewati suatu bisector
					checkPoint = this.getIntersectionPoint(checkLine, bisect);
					double ax = this.roundDouble(checkPoint.getX());
					double ay = this.roundDouble(checkPoint.getY());
					double bx = this.roundDouble(temp.getX());
					double by = this.roundDouble(temp.getY());
					if ((ax==bx)&&(ay==by)) {
						flagVertex = false;
						break;
					}else{
						flagVertex = true;
						break;
					}
				}
			}
			/*for (LineExt bisect: this.getPerpendicularList() ) { // untuk setiap bisector
				System.out.println(checkLine.getName()+"<>l("+bisect.getName()+")");
				if (checkLine.intersectsLine(bisect)) { // check apakah garis melewati bisector
					checkPoint = this.getIntersectionPoint(checkLine, bisect); // check lokasi intersection
				//	System.out.println(checkPoint.printPoint());
					for (PointExt check:this.getVertexContainer()) { // untuk setiap vertex
						if ((checkPoint.getX()==check.getX())&&(checkPoint.getY()==check.getY())) { // bila posisi intersection terletk pada vertex abaikan
							flagVertex = false;
						}else{
							flagVertex = true;
						}
					}
					
				}*/

			if (flagVertex) {
				flag.add(temp);
			}
		}
		// To Here
		//System.out.println("vertex gained : "+this.getVertexContainer().size());
		for (PointExt temp : flag ) {
			if (this.getVertexContainer().contains(temp)) {
				int i = this.getVertexContainer().indexOf(temp);
				System.out.println(this.getVertexContainer().get(i).getName()+" removed.");
				this.getVertexContainer().remove(i);

			}
		}

		Path2D.Double path = new Path2D.Double();
		path.moveTo(this.getVertexContainer().get(0).getX(), this.getVertexContainer().get(0).getY());
		for (int i = 1;i<this.getVertexContainer().size();i++) {
			path.lineTo(this.getVertexContainer().get(i).getX(), this.getVertexContainer().get(i).getY());
		}
		path.closePath();
		if (path.contains(this.getQueryPoint())) {
			return true;
		}else{
			return false;
		}
	}

	public void generateVertex(){
		for (int i = 0; i<this.getPerpendicularList().size()-1; i++) {
			for (int j = i+1; j<this.getPerpendicularList().size() ; j++) {	
				if (this.getPerpendicularList().get(i).intersectsLine(this.getPerpendicularList().get(j))) {
					String name = "v("+this.getPerpendicularList().get(i).getName()+":"+this.getPerpendicularList().get(j).getName()+")";
					PointExt p = new PointExt(name, this.getIntersectionPoint(this.getPerpendicularList().get(i), this.getPerpendicularList().get(j)));
					this.getVertexContainer().add(name, p);
				}/*else if (!checkParalel(this.getPerpendicularList().get(i), this.getPerpendicularList().get(j))) {
					PointExt p = new PointExt(this.getPerpendicularList().get(i).getP1().getX(), this.getPerpendicularList().get(i).getP1().getY());
					p.setDraw(false);
					this.getVertexContainer().add(p);
					p = new PointExt(this.getPerpendicularList().get(i).getP2().getX(), this.getPerpendicularList().get(i).getP2().getY());
					p.setDraw(false);
					this.getVertexContainer().add(p);
					p = new PointExt(this.getPerpendicularList().get(j).getP1().getX(), this.getPerpendicularList().get(j).getP1().getY());
					p.setDraw(false);
					this.getVertexContainer().add(p);
					p = new PointExt(this.getPerpendicularList().get(j).getP2().getX(), this.getPerpendicularList().get(j).getP2().getY());
					p.setDraw(false);
					this.getVertexContainer().add(p);
				}*/
			}
		}
	}

/**
	
*/
	public boolean checkParalel(LineExt a, LineExt b){
		if (a.getM() == b.getM()) {
			return true;
		}
		return false;
	}

	public double roundDouble(double d){
		DecimalFormat a = new DecimalFormat("#.####");
		return Double.valueOf(a.format(d));
	}

	public PointExt getIntersectionPoint(LineExt lineA, LineExt lineB){
			double x1 = lineA.getX1();
			double y1 = lineA.getY1();
			double x2 = lineA.getX2();
			double y2 = lineA.getY2();

			double x3 = lineB.getX1();
			double y3 = lineB.getY1();
			double x4 = lineB.getX2();
			double y4 = lineB.getY2();

			PointExt p = null;

			double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
			if (d != 0) {
				double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
				double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

				p = new PointExt(xi, yi);

			}
			return p;
	}

}
