package se.sics.sse.fresta.wirelessino;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class Main extends Activity {
	public static final String TAG = "WirelessIno";
	public static Socket socket = null;
	public static final boolean D = true; // The debug option
	
	private static PrintWriter out = null;
	private PadView view = null;
	private Menu menu = null;
	
	private static final int EXIT_INDEX = 0;		// Menu bar: exit  
	private static final int DISCONNECT_INDEX = 1;	// Menu bar: disconnect
	private static final int CONFIG_INDEX = 2	;	// Menu bar: WiFi configuration

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		view = new PadView(this);
		setContentView(view);
	}
	
	/* 
	 * Add a disconnect option when connected to a socket. 
	 */
	protected void onResume() {
		/* Disable the disconnect option if no connection has been established */
		updateMenuVisibility();
		super.onResume();
	}
	
	/* 
	 * Add menu options 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
	
		/* Add menu bars */
		menu.add(0, EXIT_INDEX, EXIT_INDEX, R.string.exit);
		menu.add(0, DISCONNECT_INDEX, DISCONNECT_INDEX, R.string.disconnect);
		menu.add(0, CONFIG_INDEX, CONFIG_INDEX, R.string.wifiConfig);
		
		/* To start with, disable the disconnect option if no connection has been established */
		updateMenuVisibility();
		
		return super.onCreateOptionsMenu(menu);
	}

	/* 
	 * Handle different menu options
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == EXIT_INDEX) {
			finish();
		} 
		else if (item.getItemId() == DISCONNECT_INDEX) {
			try {
				if (socket != null) {
					socket.close();
					socket = null;
					
					menu.getItem(DISCONNECT_INDEX).setVisible(false); // Hide the disconnect option
					view.invalidate(); // Repaint (to show "not connected" in the main view)
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		else if (item.getItemId() == CONFIG_INDEX) {
			Intent i = new Intent(Main.this, SocketConnector.class);
			startActivity(i);
		}
		
		return super.onOptionsItemSelected(item);
	}

	/* 
	 * Initialize the output stream for the socket.  
	 */
	public static void init(Socket socket) {
		Main.socket = socket;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Send a message through the socket.
	 */
	public static void send(Object message) {
		out.println(message);
	}
	
	/*
	 * Checks if a socket connection has been established and updates
	 * the visibility of the "disconnect" menu option accordingly.    
	 */
	private void updateMenuVisibility() {
		if (menu != null) {
			if (socket == null || !socket.isConnected()) 
				menu.getItem(DISCONNECT_INDEX).setVisible(false);
			else
				menu.getItem(DISCONNECT_INDEX).setVisible(true);
		}
	}
}
