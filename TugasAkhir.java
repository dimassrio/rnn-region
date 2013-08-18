package tugasakhir;
/**
REVERSE NEAREST NEIGHBOUR WITH REGION
A THESIS FINAL PROJECT
INSTITUT TEKNOLOGI TELKOM COMPUTER SCIENCE UNDER GRADUATE
DIMAS SATRIO : http://dimassrio.com

LICENSED UNDER ACADEMIC USE
**/

/**
PROGRAM UTAMA
*/


import javax.swing.JApplet;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class TugasAkhir extends JApplet implements Runnable, ActionListener, MouseListener,MouseMotionListener , TableModelListener{
	private boolean debug = false;
	private static int windowsWidth = 1024;
	private static int windowsHeight = 720;
	private static String windowsTitle = "Reverse Nearest Neighbour dengan Region";

	private TugasAkhirPanel appPanel = new TugasAkhirPanel(this);
	private JScrollPane basePanel = new JScrollPane();
	private PopUpDemo appContext = new PopUpDemo(appPanel, this);

	private JButton clearButton = new JButton("New");
	private JButton processButton = new JButton("Process");
	private JButton openButton = new JButton("Open");
	private JComboBox<String> opsiInit =  new JComboBox<String>();
	private DefaultComboBoxModel<String> opsiModel = new DefaultComboBoxModel<String>();

	public File openFile;
	String[] clmname = {"Point Name", "Point X", "Point Y"};
	private DefaultTableModel dataModel = new DefaultTableModel(clmname, 0);
	private JTable dataTable = new JTable(dataModel);
	private JScrollPane dataPane = new JScrollPane(dataTable);

	private String statusText = "Application Ready : ";
	private JLabel statusLabel = new JLabel(statusText, JLabel.LEFT);

	public static JMenuBar createMenuBar(){
		JMenuBar appMenu = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_A);
		appMenu.add(menu);
		JMenuItem menuItem = new JMenuItem("New");
		menu.add(menuItem);
		menuItem = new JMenuItem("Open");
		menu.add(menuItem);
		menuItem = new JMenuItem("Save");
		menu.add(menuItem);
		menuItem = new JMenuItem("Exit");
		menu.add(menuItem);
		return appMenu;
	}

	public static void main(String[] args) {
		// Inisialisasi Aplikasi Mandiri / Non Browser required
		TugasAkhir app = new TugasAkhir();
		app.init();
		JFrame mainWindow = new JFrame();
		mainWindow.setSize(windowsWidth, windowsHeight);
		mainWindow.setTitle(windowsTitle);
		mainWindow.setLayout(new BorderLayout());
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//mainWindow.setJMenuBar(createMenuBar());
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
		buttonPanel.add(clearButton);
		buttonPanel.add(openButton);
		buttonPanel.add(opsiInit);
		buttonPanel.add(processButton);
		this.add(buttonPanel, "North");
		appPanel.setBackground(Color.white);
		appPanel.setMinimumSize(new Dimension(500, 500));
		basePanel.setViewportView(appPanel);
		this.add(basePanel, "Center");
		clearButton.addActionListener(this);
		openButton.addActionListener(this);
		appPanel.addMouseListener(this);
		appPanel.addMouseMotionListener(this);
		JPanel tablePanel = new JPanel();
		tablePanel.add(dataPane);
		this.add(tablePanel, "East");
		JPanel statusPanel = new JPanel();
		statusPanel.add(statusLabel);
		this.add(statusPanel, "South");
	}


	public void actionPerformed(ActionEvent e){
		if (e.getSource()==clearButton) {
			appPanel.clearPoint();
			appPanel.repaint();
			dataModel.setDataVector(appPanel.pointContainer.getDataModel(), clmname);
		}else if(e.getSource()==openButton){
			JFileChooser fd = new JFileChooser();
			int returnVal = fd.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File openFile = fd.getSelectedFile();
				appPanel.openFile(openFile);
			}
		}
	}

	public void mouseClicked(MouseEvent e){

	}
	public void mousePressed(MouseEvent e){
		int buttonType = e.getButton();
		if (buttonType == e.BUTTON1) {
			if (!appContext.isShowing()) {
				if (e.getSource()!= appPanel) {
					return;
				}
				PointExt inputPoint = new PointExt(appPanel.nextName(), e.getX(), e.getY());
				if (debug) {
					System.out.println("Input " + inputPoint.printPoint());
				}
					appPanel.addPoint(inputPoint);
					statusText = "Point added : "+inputPoint.printPoint();
					dataRefresh();
					appPanel.repaint();
				}
		}else{
			PointExt temp = appPanel.getPoint(e.getX(), e.getY());
			appPanel.changePointStatus(temp);
			repaint();
			appContext.show(appPanel, e.getX(), e.getY());
		}
	}
	public void mouseReleased(MouseEvent e){

	}

	public void mouseEntered(MouseEvent e){

	}
	public void mouseExited(MouseEvent e){

	}

	public void mouseMoved(MouseEvent e){
	}
	public void mouseDragged(MouseEvent e){}

	public void tableChanged(TableModelEvent e){

	}

	public void dataRefresh(){
		this.statusLabel.setText(statusText);
		this.dataModel.setDataVector(appPanel.pointContainer.getDataModel(), clmname);
		this.opsiInit.setModel(new DefaultComboBoxModel<String>(appPanel.pointContainer.getComboBox()));
	}

	public DefaultTableModel getDataModel(){
		return this.dataModel;
	}
}

/**
PANEL GAMBAR
*/

class TugasAkhirPanel extends JPanel{
	private TugasAkhir controller;
	private Graphics2D g;
	private static int pointRadius = 3;

	public PointList pointContainer = new PointList();
	public TugasAkhirPanel (TugasAkhir controller){
		this.controller = controller;
	}
	public static void main(String[] args) {
		
	}

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

	public void removePointSelected(){
		for (int i = 0;i<pointContainer.size() ;i++ ) {
			if (pointContainer.get(i).getSelected()) {
				pointContainer.remove(i);
				repaint();
			}
		}

		controller.dataRefresh();
		
	}

	public void changePointStatus(PointExt point){
		for (int i = 0;i<pointContainer.size() ;i++ ) {
			pointContainer.get(i).changeSelected(false);
		}
		point.changeSelected();
	}

	public void changePointLocation(double x, double y){
		for (int i = 0; i<pointContainer.size() ; i++ ) {
			if (pointContainer.get(i).getSelected()) {
				pointContainer.get(i).setLocation(x,y);
				controller.getDataModel().setValueAt(x, i, 1);
				controller.getDataModel().setValueAt(y, i, 2);
			}
		}
		repaint();
		controller.dataRefresh();
	}

	public void clearPoint(){
		pointContainer.clear();
	}
/**
Draw Point
*/

	public void draw(PointExt point){
		int r = pointRadius;
		int x = (int) point.getX();
		int y = (int) point.getY();
		if (point.getSelected()) {
			g.setColor(Color.blue);
		}else{
			g.setColor(Color.red);
		}
		g.fillOval(x-r, y-r, r+r, r+r);
		g.setColor(Color.gray);
		g.drawString(point.getName(),x,y);
	}
/**
Painting Component
*/
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		this.g = (Graphics2D) g;

		if (pointContainer.size()>0) {
			for (int i=0;i<pointContainer.size() ;i++ ) {
				this.draw(pointContainer.get(i));
			}
		}
	}

	public void openFile(File of){
		System.out.println("Load data from : "+of.getAbsolutePath());
		try{
			pointContainer.clear();
			pointContainer = processFile(of.getAbsolutePath());
			repaint();
			controller.dataRefresh();
		}catch(FileNotFoundException ex){

		}
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

}

class PopUpDemo extends JPopupMenu implements ActionListener {
	public TugasAkhirPanel controller;
	public TugasAkhir ui;
	private boolean show;
	private JOptionPane dialog;
    JMenuItem deletePointItem;
    JMenuItem movePointItem;

    public PopUpDemo(TugasAkhirPanel controller, TugasAkhir ui){
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
			controller.removePointSelected();
			this.show = false;
			repaint();
		}else if(e.getSource()==movePointItem){
			Object[] userButton= {"Ok"};
			String strX = dialog.showInputDialog(ui, "Input new X location");
			String strY = dialog.showInputDialog(ui, "Input new Y location");
			if ((strX != "")&&(strY!="")){
				double newX = Double.parseDouble(strX);
				double newY = Double.parseDouble(strY);
				controller.changePointLocation(newX, newY);
			}
		}
	}

	public boolean isShowing(){
		boolean temp = this.show;
		this.show = super.isShowing();
		return temp;
	}
}