package server;

import service.*;

import java.sql.*;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {
	private PluginWebServices ws = new PluginWebServicesImpl(null);

	@Before
	public void setUp() throws Exception {
		/* Create a temporary DB */
		Connection conn = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      conn = DriverManager.getConnection("jdbc:sqlite::memory:");
	      
	      ws.setDBConnection(conn);
	      
	      System.out.println("Opened database successfully");
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    
	    Statement stat = conn.createStatement();
	    stat.setQueryTimeout(10);  // set timeout to 30 sec.

	    /* Populate the DB */
	    //TODO: Use a schema file to create tables here (instead of hardcoding it).
	    stat.executeUpdate("create table Vehicle (id integer, name string, description string, vin string)");
	    
	    stat.executeUpdate("insert into Vehicle (id, name, description, vin) "
	    		+ "values(1, 'de lux', 'A luxury car', 'a0:88:b4:de:4b:a8')");
	    stat.executeUpdate("insert into Vehicle (id, name, description, vin) "
	    		+ "values(2, 'simulator', 'Simulator instance', 'a0:88:b4:de:4b:a8:S0')");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetVehicleName() throws Exception {
		assertEquals("de lux", ws.getVehicleName(1));
	}

	@Test
	public void testGetVehicleVin() throws Exception {
		assertEquals("a0:88:b4:de:4b:a8", ws.getVehicleVin("de lux"));
	}
}
