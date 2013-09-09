import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Scanner;
import java.io.FileReader;
import java.io.BufferedReader;
public class Access {
		Scanner s;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String host;
		String name;
		String user;
		String password;
		/*String user = "root";
		String password = "";*/

	public Access(){
		try{
			Scanner s = new Scanner(new BufferedReader(new FileReader("db-config.txt")));
			while (s.hasNext()) {
				String[] temp1 = s.nextLine().split(":");
				if (temp1[0].equals("host")) {
					host = temp1[1];
				}else if (temp1[0].equals("name")) {
					name = temp1[1];
				}else if (temp1[0].equals("user")) {
					user = temp1[1];
				}else if (temp1[0].equals("password")) {
					password = temp1[1];
					if (password.equals("\"\"")) {
						password = "";
					}
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		finally{
			if (s!=null) {
				s.close();
			}
		}
		String url = "jdbc:mysql://"+host+":3306/"+name;

		try{
			con = DriverManager.getConnection(url, user, password);
		}
		catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		
	}
	public PointList getData(String table){
		return this.getData(table, "");
	}
	public PointList getData(String table, String where){
		String query = null;
		PointExt temp = null;
		PointList rest = new PointList();
		if (where=="") {
			query = "SELECT * FROM "+table;
		}else{
			query = "SELECT * FROM "+table+" "+where;
		}
		try{
			st = con.createStatement();
			rs = st.executeQuery(query);

			while(rs.next()){
				String name = Integer.toString(rs.getInt(1));
				double x = rs.getDouble(2);
				double y = rs.getDouble(3);
				temp = new PointExt(name, x, y);
				rest.add(temp);
			}

		}catch(SQLException ex){
			System.out.println(ex.getMessage());
		}finally{
			try{
				if (st!=null) {
					st.close();
				}
				if (con!=null) {
					con.close();
				}
			}
			catch(SQLException ex){
				System.out.println(ex.getMessage());
			}
		}

		return rest;
	}
}