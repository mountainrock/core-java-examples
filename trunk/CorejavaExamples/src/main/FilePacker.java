package util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class FileNavigator{
	String tab = "\t+";
	private String workingDir;
	private StringBuilder sb= new StringBuilder();
	private static final String IMAGE_PATTERN =   "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";
	Pattern pattern = Pattern.compile(IMAGE_PATTERN);
	
	public FileNavigator(String workingDir)	{
		this.workingDir = workingDir;
	}

	public static void main(String[] args) throws Exception {
		FileNavigator td = new FileNavigator("C:\\san\\backup\\uni");
		td.visitAllDirsAndFiles(new File(td.workingDir));
		String resultPath = td.workingDir+"\\result.txt";
		
		FileUtils.writeStringToFile(new File(resultPath), td.sb.toString());
		System.out.println("\r\n wrote" + resultPath);
		//TODO: string to encode and store
		
	}

	// Process all files and directories under dir

	public void visitAllDirsAndFiles(File fileOrdir) {

		if (fileOrdir.isDirectory()) {
			System.out.println("You are inside directory :" + fileOrdir.getName());
			String[] children = fileOrdir.list();
			tab = "\t";

			for (int i = 0; i < children.length; i++) {
				File child = new File(fileOrdir, children[i]);
				if (!child.isDirectory()){
					System.out.println(tab + "File:" + tab + children[i]);
					process(child);
				}
				else{
					System.out.println("Dir:" + tab + children[i]);
				}
				visitAllDirsAndFiles(child);
			}
		}
		else{
			process(fileOrdir);
		}
	}

	public void process(File file) {
		if(!isNonImage(file)){
			System.out.println("Skipping image : "+ file.getName());
			return;
		}
		StringBuffer buf = IOUtility.readFile(file);
		String result = StringUtils.replace(buf.toString(),"projectName", "uni");
		try {
			if(!result.equals(buf.toString())){
				FileUtils.writeStringToFile(file, result);
			}
			sb.append(String.format("::||%s||::\r\n",file.getAbsolutePath())).append(result).append("\r\n");
			System.out.println("\r\n wrote" + file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ----------------------------------------------------------------------------------------------------

	}
	
	public boolean isNonImage(File file){
		return !pattern.matcher(file.getAbsolutePath()).matches();
	}
	
	

}