package service;

import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class CallMySql {
     public static Connection con = null;
     public static Statement st = null;

     private static boolean init_done = false;

     private static void init() {
	 if (init_done)
	     return;

	 String url = "jdbc:mysql://localhost:3306/fresta2";
	 String user = "root";
	 String password = "root";

	 try {
	     con = DriverManager.getConnection(url, user, password);
	     st = con.createStatement();

	     init_done = true;

	 } catch (SQLException ex) {
	     System.out.println("db error 0");
	     System.out.println(ex.getMessage());
	 }
     }

     public static String getOne(String query) {
	
	 ResultSet rs = null;

	 try {
	     init();
	     //con = DriverManager.getConnection(url, user, password);
	     //st = con.createStatement();
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

	 try {
	     init();
	     int count = st.executeUpdate(query);

	     return count;

	 } catch (SQLException ex) {
	     System.out.println("db error for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);

	 }
	 return 0;

     }

     public static String [] getOneSet(String query) {
	
	 ResultSet rs = null;

	 try {
	     init();
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

	     } catch (SQLException ex) {
		 System.out.println("db error for " + query);
		 System.out.println(ex.getMessage());
		 //Logger lgr = Logger.getLogger(Test.class.getName());
		 //lgr.log(Level.WARNING, ex.getMessage(), ex);
	     }
	 }
	 return null;

     }

     public static ResultSet getResults(String query) {
	
	 ResultSet rs = null;

	 try {
	     init();
	     // we need a local Statement, otherwise we are not
	     // reentrant.
	     Statement st = con.createStatement();

	     rs = st.executeQuery(query);

	     return rs;

	 } catch (SQLException ex) {
	     System.out.println("db error for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);
	 }
	 return null;

     }

}
