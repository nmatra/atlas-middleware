package atlascollectiveintgenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CIFiles {
	private String baseDirectory;
	private HashMap<String,FileWriter> files = new HashMap<String,FileWriter>();
	
	public CIFiles(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	
	public FileWriter getOpenFile(String fileName) throws IOException {
		if (!files.containsKey(fileName)) {
			String filePathName = baseDirectory + "/" + fileName;
			FileWriter f = new FileWriter(filePathName);
			files.put(fileName, f);
			return f;
		} else {
			return files.get(fileName); 
		}
	}
	
	public void closeAllFiles() {
		for (FileWriter f : files.values()) {
		    try {
				f.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

