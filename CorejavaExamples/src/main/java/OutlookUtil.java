package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
public class OutlookUtil {
	final static String outlook="C:/Program Files/Microsoft Office/Office12/OUTLOOK.EXE";

	public static void main(final String[] args) throws Exception {

		//Desktop.getDesktop().mail(new URI("mailto:address@somewhere.com"));
		createNewEmail("sandeep@j.com&subject=test subject&body=test%20body");

	}

	public static void createNewEmail(final String emailParameters) throws IOException {
		final Process process = new ProcessBuilder(outlook,"/c","ipm.note","/m",emailParameters).start();
		final InputStream is = process.getInputStream();
		final InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);
		String line;

		System.out.printf("Output of running create email using %s is:", outlook);

		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
	}

}
