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

public class FinalApp extends JApplet implements Runnable, ActionListener, MouseListener,MouseMotionListener , TableModelListener{
	// Application Constant
	public final static String WINDOWS_TITLE = "Reverse Nearest Neighbour dengan Region";
	public final static int WINDOWS_WIDTH = 800;
	public final static int WINDOWS_HEIGHT = 600;
	// Main Panel Initialization
	private JTabbedPane basePanel = new JTabbedPane();
	private JScrollPane scrollPanel = new JScrollPane();
	private JScrollPane dataPanel = new JScrollPane();
	private JScrollPane logPanel = new JScrollPane();
	private FinalAppPanel appPanel = new FinalAppPanel(this);
	private JPanel tablePanel = new JPanel();
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
		buttonPanel.add(newButton);
		buttonPanel.add(openButton);
		buttonPanel.add(opsiInit);
		buttonPanel.add(processButton);
		buttonPanel.add(coordinateLabel);

		JPanel statusPanel = new JPanel();
		statusPanel.add(statusLabel);

		appPanel.setPreferredSize(new Dimension(2000,2000));
		appPanel.setBackground(Color.white);

		logText.setEditable(false);
		logPanel.setViewportView(logText);
		// Giving Listener
		appPanel.addMouseListener(this);
		appPanel.addMouseMotionListener(this);
		newButton.addActionListener(this);
		openButton.addActionListener(this);
		// Main Panel set up
		scrollPanel.setViewportView(appPanel);
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
		if (e.getSource()==newButton) {
			appPanel.clearPoint();
			appPanel.repaint();
			dataModel.setDataVector(appPanel.pointContainer.getDataModel(), columnName);
			statusText = "Panel cleared";
		}else if(e.getSource()==openButton){
			JFileChooser fd = new JFileChooser();
			int returnVal = fd.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File openFile = fd.getSelectedFile();
				statusText = appPanel.openFile(openFile);
			}
		}
		dataRefresh();
	}
	public void mouseClicked(MouseEvent e){}
	public void mousePressed(MouseEvent e){
		// Left Click Action
		if (e.getButton() == e.BUTTON1) {
			if (!appContext.isShowing()) {
				if (e.getSource()!= appPanel) {
					return;
				}
				PointExt inputPoint = new PointExt(appPanel.nextName(), e.getX(), e.getY());
					appPanel.addPoint(inputPoint);
					statusText = "Point added : "+inputPoint.printPoint();
					appPanel.repaint();
				}
		}
		// Right Click Action
		else if(e.getButton() == e.BUTTON3){
			PointExt temp = appPanel.getPoint(e.getX(), e.getY());
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
		coordinateLabel.setText("( "+e.getX()+" , "+e.getY()+" )");
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
}

/**
PANEL GAMBAR
*/

class FinalAppPanel extends JPanel{
	private static int pointRadius = 3;

	private FinalApp controller;
	private Graphics2D g;
	public PointList pointContainer = new PointList();

	public FinalAppPanel(FinalApp controller){
		this.controller = controller;
	}
	// Point Manipulation
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
		g.fillOval(x-r, y-r, r+r, r+r);
		g.setColor(Color.gray);
		g.drawString(point.getName()+" ("+x+","+y+")",x,y);
	}

	// Final Paint
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		this.g = (Graphics2D) g;
		if (pointContainer.size()>0) {
			for (int i=0;i<pointContainer.size() ;i++ ) {
				this.draw(pointContainer.get(i));
			}
		}
	}

	// File Management
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
}

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