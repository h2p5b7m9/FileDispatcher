// Class to manage the configuration file and the log and error log files

// package zurich.ae.downloadTool;
package DownloadTool00;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AqConfigAndLog {

	public static final char endVar = '-'; // indicates the end of an environment in INI

	public String configName = "C:\\Users\\ignac\\Documents\\Java\\DownloadTool00\\Files\\config.ini";

	public String dirToWatch = "incoming";
	public String logFile = "log.txt";
	public String var;

	public AqConfigAndLog(String var) { // Constructor ; var = var0
		this.var = new String(var);
		// this.configName = System.getProperty("config");
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = new String(var);
	}

	public String findParam(String param) { 
		// return the line in the config.ini file that starts with the String param
		String value=null;
		StringBuffer data = new StringBuffer();
		try{
			FileInputStream iniReader = new FileInputStream(configName);
			int c = iniReader.read();
			while(c>-1){
				while(c!='\n' && c>0) {
					data.append((char) c);
					c = iniReader.read();
				}
				if(data.toString().startsWith(param)) {
					return data.toString();
				}
				else{
					data = new StringBuffer();
					c=iniReader.read();
				}

			}
		}catch(Exception e){writeInError("Error while reading config file " + e.getMessage());}

		return value;
	}

	public String findParam(String param, String var) { // param es monitorpath o rootpath de config.ini, var es la variable de entorno var0, var1, etc de config.ini
		// return the line in the config.ini file that starts with the String param
		String value=null;
		StringBuffer data = new StringBuffer();
		try {
			FileInputStream iniReader = new FileInputStream(configName);
			int c = iniReader.read(); // Lee caracter
			while(c > -1) {
				while(c != '\n' && c > 0) {
					data.append((char) c);
					c = iniReader.read();
				}
				if(data.toString().startsWith(var)) {
					data = new StringBuffer();
					c = iniReader.read(); // Lee caracter
					while(c>-1 && c!=endVar) {
						while(c!='\n' && c>0) {
							data.append((char) c);
							c = iniReader.read(); // Lee caracter
						}
						if(data.toString().startsWith(param))
							return data.toString();
						else{
							data = new StringBuffer();
							c=iniReader.read();
						}
					}
				}
				else {
					data = new StringBuffer();
					c=iniReader.read(); // Lee caracter
				}
			}
		}catch(Exception e){writeInError("Error while reading config file " + e.getMessage());}

		return value;
	}

	public String getLogPath() {
		// Return the path where to put the log file
		String temp = findParam("rootpath", var);

		StringBuffer logPath = new StringBuffer(temp.substring(temp.lastIndexOf(",")+1)); // Mueve de la ultima coma , en adelante

		if(logPath.indexOf("\r") > 0) // Si contiene CR=13 lo elimina 
			logPath.deleteCharAt(logPath.indexOf("\r")); // Elimina CR=13
		logPath.append("\\");
		logPath.append("logs\\");
		File logDir = new File(logPath.toString());
		logDir.mkdir(); // Crea directorio logs
		return logPath.toString();
	}

	public String getErrorPath(){
		// Return the path where to put the error log file
		String temp = findParam("rootpath", var);

		StringBuffer errorPath = new StringBuffer(temp.substring(temp.lastIndexOf(",")+1)); // Usa de la ultima coma , en adelante
		if(errorPath.indexOf("\r")>0) // Si contiene CR=13 lo elimina 
			errorPath.deleteCharAt(errorPath.indexOf("\r")); // Elimina CR=13

		errorPath.append("\\");
		errorPath.append("errors\\");
		File errorDir = new File(errorPath.toString());
		errorDir.mkdir(); // Crea directorio errors
		return errorPath.toString();
	}

	public String getHistoricPath(){
			//Return the path where to put the error log file
			String temp = findParam("rootpath", var);

			StringBuffer historicPath = new StringBuffer(temp.substring(temp.lastIndexOf(",")+1)); // Copia de la ultima coma , en adelante
			if(historicPath.indexOf("\r")>0)
				historicPath.deleteCharAt(historicPath.indexOf("\r")); // Elimina CR=13

			historicPath.append("\\");
			historicPath.append("historic\\");
			File historicDir = new File(historicPath.toString());
			historicDir.mkdir();
			return historicPath.toString();
		}

	public void writeInLog(String msg) { // msg = new file detected C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls; msg = File Detection application started correctly ; msg = Transforming C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT to xls ; msg = Transformation completed: C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls ; msg = Original file: C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT deleted ; msg = Calling Dispatcher ; msg = Network destination path obtained: \\localhost\C:\Java\DownloadTool00\Files\ ; msg = file: C:\Users\ignac\Documents\Java\DownloadTool00\Files\02_2025-04-14-06.45.28.xls copied to: errors ; msg = file: C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT copied to: errors
		// Write in the log file the message msg
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		String logName = getLogPath() + date.format(Calendar.getInstance().getTime()) + "_" + logFile; // getLogPath() = C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp\logs\ ; logName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp\logs\2025-04-14_log.txt ; Calendar.getInstance().getTime() = Mon Apr 14 13:31:16 CEST 2025 ; logFile = log.txt  
		printInFile(logName, msg);
	}

	public String getDirToWatch(String var) { // var = var0
		// return the path of the directory to listen to
		String temp = findParam("monitorpath", var); // temp=monitorpath:,C:\Users\ignac\Documents\Java\DownloadTool00\Files\r
		StringBuffer dir;
		if(temp == null) {
			temp = findParam("rootpath", var);
			dir = new StringBuffer(temp.substring(temp.lastIndexOf(",") + 1));
			dir.replace(dir.length() - 1, dir.length(), "\\");
			dir.append(dirToWatch + "\\");
			writeInLog("Parameter monitorpath not found in config.ini file," + " listening to: " + dir);
			return dir.toString();
		}
		dir = new StringBuffer(temp.substring(temp.lastIndexOf(",") + 1)); // Copia de la ultima coma , en adelante ; dir = C:\Users\ignac\Documents\Java\DownloadTool00\Files
		dir.replace(dir.length() - 1, dir.length(), "\\"); // Sustituye \r por \ ; dir = C:\Users\ignac\Documents\Java\DownloadTool00\Files\
		//dir.append(dirToWatch + "\\");
		return dir.toString();
	}

	public String getDirToWatch() {
		//return the path of the directory to listen to                   
		String temp = findParam("monitorpath");
		StringBuffer dir = new StringBuffer(temp.substring(temp.lastIndexOf(",") + 1)); // Copia de la ultima coma , en adelante
		dir.replace(dir.length() - 1, dir.length(), "\\");
		//dir.append(dirToWatch+"\\");
		return dir.toString();
	}

	public void writeInError(String msg){
		// write the message msg in the error log file
		String errorName = getErrorPath() + "error.txt";
		printInFile(errorName, msg);
	}

	public void printInFile(String fileName, String msg) { // fileName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp\logs\2025-04-14_log.txt ; msg = File Detection application started correctly
		// write the message msg in the file called fileName
		SimpleDateFormat date = new SimpleDateFormat("MM_dd-HH:mm:ss");
		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(fileName, true));
		} catch (Exception e) {
			System.out.println(fileName + " not found\n");
		}
		try {
			out.write(new String(date.format(Calendar.getInstance().getTime()) + " "+ msg + " \r\n")); // date.format(Calendar.getInstance().getTime()) = 04_14-13:40:37
			out.close();
		} catch (Exception e) {
			System.out.println("Error writing in " + fileName + ": " + e.getMessage());
		}
	}

	public String findParamInFile(String param, String fileName) throws IOException { // param=coltypes ; param=tablename ; param=colnames ; param=repotype ; fileName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.ini.TXT
		// return the line in the file called fileName that starts with the String param
		String value = null;
		StringBuffer data = new StringBuffer();

			FileInputStream iniReader = new FileInputStream(fileName);
			int c = iniReader.read(); // Lee caracter de fichero INI
			while(c > -1) {
				while(c != '\n' && c > 0) { // No fin de linea
					if(c != '\r')
						data.append((char) c);
					c = iniReader.read();
				}
				// Fin de linea de fichero INI
				if(data.toString().startsWith(param) || data.toString().startsWith(param.toUpperCase())) {
					iniReader.close();
					return data.toString(); // data=REPOTYPE:XLS ; data=COLTYPES:A,P,A,P,P,A,A,A,A,A,A,A,A,A,A,A,A,A,P,P,A,P,P,A,P,A,A,A,A,A,A,A,A,A,P,P,P,P,A,A,A,A,A,A,A,P ; colnames=COLNAMES:CDDOCUME,NUSECUPE,DSFUNCIO,FEPETICI,FEFIN,TIPETICI,ESTADOPE,ESTADOAP,NOTRABAJ,INPROCES,NUPRIORI,ININMDIF,INBAONLI,INEXTRAE,INIMPRIM,INENVIO,INVISUAL,INMODO,NUCOPIAD,NUENVIOS,CDCADENA,FECADENA,CDORDCAD,CDTEXTO,NUVERTXT,INESTADD,CDUSUPET,CDTIPUSU,INTIPOCD,CDCENTRO,CDIMPRES,DSIP,CDAPLICA,CDENTOR,HOINITRZ,HOFINTRZ,CDERREDI,CDERRAPL,INAUXIL1,INAUXIL2,INAUXIL3,INAUXIL4,CDEMPUSU,CDUSUARI,NOPROGRA,TSULTMOD ; data=DESTINAT:localhost,,C:\Java\DownloadTool00\Files
				}
				else {
					data = new StringBuffer();
					c = iniReader.read(); // Lee caracter de fichero INI
				}
			}
		iniReader.close();
		return value;
	}

	public void error(String fileName) {
		// copy the file called fileName to the error directory
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss");
		String date = dateFormat.format(Calendar.getInstance().getTime());
		String errorDirName = getErrorPath();
		File f1 = new File(fileName);
		String type;
		String name;
		if(fileName.endsWith(".csv.txt") || fileName.endsWith(".ini.txt") ||
		   fileName.endsWith(".CSV.TXT") || fileName.endsWith(".INI.TXT")) {
			type = fileName.substring(fileName.length() - 8); // 8 es la longitud de .csv.txt
			name = f1.getName().substring(0,f1.getName().length() - 8); // 8 es la longit de .csv.txt
		} else {
			type = fileName.substring(fileName.lastIndexOf("."));
			name = f1.getName().substring(0,f1.getName().lastIndexOf("."));
		}

		File errorDir = new File(errorDirName);
		if(!errorDir.exists())
			errorDir.mkdir(); // Crea el directorio si no existe

		String errorName;
		if(f1.getName().length() > 24 && f1.getName().substring(f1.getName().length() - 24).startsWith("_20")) {
			errorName = new String(errorDir.getPath() + "\\" + name + type);
		}
		else { // Si el nombre del fichero es corto ed menor de 24 caracteres, entonces le añade la fecha al nombre de fichero
			errorName = new String(errorDir.getPath() + "\\" + name + "_" +
													  date + type);
		}

		File errorFile = new File(errorName);
		if(errorFile.exists())
			errorFile.delete();
		f1.renameTo(errorFile); // Cambia nombre de fichero de errores

		writeInLog("file: " + f1.getPath() + " copied to: " + errorDir.getName());
	}
}
