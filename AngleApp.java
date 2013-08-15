public class AngleApp{
	private PointList pointData = new PointList();
	private LineList data = new LineList();

	public void setLineList(LineList a){
		this.data = a;
	}
	public LineList getLineList(){
		return this.data;
	}

	public void setPointList(PointList a){
		this.pointData = a;
	}
	public PointList getPointList(){
		return this.pointData;
	}

	public void startProcess(){
		for (int i=1;i<this.getPointList().size() ; i++) {
			System.out.println(findAngle(this.getPointList().get(0), this.getPointList().get(i)));
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

	public static void main(String[] args) {
		
	}

}