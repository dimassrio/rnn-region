import java.awt.geom.Path2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
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
	private PointList visitedContainer = new PointList();

	private PointList vertexContainer = new PointList();
	private PointList sortedVertex = new PointList();

	private LineList bisectContainer = new LineList();
	private LineList sortedBisect = new LineList();

	private Polygon polygon = new Polygon();

	private ArrayList<EllipseExt> contactZone = new ArrayList<EllipseExt>();
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
	public ArrayList<EllipseExt> getContactZone(){
		return contactZone;
	}
	/**
	HANDLING
	*/
	public void startProcess(){
		this.sortPoint();
		this.initialProcessing();
		this.advancedProcessing();
		//System.out.println(vertexContainer.size());
		//vertexContainer.printPoint(true);
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
			/*vertexContainer.printPoint(true);*/
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
						/*if (temp.getName().equals("v(H:E)")) {
							System.out.println(ax+" : "+bx+" | "+ay+" : "+by);
						}*/
						if ((ax==bx)&&(ay==by)) {
							flagVertex = false;
						}else if(((ax-bx<0.001)&&(ax-bx>-0.001))&&((ay-by<0.001)&&(ay-by>-001))){
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
			
			/*System.out.println("===============");
			vertexContainer.printPoint(true);*/
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
			
			/*printArray(usedPoint);*/
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
			/*System.out.println("---------------");
			printArray(usedPoint);*/
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
			/*System.out.println("---------------");
			printArray(usedPoint);*/
			sortedVertex.clear();
			/*System.out.println(sortedVertex.size()+" : "+usedPoint.length);*/
			for (int i=0;i<usedPoint.length;i++) {
				/*System.out.println(usedPoint[i]);*/
				if (sortedVertex.indexOf(vertexContainer.get(usedPoint[i]))==-1) {
					sortedVertex.add(vertexContainer.get(usedPoint[i]));
				}
			}
			//System.out.println(sortedVertex.size());
			for (LineExt tempBisect : bisectContainer ) {
				tempBisect.vertex = 0;
				for (PointExt tempPoint : sortedVertex ) {

					//System.out.print(tempPoint.getName()+" on "+tempBisect.getName()+" - "+tempBisect.ptSegDist(tempPoint)+" | ");
					//System.out.println(tempBisect.ptSegDist(tempPoint)<0.1d);

					if (tempBisect.ptSegDist(tempPoint)<0.1d) {

						tempBisect.vertex++;

					}
				}
			}
			LineList lineFlag = new LineList();
			lineFlag.clear();
			for (LineExt tempBisect : bisectContainer) {
				//System.out.println(tempBisect.getName()+" : "+tempBisect.vertex);
				if (tempBisect.vertex == 0) {
					lineFlag.add(tempBisect);
				}
			}

			for (LineExt tempBisect : lineFlag ) {
				int del = bisectContainer.indexOf(tempBisect);
				bisectContainer.remove(del);
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
				for (LineExt tempBisect: bisectContainer ) {
					visitedContainer.add(sortedContainer.getPointByName(tempBisect.getName()));
					if (tempBisect.vertex<2) {
						stat = false;
					}
				}
				return stat;
			}else{
				stat = false;
				return stat;
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
	ADVANCED PROCESSING
	*/

	public void advancedProcessing() {
		EllipseExt cz = new EllipseExt();
		PointList peersExt = new PointList();

		for (PointExt tempPoint : sortedVertex) {
			cz = new EllipseExt(tempPoint, queryPoint);
			contactZone.add(cz);
			for (PointExt checkPoint : sortedContainer) {
				if (checkPoint!=queryPoint) {
					if (cz.contains(checkPoint)) {
						//System.out.println(checkPoint.getName()+" contained by "+cz.getName());
						if (!visitedContainer.checkExist(checkPoint)) {
							peersExt.add(checkPoint);
						}
					}	
				}
			}
		}
		/*System.out.println("-----------");
		peersExt.printPoint(true);*/

		LineExt bisectLine;
		for (PointExt temp : peersExt ) {
			boolean stat = true;
			bisectLine = createBisector(queryPoint, temp);
			bisectLine.setName(temp.getName());
			for (LineExt tempLine :  bisectContainer) {
				if (checkParalel(tempLine, bisectLine)) {
					stat = false;
					break;
				}
			}
			if (stat){
				bisectContainer.add(bisectLine);
			}
		}
		vertexContainer.clear();
		generateVertex();
		verifyVertex();
		jarvisMarch();
		/*System.out.println("---------------");
		vertexContainer.printPoint(true);*/
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
		//System.out.println("================");
		for (int i=0;i<bisectContainer.size();i++){
			for (int j=i+1; j<bisectContainer.size();j++){
				//System.out.println(bisectContainer.get(i).getName()+" : "+bisectContainer.get(j).getName());
				if (bisectContainer.get(i).intersectsLine(bisectContainer.get(j))) {
					String name = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
					PointExt p = new PointExt(name, getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j)));
					if (!vertexContainer.checkExist(p)) {
						//System.out.println(p.getName());
						vertexContainer.add(p);
					}
				}else if(!checkParalel(bisectContainer.get(i), bisectContainer.get(j))){
					boolean found = false;
					double upx, upy, downx, downy, m, x1, y1;
					upy = 0;
					downy = maxY;
					while(!found){
						// Extend i
						m = bisectContainer.get(i).getM();
						x1 = bisectContainer.get(i).getP1().getX();
						y1 = bisectContainer.get(i).getP1().getY();
						downy = downy + maxY;

						downx = ((downy-y1)/m)+x1;

						m = bisectContainer.get(i).getM();
						x1 = bisectContainer.get(i).getP2().getX();
						y1 = bisectContainer.get(i).getP2().getY();
						upy = upy - maxY;

						upx = ((upy-y1)/m)+x1;

						bisectContainer.get(i).setLine(upx, upy, downx, downy);

						// extend j

						m = bisectContainer.get(j).getM();
						x1 = bisectContainer.get(j).getP1().getX();
						y1 = bisectContainer.get(j).getP1().getY();
						downy = downy + maxY;

						downx = ((downy-y1)/m)+x1;

						m = bisectContainer.get(j).getM();
						x1 = bisectContainer.get(j).getP2().getX();
						y1 = bisectContainer.get(j).getP2().getY();
						upy = upy - maxY;

						upx = ((upy-y1)/m)+x1;

						bisectContainer.get(j).setLine(upx, upy, downx, downy);

						if (bisectContainer.get(i).intersectsLine(bisectContainer.get(j))) {
							found = true;
						}
					}

					String newName = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
					PointExt newP = getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j));
					newP.setName(newName);
					if (!vertexContainer.checkExist(newP)) {
						vertexContainer.add(newP);
					}
				}
			}
		}
	}

	public void verifyVertex(){
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
						/*if (temp.getName().equals("v(H:E)")) {
							System.out.println(ax+" : "+bx+" | "+ay+" : "+by);
						}*/
						if ((ax==bx)&&(ay==by)) {
							flagVertex = false;
						}else if(((ax-bx<0.001)&&(ax-bx>-0.001))&&((ay-by<0.001)&&(ay-by>-001))){
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

	public void jarvisMarch(){
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
			
			/*printArray(usedPoint);*/
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
			/*System.out.println("---------------");
			printArray(usedPoint);*/
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
			/*System.out.println("---------------");
			printArray(usedPoint);*/
			sortedVertex.clear();
			/*System.out.println(sortedVertex.size()+" : "+usedPoint.length);*/
			for (int i=0;i<usedPoint.length;i++) {
				/*System.out.println(usedPoint[i]);*/
				if (sortedVertex.indexOf(vertexContainer.get(usedPoint[i]))==-1) {
					sortedVertex.add(vertexContainer.get(usedPoint[i]));
				}
			}
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
		DecimalFormat a = new DecimalFormat("#.####");
		return Double.valueOf(a.format(d));
	}

	public void printArray(int[] a){
		for (int b : a) {
			System.out.print("[ ");
			System.out.print(b+",");
			System.out.println("] ");
		}
	}
	
}