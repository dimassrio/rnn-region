import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D;
import java.awt.Polygon;
import java.util.List;
import java.util.ArrayList;
import java.text.DecimalFormat;
public class ProcessApp{
	// Initialization
	boolean debug = true;
	boolean initialRegion = false;
	private int maxX = 0;
	private int maxY = 0;

	private PointExt queryPoint = new PointExt();
	private PointList pointContainer = new PointList();
	private PointList sortedContainer = new PointList();

	private PointList vertexContainer = new PointList();
	private PointList sortedVertex = new PointList();

	private LineList bisectContainer = new LineList();
	private LineList sortedBisect = new LineList();

	private Polygon polygon = new Polygon();

	public static void main(String[] args) {
		
	}
	/**
	CONSTRUCTOR
	*/

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
	ENCAPSULATION
	*/
	public LineList getBisect(){
		return bisectContainer;
	}
	public PointList getVertex(){
		return vertexContainer;
	}
	public Polygon getPolygon(){
		return polygon;
	}
	/**
	HANDLING
	*/
	public void startProcess(){
		this.sortPoint();
		this.initialProcessing();

		//System.out.println(vertexContainer.size());
		//vertexContainer.printPoint(true);

		bisectContainer.printPoint();
		if (initialRegion) {
			this.generatePolygon(sortedVertex);						
		}

		
	}
	/**
	PROCESSING
	*/

	public void sortPoint(){
		if (queryPoint.getName() == "") {
			return;
		}

		sortedContainer.add(queryPoint);

		for (PointExt temp : pointContainer) {
			if (!(temp.getName() ==queryPoint.getName())) {
				sortedContainer.add(temp);
			}
		}

		int n = sortedContainer.size();
		PointExt temp;
		for(int i = 0; i<n-1; i++){
			for (int j = i+1; j<n ; j++ ) {
				if (sortedContainer.get(i).distance(sortedContainer.get(0))>sortedContainer.get(j).distance(sortedContainer.get(0))) {
					temp = sortedContainer.get(i);
					sortedContainer.set(i, sortedContainer.get(j));
					sortedContainer.set(j, temp);

				}
			}
		}
	}

	public void advancedProcessing(){
		PointList peerscz = new PointList();
		Ellipse2D.Double cz = null;
		for (PointExt temp : sortedVertex ) {
			cz = new Ellipse2D.Double(temp.getX(), temp.getY(), temp.distance(queryPoint), temp.distance(queryPoint));
			for (PointExt tempPoint : sortedContainer ) {
				if (cz.contains(tempPoint)) {
					if (peerscz.indexOf(tempPoint)==-1) {
						peerscz.add(tempPoint);
					}
				}
			}
		}
		LineExt tempBisect = null;
		for (PointExt temp : peerscz) {
			tempBisect = createBisector(queryPoint, temp);
			if (bisectContainer.indexOf(tempBisect)==-1) {
					bisectContainer.add(tempBisect);
			}
		}




	}

	public void initialProcessing(){
		PointExt a = queryPoint;
		PointExt b = null;
		LineExt tempBisect = null;
		int i = 1;

		while(!initialRegion){
			//System.out.println(bisectContainer.size());
			if (sortedContainer.size()<4) {
				for (int j=1;j<sortedContainer.size();j++ ) {
					b = sortedContainer.get(j);
					tempBisect = createBisector(a, b);
					tempBisect.setName(b.getName());
					bisectContainer.add(tempBisect);
				}
				break;
			}else if (bisectContainer.size()<3) {
				b = sortedContainer.get(i);
				i++;
				tempBisect = createBisector(a, b);
				tempBisect.setName(b.getName());

				boolean flag = true;
				for (LineExt tempLine : bisectContainer ) {
					if (checkParalel(tempBisect, tempLine)) {
						flag = false;
					}
				}
				//System.out.println(flag);
				if (flag) {
					bisectContainer.add(tempBisect);
				}
			}else{
				if (findClosedPolygon()) {
					initialRegion = true;
				}else if(i<sortedContainer.size()){
					b = sortedContainer.get(i);
					i++;
					tempBisect = createBisector(a, b);
					tempBisect.setName(b.getName());

					boolean flag = true;
					for (LineExt tempLine : bisectContainer ) {
						if (checkParalel(tempBisect, tempLine)) {
							flag = false;
						}
					}
					//System.out.println(flag);
					if (flag) {
						bisectContainer.add(tempBisect);
					}
				}else{
					break;
				}
				
			}
		}
		//System.out.println(initialRegion);
	}

	public boolean findClosedPolygon(){
		boolean closed = false;
		if (checkParalel(bisectContainer.get(0), bisectContainer.get(bisectContainer.size()-1))) {
			return closed;
		}else{
			LineExt tempLine = null;
			generateVertex();
			PointList flag = new PointList();
			LineExt checkLine = null;
			PointExt checkPoint = null;
			boolean flagVertex = false;
			// Vertex Checking
			for(PointExt temp : vertexContainer){
				checkLine = new LineExt(temp, queryPoint); // Garis antara query point dengan vertex
				checkLine.setName("l("+temp.getName()+":"+this.queryPoint.getName()+")");
				flagVertex = false;

				for(LineExt bisect : bisectContainer){
					
					if (checkLine.intersectsLine(bisect)) {
						checkPoint = getIntersectionPoint(checkLine, bisect);
						
						double ax = this.roundDouble(checkPoint.getX());
						double ay = this.roundDouble(checkPoint.getY());
						double bx = this.roundDouble(temp.getX());
						double by = this.roundDouble(temp.getY());
						//System.out.println(temp.getName());
						if ((ax == bx)&&(ay==by)) {
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
				if (vertexContainer.contains(temp)) {
					int i = vertexContainer.indexOf(temp);
					vertexContainer.remove(i);
				}
			}

			//System.out.println("1");
			// Jarvis March
			int currPoint = 0, minPoint = 0, maxPoint = 0, minAngle = 0, maxAngle = 0;
			int[] usedPoint = new int[vertexContainer.size()];

			for (int i=0;i<usedPoint.length;i++) {
				usedPoint[i] = -1;
			}

			for (int i=0;i<vertexContainer.size();i++) {
				if (vertexContainer.get(i).getY() < vertexContainer.get(minPoint).getY()){
					minPoint = i;		
				}
			}

			for (int i=0;i<vertexContainer.size();i++) {
				if (vertexContainer.get(i).getY() > vertexContainer.get(maxPoint).getY()){
					maxPoint = i;		
				}
			}			

			addUsedPoint(usedPoint, minPoint);

			currPoint = minPoint;
			//System.out.println("2 minPoint ="+vertexContainer.get(minPoint).getName()+", maxPoint ="+vertexContainer.get(maxPoint).printPoint());
			int j = 0;
			
			while ((currPoint!=maxPoint)&&(j<vertexContainer.size())) {
				//System.out.println(vertexContainer.get(currPoint).printPoint());
				j++;
				maxAngle = currPoint;
				for (int i=0;i<vertexContainer.size();i++) {
					if ((findAngle(vertexContainer.get(currPoint), vertexContainer.get(maxAngle))<findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))) && (notUsed(usedPoint, i) || i == maxPoint) && (findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))<=180)){
						maxAngle = i;
					}else if((vertexContainer.get(maxAngle).getY()==vertexContainer.get(i).getY())&&(vertexContainer.get(maxAngle).getX()<vertexContainer.get(i).getX())){
						//System.out.println(vertexContainer.get(i).printPoint());
						maxAngle = i;
					}
				}
				currPoint = maxAngle;
				addUsedPoint(usedPoint, currPoint);
			}
			currPoint = maxPoint;
			//System.out.println("3");
			
			while(currPoint!=minPoint){
				minAngle = minPoint;
				for (int i = 0;i<vertexContainer.size();i++) {
					if ((findAngle(vertexContainer.get(currPoint), vertexContainer.get(minAngle))<findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))) && (notUsed(usedPoint, i)) && (findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))>=180)){
						minAngle = i;						
					}
				}
				currPoint = minAngle;
				addUsedPoint(usedPoint, currPoint);
			}
			sortedVertex.clear();
			for (int i=0;i<usedPoint.length;i++) {
				//System.out.println(usedPoint[i]);
				sortedVertex.add(vertexContainer.get(usedPoint[i]));
			}
			sortedVertex.printPoint(true);
			int[] bisectCount = new int[bisectContainer.size()];
			//System.out.println("---------");
			for (int i = 0;i<sortedVertex.size() ;i++ ) {
				for (int k=0;k<bisectContainer.size() ;k++ ) {
					System.out.println("bisect "+bisectContainer.get(k).getName()+" intersects "+sortedVertex.get(i).getName()+" "+bisectContainer.get(k).ptSegDist(sortedVertex.get(i).getX(), sortedVertex.get(i).getY()));
					if (bisectContainer.get(k).ptSegDist(sortedVertex.get(i).getX(), sortedVertex.get(i).getY())<0.001) {
						System.out.println("true");
						bisectCount[k]++;
					}
				}
			}

			for (int i=0;i<bisectCount.length ;i++ ) {
				bisectContainer.get(i).vertex = bisectCount[i];
			}

			for (int i=0;i<bisectContainer.size() ;i++ ) {
				if (bisectContainer.get(i).vertex == 0) {
					bisectContainer.remove(i);
				}
			}



			//sortedVertex.printPoint(debug);
			Path2D.Double path = new Path2D.Double();
			path.moveTo(sortedVertex.get(0).getX(), sortedVertex.get(0).getY());
			for (int i = 1; i<sortedVertex.size(); i++) {
				path.lineTo(sortedVertex.get(i).getX(), sortedVertex.get(i).getY());
			}
			path.closePath();
			boolean stat = true;
			if (path.contains(queryPoint)) {
				for (int i =0;i<bisectContainer.size() ;i++ ) {
					if (bisectContainer.get(i).vertex<2) {
						stat = false;
					}
				}
				if (stat) {
					return true;
				}else{
					return false;
				}
				//return true;
			}else{
				return false;
			}
		}
	}

	public boolean notUsed(int[] usedPoint, int a){
		for (int i = 0; i<usedPoint.length; i++ ) {
			if (usedPoint[i]==a) {
				return false;
			}
		}
		return true;
	}

	public void addUsedPoint(int[] usedPoint, int a){
		int i = 0;
		while ((usedPoint[i]!=-1)&&(i<usedPoint.length)) {
			i++;
			if(i == usedPoint.length){
				break;
			}
		}
		if (i<usedPoint.length) {
			usedPoint[i] = a;
		}
		
	}


	/**
	DRAWING
	*/

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
		for (int i=0;i<bisectContainer.size();i++){
			for (int j=i+1; j<bisectContainer.size();j++){
				if (bisectContainer.get(i).intersectsLine(bisectContainer.get(j))) {
					String name = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
					PointExt p = new PointExt(name, getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j)));
					if (!vertexContainer.contains(p)) {
						vertexContainer.add(name, p);
						if (bisectContainer.get(i).getName().equals("H")) {
							//System.out.println(name);
						}
					}
				}else if(!checkParalel(bisectContainer.get(i), bisectContainer.get(j))){
					// FIRST ALGORITHM (dummy point production).
					/*double x1 =  ((queryPoint.getY()-bisectContainer.get(i).getP1().getY())/bisectContainer.get(i).getM()) + bisectContainer.get(i).getP1().getX();
					double x2 =  ((queryPoint.getY()-bisectContainer.get(j).getP1().getY())/bisectContainer.get(j).getM()) + bisectContainer.get(j).getP1().getX();
					// check if point is within the line
					if (((queryPoint.getX()>x1) && (queryPoint.getX()>x2)) || ((queryPoint.getX()<x1) && (queryPoint.getX()<x2))){
						
						
					}else{
						PointExt[] p = new PointExt[4];

						p[0] = new PointExt(this.bisectContainer.get(i).getP1());
						p[1] = new PointExt(this.bisectContainer.get(i).getP2());
						p[2] = new PointExt(this.bisectContainer.get(j).getP1());
						p[3] = new PointExt(this.bisectContainer.get(j).getP2());
						//System.out.println(p[0].printPoint()+p[1].printPoint()+p[2].printPoint()+p[3].printPoint());
						double[] d = new double[4];
						if (p[0].getX()<p[2].getX()) {
							//System.out.println("left");
							// check upper side
							d[0] = findAngle(p[0], p[1]);
							//System.out.println("0 = "+d[0]);
							d[1] = 180 - findAngle(p[2], p[3]);
							//System.out.println("1 = "+d[1]);
							// check lower side
							d[2] = 180 - findAngle(p[0], p[1]);
							//System.out.println("2 = "+d[2]);
							d[3] = findAngle(p[2], p[3]);
							//System.out.println("3 = "+d[3]);
						}else{
							//System.out.println("right");
							// check upper side
							d[0] = 180 - findAngle(p[0], p[1]);
							//System.out.println(" 0= "+d[0]);
							d[1] = findAngle(p[2], p[3]);
							//System.out.println("1 = "+d[1]);
							// check lower side
							d[2] = findAngle(p[0], p[1]);
							//System.out.println("2 = "+d[2]);
							d[3] = 180 - findAngle(p[2], p[3]);
							//System.out.println("3 = "+d[3]);
						}
						
						if ((d[0]+d[1])>(d[2]+d[3])) {
							if (!vertexContainer.contains(p[0])) {
								vertexContainer.add(p[0]);
							}
							if (!vertexContainer.contains(p[2])) {
								vertexContainer.add(p[2]);
							}
						}else{
							if (!vertexContainer.contains(p[1])) {
								vertexContainer.add(p[1]);
							}
							if (!vertexContainer.contains(p[3])) {
								vertexContainer.add(p[3]);
							}
						}
					}*/

					// END OF FIRST ALGORITHM
					
					// SECOND ALGORITHM (EXTEND POINT)	


					// FIRST ALGORITHM (dummy point production).
					double x1 =  ((queryPoint.getY()-bisectContainer.get(i).getP1().getY())/bisectContainer.get(i).getM()) + bisectContainer.get(i).getP1().getX();
					double x2 =  ((queryPoint.getY()-bisectContainer.get(j).getP1().getY())/bisectContainer.get(j).getM()) + bisectContainer.get(j).getP1().getX();
					// check if point is within the line
					if (((queryPoint.getX()>x1) && (queryPoint.getX()>x2)) || ((queryPoint.getX()<x1) && (queryPoint.getX()<x2))){
						
						
					}else{
						PointExt[] p = new PointExt[4];

						p[0] = new PointExt(this.bisectContainer.get(i).getP1());
						p[1] = new PointExt(this.bisectContainer.get(i).getP2());
						p[2] = new PointExt(this.bisectContainer.get(j).getP1());
						p[3] = new PointExt(this.bisectContainer.get(j).getP2());
						//System.out.println(p[0].printPoint()+p[1].printPoint()+p[2].printPoint()+p[3].printPoint());
						double[] d = new double[4];
						if (p[0].getX()<p[2].getX()) {
							//System.out.println("left");
							// check upper side
							d[0] = findAngle(p[0], p[1]);
							//System.out.println("0 = "+d[0]);
							d[1] = 180 - findAngle(p[2], p[3]);
							//System.out.println("1 = "+d[1]);
							// check lower side
							d[2] = 180 - findAngle(p[0], p[1]);
							//System.out.println("2 = "+d[2]);
							d[3] = findAngle(p[2], p[3]);
							//System.out.println("3 = "+d[3]);
						}else{
							//System.out.println("right");
							// check upper side
							d[0] = 180 - findAngle(p[0], p[1]);
							//System.out.println(" 0= "+d[0]);
							d[1] = findAngle(p[2], p[3]);
							//System.out.println("1 = "+d[1]);
							// check lower side
							d[2] = findAngle(p[0], p[1]);
							//System.out.println("2 = "+d[2]);
							d[3] = 180 - findAngle(p[2], p[3]);
							//System.out.println("3 = "+d[3]);
						}
						
						if ((d[0]+d[1])>(d[2]+d[3])) {
							// UPPER EXTEND
							double x;
							double y = 0 - maxY;
							double ya = bisectContainer.get(i).getP1().getY();
							double xa = bisectContainer.get(i).getP1().getX();
							double m = bisectContainer.get(i).getM();

							x = ((y - ya)/m) + xa;
							bisectContainer.get(i).setLine(bisectContainer.get(i).getP1().getX(), bisectContainer.get(i).getP1().getY(), x,y);

							ya = bisectContainer.get(j).getP1().getY();
							xa = bisectContainer.get(j).getP1().getX();
							m = bisectContainer.get(j).getM();

							x = ((y - ya)/m) + xa;

							bisectContainer.get(j).setLine(bisectContainer.get(j).getP1().getX(), bisectContainer.get(j).getP1().getY(), x,y);

							PointExt intPoint = getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j));
							String name = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
							intPoint.setName(name);
							System.out.println(intPoint.printPoint());
							vertexContainer.add(intPoint);

						}else{
							// BELOW EXTEND
							double x;
							double y = maxY * 2;
							double ya = bisectContainer.get(i).getP1().getY();
							double xa = bisectContainer.get(i).getP1().getX();
							double m = bisectContainer.get(i).getM();

							x = ((y - ya)/m) + xa;

							bisectContainer.get(i).setLine(bisectContainer.get(i).getP1().getX(), bisectContainer.get(i).getP1().getY(), x,y);

							ya = bisectContainer.get(j).getP1().getY();
							xa = bisectContainer.get(j).getP1().getX();
							m = bisectContainer.get(j).getM();

							x = ((y - ya)/m) + xa;

							bisectContainer.get(j).setLine(bisectContainer.get(j).getP1().getX(), bisectContainer.get(j).getP1().getY(), x,y);

							PointExt intPoint = getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j));
							String name = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
							intPoint.setName(name);
							System.out.println(intPoint.printPoint());
							vertexContainer.add(intPoint);
						}
					}

					// END OF SECOND ALGORITHM




					// investigate left / right line position
					/*if (bisectContainer.get(i).getM()<0) {
						if (bisectContainer.get(j).getM()>bisectContainer.get(i).getM()) {
							vertexContainer.add(p[0]);
							vertexContainer.add(p[2]);
						}else{
							vertexContainer.add(p[1]);
							vertexContainer.add(p[3]);
						}
					}else{
						if (bisectContainer.get(j).getM()>bisectContainer.get(i).getM()) {
							vertexContainer.add(p[1]);
							vertexContainer.add(p[3]);
						}else{
							vertexContainer.add(p[0]);
							vertexContainer.add(p[2]);
						}
					}*/
					

					/*LineExt l = new LineExt(p[0], this.queryPoint);
					double[] d = new double[2];
					d[0] = radianOfTwoLines(this.bisectContainer.get(i), l);
					l = new LineExt(p[1], this.queryPoint);
					d[1] = radianOfTwoLines(this.bisectContainer.get(i), l);
					
					if (d[0]>d[1]) {
						if (!vertexContainer.contains(p[0])) {
							vertexContainer.add(p[0]);
						}
					}else{
						if (!vertexContainer.contains(p[1])) {
							vertexContainer.add(p[1]);
						}
					}

					p[0] = new PointExt(this.bisectContainer.get(j).getP1());
					p[1] = new PointExt(this.bisectContainer.get(j).getP2());

					l = new LineExt(p[0], this.queryPoint);
					d[0] = radianOfTwoLines(this.bisectContainer.get(i), l);
					l = new LineExt(p[1], this.queryPoint);
					d[1] = radianOfTwoLines(this.bisectContainer.get(i), l);
					if (d[0]>d[1]) {
						if (!vertexContainer.contains(p[0])) {
							vertexContainer.add(p[0]);
						}
					}else{
						if (!vertexContainer.contains(p[1])) {
							vertexContainer.add(p[1]);
						}
					}*/

				}
			}
		}
	}

	public void generatePolygon(PointList a){
		int[] xPoint = new int[a.size()];
		int[] yPoint = new int[a.size()];
		int n = a.size();

		for (int i=0;i<a.size();i++) {
			xPoint[i] = (int) a.get(i).getX();
			yPoint[i] = (int) a.get(i).getY();
		}

		polygon = new Polygon(xPoint, yPoint, n);
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

	public boolean pointAtLine(LineExt a, PointExt b){
		return true;
	}

	/**
	ANGLE
	*/
	public static double radianOfTwoLines(LineExt line1, LineExt line2){
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

	/**
	UTILITIES
	*/

	public boolean checkParalel(LineExt a, LineExt b){
		if (a.getM() == b.getM()) {
			return true;
		}
		return false;
	}

	public double roundDouble(double d){
		DecimalFormat a = new DecimalFormat("#.##");
		return Double.valueOf(a.format(d));
	}
	
}