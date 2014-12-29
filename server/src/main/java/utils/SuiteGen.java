package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SuiteGen {
	// executable process
	private static Process process;
	private String squawkDir;
	private String cmd;
	private String reply;
	
	public SuiteGen(String squawkDir) {
		this.squawkDir = squawkDir;
		cmd = "./d.sh user-suite ";
	}

	public String getCmd() {
		return cmd;
	}

	public String generateSuite(String source) {
		reply = "";
		File sourceFileFolder = new File(source);
		if (sourceFileFolder.exists() && sourceFileFolder.isDirectory()) {
			BufferedWriter bw = null;

			try {
				Runtime runtime = Runtime.getRuntime();
				process = runtime.exec("/bin/sh");
				// In case cmd.exe application is not initiated
				Thread.sleep(1000);

				// Write command to console
				bw = new BufferedWriter(new OutputStreamWriter(
						process.getOutputStream()));
				
				// Go to directory of squawk
				bw.write("cd "+squawkDir+"\n");
				bw.flush();
				
				// Achieve output from console
				Thread runtimeInput = new Thread(new RuntimeInput());
				runtimeInput.start();
				Thread.sleep(500);
				
				bw.write(cmd + source + "\n");
				bw.close();

				Thread.sleep(500);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		
		return reply;
	}

	public class RuntimeInput implements Runnable {

		@Override
		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String content = null;
			try {
				while ((content = br.readLine()) != null) {
					reply += content + "<br \\>";
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
