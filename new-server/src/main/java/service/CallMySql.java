package service;

import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import utils.PropertyAPI;

public class CallMySql {
     public Connection con = null;
     public Statement st = null;

    private String url = PropertyAPI.getInstance().getProperty("db_location");
    private String user = PropertyAPI.getInstance().getProperty("db_user");
    private String password = PropertyAPI.getInstance().getProperty("db_password");

     public CallMySql() {

	 try {
	     con = DriverManager.getConnection(url, user, password);
	     st = con.createStatement();

	 } catch (SQLException ex) {
	     System.out.println("DB ERROR 0");
	     System.out.println(ex.getMessage());
	 }
     }

    public String getOne(String query) {
	return getOne(query, true);
    }

    public String getOne(String query, Boolean warn) {
	
	 ResultSet rs = null;

	 checkdb();

	 try {
	     //con = DriverManager.getConnection(url, user, password);
	     //st = con.createStatement();
	     rs = st.executeQuery(query);

	     if (rs.next()) {
		 return rs.getString(1);
	     } else {
		 return "none";
	     }

	 } catch (SQLException ex) {
	     System.out.println("DB ERROR for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);

	 } finally {
	     try {
		 if (rs != null) {
		     if (rs.next() && warn) {
			 System.out.println("DB WARNING; more than one solution for: " + query);
		     }
		     rs.close();
		 }

	     } catch (SQLException ex) {
		 System.out.println("DB ERROR for " + query);
		 System.out.println(ex.getMessage());
		 //Logger lgr = Logger.getLogger(Test.class.getName());
		 //lgr.log(Level.WARNING, ex.getMessage(), ex);
	     }
	 }
	 return "error";

     }

     public int update(String query) {

	 checkdb();

	 try {
	     int count = st.executeUpdate(query);

	     return count;

	 } catch (SQLException ex) {
	     System.out.println("DB ERROR for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);

	 }
	 return 0;

     }

     public String [] getOneSet(String query) {
	
	 checkdb();

	 ResultSet rs = null;

	 try {
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
	     System.out.println("DB ERROR for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);

	 } finally {
	     try {
		 if (rs != null) {
		     if (rs.next()) {
			 System.out.println("DB WARNING; more than one solution for: " + query);
		     }
		     rs.close();
		 }

	     } catch (SQLException ex) {
		 System.out.println("DB ERROR for " + query);
		 System.out.println(ex.getMessage());
		 //Logger lgr = Logger.getLogger(Test.class.getName());
		 //lgr.log(Level.WARNING, ex.getMessage(), ex);
	     }
	 }
	 return null;

     }

     public ResultSet getResults(String query) {
	
	 checkdb();

	 ResultSet rs = null;

	 try {
	     // we need a local Statement, otherwise we are not
	     // reentrant.
	     Statement st = con.createStatement();

	     rs = st.executeQuery(query);

	     return rs;

	 } catch (SQLException ex) {
	     System.out.println("DB ERROR for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);
	 }
	 return null;

     }


    private void checkdb() {
	 try {
	     if (!con.isValid(0)) {
		 try {
		     // do we need to close the old one?
		     System.out.println("reopening db connection");
		     con = DriverManager.getConnection(url, user, password);
		     st = con.createStatement();
		 } catch (SQLException ex) {
		     System.out.println("DB ERROR 0");
		     System.out.println(ex.getMessage());
		 }
	     }

	 } catch (SQLException ex) {
	     System.out.println("db exception " + ex.getMessage());
	 }
    }



     public MySqlIterator getIterator(String query) {
	
	 ResultSet rs = null;

	 checkdb();

	 try {
	     // we need a local Statement, otherwise we are not
	     // reentrant.
	     Statement st = con.createStatement();

	     rs = st.executeQuery(query);

	     MySqlIterator it = new MySqlIterator(rs);

	     return it;

	 } catch (SQLException ex) {
	     System.out.println("DB ERROR for " + query);
	     System.out.println(ex.getMessage());
	     //Logger lgr = Logger.getLogger(Test.class.getName());
	     //lgr.log(Level.SEVERE, ex.getMessage(), ex);
	 }
	 return null;

     }
}
