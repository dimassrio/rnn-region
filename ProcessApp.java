import java.util.*;
import java.awt.geom.*;
import java.awt.Polygon;
import java.text.*;
public class ProcessApp{
	private int maxX = 0;
	private int maxY = 0;
	private boolean polygonStat = false;
	private PointExt queryPoint = new PointExt();
	private PointList pointContainer =  new PointList();
	private PointList sortedContainer = new PointList();
	private PointList vertexContainer = new PointList();
	private PointList sortedVertex = new PointList();
	private LineList perpendicularList = new LineList();
	private LineList sortedLine = new LineList();
	private LineList euclidianList = new LineList();
	private Path2D.Double polygon = new Path2D.Double();
	private Polygon poly = new Polygon();
	/**
	CONSTRUCTOR
	**/
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
	/**
	MAIN
	*/
	public static void main(String[] args) {
		
	}
	/**
	Encapsulation
	*/
	public boolean getPolygonStat(){return this.polygonStat;}

	public PointList getVertex(){return this.vertexContainer;}

	public PointExt getQueryPoint(){return this.queryPoint;}

	public PointList getPointContainer(){return this.pointContainer;}

	public PointList getSortedContainer(){return this.sortedContainer;}

	public PointList getVertexContainer(){return this.vertexContainer;}

	public PointList getSortedVertex(){return this.sortedVertex;}

	public LineList getPerpendicularList(){return this.perpendicularList;}

	public LineList getLine(){return this.perpendicularList;}

	public LineList getSortedLine(){return this.sortedLine;}

	public LineList getEuclidianList(){return this.euclidianList;}

	public Path2D.Double getPolygon(){return this.polygon;}

	public Polygon getPoly(){return this.poly;}

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
	PRINT POINT RELATED
	*/
	public void printContainer(){
		this.getPointContainer().printPoint();
	}
	public void printQueryPoint(){
		System.out.print("Query Point : "+this.getQueryPoint().printPoint());
	}
	/**
	PROCESS
	*/
		// SORTING
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

	// STEP 1 PROCESS
	public void startProcess(){
		this.sortingContainer();
		this.lineProcess();
		//this.getVertexContainer().printLine();
		/*this.jarvisMarch(this.getVertexContainer());*/
		this.getSortedVertex().printLine();
		/*System.out.println(getPolygonStat());*/
		if (this.getPolygonStat()) {
			this.createPolygon(this.getSortedVertex());
			this.createPoly(this.getSortedVertex());	
		}
		
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
						this.polygonStat = true;		
					}	
				}
			}else{
				if (this.findClosedPolygon()){
					this.polygonStat = true;
				}else{
					
					tempLine = this.createBisector(a, b);
					tempLine.setName(b.getName());
					boolean flag = true;
					for (int j = 0;j<this.getPerpendicularList().size() ;j++ ){
						if (this.checkParalel(tempLine, this.getPerpendicularList().get(j))) {
							flag = false;
						}
					}
					if(flag){
						this.getPerpendicularList().add(tempLine);
					}
					
					if (this.findClosedPolygon()) {
						this.polygonStat = true;
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
					}else{
						flagVertex = true;
						break;
					}
				}
			}

			if (flagVertex) {
				flag.add(temp);
			}
		}
		for (PointExt temp : flag ) {
			if (this.getVertexContainer().contains(temp)) {
				int i = this.getVertexContainer().indexOf(temp);
				this.getVertexContainer().remove(i);

			}
		}
		this.getSortedVertex().clear();
		this.jarvisMarch(this.getVertexContainer());
		Path2D.Double path = new Path2D.Double();
		path.moveTo(this.getSortedVertex().get(0).getX(), this.getSortedVertex().get(0).getY());
		for (int i = 1;i<this.getSortedVertex().size();i++) {
			path.lineTo(this.getSortedVertex().get(i).getX(), this.getSortedVertex().get(i).getY());
		}
		path.closePath();
		if (path.contains(this.getQueryPoint())) {
			return true;
		}else{
			return false;
		}
	}
	
	/**
	Draw related.
	*/
	public void createPolygon(PointList a){
		double x = (double) a.get(0).getX();
		double y = (double) a.get(0).getY();
		this.getPolygon().moveTo(x, y);
		for (int i = 1;i<a.size() ;i++ ) {
			x = (double) a.get(i).getX();
			y = (double) a.get(i).getY();
			this.getPolygon().lineTo(x, y);
		}
		x = (double) a.get(0).getX();
		y = (double) a.get(0).getY();
		this.getPolygon().moveTo(x, y);
		this.getPolygon().closePath();
	}

	public void createPoly(PointList a){
		/*System.out.println(a.size());*/
		int[] xPoint = new int[a.size()];
		int[] yPoint = new int[a.size()];
		int n = a.size();
		for (int i=0; i<a.size(); i++ ) {
			xPoint[i] = (int) a.get(i).getX();
			yPoint[i] = (int) a.get(i).getY();
		}
		poly = new Polygon(xPoint, yPoint, n);
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

	public void generateVertex(){
		for (int i = 0; i<this.getPerpendicularList().size()-1; i++) {
			for (int j = i+1; j<this.getPerpendicularList().size() ; j++) {	
				if (this.getPerpendicularList().get(i).intersectsLine(this.getPerpendicularList().get(j))) {
					String name = "v("+this.getPerpendicularList().get(i).getName()+":"+this.getPerpendicularList().get(j).getName()+")";
					PointExt p = new PointExt(name, this.getIntersectionPoint(this.getPerpendicularList().get(i), this.getPerpendicularList().get(j)));
					if (!this.getVertexContainer().contains(p)) {
						this.getVertexContainer().add(name, p);
					}
				}else if (!checkParalel(this.getPerpendicularList().get(i), this.getPerpendicularList().get(j))) {
					PointExt[] p = new PointExt[2];
					p[0] = new PointExt(this.getPerpendicularList().get(i).getP1());
					p[1] = new PointExt(this.getPerpendicularList().get(i).getP2());

					LineExt l = new LineExt(p[0], this.getQueryPoint());
					double[] d = new double[2];
					d[0] = this.angleOfTwoLines(this.getPerpendicularList().get(i), l);
					l = new LineExt(p[1], this.getQueryPoint());
					d[1] = this.angleOfTwoLines(this.getPerpendicularList().get(i), l);
					
					if (d[0]<d[1]) {
						if (!this.getVertexContainer().contains(p[0])) {
							this.getVertexContainer().add(p[0]);
						}
					}else{
						if (!this.getVertexContainer().contains(p[1])) {
							this.getVertexContainer().add(p[1]);
						}
					}

					p[0] = new PointExt(this.getPerpendicularList().get(j).getP1());
					p[1] = new PointExt(this.getPerpendicularList().get(j).getP2());

					l = new LineExt(p[0], this.getQueryPoint());
					d[0] = this.angleOfTwoLines(this.getPerpendicularList().get(i), l);
					l = new LineExt(p[1], this.getQueryPoint());
					d[1] = this.angleOfTwoLines(this.getPerpendicularList().get(i), l);
					
					if (d[0]<d[1]) {
						if (!this.getVertexContainer().contains(p[0])) {
							this.getVertexContainer().add(p[0]);
						}
					}else{
						if (!this.getVertexContainer().contains(p[1])) {
							this.getVertexContainer().add(p[1]);
						}
					}

				}
			}
		}
	}

	/**
	Utilities
	*/

	public void jarvisMarch(PointList a){
		// converting to array to reduce overhead in operation
		PointExt[] q = a.toArray();
		
		int currPoint = 0;	
		int minPoint = 0;
		int maxPoint = 0;
		int minAngle = 0;
		int maxAngle = 0;
		int[] usedPoint = new int[q.length];

		for (int i=0;i<usedPoint.length ; i++) {
			usedPoint[i] = -1;
		}

		for (int i=0; i<q.length ; i++ ) {
			if (q[i].getY()>q[maxPoint].getY()) {
				maxPoint = i; // Find Max Point *highest y
			}
		}

		for (int i=0;i<q.length ; i++ ) {
			if (q[i].getY()<q[minPoint].getY()) {
				minPoint = i;
			}
		}

		this.addUsedPoint(usedPoint, minPoint);
		//System.out.println(q[minPoint].getName()+" : "+q[maxPoint].getName());
		currPoint = minPoint;

		while(currPoint!=maxPoint){
			maxAngle = currPoint;
			for (int i=0;i<q.length ;i++ ) {
				//System.out.println("Angle : "+findAngle(q[currPoint], q[maxAngle])+" : "+findAngle(q[currPoint], q[i]));
				if ((findAngle(q[currPoint], q[maxAngle])<findAngle(q[currPoint],q[i])) && (notUsed(usedPoint, i)||i==maxPoint) && (findAngle(q[currPoint], q[i])<=180)) {
					maxAngle = i;
					//System.out.println("maxAngle : "+q[i].getName());
				}
			}
			currPoint = maxAngle;
			this.addUsedPoint(usedPoint, currPoint);
		}
		//System.out.println("Left Side Completed");
		//currPoint = minPoint;
		int z = 0;
		currPoint = maxPoint;

		while (currPoint!=minPoint) {
			minAngle = minPoint;
			for (int i = 0; i<q.length ; i++ ) {
				if ((findAngle(q[currPoint], q[minAngle])<findAngle(q[currPoint], q[i])) && (notUsed(usedPoint, i)) &&(findAngle(q[currPoint], q[i])>=180)) {
					minAngle = i;
				}
			}
			currPoint = minAngle;
			this.addUsedPoint(usedPoint, currPoint);
		}

		for (int i = 0;i<usedPoint.length ;i++ ) {
			this.getSortedVertex().add(q[usedPoint[i]]);
		}
		/*while (currPoint!=maxPoint) {
			minAngle = maxPoint;
			for (int i=0;i<q.length ;i++ ) {
				if (q[i].getName().equals("v(E:D)")) {
					System.out.println(q[currPoint].getName());
					System.out.println("Angle : "+findAngle(q[currPoint], q[minAngle])+" : "+findAngle(q[currPoint], q[i])+":"+(notUsed(usedPoint,i)));
				}
				if ((findAngle(q[currPoint], q[minAngle])>findAngle(q[currPoint], q[i])) && (notUsed(usedPoint,i)||i==maxPoint) && (findAngle(q[currPoint], q[i])>=0)) {
					minAngle = i;
				}
			}
			currPoint = minAngle;
			this.addUsedPoint(usedPoint, currPoint);
			
		}
		if (usedPoint.length % 2 == 0) {
			System.out.println("true");
			for (int i=0;i<=(usedPoint.length/2) ; i++) {
				this.getSortedVertex().add(q[usedPoint[i]]);
			}
			for (int i = usedPoint.length-1; i>(usedPoint.length/2) ; i-- ) {
				this.getSortedVertex().add(q[usedPoint[i]]);
			}
		}else{
			System.out.println("false");
			for (int i=0;i<=(usedPoint.length/2) ; i++) {
				this.getSortedVertex().add(q[usedPoint[i]]);
			}

			for (int i = usedPoint.length-1; i>(usedPoint.length/2) ; i-- ) {
				this.getSortedVertex().add(q[usedPoint[i]]);
			}
		}*/
		/*for (int i=0;i<usedPoint.length ;i++ ) {
			this.getSortedVertex().add(q[usedPoint[i]]);
		}*/
	}

	public boolean notUsed(int[] arr, int a){
		for (int i = 0; i<arr.length; i++ ) {
			if (arr[i]==a) {
				return false;
			}
		}
		return true;
	}

	public void addUsedPoint(int[] arr, int a){
		int i = 0;
		while ((arr[i]!=-1)&&(i<arr.length)) {
			i++;
			if(i == arr.length){
				break;
			}
		}
		if (i<arr.length) {
			arr[i] = a;
			//System.out.println(this.getVertexContainer().get(a).getName()+" added at index "+i);
		}
		
	}

	public double findAngle(double x1, double x2, double y1, double y2){
		double deltaX = (double) (x2-x1);
		double deltaY = (double) (y2-y1);
		double angle;
		if (deltaX == 0 && deltaY == 0) {
			return 0;
		}
		angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
		if (angle < 0) {
			angle += 360.0;
		}
		return angle;
	}	

	public double findAngle(PointExt p1, PointExt p2){
		double angle = this.findAngle(p1.getX(), p2.getX(), p1.getY(), p2.getY());
		return angle;
	}

	public static double angleOfTwoLines(LineExt line1, LineExt line2){
		double angle1 = Math.atan2(line1.getY1() - line1.getY2(),
								   line1.getX1() - line1.getX2());
		double angle2 = Math.atan2(line2.getY1() - line2.getY2(),
								   line2.getX1() - line2.getX2());
		angle1 = angle1-angle2;
		if (angle1<0) {
			angle1 = -angle1;
		}
		return angle1;
	}

	public boolean contains(int[] a, int idx){
		for (int i=0; i<a.length ; i++ ) {
			if (a[i]==idx) {
				return false;
			}
		}
		return true;
	}

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