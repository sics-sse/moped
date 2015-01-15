package ecm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import messages.LinkContextEntry;
import messages.LoadMessage;
import db.DataRecord;

public class Loader implements Runnable {
	private Ecm ecm;
	private int ecuId;
	
	public Loader(Ecm ecm, int ecuId) {
		this.ecm = ecm;
		this.ecuId = ecuId;
	}
	
	public void run() {
		load(ecuId);
	}

	public void load(int ecuId) {
		// Prepare APPs
		HashMap<String, DataRecord> installedApps = ecm.getInstalledApps(ecuId);
		System.out.println("Checking installed apps, size: " + installedApps.size());
		Iterator<Entry<String, DataRecord>> iterator = installedApps.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, DataRecord> entry = iterator.next();
			DataRecord record = entry.getValue();

			int reference = record.getRemoteEcuId();
			String executablePluginName = record.getExecutablePluginName();
			int callbackPortID = record.getCallbackPortID();
			HashMap<String, Integer> portInitialContext = record
					.getPortInitialContext();
			ArrayList<LinkContextEntry> portLinkingContext = record.getPortLinkingContext();
			String location = record.getLocation();
			byte[] pluginBytes;
			try {
				pluginBytes = readBytesFromFile(location);
				LoadMessage loadMessage = new LoadMessage(reference,
						executablePluginName, callbackPortID,
						portInitialContext, portLinkingContext, pluginBytes);

				ecm.process(loadMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private byte[] readBytesFromFile(String location) throws IOException {
		File file = new File(location);
		InputStream is = new FileInputStream(file);
		// Get the size of the file
		long length = file.length();
		// You cannot create an array using a long type.
		// It needs to be an integer type.
		// Before converting to an integer type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName() + " as it is too long (" + length
					+ " bytes, max supported " + Integer.MAX_VALUE + ")");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
}
