package Logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private  BufferedWriter writer ;
	
	private static Logger instance;

	public Logger() {
		try {
			writer = new BufferedWriter(new FileWriter("logger"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	public static Logger getInstance() {
		if (instance == null) {
			return new Logger();
		}
		return instance;
	}
	public void println(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date now = new Date();
		try {
			writer.write(sdf.format(now) + " : " + str);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public BufferedWriter getBufferedWriter() {
		return writer;
	}
}
