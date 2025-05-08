/*
main
Thread crea cuando hace startMonitoring() de AqDirectoryMonitor.
start()
new Thread(new AqFileDetectionRunnable(file, var)).start()

*/


// package zurich.ae.downloadTool;
package DownloadTool00;

import java.io.File;

public class AqNewAS400FileDetection {
	public static AqConfigAndLog cl;
	public static long interval = 0l; // l=Long
	public static String var;

	public static void detectFiles() {
		// When a ini file is detected creates a new thread that calls the AqReportTypeTransformation
		AqDirectoryMonitor dm = new AqDirectoryMonitor(new File(cl.getDirToWatch(var)), interval, var); // interval = 5000 ; var = var0
		dm.addDirectoryChangeListener(new AqDirectoryChangeListener() { // Listener ; Funcion anonima o lambda ({}) ; 
			public void directoryChange(AqDirectoryChangeEvent dce) {
				File file = (File)dce.getSource(); // .getSource() obtiene el componente que ha provocado/originado el evento ; file = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT
				if (dce.getType() == AqDirectoryChangeEvent.FILE_ADDED) { // AqDirectoryChangeEvent.FILE_ADDED = 1
					String type = file.toString().substring(file.toString().length() - 7, file.toString().length() - 4); // Devuelve INI (de KTOR34T.INI.TXT), CSV (de KTOR34T.CSV.TXT), es\ (de Files\Temp) o FIG (de config.ini), el que interesa es INI ; file.toString() = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT ; 7 es la longit de CSV.TXT ; 
					//String type = file.toString().substring(file.toString().length() - 3);
					cl.writeInLog("new file detected "+ file.toString()); // file.toString() = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
					if(type.equals("ini") || type.equals("INI")) {
                		new Thread(new AqFileDetectionRunnable(file, var)).start(); // Ejecuta run() creo
					}
				}
			}
		});
		dm.setDaemon(true); // Thread.setDaemon() must be called before the thread is started
		dm.startMonitoring(); // new Thread, start()
	}

	public static void main(String args[]) {
		interval = (long) 5000;
		//int count = 0; //test
		var = args[0]; // var = var0
		cl = new AqConfigAndLog(args[0]);
		detectFiles();
		cl.writeInLog("File Detection application started correctly");
		while(true) {
			try {
				// Thread.sleep(60000); // Espera 6 segundos
			} catch(Exception e){};
		}
	}
}
