package DownloadTool00;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AqReportDispatcher {
	public static AqConfigAndLog cl;
	final static String SEPARADOR = ";"; // Caracter separador punto y coma de CSV
	final static String SEPARADOR_INI = ","; // Caracter separador coma de INI

	public static String buidNetworkDestinationPath(String fileName) throws AqExceptionDownloadTool { // fileName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls ; fileName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls
		int index, index2;
		StringBuffer networkDestinationPath = new StringBuffer();
		StringBuffer iniFile = new StringBuffer(fileName);
		iniFile.delete(iniFile.lastIndexOf("."), iniFile.length()); // Elimina el punto y todo lo que va detras en el nombre del fichero
		iniFile.append(".INI.TXT");
		String temp;
		FileInputStream iniReader = null;
		try {
			// reading ini file
			temp = cl.findParamInFile("destinat", iniFile.toString()); // Busca parametro DESTINAT en fichero INI ; temp = DESTINAT:DSMAQDES,DSRECURS,DSPATHDS ; temp = DESTINAT:localhost,,C:\Java\DownloadTool00\Files
			// reading destination machine Server Servidor Destino
			index = temp.indexOf(":"); // Busca dos puntos : en nombre de fichero INI
			index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca el siguiente caracter separador coma despues de dos puntos : en nombre de fichero INI
			if(index < 0) { // No encuentra caracter separador ed campo Destination machine vacio
				cl.writeInError("Destination machine not found");
				throw new AqExceptionDownloadTool();
			}
			if(index2 < 0) // No encuentra caracter separador ed campo Destination machine vacio
				index2 = temp.length();
			if(!temp.substring(index, index2).startsWith("\\\\"))
				networkDestinationPath.append("\\\\");
			networkDestinationPath.append(temp.substring(index, index2)); // networkDestinationPath=\\DSMAQDES ; networkDestinationPath=\\localhost ; Añade Server Servidor Destino

			// reading destination resource
			index = ++index2;
			index2 = temp.indexOf(SEPARADOR_INI, index);
			if(index < 0) { // No encuentra siguiente caracter separador ed campo Destination resource vacio
				cl.writeInError("Destination resource not found");
				throw new AqExceptionDownloadTool();
			}
			if(index2 < 0) // No encuentra siguiente caracter separador ed campo Destination resourcevacio
				index2 = temp.length();
			String resource = temp.substring(index, index2);
			if(!resource.equals("") && !resource.equals(" ")) {
				if(!temp.substring(index, index2).startsWith("\\"))
					networkDestinationPath.append("\\");
				networkDestinationPath.append(resource); // networkDestinationPath=\\DSMAQDES\DSRECURS ; Añade \recurso
			}

			// reading destination directory
			index = ++index2;
			if(!temp.substring(index).startsWith("\\"))
				networkDestinationPath.append("\\");
			networkDestinationPath.append(temp.substring(index)); // Añade \destination directory ; networkDestinationPath=\\DSMAQDES\DSRECURS\DSPATHDS

			if(networkDestinationPath.charAt(networkDestinationPath.length() - 1) != '\\')
				networkDestinationPath.append("\\"); // networkDestinationPath=\\DSMAQDES\DSRECURS\DSPATHDS\ ; Añade \ al final

		} catch(Exception e) {
			cl.writeInError("Error reading destination path " + e.getMessage());
			/*try {
				iniReader.close();
			} catch(Exception e1) {}*/
			if(fileName.indexOf("errors") == -1) {
				cl.error(fileName);
				cl.error(iniFile.toString());
			}
			throw new AqExceptionDownloadTool();
		}
		return networkDestinationPath.toString(); // networkDestinationPath=\\localhost\C:\Java\DownloadTool00\Files\
	}

	public static boolean copy(File src, File dest) { // Copia fichero de src=origen a dest=destino ; src=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02_2025-04-14-06.45.28.xls ; dest=\\localhost\C:\Java\DownloadTool00\Files\02_2025-04-14-06.45.28.xls
		// copy the file src to the destination in the dest file
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			//opening source file
			in = new FileInputStream(src);
		}
		catch(Exception e) {
			cl.writeInError("Error while opening: " + src.getPath() + " " + e.getMessage());
			return false;
		}
      try {
      	//opening destination file
      	out = new  FileOutputStream(dest);
      } catch(Exception e1) {
      	cl.writeInError("Error while opening: " + dest.getPath() + " " + e1.getMessage()); // dest.getPath()=\\localhost\C:\Java\DownloadTool00\Files\02_2025-04-14-06.45.28.xls ; e1.getMessage()= \\localhost\C:\Java\DownloadTool00\Files\02_2025-04-14-06.45.28.xls (No se encuentra el nombre de red especificado)
      	try{
      	   in.close();
      	}
      	catch(Exception e){}
      	return false;
      }
      try {
      	// copying file
      	byte[] buf = new byte[1024];
      	int len;
      	while ((len = in.read(buf)) > 0) {
      		out.write(buf, 0, len);
      	}
      	in.close();
      	out.close();
      }
      catch(Exception e) {
      	cl.writeInError("Error while copying file: " + src.toString() +
      					    " to: " + dest.toString() + ": " + e.getMessage());
      	try {
      	   in.close();
      	}
      	catch(Exception e2){}
      	return false;
      }

		return true;
	}

	// Copia fichero a networkDestinationPath
	public static void copyFileToNetworkDestination(String networkDestinationPath, String fileName) throws AqExceptionDownloadTool { // networkDestinationPath=\\DSMAQDES\DSRECURS\DSPATHDS\ ; fileName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls ; networkDestinationPath=\\localhost\C:\Java\DownloadTool00\Files\ ; fileName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls
		String DSFICHER;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss");
		File f1 = new File(fileName); // f1 = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls
		//creating ini file name
		StringBuffer iniName = new StringBuffer(fileName);
		iniName.delete(iniName.lastIndexOf("."), iniName.length()); // Elimina el punto y lo de detras del nombre de fichero ; iniName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02
		iniName.append(".INI.TXT");// iniName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT
		File iniFile = new File(iniName.toString());
		String type = fileName.substring(fileName.lastIndexOf(".")); // type = .xls ; substring desde lo que busca y encuentra hasta el final
		String date = dateFormat.format(Calendar.getInstance().getTime()); // date=2025-04-12-02.50.41
		// create file name: table name + date
		String table = null;
		table = f1.getName().substring(0, f1.getName().lastIndexOf(".")); // table=KTOR34T ; f1=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls;  table=02 ; f1=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls ; f1.getName()=02.xls

		// check if file name contains date from a previous execution
		if(f1.getName().length()> 24 && f1.getName().substring(f1.getName().length() - 24).startsWith("_20")) // f1.getName() = KTOR34T.xls ; f1.getName().length()=11 ; f1.getName()=02.xls
			DSFICHER = f1.getPath().substring(0, f1.getPath().lastIndexOf("\\")) +
						 "\\" + table + type;
		else // No empieza por _20
			DSFICHER = f1.getPath().substring(0, f1.getPath().lastIndexOf("\\")) + "\\" + table + "_" + date + type; // DSFICHER = C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T_2025-04-12-02.50.41.xls ; f1.getPath()=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls

		File f2 = new File(DSFICHER); // f2 = C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T_2025-04-12-02.50.41.xls ; f2 = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02_2025-04-14-06.45.28.xls
		// renaming original file
		f1.renameTo(f2); // f1=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls ; f2 = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02_2025-04-14-06.45.28.xls
		File dest = new File(networkDestinationPath +  f2.getName()); // dest = \\DSMAQDES\DSRECURS\DSPATHDS\KTOR34T_2025-04-12-02.50.41.xls ; f2.getName() = KTOR34T_2025-04-12-02.50.41.xls ; f2.getName()=02_2025-04-14-06.45.28.xls

		// start copy process
		if(copy(f2, dest)) { // Copia fichero creo
			// successful copy
			cl.writeInLog("File: " + fileName + " copied correctly");
			// copy data and ini file to historic directory
			if(fileName.indexOf("historic") == -1) { // fileName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls
				File historicDir = new File(cl.getHistoricPath());
				if(!historicDir.exists())
					historicDir.mkdir();
				File historicData = new File(historicDir.getPath() + "\\" + f1.getName());
				if(historicData.exists())
					historicData.delete();
				File historicIni;
				// check if files contain the date
				if(table.indexOf("_20") == table.length() - 20) {
					f2.renameTo(new File(historicDir.getPath() + "\\" + table + type));
					historicIni = new File(historicDir.getPath() + "\\" + table + ".ini.txt");
				} else {
					f2.renameTo(new File(historicDir.getPath() + "\\" + table + "_" + date + type));
					historicIni = new File(historicDir.getPath() + "\\" + table + "_" + date + ".ini.txt");
				}
				cl.writeInLog("File: " + f1.getName() + " moved to: " + historicDir.getPath());

				if(historicIni.exists())
					historicIni.delete();
				iniFile.renameTo(historicIni);
				cl.writeInLog("File: " + f1.getName() + " moved to: " + historicDir.getPath());
			}
		}
		else {
			// unsuccessful copy
			// moving data file and ini file to errors directory
			if(f1.getPath().indexOf("error") == -1) { // f1=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls ; f1.getPath()=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls ; f1.getPath()=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls
				cl.error(f2.getPath()); // f2.getPath()=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T_2025-04-12-02.50.41.xls
				StringBuffer ini = new StringBuffer(f1.toString()); // ini = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls
				ini.delete(ini.length()-3,ini.length()); // Elimina 3 ultimos caracteres .xls ; ini = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.
				ini.append("INI.TXT"); // ini=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT
				cl.error(ini.toString());
			}
			throw new AqExceptionDownloadTool();
		}
   }

	public static void main(String[] args) throws AqExceptionDownloadTool {
		cl = new AqConfigAndLog(args[1]);
		String networkDestinationPath = null;
		if((args[0].indexOf("error") > -1) || (args[0].indexOf("historic") > -1)) { // args[0] = C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls
			File f1 = new File(args[0]);
			try {
				networkDestinationPath = buidNetworkDestinationPath(f1.getPath());
				copyFileToNetworkDestination(networkDestinationPath,args[0]);
			}
			catch(AqExceptionDownloadTool exp) {}
			catch(Exception e) {
				cl.writeInError("Unexpected error processing " + f1.getName()
						+ ": " + e.getMessage());
			}
		}
		else {
			networkDestinationPath = buidNetworkDestinationPath(args[0]);networkDestinationPath = \\localhost\C:\Java\DownloadTool00\Files\
			cl.writeInLog("Network destination path obtained: " + networkDestinationPath);
			copyFileToNetworkDestination(networkDestinationPath, args[0]);
		}
	}
}
