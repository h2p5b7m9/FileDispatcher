/*
Runnable
run()

*/

package DownloadTool00;

import java.io.File;

public class AqFileDetectionRunnable implements Runnable {
	public File file;
	public AqConfigAndLog cl ;
	public String var;

	AqFileDetectionRunnable(File file, String var) { // Constructor ; file = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT ; var = var0
		this.file = file;
		this.var = var; // var = var0
		cl = new AqConfigAndLog(var);
	}

	public void run() {
		// TODO Auto-generated method stub
		boolean exist = true;
		String nameFile = file.toString().substring(0, file.toString().length() - 7); // 7 es la longitud de CSV.TXT ; file = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT ; nameFile = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02. 
		File csv = new File(nameFile + "CSV.TXT"); // csv = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
		if(!csv.exists()) { // No entra
			csv = new File(nameFile + "csv.txt");
			if (!csv.exists())
				exist = false;
		}
		if(exist) {
			//AqReportTypeTransformation rp = new AqReportTypeTransformation();
			AqReportTypeTransformation rp = new AqReportTypeTransformation(var); // Transforma a CSV=Excel ; Todo empieza con un new
			try {
				rp.getFileType(csv.toString());
			} catch (AqExceptionDownloadTool e) {
			} catch(Exception exc) {
				cl.writeInError("Unexpected error processing " + nameFile + "csv.txt file: " + exc.getMessage());
				// look if the files remain in the dir and delete them
				if(csv.exists()) {
					cl.error(csv.getPath());
					StringBuffer iniName = new StringBuffer(csv.getPath());
					iniName.replace(iniName.length() - 7, iniName.length() - 4, "INI");
					File iniFile = new File(iniName.toString());
					if(iniFile.exists()) {
						cl.error(iniFile.getPath());
					}
				}
			}
		}
		else {
			cl.writeInError("CSV file: " + csv.toString() + " does not exist");
			cl.error(file.getPath());
		}
	}
}
