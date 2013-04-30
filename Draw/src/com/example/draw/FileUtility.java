/* This class was provided by Chris Allen.
 * 
 * The purpose of this class is to handle input/output string to a file.
 * 
 * 
 */


package com.example.draw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

public class FileUtility {
	public static boolean writeToFile(Context ctx, String strContent, String filename) {
		boolean bReturn = true;
		FileOutputStream osw = null;
		
		try {
			//Make sub directories if needed
			String[] split = filename.split(File.separator);
			String file = split[split.length-1];
			String path = filename.substring(0, filename.indexOf(file));
			if(path != "")
				new File(ctx.getFilesDir()+File.separator+path).mkdirs();
			File outputFile = new File(ctx.getFilesDir()+File.separator+filename);

			osw = new FileOutputStream(outputFile);
			osw.write(strContent.getBytes());
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
			bReturn = false;
		} finally {
			try {
				if(osw != null) osw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bReturn;
	}

	public static String readFile(Context ctx, String filename) {
		FileInputStream fis = null;
		String line = null;
	    StringBuilder sb = new StringBuilder();
		try {
			fis = new FileInputStream(ctx.getFilesDir()+File.separator+filename);
		    InputStreamReader inputStreamReader = new InputStreamReader(fis);
		    @SuppressWarnings("resource")
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    while ((line = bufferedReader.readLine()) != null) {
		        sb.append(line);
		    }
		}
		catch(Exception e) {
			//e.printStackTrace();
			return null;
		}
		finally {
			try { if(fis != null) fis.close(); }
			catch (IOException e) { return null; }	
		}
		
		return sb.toString();
	}
	
	
	
}