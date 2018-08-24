package com.mycompany.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class App extends Thread {

	private static final String FEED_URL = "http://listen.broadcastify.com:80?t=7AE05E372823C3CAEC9CBF1894214520165698A45D0691A2C105E074C0613134B02B3C6E42ECFD7FECB3C8C966E30123A9ED33F757A7D3425EE926D0EEA8A057";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	private static final int FILE_SPLIT_SIZE = 10485760;
	private static Boolean done = Boolean.FALSE;

	public static void main(String[] args) throws IOException {
		//start thread to listen for 'stop' on System.in
		App app = new App();
		app.start();
		
		//open connection to feed
		URL feedUrl = new URL(FEED_URL);
		URLConnection connection = feedUrl.openConnection();
		InputStream stream = null;
		OutputStream outstream = null;
		
		try {
			//start streaming data from feed
			stream = connection.getInputStream();
			
			//build output stream to output file
			outstream = new FileOutputStream(new File(generateOutFileName()));
			System.out.println("Connection opened to: " + FEED_URL);

			//build buffer to read stream into
			byte[] buffer = new byte[4096];
			int len;
			int bytesRead = 0;
			
			//read stream until there isn't any data or 'stop' has been submitted to System.in
			while ((len = stream.read(buffer)) != -1 && !done) {
				//write and flush data to file
				bytesRead += len;
				outstream.write(buffer, 0, len);
				outstream.flush();
				
				//check if file needs split
				if (bytesRead > FILE_SPLIT_SIZE) {
					System.out.println("Reached max file size [" + FILE_SPLIT_SIZE + "] splitting file.");
					
					//close out current file
					bytesRead = 0;
					outstream.close();
					
					//open new output stream to new file
					outstream = new FileOutputStream(new File(generateOutFileName()));
				}
			}
		} finally {
			//clean up resources
			System.out.println("Cleaning up...");
			
			//close input stream
			if (null != stream ) {
				System.out.println("Closing in stream...");
				stream.close();
			}
			
			//close output stream
			if (null != outstream) {
				System.out.println("Closing out stream...");
				outstream.close();
			}
		}
	}
	
	@Override
	public void run() {
		System.out.println("Type 'stop' to kill process.");
		
		//listen to System.in
		Scanner sc = new Scanner(System.in);
		
		try {
			//check for data on System.in
	        while(sc.hasNextLine()) {
	        	
	        	//check if 'stop' was typed on System.in
	        	if ("stop".equalsIgnoreCase(sc.nextLine())) {
	        		System.out.println("Stopping...");
	        		
	        		//set condition so reading from buffer stops
	        		//kill this thread
	        		done = Boolean.TRUE;
	        		Thread.currentThread().interrupt();
	        		return;
	        	}
	        }
		} finally {
			sc.close();
		}
        
	}

	private static String generateOutFileName() {
		return DATE_FORMAT.format(new Date(System.currentTimeMillis())) + ".mp3";
	}
}
