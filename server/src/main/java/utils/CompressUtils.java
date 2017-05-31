package utils;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import common.MopedException;

public class CompressUtils {
    public String unzip(String source) throws MopedException {
	String dest = "";
	try {
	    File f = new File(source);
	    if (!f.exists()) {
		throw new MopedException("unzip: file not found " + source);
	    }
	    ZipFile zipFile = new ZipFile(source);
	    if(!zipFile.isValidZipFile()) {
		throw new MopedException("unzip: not a valid zip file " + source);
	    } else {
		// Prepare uncompress directory
		//				int lastIndexOf = source.lastIndexOf(File.separator);
		//				if(lastIndexOf < 0)
		//					dest = ".";
		//				else
		//					dest = source.substring(0, lastIndexOf);
				
		dest = source.substring(0,  source.length()-4); //Remove the .zip-ending, turning this file name into a directory
		dest += File.separator + "j2meclasses";
				
		System.out.println("Output directory:"+dest);
		File destDir = new File(dest);
		if(destDir.isDirectory() && !destDir.exists()) {
		    destDir.mkdirs();
		}
				
		zipFile.extractAll(dest);
	    }
			
	} catch (ZipException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    throw new MopedException("unzip: ZipException " + source, e);
	}
	return dest;
    }
}
