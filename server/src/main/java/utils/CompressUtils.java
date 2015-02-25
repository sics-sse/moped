package utils;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class CompressUtils {
	public String unzip(String source) {
		String dest = "";
		try {
			ZipFile zipFile = new ZipFile(source);
			if(!zipFile.isValidZipFile()) {
				throw new ZipException("Compressed file is invalid, maybe damage");
			} else {
				// Prepare uncompress directory
//				int lastIndexOf = source.lastIndexOf(File.separator);
//				if(lastIndexOf < 0)
//					dest = ".";
//				else
//					dest = source.substring(0, lastIndexOf);
				
				dest = source.substring(0,  source.length()-4); //Remove the .zip-ending, turning this file name into a directory
				dest += File.separator + "j2meclasses";
				
				System.out.println("Ouput directory:"+dest);
				File destDir = new File(dest);
				if(destDir.isDirectory() && !destDir.exists()) {
					destDir.mkdirs();
				}
				
				zipFile.extractAll(dest);
			}
			
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dest;
	}
}
