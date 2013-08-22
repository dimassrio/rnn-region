/**
PROGRAM UTAMA
*/
import javax.swing.JApplet;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
UI Class, designed to have only UI component inside it,
*/
public class FinalApp extends JApplet implements Runnable, ActionListener, MouseListener,MouseMotionListener , TableModelListener{
	// Application Constant
	public final static String WINDOWS_TITLE = "Reverse Nearest Neighbour dengan Region";
	public final static int WINDOWS_WIDTH = 1000;
	public final static int WINDOWS_HEIGHT = 1000;
	// Change drawing orientation, true (0,0) at bottom-left or false for (0,0) at up-left
	public final static boolean APP_ORI = false; 
	// Main Panel Initialization
	private JTabbedPane basePanel = new JTabbedPane();
	private JScrollPane scrollPanel = new JScrollPane();
	private JScrollPane dataPanel = new JScrollPane();
	private JScrollPane logPanel = new JScrollPane();
	private FinalAppPanel appPanel = new FinalAppPanel(this);
	private JPanel tablePanel = new JPanel();
	public final static int MAX_X = 1200;
	public final static int MAX_Y = 1200;
	// Data Table Initialization
	private final static String[] columnName = {"Point Name", "Point X", "Point Y"};
	private DefaultTableModel dataModel = new DefaultTableModel(columnName, 0);
	private JTable dataTable = new JTable(dataModel);
	// Log Initialization
	private JTextArea logText = new JTextArea(100,100);
	// Button Iniliatization
	private JButton newButton = new JButton("New");
	private JButton processButton = new JButton("Process");
	private JButton openButton = new JButton("Open");
	private JButton loadButton = new JButton("Load");
	private JButton saveButton = new JButton("Save");
	private JCheckBox pointBox = new JCheckBox("point");
	private JCheckBox lineBox = new JCheckBox("line");
	private JCheckBox areaBox = new JCheckBox("area");
	// Status Panel Inialization
	private String statusText = "Application Ready : ";
	private JLabel statusLabel = new JLabel(statusText, JLabel.LEFT);
	private JLabel coordinateLabel = new JLabel();
	// Opsi query
	private JComboBox<String> opsiInit =  new JComboBox<String>();
	// Contet Window
	private PopUpDemo appContext = new PopUpDemo(appPanel, this);

	public static void main(String[] args) {
		// Stand Alone Application initialization
		FinalApp app = new FinalApp();
		app.init();
		// Window Set Up
		JFrame mainWindow = new JFrame();
		mainWindow.setSize(WINDOWS_WIDTH, WINDOWS_HEIGHT);
		mainWindow.setTitle(WINDOWS_TITLE);
		mainWindow.setLayout(new BorderLayout());
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.add(app, "Center");
		mainWindow.setVisible(true);


	}

	public void init(){
		try {SwingUtilities.invokeAndWait(this);}
		catch (Exception e) {System.err.println("Initialization Failure");}
	}

	public void run(){
		setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		newButton.setIcon(new ImageIcon("images/glyphicons_036_file.png"));
		newButton.setIconTextGap(10);
		buttonPanel.add(newButton);
		openButton.setIcon(new ImageIcon("images/glyphicons_358_file_import.png"));
		openButton.setIconTextGap(10);
		buttonPanel.add(openButton);
		buttonPanel.add(loadButton);
		opsiInit.setPreferredSize(new Dimension(50, 30));
		buttonPanel.add(opsiInit);
		processButton.setIcon(new ImageIcon("images/glyphicons_193_circle_ok.png"));
		processButton.setIconTextGap(10);
		buttonPanel.add(processButton);
		saveButton.setIcon(new ImageIcon("images/glyphicons_359_file_export.png"));
		saveButton.setIconTextGap(10);
		buttonPanel.add(saveButton);
		coordinateLabel.setPreferredSize(new Dimension(200, 50));
		coordinateLabel.setText("( 0,0 )");
		coordinateLabel.setIcon(new ImageIcon("images/glyphicons_233_direction.png"));
		buttonPanel.add(this.createVerticalSeparator(3, 30));
		buttonPanel.add(coordinateLabel);

		JPanel statusPanel = new JPanel();
		statusPanel.add(statusLabel);

		appPanel.setPreferredSize(new Dimension(MAX_X,MAX_Y));
		appPanel.setBackground(Color.white);

		logText.setEditable(false);
		logPanel.setViewportView(logText);
		// Giving Listener
		appPanel.addMouseListener(this);
		appPanel.addMouseMotionListener(this);
		newButton.addActionListener(this);
		openButton.addActionListener(this);
		loadButton.addActionListener(this);
		processButton.addActionListener(this);
		saveButton.addActionListener(this);
		// Main Panel set up
		JViewport a = new JViewport();
		a.setView(appPanel);
		if (this.APP_ORI) {
			a.setViewPosition(new Point(0, this.MAX_Y - this.WINDOWS_HEIGHT + 173));
		}
		scrollPanel.setViewport(a);
		dataPanel.setViewportView(dataTable);
		basePanel.add("Drawing Panel", scrollPanel);
		basePanel.add("Data Panel", dataPanel);
		basePanel.add("Log Panel", logPanel);
		basePanel.setMnemonicAt(0, KeyEvent.VK_1);
		basePanel.setMnemonicAt(1, KeyEvent.VK_2);
		basePanel.setMnemonicAt(2, KeyEvent.VK_3);
		// Finalization
		this.add(basePanel, "Center");
		this.add(buttonPanel, "North");
		this.add(statusPanel, "South");
	}

	public void actionPerformed(ActionEvent e){
		JFileChooser fd = new JFileChooser();	
		if (e.getSource()==newButton) {
			appPanel.clearPoint();
			appPanel.repaint();
			dataModel.setDataVector(appPanel.pointContainer.getDataModel(), columnName);
			statusText = "Panel cleared";
			dataRefresh();
		}else if(e.getSource()==openButton){
			int returnVal = fd.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File openFile = fd.getSelectedFile();
				statusText = appPanel.openFile(openFile);
			}
			dataRefresh();
		}else if(e.getSource()==loadButton){
			Access a = new Access();
			appPanel.setPointContainer(a.getData("peers10", "limit 10"));
			dataRefresh();
			repaint();
		}else if(e.getSource()==saveButton){
			int returnVal = fd.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = fd.getSelectedFile();
				statusText = appPanel.saveFile(saveFile);
			}
			dataRefresh();
		}else if(e.getSource()==processButton){
			appPanel.processPoint();
		}
	}

	public void mouseClicked(MouseEvent e){}

	public void mousePressed(MouseEvent e){
		// Left Click Action
		if (e.getButton() == e.BUTTON1) {
			if (!appContext.isShowing()) {
				if (e.getSource()!= appPanel) {
					return;
				}
				PointExt inputPoint = null;
				if (this.APP_ORI) {
					inputPoint = new PointExt(appPanel.nextName(), e.getX(),this.MAX_Y - e.getY());	
				}else{
					inputPoint = new PointExt(appPanel.nextName(), e.getX(), e.getY());
				}				
					appPanel.addPoint(inputPoint);
					statusText = "Point added : "+inputPoint.printPoint();
					appPanel.repaint();
				}
		}
		// Right Click Action
		else if(e.getButton() == e.BUTTON3){
			PointExt temp = null;
			if (this.APP_ORI) {
				temp = appPanel.getPoint(e.getX(), this.MAX_Y - e.getY());
			}else{
				temp = appPanel.getPoint(e.getX(), e.getY());
			}
			appPanel.changePointStatus(temp);
			repaint();
			appContext.show(appPanel, e.getX(), e.getY());
		}
		dataRefresh();
	}

	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}

	public void mouseMoved(MouseEvent e){
		if (e.getSource()!= appPanel) {
			return;
		}
		if (this.APP_ORI) {
			coordinateLabel.setText("( "+e.getX()+" , "+(this.MAX_Y - e.getY())+" )");
		}else{
			coordinateLabel.setText("( "+e.getX()+" , "+e.getY()+" )");
		}
	}

	public void mouseDragged(MouseEvent e){}
	public void tableChanged(TableModelEvent e){}

	// Refresh all ui related component
	public void dataRefresh(){
		this.statusLabel.setText(statusText);
		this.dataModel.setDataVector(appPanel.pointContainer.getDataModel(), columnName);
		this.opsiInit.setModel(new DefaultComboBoxModel<String>(appPanel.pointContainer.getComboBox()));
		String newline = "\n";
		logText.append(statusText + newline);
	}

	public DefaultTableModel getDataModel(){
		return this.dataModel;
	}

	public void setStatusText(String data){
		this.statusText = data;
	}

	public JComponent createVerticalSeparator(int width, int height) {  
        JSeparator x = new JSeparator(SwingConstants.VERTICAL);  
        x.setPreferredSize(new Dimension(width,height));  
        return x;  
    }

    public JComboBox<String> getOpsi(){
    	return this.opsiInit;
    }

    public JTextArea getLogText(){
    	return this.logText;
    }
}

/**
UI Panel Class, all drawing related method is defined here.
*/
class FinalAppPanel extends JPanel{
	private static int pointRadius = 3;

	private FinalApp controller;
	private Graphics2D g;
	public PointList pointContainer = new PointList();
	public PointList vertexContainer = new PointList();
	public LineList perpendicularList = new LineList();
	public Path2D.Double polygon = null;
	public Polygon poly = null;
	/**
	* MVC initialization 
	* @param controller of this panel
	*/
	public FinalAppPanel(FinalApp controller){
		this.controller = controller;
	}
	/**
	* Adding point method, point based on mouse activity inside panel will be created and inserted in pointContainer variable,
	* @param point created upon left click event.
	*/
	public void addPoint(PointExt point){
		this.pointContainer.add(point);
	}

	public PointExt getPoint(double x, double y){
		PointExt temp = new PointExt();
		for(int i=0;i<pointContainer.size();i++){
			if( ((pointContainer.get(i).getX()+3 >= x)&&(pointContainer.get(i).getX()-3 <= x)) && ((pointContainer.get(i).getY()-3<=y)&&(pointContainer.get(i).getY()+3>=y))) {
				return pointContainer.get(i);
			}
		}
		return temp;
	}

	public void setPointContainer(PointList a){
		this.pointContainer = a;
	}

	public String removePointSelected(){
		String data = "";
		for (int i = 0;i<pointContainer.size() ;i++ ) {
			if (pointContainer.get(i).getSelected()) {
				data = "Point "+pointContainer.get(i).getName()+" deleted.";
				pointContainer.remove(i);
				repaint();
			}
		}
		return data;
	}

	public void changePointStatus(PointExt point){
		for (int i = 0;i<pointContainer.size() ;i++ ) {
			pointContainer.get(i).changeSelected(false);
		}
		point.changeSelected();
	}

	public String changePointLocation(double x, double y){
		String data = "";
		for (int i = 0; i<pointContainer.size() ; i++ ) {
			if (pointContainer.get(i).getSelected()) {
				data = pointContainer.get(i).printPoint()+" moved to ";
				pointContainer.get(i).setLocation(x,y);
				data = data+pointContainer.get(i).printPoint();
				controller.getDataModel().setValueAt(x, i, 1);
				controller.getDataModel().setValueAt(y, i, 2);
			}
		}
		repaint();
		return data;
	}

	public void clearPoint(){
		pointContainer.clear();
		perpendicularList.clear();
		vertexContainer.clear();
		poly = null;
		polygon = null;
	}
	//
	public void processPoint(){
		String choice = (String) controller.getOpsi().getSelectedItem();
		ProcessApp dataProcess = new ProcessApp(pointContainer, pointContainer.getPointByName(choice), controller.MAX_X, controller.MAX_Y);
		controller.getLogText().append("Process Started ---------- !!\n");
		controller.getLogText().append("Query Point input : "+pointContainer.getPointByName(choice).printPoint()+"\n");
		dataProcess.startProcess();
		perpendicularList = dataProcess.getBisect();
		vertexContainer = dataProcess.getVertex();
		poly = dataProcess.getPolygon();
		repaint();
		/*AngleApp dataAngle = new AngleApp();
		dataAngle.setPointList(pointContainer);
		dataAngle.startProcess();*/

	}
	// Draw Method
	public void draw(PointExt point){
		int r = pointRadius;
		int x = (int) point.getX();
		int y = (int) point.getY();
		if (point.getSelected()) {
			g.setColor(Color.blue);
		}else{
			g.setColor(Color.red);
		}
		if(point.getDraw()){
			g.fillOval(x-r, y-r, r+r, r+r);
			g.setColor(Color.gray);
			if (controller.APP_ORI) {
				g.rotate(Math.PI, x, y);
				g.scale(-1.0, 1.0);
				g.drawString(point.getName()+" ("+x+","+y+")",0-x,y);
				g.scale(-1.0, 1.0);
				g.rotate(-Math.PI, x, y);
			}else{
				g.drawString(point.getName()+" ("+x+","+y+")",x,y);
			}
			
 		}
	}

	public void draw(LineExt line){
		g.setColor(Color.blue);
		int p1_x = (int) line.getP1().getX();
		int p1_y = (int) line.getP1().getY();
		int p2_x = (int) line.getP2().getX();
		int p2_y = (int) line.getP2().getY();
		g.drawLine(p1_x, p1_y, p2_x, p2_y);
	}

	public void draw(Path2D.Double polygon){
		Color warna = new Color(62,173,255,130);
		g.setColor(warna);
		g.draw(polygon);
		g.fill(polygon);
	}

	public void draw(Polygon poly){
		Color warna = new Color(62,173,255,130);
		g.setColor(warna);
		g.fill(poly);
/*		System.out.println("Polygon draw");
*/	}
	// Final Paint
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		this.g = (Graphics2D) g;
		if (controller.APP_ORI) {
			this.g.translate(0.0, controller.MAX_Y);  // Move the origin to the lower left
			this.g.scale(1.0, -1.0); 
		}
		if (pointContainer.size()>0) {
			for (int i=0;i<pointContainer.size() ;i++ ) {
				this.draw(pointContainer.get(i));
			}
		}

		if (perpendicularList.size()>0) {
			for (int i=0;i<perpendicularList.size() ;i++ ) {
				this.draw(perpendicularList.get(i));
			}
		}

		if (vertexContainer.size()>0) {
			for (int i=0;i<vertexContainer.size() ;i++ ) {
				this.draw(vertexContainer.get(i));
			}
		}
		if (polygon!=null) {
			this.draw(polygon);
		}

		if (poly != null) {
			this.draw(poly);
		}
	}

	/**
	* File Management
	* Opening a txt or related file with a format name (x, y)
	* @param file opened
	*/
	public String openFile(File of){
		String data = "Load data from : "+of.getAbsolutePath();
		try{
			pointContainer.clear();
			pointContainer = processFile(of.getAbsolutePath());
			repaint();
			data = "File "+of.getAbsolutePath()+" Loaded.";
		}catch(FileNotFoundException ex){
			data = "File "+of.getAbsolutePath()+" is not found. Please check again.";
		}
		return data;
	}

	public String saveFile(File of){
		String data = "Save data to : "+of.getAbsolutePath();
		try{
			/*String content = "";*/

			FileWriter fw = new FileWriter(of.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (PointExt p : this.pointContainer ) {
				/*content = content+p.getName();
				content = content+"("+p.getX()+","+p.getY()+")";
				content = content+'\n';*/
				bw.write(p.getName()+"("+p.getX()+","+p.getY()+")");
				bw.newLine();
			}

			if (!of.exists()) {
				of.createNewFile();
			}
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return data;
	}

	public PointList processFile(String str) throws FileNotFoundException	{
		Scanner s = null;
		PointList pContExt = new PointList();
		String[] temp1;
		String[] temp2;
		PointExt tempPointExt;

		try{
			s = new Scanner(new BufferedReader(new FileReader(str)));
			while(s.hasNext()){
				temp1 = s.nextLine().split("\\(");
				temp2 = temp1[1].split(",");
				temp2[1] = temp2[1].substring(0, temp2[1].length()-1);

				double check = Double.parseDouble(temp2[0]);
				double tex = check;
				check = Double.parseDouble(temp2[1]);
				double tey = check;

				tempPointExt = new PointExt(temp1[0], tex, tey);
				pContExt.add(tempPointExt);
			}
		}finally{
			if (s!=null) {
				s.close();
			}
		}
		return pContExt;
	}

	// Point Naming
	public String nextName() {
		char[] ls = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		String r = "";
		int number = this.pointContainer.size();
		boolean stat = true;
		while(true) {
			r = ls[number % 26] + r;
			if(number < 26) {
				break;
			}
			if (number>=26) {
					number /= 26;	
				number -=1;			
			}else{
				number /= 26;
				number -=1;			
			}

		}
			return r;
		
	}

	public static void main(String[] args) {
		
	}

	public PointExt getPointByName(String name){
		PointExt data = new PointExt();
		for (int i=0;i<pointContainer.size() ;i++ ) {
			if (name == pointContainer.get(i).getName()) {
				data = pointContainer.get(i);
			}
		}
		return data;
	}
}
/**
Popup UI Panel, binded to panel class, right click inside panel will activate popup.
*/
class PopUpDemo extends JPopupMenu implements ActionListener {
	public FinalAppPanel controller;
	public FinalApp ui;
	private boolean show;
	private JOptionPane dialog;
    JMenuItem deletePointItem;
    JMenuItem movePointItem;

    public PopUpDemo(FinalAppPanel controller, FinalApp ui){
    	this.controller = controller;
    	this.ui = ui;
    	this.show = true;
        deletePointItem = new JMenuItem("Delete Point (D)");
        movePointItem = new JMenuItem("Move Point (M)");
        deletePointItem.addActionListener(this);
        movePointItem.addActionListener(this);
        add(deletePointItem);
        addSeparator();
        add(movePointItem);
    }

	public void actionPerformed(ActionEvent e){
		if (e.getSource()==deletePointItem) {
			ui.setStatusText(controller.removePointSelected());
			this.show = false;
			repaint();
		}else if(e.getSource()==movePointItem){
			Object[] userButton= {"Ok"};
			String strX = dialog.showInputDialog(ui, "Input new X location");
			String strY = dialog.showInputDialog(ui, "Input new Y location");
			if ((strX != "")&&(strY!="")){
				double newX = Double.parseDouble(strX);
				double newY = Double.parseDouble(strY);
				ui.setStatusText(controller.changePointLocation(newX, newY));
			}
		}
		ui.dataRefresh();
	}

	public boolean isShowing(){
		boolean temp = this.show;
		this.show = super.isShowing();
		return temp;
	}	
}