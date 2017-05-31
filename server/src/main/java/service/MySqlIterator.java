package service;

import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlIterator {

    private ResultSet rs;


    public MySqlIterator(ResultSet rs0) {
	
	rs = rs0;
    }

    public boolean next() {
	boolean n = false;
	try {
	    n = rs.next();
	} catch (SQLException ex) {
	    System.out.println("db error");
	    System.out.println(ex.getMessage());
	} finally {
	    if (!n) {
		try {
		    rs.close();
		} catch (SQLException ex) {
		    System.out.println("db error");
		    System.out.println(ex.getMessage());
		}
	    }
	}

	return n;
    }

    public void close() {
	try {
	    rs.close();
	} catch (SQLException ex) {
	    System.out.println("db error");
	    System.out.println(ex.getMessage());
	}
    }

    public String getString(int i) {
	boolean error = false;
	try {
	    return rs.getString(i);
	} catch (SQLException ex) {
	    System.out.println("db error");
	    System.out.println(ex.getMessage());
	    error = true;
	} finally {
	    if (error) {
		try {
		    rs.close();
		} catch (SQLException ex) {
		    System.out.println("db error");
		    System.out.println(ex.getMessage());
		}
	    }
	}
	return null;
    }

}
