package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
}
