package service;

import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

 class CallMySql {
     public static String getOne(String query) {
	
	 Connection con = null;
	 Statement st = null;
	 ResultSet rs = null;

	 String url = "jdbc:mysql://localhost:3306/fresta";
	 String user = "root";
	 String password = "root";

	 try {
	     con = DriverManager.getConnection(url, user, password);
	     st = con.createStatement();
	     rs = st.executeQuery(query);

	     if (rs.next()) {
		 return rs.getString(1);
	     } else {
		 return "none";
	     }

	 } catch (SQLException ex) {
	     System.out.println("db error for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);

	 } finally {
	     try {
		 if (rs != null) {
		     rs.close();
		 }
		 if (st != null) {
		     st.close();
		 }
		 if (con != null) {
		     con.close();
		 }

	     } catch (SQLException ex) {
		 System.out.println("db error for " + query);
		 System.out.println(ex.getMessage());
		 //Logger lgr = Logger.getLogger(Test.class.getName());
		 //lgr.log(Level.WARNING, ex.getMessage(), ex);
	     }
	 }
	 return "error";

     }

     public static int update(String query) {
	
	 Connection con = null;
	 Statement st = null;
	 ResultSet rs = null;

	 String url = "jdbc:mysql://localhost:3306/fresta";
	 String user = "root";
	 String password = "root";

	 try {
	     con = DriverManager.getConnection(url, user, password);
	     st = con.createStatement();
	     int count = st.executeUpdate(query);

	     return count;

	 } catch (SQLException ex) {
	     System.out.println("db error for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);

	 } finally {
	     try {
		 if (rs != null) {
		     rs.close();
		 }
		 if (st != null) {
		     st.close();
		 }
		 if (con != null) {
		     con.close();
		 }

	     } catch (SQLException ex) {
		 System.out.println("db error for " + query);
		 System.out.println(ex.getMessage());
		 //Logger lgr = Logger.getLogger(Test.class.getName());
		 //lgr.log(Level.WARNING, ex.getMessage(), ex);
	     }
	 }
	 return 0;

     }

     public static String [] getOneSet(String query) {
	
	 Connection con = null;
	 Statement st = null;
	 ResultSet rs = null;

	 String url = "jdbc:mysql://localhost:3306/fresta";
	 String user = "root";
	 String password = "root";

	 try {
	     con = DriverManager.getConnection(url, user, password);
	     st = con.createStatement();
	     rs = st.executeQuery(query);

	     ResultSetMetaData rsmd = rs.getMetaData();
	     int columnsNumber = rsmd.getColumnCount();

	     if (rs.next()) {
		 String [] res = new String[columnsNumber];
		 for (Integer i = 0; i < columnsNumber; i++)
		     res[i] = rs.getString(i+1);
		 return res;
	     } else {
		 return null;
	     }

	 } catch (SQLException ex) {
	     System.out.println("db error for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);

	 } finally {
	     try {
		 if (rs != null) {
		     rs.close();
		 }
		 if (st != null) {
		     st.close();
		 }
		 if (con != null) {
		     con.close();
		 }

	     } catch (SQLException ex) {
		 System.out.println("db error for " + query);
		 System.out.println(ex.getMessage());
		 //Logger lgr = Logger.getLogger(Test.class.getName());
		 //lgr.log(Level.WARNING, ex.getMessage(), ex);
	     }
	 }
	 return null;

     }
}
