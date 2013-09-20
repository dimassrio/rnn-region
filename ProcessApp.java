
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.ArrayList;
import java.text.DecimalFormat;

public class ProcessApp{
	// Initialization
	boolean debug = true;
	public boolean initialRegion = false;
	private int maxX = 0;
	private int maxY = 0;

	private PointExt queryPoint = new PointExt();
	private PointList pointContainer = new PointList();
	private PointList sortedContainer = new PointList();
	private PointList visitedContainer = new PointList();

	private PointList vertexContainer = new PointList();
	private PointList tempVertex = new PointList();
	private PointList sortedVertex = new PointList();

	private LineList bisectContainer = new LineList();
	private LineList sortedBisect = new LineList();

	private Polygon polygon = new Polygon();
	private Polygon prunn = new Polygon();
	private ArrayList<EllipseExt> contactZone = new ArrayList<EllipseExt>();
	public int irPeers = 0;
	public int afPeers = 0;
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
	public Polygon getPrunningRegion(){
		return prunn;
	}
	public ArrayList<EllipseExt> getContactZone(){
		return contactZone;
	}
	/**
	HANDLING
	*/
	public void startProcess(){
		irPeers = 0;
		double area1= 0, area2 = 0, area3 = 0;
		this.sortPoint();
		this.initialProcessing();
		if (sortedVertex.size()>2) {
			this.generatePolygon(sortedVertex);						
		}
		if (initialRegion) {
			Polygon a = this.getPolygon();
			area1 = getMeasure(a);
		}
		
		this.advancedProcessing3();
		if (sortedVertex.size()>2) {
			this.generatePolygon(sortedVertex);						
		}
		if (initialRegion) {
			Polygon b = this.getPolygon();
			area2 = getMeasure(b);
			
		}
		area3 = area1 - area2;
		System.out.println(area2);
		//System.out.println(initialRegion);
		//sortedVertex.printPoint(true);
		//System.out.println(bisectContainer.size()+irPeers);
		//System.out.println(initialRegion);
		//System.out.println(irPeers);
		//System.out.println(afPeers);
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

	public void sortPoint(PointList a){
		int n = a.size();
		PointExt temp;
		for(int i = 0; i<n-1; i++){
			for (int j = i+1; j<n ; j++ ) {
				if (a.get(i).distance(queryPoint)>a.get(j).distance(queryPoint)) {
					temp = a.get(i);
					a.set(i, a.get(j));
					a.set(j, temp);
				}
			}
		}
	}

	public void initialProcessing(){
		PointExt a = queryPoint;
		PointExt b = null;
		LineExt tempBisect = null;
		int i = 1;
		int z = 0;
		while(!initialRegion){
			if (i>100) {
				break;
			}
			irPeers = 0;
			if (sortedContainer.size()<4) {
			//	z=1;
				for (int j=1;j<sortedContainer.size();j++ ) {
					b = sortedContainer.get(j);
					tempBisect = createBisector(a, b);
					tempBisect.setName(b.getName());
					bisectContainer.add(tempBisect);
				}
				break;
			}else if (bisectContainer.size()<3) {
			//	z = 2;
				b = sortedContainer.get(i);
				i++;
				//System.out.println(i);
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
					z = 2;
					b = sortedContainer.get(i);
					i++;
					//System.out.println(i);
					tempBisect = createBisector(a, b);
					tempBisect.setName(b.getName());
					
					boolean flag = true;
					for (LineExt tempLine : bisectContainer ) {
						if (checkParalel(tempBisect, tempLine)) {
							flag = false;
						}
					}
					if (flag) {
						bisectContainer.add(tempBisect);
					}
				}else{
					break;
				}
			}
			//System.out.println(bisectContainer.size());			
		}
		irPeers += bisectContainer.size();
	}

	public void initialProcessing2(){
		PointList exclude = new PointList();
		LineExt tempBisect = null;
		ArrayList<Integer> P = new ArrayList<Integer>();

		for (PointExt x : sortedContainer) {
			if (!x.getName().equals(queryPoint.getName())) {
				P.add(sortedContainer.indexOf(x));
			}
		}
		initialRegion = false;
		while(!P.isEmpty()&&!initialRegion){
			PointExt pi = sortedContainer.get(P.get(0));
			LineExt bi = createBisector(queryPoint, pi);
			bi.setName(pi.getName());
			exclude.add(pi);
			P.remove(0);
			boolean flag = true;
			for (LineExt tempLine : bisectContainer ) {
				if (checkParalel(bi, tempLine)) {
					flag = false;
				}
			}
			//System.out.println(flag);
			if (flag) {
				bisectContainer.add(bi);
			}
			if (bisectContainer.size()>2) {
				generateVertex();
				verifyVertex();
				jarvisMarch();
				for (LineExt x : bisectContainer ) {
					x.vertex = 0;
					for (PointExt tempPoint : sortedVertex ) {
						if (x.ptSegDist(tempPoint)<0.1d) {
							x.vertex++;
						}
					}
				}

				boolean in = true;
				for (PointExt tempPoint : sortedVertex) {
					String[] name = tempPoint.getName().split("v\\(");
					String[] name2 = name[1].split(":");
					name2[1]  = name2[1].substring(0, name2[1].length()-1);
					String a = name2[0];
					String b = name2[1];
					LineExt pa = bisectContainer.getLineByName(a);
					LineExt pb = bisectContainer.getLineByName(b);
					if (pa.vertex<2) {
						in = false;
					}
					if (pa.vertex<2) {
						in = false;
					}

				}
				if (in) {
					initialRegion = true;
				}
			}
		}
		irPeers = bisectContainer.size();
	}

	public boolean findClosedPolygon(){
		//System.out.println("---------");
			
		boolean closed = false;
		if (checkParalel(bisectContainer.get(0), bisectContainer.get(bisectContainer.size()-1))) {

			return closed;
		}else{

			LineExt tempLine = null;
			//System.out.println("1");
			generateVertex(); 
			//System.out.println("2");
			
			//vertexContainer.printPoint(true);
			PointList flag = new PointList();
			LineExt checkLine = null;
			PointExt checkPoint = null;
			boolean flagVertex = false;
			// Vertex Checking
			verifyVertex();
			

			// Jarvis March
			jarvisMarch();
			
			for (LineExt tempBisect : bisectContainer ) {
				tempBisect.vertex = 0;
				for (PointExt tempPoint : sortedVertex ) {
					if (tempBisect.ptSegDist(tempPoint)<0.1d) {
						tempBisect.vertex++;
					}
				}
			}
		
			LineList lineFlag = new LineList();
			lineFlag.clear();
			for (LineExt tempBisect : bisectContainer) {
				if (tempBisect.vertex == 0) {
					lineFlag.add(tempBisect);
				}
			}
			
			for (LineExt tempBisect : lineFlag ) {
				int del = bisectContainer.indexOf(tempBisect);
				bisectContainer.remove(del);
				irPeers++;
			}
			
			Path2D.Double path = new Path2D.Double();
			path.moveTo(sortedVertex.get(0).getX(), sortedVertex.get(0).getY());
			for (int i = 1; i<sortedVertex.size(); i++) {
				path.lineTo(sortedVertex.get(i).getX(), sortedVertex.get(i).getY());
			}

			path.closePath();
			boolean stat = true;
			
			for (LineExt tempBisect: bisectContainer ) {
				visitedContainer.add(sortedContainer.getPointByName(tempBisect.getName()));
				if (tempBisect.vertex==1) {
					stat = false;
				}
			}
			
			if (path.contains(queryPoint)) {
				if (sortedVertex.size()!=vertexContainer.size()) {
					stat = false;
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
	public void advancedProcessing4(){
		EllipseExt cz = new EllipseExt();
		PointList peersExt = new PointList();
		PointList visitedVertex = new PointList();
		PointList exclude = new PointList();
		ArrayList<Integer> P = new ArrayList<Integer>();
		ArrayList<Integer> v = new ArrayList<Integer>();
		if (initialRegion) {
			for (PointExt x : vertexContainer ) {
				v.add(vertexContainer.indexOf(x));
			}
			while(!v.isEmpty()) {
				PointExt tempPoint = vertexContainer.get(v.get(0));
				if (!visitedVertex.checkExist(tempPoint)) {
					cz = new EllipseExt(tempPoint, queryPoint);
					contactZone.add(cz);
					for (PointExt checkPoint:sortedContainer){
						if (!cz.contains(checkPoint)) {
							if (!exclude.checkExist(checkPoint)) {
								exclude.add(checkPoint);
							}
						}
					}
					for (PointExt x:sortedContainer ) {
						if (!exclude.checkExist(x)) {
							if (queryPoint.getName().equals(x.getName())) {
								P.add(sortedContainer.indexOf(x));
							}
						}
					}
					visitedVertex.add(tempPoint);
					while (!P.isEmpty()) {
						PointExt pi = sortedContainer.get(P.get(0));
						LineExt bi = createBisector(queryPoint, pi);
						bi.setName(pi.getName());
						exclude.add(pi);
						P.remove(0);
						if (!bisectContainer.checkExist(bi)) {
							boolean flag = true;
							for (LineExt x : bisectContainer) {
								if (checkParalel(bi, x)) {
									flag = false;
								}
							}
							if (flag) {
								bisectContainer.add(bi);
							}
							generateVertex();
							verifyVertex();
							jarvisMarch();
							for (PointExt x : vertexContainer) {
								if (!visitedVertex.checkExist(x)) {
									cz = new EllipseExt(x, queryPoint);
									contactZone.add(cz);
									PointList czData = cz.getMember(sortedContainer, queryPoint, exclude);
									for (int i : P ) {
										PointExt z = sortedContainer.get(i);
											if (!z.getName().equals(queryPoint.getName())) {
												if (!czData.contains(z)) {
													exclude.add(z);
												}
											}
									}
								}
							}
							/*PointExt x = vertexContainer.getNext(visitedVertex);
							cz = new EllipseExt(x, queryPoint);
							contactZone.add(cz);
							for (int i : P ) {
								PointExt z = sortedContainer.get(i);
								if (!z.getName().equals(queryPoint.getName())) {
									if (!cz.contains(z)) {
										exclude.add(z);
									}
								}
							}*/
							//visitedVertex.add(x);
							P.clear();
							for (PointExt z : sortedContainer) {
								if (!exclude.checkExist(z)) {
									if (queryPoint.getName().equals(z.getName())) {
										P.add(sortedContainer.indexOf(z));
									}
								}
							}
						}
					}
				}
				//recreate v;
				v.clear();
				for (PointExt x : vertexContainer ) {
					if (!visitedVertex.checkExist(x)) {
						v.add(vertexContainer.indexOf(x));
					}
				}
			}
		}
	}
	public void advancedProcessing3(){
		EllipseExt cz = new EllipseExt();
		PointList peersExt = new PointList();
		PointList visitedVertex = new PointList();
		PointList exclude = new PointList();
		ArrayList<Integer> cd = new ArrayList<Integer>();
		ArrayList<Integer> v = new ArrayList<Integer>();
		if (initialRegion) {
			for (PointExt x : sortedVertex ) {
				v.add(sortedVertex.indexOf(x));
			}

			while(!v.isEmpty()) {
				PointExt tempPoint = sortedVertex.get(v.get(0));
				if (!visitedVertex.checkExist(tempPoint)) {
					cz = new EllipseExt(tempPoint, queryPoint);
					contactZone.add(cz);
					for (PointExt checkPoint:sortedContainer){
						if (cz.contains(checkPoint)&&!checkPoint.getName().equals(queryPoint.getName())) {
							if (!exclude.checkExist(checkPoint)) {
								peersExt.add(checkPoint);
								//exclude.add(checkPoint);
							}
						}
					}
					this.sortPoint(peersExt);
					for (PointExt x : peersExt) {
						cd.add(sortedContainer.indexOf(x));
					}
					visitedVertex.add(tempPoint);
					while (!cd.isEmpty()) {
						//System.out.println(cd.size());
						afPeers++;
						PointExt pi = sortedContainer.get(cd.get(0));
						LineExt bi = createBisector(queryPoint, pi);
						bi.setName(pi.getName());
						exclude.add(pi);
						cd.remove(0);
						if (!bisectContainer.checkExist(bi)) {
							boolean flag = true;
							for (LineExt x : bisectContainer) {
								if (checkParalel(bi, x)) {
									flag = false;
								}
							}
							if (flag) {
								bisectContainer.add(bi);
							}
							generateVertex();
							verifyVertex();
							jarvisMarch();
							peersExt.clear();
							cd.clear();
							PointExt x = sortedVertex.getNext(visitedVertex);
							cz = new EllipseExt(x, queryPoint);
							contactZone.add(cz);
							for (PointExt checkPoint : sortedContainer) {
								if (checkPoint!=queryPoint) {
									if (cz.contains(checkPoint)) {
										if (!exclude.checkExist(checkPoint)) {
											peersExt.add(checkPoint);
											//exclude.add(checkPoint);
										}
									}	
								}
							}
							visitedVertex.add(x);
							this.sortPoint(peersExt);
							for (PointExt z : peersExt) {
								cd.add(sortedContainer.indexOf(z));
							}
						}
						
					}
				}
				//recreate v;
				v.clear();
				for (PointExt x : sortedVertex ) {
					if (!visitedVertex.checkExist(x)) {
						v.add(sortedVertex.indexOf(x));
					}
				}
			}
		}else{
			for (PointExt tempPoint : sortedVertex) {
				cz = new EllipseExt(tempPoint, queryPoint);
				contactZone.add(cz);
				for (PointExt checkPoint : sortedContainer) {
					if (checkPoint!=queryPoint) {
						if (cz.contains(checkPoint)) {
							if (!visitedContainer.checkExist(checkPoint)) {
								peersExt.add(checkPoint);
							}
						}	
					}
				}
			}
			// Sort point find
			this.sortPoint(peersExt);
			while (peersExt.size()>0) {
				LineExt bisectLine;
				for (PointExt temp : peersExt ) {
					boolean stat = true;
					bisectLine = createBisector(queryPoint, temp);
					visitedContainer.add(temp);
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
				peersExt.clear();
				vertexContainer.clear();
				// Generate temporary vertex candidate based on vertex
				generateVertex();
				// Check vertex validity, remove invalid candidate
				verifyVertex();
				// Convex hull processing.
				jarvisMarch();
				// Repeat contact zone generation
				contactZone.clear();
				for (PointExt tempPoint : sortedVertex) {
					cz = new EllipseExt(tempPoint, queryPoint);
					contactZone.add(cz);
					for (PointExt checkPoint : sortedContainer) {
						if (checkPoint!=queryPoint) {
							if (cz.contains(checkPoint)) {
								if (!visitedContainer.checkExist(checkPoint)) {
									peersExt.add(checkPoint);
								}
							}	
						}
					}
				}
				
			}
		}
	}
	/**
	ADVANCED PROCESSING
	*/
	public void advancedProcessing2(){
		EllipseExt cz = new EllipseExt();
		PointList peersExt = new PointList();
		PointList visitedVertex = new PointList();
		PointList exclude = new PointList();
		ArrayList<Integer> cd = new ArrayList<Integer>();
		ArrayList<Integer> v = new ArrayList<Integer>();
		if (initialRegion) {
			for (PointExt x : sortedVertex ) {
				v.add(sortedVertex.indexOf(x));
			}

			while(!v.isEmpty()) {
				PointExt tempPoint = sortedVertex.get(v.get(0));
				if (!visitedVertex.checkExist(tempPoint)) {
					cz = new EllipseExt(tempPoint, queryPoint);
					contactZone.add(cz);
					for (PointExt checkPoint:sortedContainer){
						if (cz.contains(checkPoint)&&!checkPoint.getName().equals(queryPoint.getName())) {
							if (!exclude.checkExist(checkPoint)) {
								peersExt.add(checkPoint);
								exclude.add(checkPoint);
							}
						}
					}
					this.sortPoint(peersExt);
					for (PointExt x : peersExt) {
						cd.add(sortedContainer.indexOf(x));
					}
					visitedVertex.add(tempPoint);
					while (!cd.isEmpty()) {
						PointExt pi = sortedContainer.get(cd.get(0));
						LineExt bi = createBisector(queryPoint, pi);
						bi.setName(pi.getName());
						exclude.add(pi);
						cd.remove(0);
						if (!bisectContainer.checkExist(bi)) {
							boolean flag = true;
							for (LineExt x : bisectContainer) {
								if (checkParalel(bi, x)) {
									flag = false;
								}
							}
							if (flag) {
								bisectContainer.add(bi);
							}
							generateVertex();
							verifyVertex();
							jarvisMarch();
							peersExt.clear();
							cd.clear();
							for (PointExt x: sortedVertex ) {
								if (!visitedVertex.checkExist(x)) {
									cz = new EllipseExt(x, queryPoint);
									contactZone.add(cz);
									for (PointExt checkPoint : sortedContainer) {
										if (checkPoint!=queryPoint) {
											if (cz.contains(checkPoint)) {
												if (!exclude.checkExist(checkPoint)) {
													peersExt.add(checkPoint);
													//exclude.add(checkPoint);
												}
											}	
										}
									}
								}
							}
							for (PointExt x : peersExt) {
								cd.add(sortedContainer.indexOf(x));
							}
						}
						
					}
				}
				//recreate v;
				v.clear();
				for (PointExt x : sortedVertex ) {
					if (!visitedVertex.checkExist(x)) {
						v.add(sortedVertex.indexOf(x));
					}
				}
			}
		}else{
			for (PointExt tempPoint : sortedVertex) {
				cz = new EllipseExt(tempPoint, queryPoint);
				contactZone.add(cz);
				for (PointExt checkPoint : sortedContainer) {
					if (checkPoint!=queryPoint) {
						if (cz.contains(checkPoint)) {
							if (!visitedContainer.checkExist(checkPoint)) {
								peersExt.add(checkPoint);
							}
						}	
					}
				}
			}
			// Sort point find
			this.sortPoint(peersExt);
			while (peersExt.size()>0) {
				LineExt bisectLine;
				for (PointExt temp : peersExt ) {
					boolean stat = true;
					bisectLine = createBisector(queryPoint, temp);
					visitedContainer.add(temp);
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
				peersExt.clear();
				vertexContainer.clear();
				// Generate temporary vertex candidate based on vertex
				generateVertex();
				// Check vertex validity, remove invalid candidate
				verifyVertex();
				// Convex hull processing.
				jarvisMarch();
				// Repeat contact zone generation
				contactZone.clear();
				for (PointExt tempPoint : sortedVertex) {
					cz = new EllipseExt(tempPoint, queryPoint);
					contactZone.add(cz);
					for (PointExt checkPoint : sortedContainer) {
						if (checkPoint!=queryPoint) {
							if (cz.contains(checkPoint)) {
								if (!visitedContainer.checkExist(checkPoint)) {
									peersExt.add(checkPoint);
								}
							}	
						}
					}
				}
				
			}
			cleanBisector();
		}
	}
	public void advancedProcessing() {
		// Create Contact Zone
		EllipseExt cz = new EllipseExt();
		PointList peersExt = new PointList();
		if (initialRegion) {
			for (PointExt tempPoint : sortedVertex) {
			cz = new EllipseExt(tempPoint, queryPoint);
			contactZone.add(cz);
				for (PointExt checkPoint : sortedContainer) {
					if (checkPoint!=queryPoint) {
						if (cz.contains(checkPoint)) {
							if (!visitedContainer.checkExist(checkPoint)) {
								peersExt.add(checkPoint);
							}
						}	
					}
				}
			}
			// Sort point find
			this.sortPoint(peersExt);
			// Create bisector based on new point
			if (peersExt.size()>50) {
				while(peersExt.size()>0){
					LineExt bisectLine;
					PointExt temp = peersExt.get(0);
					boolean stat = true;
					bisectLine = createBisector(queryPoint, temp);
					visitedContainer.add(temp);
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
					peersExt.clear();
					vertexContainer.clear();
					// Generate temporary vertex candidate based on vertex
					generateVertex();
					// Check vertex validity, remove invalid candidate
					verifyVertex();
					// Convex hull processing.
					jarvisMarch();
					// Repeat contact zone generation
					contactZone.clear();
					for (PointExt tempPoint : sortedVertex) {
						cz = new EllipseExt(tempPoint, queryPoint);
						contactZone.add(cz);
						for (PointExt checkPoint : sortedContainer) {
							if (checkPoint!=queryPoint) {
								if (cz.contains(checkPoint)) {
									if (!visitedContainer.checkExist(checkPoint)) {
										peersExt.add(checkPoint);
									}
								}	
							}
						}
					}
				}
			}else{
				while (peersExt.size()>0) {
				LineExt bisectLine;
				for (PointExt temp : peersExt ) {
					boolean stat = true;
					bisectLine = createBisector(queryPoint, temp);
					visitedContainer.add(temp);
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
				peersExt.clear();
				vertexContainer.clear();
				// Generate temporary vertex candidate based on vertex
				generateVertex();
				// Check vertex validity, remove invalid candidate
				verifyVertex();
				// Convex hull processing.
				jarvisMarch();
				// Repeat contact zone generation
				contactZone.clear();
				for (PointExt tempPoint : sortedVertex) {
					cz = new EllipseExt(tempPoint, queryPoint);
					contactZone.add(cz);
					for (PointExt checkPoint : sortedContainer) {
						if (checkPoint!=queryPoint) {
							if (cz.contains(checkPoint)) {
								if (!visitedContainer.checkExist(checkPoint)) {
									peersExt.add(checkPoint);
								}
							}	
						}
					}
				}
				
			}

			}
			cleanBisector();
		}else{
			for (PointExt tempPoint : sortedVertex) {
				cz = new EllipseExt(tempPoint, queryPoint);
				contactZone.add(cz);
				for (PointExt checkPoint : sortedContainer) {
					if (checkPoint!=queryPoint) {
						if (cz.contains(checkPoint)) {
							if (!visitedContainer.checkExist(checkPoint)) {
								peersExt.add(checkPoint);
							}
						}	
					}
				}
			}
			// Sort point find
			this.sortPoint(peersExt);
			while (peersExt.size()>0) {
				LineExt bisectLine;
				for (PointExt temp : peersExt ) {
					boolean stat = true;
					bisectLine = createBisector(queryPoint, temp);
					visitedContainer.add(temp);
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
				peersExt.clear();
				vertexContainer.clear();
				// Generate temporary vertex candidate based on vertex
				generateVertex();
				// Check vertex validity, remove invalid candidate
				verifyVertex();
				// Convex hull processing.
				jarvisMarch();
				// Repeat contact zone generation
				contactZone.clear();
				for (PointExt tempPoint : sortedVertex) {
					cz = new EllipseExt(tempPoint, queryPoint);
					contactZone.add(cz);
					for (PointExt checkPoint : sortedContainer) {
						if (checkPoint!=queryPoint) {
							if (cz.contains(checkPoint)) {
								if (!visitedContainer.checkExist(checkPoint)) {
									peersExt.add(checkPoint);
								}
							}	
						}
					}
				}
				
			}
		cleanBisector();
		}
		//sortedVertex.printPoint(true);
	}

	public void czProcessing4(){
		PointList exclude = new PointList();
		PointList peersExt = new PointList();
		PointList visitedVertex = new PointList();
		PointList czData = new PointList();
		Polygon prunningRegion = new Polygon();

		EllipseExt cz = new EllipseExt();
		ArrayList<Integer> P = new ArrayList<Integer>();
		ArrayList<Integer> cd =  new ArrayList<Integer>();

		for (PointExt temp : sortedContainer) {
			if (!temp.getName().equals(queryPoint.getName())) {
				P.add(sortedContainer.indexOf(temp));
			}
		}

		while(!P.isEmpty()){
			//System.out.println(P.size());
			PointExt pi = sortedContainer.get(P.get(0));
			LineExt bi = createBisector(queryPoint, pi);
			bi.setName(pi.getName());
			bisectContainer.add(bi);
			exclude.add(pi);
			if (bisectContainer.size()>2) {
				generateVertex();
				verifyVertex();
				//System.out.println(vertexContainer.size());
				czData.clear();
				for (PointExt vtx : vertexContainer ) {
					if (!visitedVertex.checkExist(vtx)) {
						System.out.println(vtx.printPoint());
						cz = new EllipseExt(vtx, queryPoint);
						contactZone.add(cz);
						czData = cz.getMember(sortedContainer, queryPoint);
					}
				}
				for (PointExt vtx : vertexContainer ) {
					if (!visitedVertex.checkExist(vtx)) {
						prunningRegion = getPrunningRegion2(vtx);
						visitedVertex.add(vtx);	
					}
					prunn = prunningRegion;
					for (PointExt peers : sortedContainer) {
						if (prunningRegion.contains(peers)) {
							if (!czData.checkExist(peers)) {
								if (!exclude.checkExist(peers)) {
									exclude.add(peers);
								}	
							}	
						}
					}
				}
				for (PointExt data: czData ) {
					int c = sortedContainer.indexOf(data);
					cd.add(c);
				}
				while(!cd.isEmpty()){
					pi = sortedContainer.get(cd.get(0));
					bi = createBisector(queryPoint, pi);
					bi.setName(pi.getName());
					if (!bisectContainer.checkExist(bi)) {
						bisectContainer.add(bi);
						exclude.add(pi);
						generateVertex();
						verifyVertex();
						for (PointExt tempV : vertexContainer) {
							if (!visitedVertex.checkExist(tempV)) {
								cz = new EllipseExt(tempV, queryPoint);
								contactZone.add(cz);
								czData = cz.getMember(sortedContainer, queryPoint, exclude);
								for (PointExt data: czData ) {
									int c = sortedContainer.indexOf(data);
									boolean in = false;
									for (int i : cd) {
										if (i == c) {
											in = true;
										}
									}
									if (!in) {
										cd.add(c);
									}
								}
							}
						}
					}
					cd.remove(0);
				}

			}
			// Recreate P
			P.clear();
			for (PointExt po : sortedContainer ) {
				if (!queryPoint.getName().equals(po.getName())) {
					if (!exclude.checkExist(po)) {
						P.add(sortedContainer.indexOf(po));
					}
				}
			}
		}


	}

	/*public void czProcessing3(){
		PointList exclude = new PointList();
		PointList peersExt = new PointList();
		PointList visitedVertex = new PointList();
		Polygon prunningRegion = new Polygon();
		EllipseExt cz = new EllipseExt();

		ArrayList<Integer> processPoint = new ArrayList<Integer>();
		for (PointExt temp : sortedContainer) {
			if (!temp.getName().equals(queryPoint.getName())) {
				processPoint.add(sortedContainer.indexOf(temp));
			}
		}
		int z = 0;
		while (processPoint.size()>0) {

			z++;
			System.out.println(processPoint.size());
			PointExt temp = sortedContainer.get(processPoint.get(0));
			LineExt tempBisect = createBisector(queryPoint, temp);			
			tempBisect.setName(temp.getName());
			bisectContainer.add(tempBisect);
			exclude.add(temp);
			if (bisectContainer.size()>1) {
				generateVertex();
				verifyVertex();
				//System.out.println("-----");
				/*for (PointExt t: visitedVertex) {
					System.out.println(t.printPoint());
				}
				for (PointExt tempV : vertexContainer) {
					if (!visitedVertex.checkExist(tempV)) {
						visitedVertex.add(tempV);
						System.out.println(tempV.printPoint());
						prunningRegion.reset();
						prunningRegion = getPrunningRegion2(tempV);
						//System.out.println(prunningRegion.npoints);
						prunn = prunningRegion;
						cz = new EllipseExt(tempV, queryPoint);
						contactZone.add(cz);
						PointList czData = cz.getMember(sortedContainer, queryPoint, exclude);
						for (int p : processPoint) {
							PointExt peers = sortedContainer.get(p);
							if (prunningRegion.contains(peers)&&!czData.checkExist(peers)&&!exclude.checkExist(peers)) {
								exclude.add(peers);
							}
						}
						while (czData.size()>0) {
								PointExt tempCz = czData.get(0);
								tempBisect = createBisector(queryPoint, tempCz);
								tempBisect.setName(tempCz.getName());
								bisectContainer.add(tempBisect);
								exclude.add(tempCz);
								generateVertex2();
								verifyVertex2();
								czData.clear();
								//for (PointExt tempVe : tempVertex) {
								cz = new EllipseExt(tempVertex.get(0), queryPoint);
								//	visitedVertex.add(tempVertex.get(0));	
								//}
								
								czData = cz.getMember(sortedContainer, queryPoint, exclude);
						}
					}
				}
			}

			processPoint.clear();
			for (PointExt tempX : sortedContainer) {
				if (!tempX.getName().equals(queryPoint.getName())&&!exclude.checkExist(tempX)) {
					processPoint.add(sortedContainer.indexOf(tempX));
				}
			}
		}
		vertexContainer.clear();
		generateVertex();
		verifyVertex();
		
	}*/

	public void czProcessing2(){
		PointList exclude = new PointList();
		PointList peersExt = new PointList();
		PointList visitedVertex = new PointList();
		Polygon prunningRegion = new Polygon();
		EllipseExt cz = new EllipseExt();

		for (PointExt temp : sortedContainer) {
			//System.out.println(exclude.size());
			if (exclude.size()+peersExt.size()>sortedContainer.size()) {
				System.out.println("looping detected : "+exclude.size());
				break;
			}
			if (!temp.equals(queryPoint)&&!exclude.checkExist(temp)) {
				LineExt tempBisect = createBisector(queryPoint, temp);
				tempBisect.setName(temp.getName());
				bisectContainer.add(tempBisect);
				exclude.add(temp);	
				if (bisectContainer.size()>1) {
					generateVertex();
					verifyVertex();
					for (PointExt tempV : vertexContainer) {
						//System.out.println(tempV.printPoint());
						prunningRegion = getPrunningRegion2(tempV);
						prunn = prunningRegion;
						cz = new EllipseExt(tempV, queryPoint);
						contactZone.add(cz);
						for (PointExt peers : sortedContainer) {
							if (!exclude.checkExist(peers)) {
								if (prunningRegion.contains(peers)) {
									//System.out.print(peers.printPoint()+" : ");
									if (cz.contains(peers)) {
										//System.out.println("contact zone");
										if (!peersExt.checkExist(peers)) {
											peersExt.add(peers);
										}
									}else{
										
											//System.out.println("exclude");
											exclude.add(peers);
									}
								}
							}
						}
						//System.out.println(exclude.size());
						int n = peersExt.size();
						int i = 0;
						//peersExt.printPoint(true);
						while (i<n) {
							if (!peersExt.get(i).equals(queryPoint)) {
								if (!exclude.checkExist(peersExt.get(i))){
									tempBisect = createBisector(queryPoint, peersExt.get(i));
									tempBisect.setName(peersExt.get(i).getName());
									bisectContainer.add(tempBisect);
									exclude.add(peersExt.get(i));
									generateVertex2();
									verifyVertex2();
									for (PointExt tempVe : tempVertex ) {
										cz = new EllipseExt(tempVe, queryPoint);
										contactZone.add(cz);
										for (PointExt peers : sortedContainer) {
											if (!exclude.checkExist(peers)) {
												if (cz.contains(peers)) {
													if (!peersExt.checkExist(peers)) {
														peersExt.add(peers);
														//exclude.add(peers);
													}
												}	
											}
										}
									}
								}
							}
							i++;
							n = peersExt.size();
						}
					}
				}
			}
		}
		for (LineExt tempBisect : bisectContainer ) {
			tempBisect.vertex = 0;
			for (PointExt tempPoint : sortedVertex ) {
				if (tempBisect.ptSegDist(tempPoint)<0.1d) {
					tempBisect.vertex++;
				}
			}
		}
	}

	public void czProcessing(){
		PointList processPoint = new PointList();
		for (PointExt temp : sortedContainer) {
			processPoint.add(temp);
		}
		processPoint.remove(0);
		PointExt b;
		LineExt tempBisect;
		int i= 0;

		PointList visited = new PointList();
		PointList visitedPeers = new PointList();
		Polygon prunningRegion;
		EllipseExt cz = new EllipseExt();
		PointList peersExt = new PointList(), exclude = new PointList();
		System.out.println("start : "+processPoint.size());
		while(processPoint.size()>0){
			i++;
			b = processPoint.get(0);
			tempBisect = createBisector(queryPoint, b);
			tempBisect.setName(b.getName());
			bisectContainer.add(tempBisect);
			visitedPeers.add(b);
			if (bisectContainer.size()>1) {
				generateVertex();
				System.out.println("---");
				vertexContainer.printPoint(true);
				verifyVertex();
				for (PointExt temp : vertexContainer) {
					if (!visited.checkExist(temp)) {
						prunningRegion = getPrunningRegion2(temp);
						prunn = prunningRegion;
						cz = new EllipseExt(temp, queryPoint);
						for (PointExt peers : processPoint) {
							if (prunningRegion.contains(peers)) {
								if (cz.contains(peers)) {
									peersExt.add(peers);
								}else{
									exclude.add(peers);
								}
							}
						}
						visited.add(temp);
						for (PointExt tempPoint : exclude ) {
							int del = processPoint.indexOf(tempPoint);
							if (del!=-1) {
								processPoint.remove(del);
							}
						}
						while(peersExt.size()>0){
							for (PointExt peers : peersExt) {
								if (!visitedPeers.checkExist(peers)) {
									b = peers;
									tempBisect = createBisector(queryPoint, b);
									tempBisect.setName(b.getName());
									bisectContainer.add(tempBisect);
									visitedPeers.add(peers);
								}
							}
							generateVertex2();
							verifyVertex2();
							peersExt.clear();
							for (PointExt tempPoint : tempVertex) {
								if (!visited.checkExist(tempPoint)) {
									cz = new EllipseExt(tempPoint, queryPoint);
									contactZone.add(cz);
									for (PointExt peers : processPoint) {
										if (cz.contains(peers)) {
											if (visitedPeers.checkExist(peers)) {
												peersExt.add(peers);
												visitedPeers.add(peers);
											}
										}
									}
									visited.add(tempPoint);
								}
							}
						}
					}
				}


			}
			for (PointExt temp : visitedPeers) {
				int del = processPoint.indexOf(temp);
				if (del!=-1) {
					processPoint.remove(del);
				}
			}
			//System.out.println(processPoint.size());
		}
	}

	public void czProcessing5(){
		PointList exclude = new PointList();
		PointList peersExt = new PointList();
		PointList visitedVertex = new PointList();
		PointList czData = new PointList();
		Polygon prunningRegion = new Polygon();

		EllipseExt cz = new EllipseExt();
		ArrayList<Integer> P = new ArrayList<Integer>();
		ArrayList<Integer> cd =  new ArrayList<Integer>();

		for (PointExt temp : sortedContainer) {
			if (!temp.getName().equals(queryPoint.getName())) {
				P.add(sortedContainer.indexOf(temp));
			}
		}

		while(!P.isEmpty()){
			/*if (P.size()<250) {
				break;
			}*/
			//System.out.println(P.size());
			PointExt pi = sortedContainer.get(P.get(0));
			LineExt bi = createBisector(queryPoint, pi);
			bi.setName(pi.getName());
			boolean flag = true;
			for (LineExt tempLine : bisectContainer ) {
				if (checkParalel(bi, tempLine)) {
						flag = false;
				}
			}
			if (flag) {
				if (!bisectContainer.checkExist(bi)) {
					bisectContainer.add(bi);
				}
			}
		/*	if (bi.getName().equals("220")||bi.getName().equals("240")) {
				System.out.println(bi.getName()+":"+bi.getM());
			}*/
			exclude.add(pi);
			P.remove(P.get(0));
			if (bisectContainer.size()>2) {
				generateVertex();
				verifyVertex();
				//System.out.println(vertexContainer.getNext(visitedVertex).printPoint());
				prunningRegion = getPrunningRegion3(vertexContainer.getNext(visitedVertex));
				cz = new EllipseExt(vertexContainer.getNext(visitedVertex), queryPoint);
				contactZone.add(cz);
				/*if (vertexContainer.getNext(visitedVertex).getName().equals("v(220:240)")) {
					System.out.println("1");
				}*/
				for (PointExt temp : sortedContainer ) {
					if(prunningRegion.contains(temp)&&!cz.contains(temp)){
						exclude.add(temp);
					}
				}
				czData.clear();
				cd.clear();
				czData = cz.getMember(sortedContainer, queryPoint, exclude);
				for (PointExt temp : czData ) {
					cd.add(sortedContainer.indexOf(temp));
				}
				if (vertexContainer.getNext(visitedVertex).getName().equals("v(163:99)")) {
					prunn = prunningRegion;
					break;
				}
				if (!visitedVertex.checkExist(vertexContainer.getNext(visitedVertex))) {
					visitedVertex.add(vertexContainer.getNext(visitedVertex));
				}
				//System.out.println(cd.size());
				while(!cd.isEmpty()){	
					//System.out.println(cd.size());
					pi = sortedContainer.get(cd.get(0));
					bi = createBisector(queryPoint, pi);
					if (!bisectContainer.checkExist(bi)) {
						bi.setName(pi.getName());
						flag = true;
						for (LineExt tempLine : bisectContainer ) {
							if (checkParalel(bi, tempLine)) {
								flag = false;
							}
						}
						if (flag) {
							bisectContainer.add(bi);
						}
						
						generateVertex();
						verifyVertex();
						//System.out.println(vertexContainer.size());
						for (PointExt temp : vertexContainer ) {
							
						}
						cz = new EllipseExt(vertexContainer.getNext(visitedVertex), queryPoint);
						contactZone.add(cz);
						//visitedVertex.add(vertexContainer.getNext(visitedVertex)); // remove after use
						czData.clear();
						czData = cz.getMember(sortedContainer, queryPoint, exclude);
						cd.clear();
						for (PointExt temp : czData ) {
							int c = sortedContainer.indexOf(temp);
							boolean in = false;
							for (int i : cd) {
								if (i == c) {
									in = true;
								}
							}
							if (!in) {
								cd.add(c);
							}
						}
					}
					exclude.add(pi);
				}
			}
			P.clear();
			for (PointExt temp : sortedContainer) {
				if (!temp.getName().equals(queryPoint.getName())) {
					if (!exclude.checkExist(temp)) {
						P.add(sortedContainer.indexOf(temp));
					}
				}
			}
		}
	}

	public Polygon getPrunningRegion3(PointExt vertex){
		String[] name = vertex.getName().split("v\\(");
		String[] name2 = name[1].split(":");
		name2[1]  = name2[1].substring(0, name2[1].length()-1);
		String a = name2[0];
		String b = name2[1];
		PointExt pa = sortedContainer.getPointByName(a);
		PointExt pb = sortedContainer.getPointByName(b);
		double alpha = findAngle(queryPoint, pa);
		double beta = findAngle(queryPoint, pb);
		int npoint;
		int[] xpoint, ypoint;
		double m;
		double c;
		double x, y;
		LineExt pruna = new LineExt(), prunb = new LineExt();
		pruna.setName("q"+pa.getName());
		pruna.setName("q"+pb.getName());
		// create prun a
		if (queryPoint.getX() == pa.getX()) {
			if (queryPoint.getY()>pa.getY()) {
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), pa.getX(), 0);
			}else{
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), pa.getX(), this.maxY);
			}
		}else if (queryPoint.getY() == pa.getY()) {
			if (queryPoint.getX()>pa.getX()) {
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), 0, pa.getY());
			}else{
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), this.maxX, pa.getY());
			}
		}else{
			pruna = new LineExt(queryPoint, pa);
			m = pruna.getM();
			if (pa.getY()>queryPoint.getY()) {
				y = this.maxY;
			}else{
				y = 0;
			}
			x = (((y - queryPoint.getY())+(m*queryPoint.getX()))/m);
			pruna.setLine(queryPoint.getX(), queryPoint.getY(), x, y);
			pruna.setName("q"+pa.getName());
		}
		// create prunb
		if (queryPoint.getX() == pb.getX()) {
			if (queryPoint.getY()>pb.getY()) {
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), pb.getX(), 0);
			}else{
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), pb.getX(), this.maxY);
			}
		}else if (queryPoint.getY() == pb.getY()) {
			
			if (queryPoint.getX()>pb.getX()) {
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), 0, pb.getY());
			}else{
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), this.maxX, pb.getY());
			}
		}else{
			
			prunb = new LineExt(queryPoint, pb);
			m = prunb.getM();
			if (pb.getY()>queryPoint.getY()) {
				y = this.maxY;
			}else{
				y = 0;
			}
			x = (((y - queryPoint.getY())+(m*queryPoint.getX()))/m);
			prunb.setLine(queryPoint.getX(), queryPoint.getY(), x, y);
			prunb.setName("q"+pa.getName());
		}
		// create region
		// bila kedua y sama
		if (pruna.getP2().getY() == prunb.getP2().getY()) {
			npoint = 3;
			xpoint = new int[npoint];
			ypoint = new int[npoint];

			xpoint[0] = (int) pruna.getP2().getX();
			xpoint[1] = (int) queryPoint.getX();
			xpoint[2] = (int) prunb.getP2().getX();
			
			ypoint[0] = (int) pruna.getP2().getY();
			ypoint[1] = (int) queryPoint.getY();
			ypoint[2] = (int) prunb.getP2().getY();
		// bila salah satu garis horizontal
		}else if (alpha == 0 || alpha == 180 || beta == 0 || beta == 180) {
			npoint = 4;
			xpoint = new int[npoint];
			ypoint = new int[npoint];

			xpoint[0] = (int) pruna.getP2().getX();
			xpoint[1] = (int) queryPoint.getX();
			xpoint[2] = (int) prunb.getP2().getX();
			
			ypoint[0] = (int) pruna.getP2().getY();
			ypoint[1] = (int) queryPoint.getY();
			ypoint[2] = (int) prunb.getP2().getY();

			if (alpha == 0) {
				if (beta>0 && beta <180) {
					xpoint[3] = (int) this.maxX;
					ypoint[3] = (int) this.maxY;
				}else if(beta>180){
					xpoint[3] = (int) this.maxX;
					ypoint[3] = (int) 0;
				}
			}else if(beta == 0){
				if (alpha>0 && alpha <180) {
					xpoint[3] = (int) this.maxX;
					ypoint[3] = (int) this.maxY;
				}else if(alpha>180){
					xpoint[3] = (int) this.maxX;
					ypoint[3] = (int) 0;
				}
			}else if(alpha == 180){
				if (beta>0 && beta <180) {
					xpoint[3] = (int) 0;
					ypoint[3] = (int) this.maxY;
				}else if(beta>180){
					xpoint[3] = (int) 0;
					ypoint[3] = (int) 0;
				}
			}else if(beta == 180){
				if (alpha>0 && alpha<180) {
					xpoint[3] = (int) 0;
					ypoint[3] = (int) this.maxY;
				}else if(alpha>180){
					xpoint[3] = (int) 0;
					ypoint[3] = (int) 0;
				}
			}
		// normal
		}else{
			npoint = 5;
			xpoint = new int[npoint];
			ypoint = new int[npoint];

			xpoint[0] = (int) pruna.getP2().getX();
			xpoint[1] = (int) queryPoint.getX();
			xpoint[2] = (int) prunb.getP2().getX();
			
			ypoint[0] = (int) pruna.getP2().getY();
			ypoint[1] = (int) queryPoint.getY();
			ypoint[2] = (int) prunb.getP2().getY();
			
			if (pruna.getP2().getY()==0) {
				if (beta<(alpha-180)) {
					xpoint[3] = (int) this.maxX;
					xpoint[4] = (int) this.maxX;
					ypoint[3] = (int) this.maxY;
					ypoint[4] = (int) 0;
				}else if(beta>(alpha-180)){
					xpoint[3] = (int) 0;
					xpoint[4] = (int) 0;
					ypoint[3] = (int) this.maxY;
					ypoint[4] = (int) 0;
				}
			}else if(prunb.getP2().getY()==0){
				if (alpha<(beta-180)) {
					xpoint[3] = (int) this.maxX;
					xpoint[4] = (int) this.maxX;
					ypoint[3] = (int) 0;
					ypoint[4] = (int) this.maxY;
				}else if(alpha>(beta-180)){
					xpoint[3] = (int) 0;
					xpoint[4] = (int) 0;
					ypoint[3] = (int) 0;
					ypoint[4] = (int) this.maxY;
				}
			}
		}

		Polygon poly = new Polygon(xpoint, ypoint, npoint);
		return poly;
	}
	public Polygon getPrunningRegion2(PointExt vertex){
		String[] name = vertex.getName().split("v\\(");
		String[] name2 = name[1].split(":");
		name2[1]  = name2[1].substring(0, name2[1].length()-1);
		String a = name2[0];
		String b = name2[1];
		PointExt pa = sortedContainer.getPointByName(a);
		PointExt pb = sortedContainer.getPointByName(b);
		double m;
		double c;
		double x, y;
		LineExt pruna, prunb;
		Polygon retval = new Polygon();
		
		if (queryPoint.getX() == pa.getX()) {
				// garis ke atas
			if (pa.getY()<queryPoint.getY()) {
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), 0);
				pruna.setName("q"+pa.getName());
			}else{
				// garis ke bawah
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), this.maxY);
				pruna.setName("q"+pa.getName());
			}
		}else if(queryPoint.getY() == pa.getY()){
			// garis ke kanan
			if (pa.getX()>queryPoint.getX()) {
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), this.maxX, queryPoint.getY());
				pruna.setName("q"+pa.getName());
			}else{
				// garis ke kiri
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), 0, queryPoint.getY());
				pruna.setName("q"+pa.getName());
			}
		}else{
			pruna = new LineExt(queryPoint, pa);
			m = pruna.getM();
			if (pa.getY()>queryPoint.getY()) {
				y = this.maxY;
			}else{
				y = 0;
			}
			x = (((y - queryPoint.getY())+(m*queryPoint.getX()))/m);
			pruna.setLine(queryPoint.getX(), queryPoint.getY(), x, y);
			pruna.setName("q"+pa.getName());

		}

		if (queryPoint.getX() == pb.getX()) {
				// garis ke atas
			if (pb.getY()<queryPoint.getY()) {
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), 0);
				prunb.setName("q"+pb.getName());
			}else{
				// garis ke bawah
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), this.maxY);
				prunb.setName("q"+pb.getName());
			}
		}else if(queryPoint.getY() == pb.getY()){
			// garis ke kanan
			if (pb.getX()>queryPoint.getX()) {
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), this.maxX, queryPoint.getY());
				prunb.setName("q"+pb.getName());
			}else{
				// garis ke kiri
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), 0, queryPoint.getY());
				prunb.setName("q"+pb.getName());
			}
		}else{
			prunb = new LineExt(queryPoint, pb);
			m = prunb.getM();
			if (pb.getY()>queryPoint.getY()) {
				y = this.maxY;
			}else{
				y = 0;
			}
			x = (((y - queryPoint.getY())+(m*queryPoint.getX()))/m);
			prunb.setLine(queryPoint.getX(), queryPoint.getY(), x, y);
			prunb.setName("q"+pb.getName());
		}
		
		// Polygon check
		
		int npoint = 3;
		int[] xpoint = new int[npoint];
		int[] ypoint = new int[npoint];

		xpoint[0] = (int) queryPoint.getX();
		xpoint[1] = (int) pruna.getP2().getX();
		xpoint[2] = (int) prunb.getP2().getX();

		ypoint[0] = (int) queryPoint.getY();
		ypoint[1] = (int) pruna.getP2().getY();
		ypoint[2] = (int) prunb.getP2().getY();

		if (pruna.getP2().getY() == prunb.getP2().getY()) {
			retval = new Polygon(xpoint, ypoint, npoint); // Polygon berbentuk segitiga
		}else if((pruna.getM()==0)&&(prunb.getM()==0)){
			npoint = 5;
			xpoint = new int[npoint];
			ypoint = new int[npoint];
			xpoint[0] = (int) queryPoint.getX();
			xpoint[1] = (int) pruna.getP2().getX();
			xpoint[2] = (int) prunb.getP2().getX();
			xpoint[3] = (int) prunb.getP2().getX();
			xpoint[4] = (int) pruna.getP2().getX();
			ypoint[0] = (int) queryPoint.getY();
			ypoint[1] = (int) pruna.getP2().getY();
			ypoint[2] = (int) prunb.getP2().getY();
			if (vertex.getY()<queryPoint.getY()) {
				ypoint[3] = 0;
				ypoint[4] = 0;
			}else{
				ypoint[3] = this.maxX;
				ypoint[4] = this.maxX;
			}
			retval = new Polygon(xpoint, ypoint, npoint); // Polygon berbentuk persegi panjang dengan titik sudut (0,0) (maxX, 0), (maxX, prunY), (0, prunY) atau (0. maxY), (maxX, maxY)
		}else if((Double.isInfinite(pruna.getM())&&(Double.isInfinite(prunb.getM())))){
			npoint = 5;
			xpoint = new int[npoint];
			ypoint = new int[npoint];
			ypoint[0] = (int) queryPoint.getY();
			ypoint[1] = (int) pruna.getP2().getY();
			ypoint[2] = (int) prunb.getP2().getY();
			ypoint[3] = (int) prunb.getP2().getY();
			ypoint[4] = (int) pruna.getP2().getY();
			xpoint[0] = (int) queryPoint.getX();
			xpoint[1] = (int) pruna.getP2().getX();
			xpoint[2] = (int) prunb.getP2().getX();
			if (vertex.getX()<queryPoint.getX()) {
				xpoint[3] = 0;
				xpoint[4] = 0;
			}else{
				xpoint[3] = this.maxX;
				xpoint[4] = this.maxX;
			}
			retval = new Polygon(xpoint, ypoint, npoint);
		}else{
			npoint = 5;
			xpoint = new int[npoint];
			ypoint = new int[npoint];
			LineExt upL;
			LineExt dwL;
			if(pruna.getP2().getY() == 0){
				upL = pruna;
				dwL = prunb;
			}else{
				upL = prunb;
				dwL = pruna;
			}

			xpoint[0] = (int) upL.getP2().getX();
			xpoint[1] = (int) queryPoint.getX();
			xpoint[2] = (int) dwL.getP2().getX();

			ypoint[0] = (int) upL.getP2().getY();
			ypoint[1] = (int) queryPoint.getY();
			ypoint[2] = (int) dwL.getP2().getY();

			if ((pruna.getP2().getX()<0 || pruna.getP2().getX()>this.maxX)&&(prunb.getP2().getX()<0||prunb.getP2().getX()>this.maxX)) {
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}
				if (vertex.getX()<x) {
					xpoint[3] = (int) dwL.getP2().getX();
					xpoint[4] = (int) dwL.getP2().getX();
					ypoint[3] = (int) upL.getP2().getY();
					ypoint[4] = (int) upL.getP2().getY();	
				}else{
					xpoint[3] = (int) upL.getP2().getX();
					xpoint[4] = (int) upL.getP2().getX();
					ypoint[3] = (int) dwL.getP2().getY();
					ypoint[4] = (int) dwL.getP2().getY();
				}
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}else if((pruna.getP2().getX()>0 && pruna.getP2().getX()<this.maxX)&&(prunb.getP2().getX()>0 && prunb.getP2().getX()<this.maxX)){
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}
				if (vertex.getX()<x) {
					xpoint[3] = 0;
					xpoint[4] = 0;
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
				}else{
					xpoint[3] = this.maxX;
					xpoint[4] = this.maxX;
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
				}
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}else if((upL.getP2().getX()>0&&upL.getP2().getX()<this.maxX)&&(dwL.getP2().getX()<0||dwL.getP2().getX()>this.maxX)){
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}
				if (vertex.getX()<x) {
					xpoint[3] = 0;
					xpoint[4] = 0;
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
				}else{
					xpoint[3] = this.maxX;
					xpoint[4] = this.maxX;
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
				}
				/*if (vertex.getName().equals("v(754:374)")) {
					for (int i=0;i<npoint ;i++) {
						System.out.println(xpoint[i]+":"+ypoint[i]);
					}
				}*/
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}else if((dwL.getP2().getX()>0&&dwL.getP2().getX()<this.maxX)&&(upL.getP2().getX()<0||upL.getP2().getX()>this.maxX)){
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}
				if (vertex.getX()<x) {
					xpoint[3] = 0;
					xpoint[4] = 0;
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
				}else{
					xpoint[3] = this.maxX;
					xpoint[4] = this.maxX;
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
				}
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}else if(upL.getP2().getX()==0){
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}

				if (vertex.getX()<x) {
					npoint = 4;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getX();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = 0;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = this.maxY;
				}else{
					npoint = 6;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getY();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = this.maxX;
					xpoint[4] = this.maxX;
					xpoint[5] = 0;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
					ypoint[5] = 0;
				}
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}else if(upL.getP2().getX()==this.maxX){
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}

				if (vertex.getX()>x) {
					npoint = 4;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getX();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = this.maxX;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = this.maxY;
				}else{
					npoint = 6;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getY();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = 0;
					xpoint[4] = 0;
					xpoint[5] = this.maxX;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = this.maxY;
					ypoint[4] = 0;
					ypoint[5] = 0;
				}
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}else if(dwL.getP2().getX()==0){
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}

				if (vertex.getX()<x) {
					npoint = 4;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getX();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = 0;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = 0;
				}else{
					npoint = 6;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getY();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = 0;
					xpoint[4] = this.maxX;
					xpoint[5] = this.maxX;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = this.maxY;
					ypoint[4] = this.maxY;
					ypoint[5] = 0;
				}
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}else if(dwL.getP2().getX()==this.maxX){
				if (vertex.getY()<queryPoint.getY()) {
					m = upL.getM();
					c = upL.getP2().getY() - (m*upL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}else{
					m = dwL.getM();
					c = dwL.getP2().getY() - (m*dwL.getP2().getX());
					x = (vertex.getY() - c ) / m;
				}

				if (vertex.getX()>x) {
					npoint = 4;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getX();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = 0;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = 0;
				}else{
					npoint = 6;
					xpoint = new int[npoint];
					ypoint = new int[npoint];
					xpoint[0] = (int) upL.getP2().getY();
					xpoint[1] = (int) queryPoint.getX();
					xpoint[2] = (int) dwL.getP2().getX();
					xpoint[3] = this.maxX;
					xpoint[4] = 0;
					xpoint[5] = 0;
					ypoint[0] = (int) upL.getP2().getY();
					ypoint[1] = (int) queryPoint.getY();
					ypoint[2] = (int) dwL.getP2().getY();
					ypoint[3] = this.maxY;
					ypoint[4] = this.maxY;
					ypoint[5] = 0;
				}
				Polygon poly = new Polygon(xpoint, ypoint, npoint);
				retval = poly;
			}
		}
		return retval;
	}

	public Polygon getPrunningRegion(PointExt vertex){
		String[] name = vertex.getName().split("v\\(");
		String[] name2 = name[1].split(":");
		name2[1]  = name2[1].substring(0, name2[1].length()-1);
		String a = name2[0];
		String b = name2[1];
		PointExt pa = sortedContainer.getPointByName(a);
		PointExt pb = sortedContainer.getPointByName(b);
		double m;
		double x, y;
		LineExt pruna, prunb;
		Polygon retval = new Polygon();
		if (queryPoint.getX() == pa.getX()) {
				// garis ke atas
			if (pa.getY()<queryPoint.getY()) {
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), 0);
				pruna.setName("q"+pa.getName());
			}else{
				// garis ke bawah
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), this.maxY);
				pruna.setName("q"+pa.getName());
			}
		}else if(queryPoint.getY() == pa.getY()){
			// garis ke kanan
			if (pa.getX()>queryPoint.getX()) {
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), this.maxX, queryPoint.getY());
				pruna.setName("q"+pa.getName());
			}else{
				// garis ke kiri
				pruna = new LineExt();
				pruna.setLine(queryPoint.getX(), queryPoint.getY(), 0, queryPoint.getY());
				pruna.setName("q"+pa.getName());
			}
		}else{
			pruna = new LineExt(queryPoint, pa);
			m = pruna.getM();
			if (pa.getY()>queryPoint.getY()) {
				y = this.maxY;
			}else{
				y = 0;
			}
			x = (((y - queryPoint.getY())+(m*queryPoint.getX()))/m);
			pruna.setLine(queryPoint.getX(), queryPoint.getY(), x, y);
			pruna.setName("q"+pa.getName());

		}

		if (queryPoint.getX() == pb.getX()) {
				// garis ke atas
			if (pb.getY()<queryPoint.getY()) {
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), 0);
				prunb.setName("q"+pb.getName());
			}else{
				// garis ke bawah
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), queryPoint.getX(), this.maxY);
				prunb.setName("q"+pb.getName());
			}
		}else if(queryPoint.getY() == pb.getY()){
			// garis ke kanan
			if (pb.getX()>queryPoint.getX()) {
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), this.maxX, queryPoint.getY());
				prunb.setName("q"+pb.getName());
			}else{
				// garis ke kiri
				prunb = new LineExt();
				prunb.setLine(queryPoint.getX(), queryPoint.getY(), 0, queryPoint.getY());
				prunb.setName("q"+pb.getName());
			}
		}else{
			prunb = new LineExt(queryPoint, pb);
			m = prunb.getM();
			if (pb.getY()>queryPoint.getY()) {
				y = this.maxY;
			}else{
				y = 0;
			}
			x = (((y - queryPoint.getY())+(m*queryPoint.getX()))/m);
			prunb.setLine(queryPoint.getX(), queryPoint.getY(), x, y);
			prunb.setName("q"+pb.getName());
		}

		// Polygon checking
		
		if (pruna.getP2().getY() == prunb.getP2().getY()) {
			int npoint = 3;
			int[] xpoint = new int[npoint];
			xpoint[0] = (int) queryPoint.getX();
			xpoint[1] = (int) pruna.getP2().getX();
			xpoint[2] = (int) prunb.getP2().getX();
			int[] ypoint = new int[npoint];
			ypoint[0] = (int) queryPoint.getY();
			ypoint[1] = (int) pruna.getP2().getY();
			ypoint[2] = (int) prunb.getP2().getY();

			Polygon prun = new Polygon(xpoint, ypoint, npoint);

			retval = prun;

		}else{
			PointExt pointa = new PointExt(pruna.getP2().getX(), pruna.getP2().getY());
			PointExt pointb = new PointExt(prunb.getP2().getX(), prunb.getP2().getY());
			LineExt tempLine = new LineExt(pointa, pointb);
			double checkm = tempLine.getM();
			if (checkm == 0) {
				int npoint = 4;
				int[] xpoint = new int[npoint];
				int[] ypoint1 = new int[npoint];
				int[] ypoint2 = new int[npoint];

				xpoint[0] = (int) pruna.getP2().getX();
				xpoint[1] = (int) prunb.getP2().getX();
				xpoint[2] = (int) prunb.getP2().getX();
				xpoint[3] = (int) pruna.getP2().getX();
				

				ypoint1[0] = (int) pruna.getP2().getY();
				ypoint1[1] = (int) prunb.getP2().getY();
				ypoint1[2] = (int) 0;
				ypoint1[3] = (int) 0;

				Polygon polya = new Polygon(xpoint, ypoint1, npoint);

				ypoint2[0] = (int) pruna.getP2().getY();
				ypoint2[1] = (int) prunb.getP2().getY();
				ypoint2[2] = (int) this.maxY;
				ypoint2[3] = (int) this.maxY;

				Polygon polyb = new Polygon(xpoint, ypoint2, npoint);

				if (polya.contains(vertex)) {
					retval =  polya;
				}else{
					retval =  polyb;
				}
			}else if(Double.isInfinite(checkm)){
				int npoint = 4;
				int[] xpoint1 = new int[npoint];
				int[] xpoint2 = new int[npoint];
				int[] ypoint = new int[npoint];

				ypoint[0] = (int) pruna.getP2().getY();
				ypoint[1] = (int) prunb.getP2().getY();
				ypoint[2] = (int) prunb.getP2().getY();
				ypoint[3] = (int) pruna.getP2().getY();
				
				xpoint1[0] = (int) pruna.getP2().getX();
				xpoint1[1] = (int) prunb.getP2().getX();
				xpoint1[2] = (int) 0;
				xpoint1[3] = (int) 0;

				Polygon polya = new Polygon(xpoint1, ypoint, npoint);

				xpoint2[0] = (int) pruna.getP2().getX();
				xpoint2[1] = (int) prunb.getP2().getX();
				xpoint2[2] = (int) this.maxX;
				xpoint2[3] = (int) this.maxX;

				Polygon polyb = new Polygon(xpoint2, ypoint, npoint);

				if (polya.contains(vertex)) {
					retval = polya;
				}else{
					retval = polyb;
				}

			}else{
				if ((pruna.getP2().getX()>0 && pruna.getP2().getX()<this.maxX)&&(prunb.getP2().getX()>0&&prunb.getP2().getX()<this.maxX)) {
					// Bila kedua garis tidak melebihi batas-batas X
					int npoint = 4;
					int[] xpoint1 = new int[npoint];
					int[] xpoint2 = new int[npoint];
					int[] ypoint = new int[npoint];

					ypoint[0] = (int) pruna.getP2().getY();
					ypoint[1] = (int) prunb.getP2().getY();
					ypoint[2] = (int) prunb.getP2().getY();
					ypoint[3] = (int) pruna.getP2().getY();

					xpoint1[0] = (int) pruna.getP2().getX();
					xpoint1[1] = (int) prunb.getP2().getX();
					xpoint1[2] = (int) 0;
					xpoint1[3] = (int) 0;

					Polygon polya = new Polygon(xpoint1, ypoint, npoint);

					xpoint2[0] = (int) pruna.getP2().getX();
					xpoint2[1] = (int) prunb.getP2().getX();
					xpoint2[2] = (int) this.maxX;
					xpoint2[3] = (int) this.maxX;

					Polygon polyb = new Polygon(xpoint2, ypoint, npoint);

					if (polya.contains(vertex)) {
						retval = polya;
					}else{
						retval = polyb;
					}
				}else if ((pruna.getP2().getX()<0 && pruna.getP2().getX()>this.maxX)&&(prunb.getP2().getX()<0&&prunb.getP2().getX()>this.maxX)) {
					int npoint = 3;
					int[] xpoint1 = new int[npoint];
					int[] xpoint2 = new int[npoint];
					int[] ypoint1 = new int[npoint];
					int[] ypoint2 = new int[npoint];

					xpoint1[0] = (int) pruna.getP2().getX();
					xpoint1[1] = (int) prunb.getP2().getX();
					xpoint1[2] = (int) pruna.getP2().getX();

					ypoint1[0] = (int) pruna.getP2().getY();
					ypoint1[1] = (int) prunb.getP2().getY();
					ypoint1[2] = (int) prunb.getP2().getY();

					Polygon polya = new Polygon(xpoint1, ypoint1, npoint);

					xpoint2[0] = (int) pruna.getP2().getX();
					xpoint2[1] = (int) prunb.getP2().getX();
					xpoint2[2] = (int) prunb.getP2().getX();

					ypoint2[0] = (int) pruna.getP2().getY();
					ypoint2[1] = (int) prunb.getP2().getY();
					ypoint2[2] = (int) pruna.getP2().getY();

					Polygon polyb = new Polygon(xpoint2, ypoint2, npoint);

					if (polya.contains(vertex)) {
						retval = polya;
					}else{
						retval = polyb;
					}
				}else if((pruna.getP2().getX()>0 && pruna.getP2().getX()<this.maxX)&&(prunb.getP2().getX()<0&&prunb.getP2().getX()>this.maxX)){
					int npoint1 = 3;
					int npoint2 = 4;
					int[] xpoint1 = new int[npoint1];
					int[] xpoint2 = new int[npoint2];
					int[] ypoint1 = new int[npoint1];
					int[] ypoint2 = new int[npoint2];

					xpoint1[0] = (int) pruna.getP2().getX();
					xpoint1[1] = (int) prunb.getP2().getX();
					xpoint1[2] = (int) prunb.getP2().getX();

					ypoint1[0] = (int) pruna.getP2().getY();
					ypoint1[1] = (int) prunb.getP2().getY();
					ypoint1[2] = (int) pruna.getP2().getY();

					Polygon polya = new Polygon(xpoint1, ypoint1, npoint1);

					xpoint2[0] = (int) pruna.getP2().getX();
					xpoint2[1] = (int) prunb.getP2().getX();
					double check = 0 - prunb.getP2().getX();
					if (check>0) {
						xpoint2[2] = (int) this.maxX;
						xpoint2[3] = (int) this.maxX;
					}else{
						xpoint2[2] = (int) 0;
						xpoint2[3] = (int) 0;
					}
					
					ypoint2[0] = (int) pruna.getP2().getY();
					ypoint2[1] = (int) prunb.getP2().getY();
					ypoint2[2] = (int) prunb.getP2().getY();
					ypoint2[3] = (int) pruna.getP2().getY();

					Polygon polyb = new Polygon(xpoint2, ypoint2, npoint2);

					if (polya.contains(vertex)) {
						retval = polya;
					}else{
						retval = polyb;
					}

				}else if((pruna.getP2().getX()<0 && pruna.getP2().getX()>this.maxX)&&(prunb.getP2().getX()>0&&prunb.getP2().getX()<this.maxX)){
					int npoint1 = 3;
					int npoint2 = 4;
					int[] xpoint1 = new int[npoint1];
					int[] xpoint2 = new int[npoint2];
					int[] ypoint1 = new int[npoint1];
					int[] ypoint2 = new int[npoint2];

					xpoint1[0] = (int) prunb.getP2().getX();
					xpoint1[1] = (int) pruna.getP2().getX();
					xpoint1[2] = (int) pruna.getP2().getX();

					ypoint1[0] = (int) prunb.getP2().getY();
					ypoint1[1] = (int) pruna.getP2().getY();
					ypoint1[2] = (int) prunb.getP2().getY();

					Polygon polya = new Polygon(xpoint1, ypoint1, npoint1);

					xpoint2[0] = (int) prunb.getP2().getX();
					xpoint2[1] = (int) pruna.getP2().getX();
					double check = 0 - pruna.getP2().getX();
					if (check>0) {
						xpoint2[2] = (int) this.maxX;
						xpoint2[3] = (int) this.maxX;
					}else{
						xpoint2[2] = (int) 0;
						xpoint2[3] = (int) 0;
					}
					
					ypoint2[0] = (int) prunb.getP2().getY();
					ypoint2[1] = (int) pruna.getP2().getY();
					ypoint2[2] = (int) pruna.getP2().getY();
					ypoint2[3] = (int) prunb.getP2().getY();

					Polygon polyb = new Polygon(xpoint2, ypoint2, npoint2);

					if (polya.contains(vertex)) {
						retval = polya;
					}else{
						retval = polyb;
					}
				}
			}
		}
		return retval;
	}

	public void czProcess(){
		this.sortPoint();
		this.czProcessing5();
		this.verifyVertex();
		this.jarvisMarch();
		if (sortedVertex.size()>2) {
			this.generatePolygon(sortedVertex);						
		}
		//vertexContainer.printPoint(true);
		System.out.println(bisectContainer.size());
	}
	/**
	DRAWING
	*/

	public LineExt createBisector(PointExt a, PointExt b){
		LineExt line = new LineExt();
		PointExt tempPoint = new PointExt(((b.getX()+a.getX())/2),((b.getY()+a.getY())/2));
		int z = 0;
			if (a.getX() == b.getX()) { 
				z = 1;

				line.setLine(0, tempPoint.getY(), this.maxX, tempPoint.getY());
			}else if(a.getY() == b.getY()) {
				z = 2;
				line.setLine(tempPoint.getX(), 0, tempPoint.getX(), this.maxY);
			}else{
				z =3;
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
			/*if (b.getName().equals("220")) {
					System.out.println(line.getP1()+":"+line.getP2()+tempPoint.printPoint());
			}*/
			/*System.out.println(line.getP1()+" : "+line.getP2());*/
		return line;
	}

	public void cleanBisector(){
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
		//	System.out.println("5");
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
	}

	public void generateVertex(){
		//System.out.println("================");
		for (int i=0;i<bisectContainer.size();i++){		
			for (int j=i+1; j<bisectContainer.size();j++){
				if (bisectContainer.get(i).intersectsLine(bisectContainer.get(j))) {
					String name = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
					PointExt p = new PointExt(name, getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j)));
					if (Double.isNaN(p.getX())) {
						System.out.println("nan detected "+bisectContainer.get(i).getP1()+":"+bisectContainer.get(i).getP2()+" | "+bisectContainer.get(j).getP1()+":"+bisectContainer.get(j).getP2());
					}
					if (!vertexContainer.checkExist(p)) {
						vertexContainer.add(p);
					}
				}else if(!checkParalel(bisectContainer.get(i), bisectContainer.get(j))){
					/*if (bisectContainer.get(i).getName().equals("334")&&bisectContainer.get(j).getName().equals("380")) {
						System.out.println(bisectContainer.get(i).getP1()+":"+bisectContainer.get(i).getP2());
						System.out.println(bisectContainer.get(j).getP1()+":"+bisectContainer.get(j).getP2());
					}*/
					boolean found = false;
					double upx, upy, downx, downy, m, x1, y1;
					double upix;
					double upiy, downix, downiy, upjx, upjy, downjx, downjy;

					if (bisectContainer.get(i).getM()==0) {
						upix = bisectContainer.get(i).getP1().getX();
						downix = bisectContainer.get(i).getP2().getX();
						upiy = bisectContainer.get(i).getP1().getY();
						downiy = bisectContainer.get(i).getP2().getY();
					}else{
						upix = 0;
						downix = 0;
						upiy = 0;
						downiy = 0;
					}

					if (bisectContainer.get(j).getM()==0) {
						upjx = bisectContainer.get(j).getP1().getX();
						downjx = bisectContainer.get(j).getP2().getX();
						upjy = bisectContainer.get(i).getP1().getY();
						downjy = bisectContainer.get(i).getP2().getY();
					}else{
						upjx = 0;
						downjx = 0;
						upjy = 0;
						downjy = 0;
					}

					upy = 0;
					downy = maxY;
					while(!found){
						if (bisectContainer.get(i).getM()==0) {
							if (upix < downix) {
								upix = upix - maxX;
								downix = downix + maxX;
							}else{
								upix = upix + maxX;
								downix = downix - maxX;
							}
						}else{
							m = bisectContainer.get(i).getM();
							x1 = bisectContainer.get(i).getP1().getX();
							y1 = bisectContainer.get(i).getP1().getY();
							downiy = downiy + maxY;

							downix = ((downiy-y1)/m)+x1;

							m = bisectContainer.get(i).getM();
							x1 = bisectContainer.get(i).getP2().getX();
							y1 = bisectContainer.get(i).getP2().getY();
							upiy = upiy - maxY;

							upix = ((upiy-y1)/m)+x1;
						}
						bisectContainer.get(i).setLine(upix, upiy, downix, downiy);
						// extend j

						if (bisectContainer.get(j).getM()==0) {
							if (upjx < downjx) {
								upjx = upjx - maxX;
								downjx = downjx + maxX;
							}else{
								upjx = upjx + maxX;
								downjx = downjx - maxX;
							}
						}else{
							m = bisectContainer.get(j).getM();
							x1 = bisectContainer.get(j).getP1().getX();
							y1 = bisectContainer.get(j).getP1().getY();
							downjy = downjy + maxY;

							downjx = ((downjy-y1)/m)+x1;

							m = bisectContainer.get(j).getM();
							x1 = bisectContainer.get(j).getP2().getX();
							y1 = bisectContainer.get(j).getP2().getY();
							upjy = upjy - maxY;

							upjx = ((upjy-y1)/m)+x1;
						}
						bisectContainer.get(j).setLine(upjx, upjy, downjx, downjy);
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

	public void generateVertex2(){
		for (int i=0;i<bisectContainer.size()-1;i++){		
			for (int j=i+1; j<bisectContainer.size();j++){
				//System.out.println(bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName());
				if (bisectContainer.get(i).intersectsLine(bisectContainer.get(j))) {
					String name = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
					PointExt p = new PointExt(name, getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j)));
					if (Double.isNaN(p.getX())) {
						System.out.println("nan detected "+bisectContainer.get(i).getP1()+":"+bisectContainer.get(i).getP2()+" | "+bisectContainer.get(j).getP1()+":"+bisectContainer.get(j).getP2());
					}
					if (!tempVertex.checkExist(p)) {
						tempVertex.add(p);
					}
				}else if(!checkParalel(bisectContainer.get(i), bisectContainer.get(j))){
					boolean found = false;
					double upx, upy, downx, downy, m, x1, y1;
					double upix;
					double upiy, downix, downiy, upjx, upjy, downjx, downjy;

					if (bisectContainer.get(i).getM()==0) {
						upix = bisectContainer.get(i).getP1().getX();
						downix = bisectContainer.get(i).getP2().getX();
						upiy = bisectContainer.get(i).getP1().getY();
						downiy = bisectContainer.get(i).getP2().getY();
					}else{
						upix = 0;
						downix = 0;
						upiy = 0;
						downiy = 0;
					}

					if (bisectContainer.get(j).getM()==0) {
						upjx = bisectContainer.get(j).getP1().getX();
						downjx = bisectContainer.get(j).getP2().getX();
						upjy = bisectContainer.get(i).getP1().getY();
						downjy = bisectContainer.get(i).getP2().getY();
					}else{
						upjx = 0;
						downjx = 0;
						upjy = 0;
						downjy = 0;
					}

					upy = 0;
					downy = maxY;
					while(!found){
						if (bisectContainer.get(i).getM()==0) {
							if (upix < downix) {
								upix = upix - maxX;
								downix = downix + maxX;
							}else{
								upix = upix + maxX;
								downix = downix - maxX;
							}
						}else{
							m = bisectContainer.get(i).getM();
							x1 = bisectContainer.get(i).getP1().getX();
							y1 = bisectContainer.get(i).getP1().getY();
							downiy = downiy + maxY;

							downix = ((downiy-y1)/m)+x1;

							m = bisectContainer.get(i).getM();
							x1 = bisectContainer.get(i).getP2().getX();
							y1 = bisectContainer.get(i).getP2().getY();
							upiy = upiy - maxY;

							upix = ((upiy-y1)/m)+x1;
						}
						bisectContainer.get(i).setLine(upix, upiy, downix, downiy);
						// extend j

						if (bisectContainer.get(j).getM()==0) {
							if (upjx < downjx) {
								upjx = upjx - maxX;
								downjx = downjx + maxX;
							}else{
								upjx = upjx + maxX;
								downjx = downjx - maxX;
							}
						}else{
							m = bisectContainer.get(j).getM();
							x1 = bisectContainer.get(j).getP1().getX();
							y1 = bisectContainer.get(j).getP1().getY();
							downjy = downjy + maxY;

							downjx = ((downjy-y1)/m)+x1;

							m = bisectContainer.get(j).getM();
							x1 = bisectContainer.get(j).getP2().getX();
							y1 = bisectContainer.get(j).getP2().getY();
							upjy = upjy - maxY;

							upjx = ((upjy-y1)/m)+x1;
						}
						bisectContainer.get(j).setLine(upjx, upjy, downjx, downjy);
						if (bisectContainer.get(i).intersectsLine(bisectContainer.get(j))) {
							found = true;
						}
					}

					String newName = "v("+bisectContainer.get(i).getName()+":"+bisectContainer.get(j).getName()+")";
					PointExt newP = getIntersectionPoint(bisectContainer.get(i), bisectContainer.get(j));
					newP.setName(newName);
					if (!tempVertex.checkExist(newP)) {
						tempVertex.add(newP);
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
						//System.out.println(checkLine.getName()+":"+checkLine.getP1()+"-"+checkLine.getP2()+":"+bisect.getName()+":"+bisect.getP1()+"-"+bisect.getP2());
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
						}else if(((ax-bx<0.0001)&&(ax-bx>-0.0001))&&((ay-by<0.0001)&&(ay-by>-0.0001))){
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

	public void verifyVertex2(){
		PointList flag = new PointList();
			LineExt checkLine = null;
			PointExt checkPoint = null;
			boolean flagVertex = false;
			// Vertex Checking
			for(PointExt temp : tempVertex){
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
				if (tempVertex.contains(temp)) {
					int i = tempVertex.indexOf(temp);
					tempVertex.remove(i);
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
			int test = 0;
			
			while ((currPoint!=maxPoint)) {
				maxAngle = currPoint;
				for (int i=0;i<vertexContainer.size();i++) {
					if ((findAngle(vertexContainer.get(currPoint), vertexContainer.get(maxAngle))<findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))) && (notUsed(usedPoint, i) || i == maxPoint) && (findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))<=180)){
						maxAngle = i;
					}else if ((findAngle(vertexContainer.get(currPoint), vertexContainer.get(maxAngle)) == findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))) && (notUsed(usedPoint, i) || i == maxPoint) && (findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))<=180)) {
						if(vertexContainer.get(i).getX()<vertexContainer.get(maxAngle).getX()){
							maxAngle = i;
						}
					}
				}
				currPoint = maxAngle;
				addUsedPoint(usedPoint, currPoint);
			}
			currPoint = maxPoint;
			while(currPoint!=minPoint){
				
				minAngle = minPoint;
				for (int i = 0;i<vertexContainer.size();i++) {
					if ((findAngle(vertexContainer.get(currPoint), vertexContainer.get(minAngle))<findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))) && (notUsed(usedPoint, i)) && (findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))>=180)){
						minAngle = i;						
					}else if ((findAngle(vertexContainer.get(currPoint), vertexContainer.get(minAngle))==findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))) && (notUsed(usedPoint, i)) && (findAngle(vertexContainer.get(currPoint), vertexContainer.get(i))>=180)) {
						if(vertexContainer.get(i).getX()>vertexContainer.get(minAngle).getX()){
							minAngle = i;
						}
					}
				}
				currPoint = minAngle;
				addUsedPoint(usedPoint, currPoint);
			}
			sortedVertex.clear();
			
			for (int i=0;i<usedPoint.length;i++) {
				if (usedPoint[i]!=-1) {
					if (sortedVertex.indexOf(vertexContainer.get(usedPoint[i]))==-1) {
						sortedVertex.add(vertexContainer.get(usedPoint[i]));
					}	
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
		return roundDouble(angle);
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
		}else if((a.getM() - b.getM()>-0.01)&&(a.getM() - b.getM()<0.01)){
			return true;
		}
		return false;
	}

	public double roundDouble(double d){
		DecimalFormat a = new DecimalFormat("#.####");
		//System.out.println(Double.valueOf(a.format(d)));
		return Double.valueOf(a.format(d));
	}

	public void printArray(int[] a){
		for (int b : a) {
			System.out.print("[ ");
			System.out.print(b+",");
			System.out.println("] ");
		}
	}

	public int turn(PointExt p, PointExt q, PointExt r){
		double res = (q.getX() - p.getX())*(r.getY() - p.getY()) - (r.getX() - p.getX())*(q.getY()-p.getY());

		if (res>0) {
			return 1;
		}else if(res<0){
			return -1;
		}else{
			return 0;
		}
	}

    public PointExt nextHull(PointList a, PointExt p){
    	PointExt q = p;
    	int t;
    	for ( PointExt r : a ) {
    		t = turn(p, q, r);
    		if ((t == -1)||(t == 0) && (p.distanceSq(r) > p.distanceSq(q))) {
    			q = r;
    		}
    	}
    	return q;
    }

    public void jarvisMarch2(){
    	PointExt minPoint = vertexContainer.get(0);
    	for (PointExt temp : vertexContainer) {
    		if (temp.getY() < minPoint.getY()){
				minPoint = temp;
			}
    	}
    	sortedVertex.add(minPoint);
    	for (PointExt p : sortedVertex ) {
    		PointExt q = nextHull(vertexContainer, p);
    		if (q!=vertexContainer.get(0)) {
    			if (sortedVertex.checkExist(q)) {
    				sortedVertex.add(q);
    			}
    		}
    	}
    }
   public double getMeasure(Polygon p){
   		int n = p.npoints;
   		int[] xpoint = new int[n];
   		int[] ypoint = new int[n];
   		xpoint = p.xpoints;
   		ypoint = p.ypoints;
   		double area = 0;
		for(int i = 0; i <= n-1; i++){
			if(i == n-1){
				area += (xpoint[i]*ypoint[0])-(xpoint[0]*ypoint[i]);
			}else{
				area += (xpoint[i]*ypoint[i+1])-(xpoint[i+1]*ypoint[i]);
			}
		}
		area /= 2;
		//if they enter points counterclockwise the area will be negative but correct.
		if(area < 0)
			area *= -1;
		return area;
	}
	
}