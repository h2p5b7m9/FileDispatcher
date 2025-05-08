/*
OJO
run()
Runnable
start()
synchronized
Thread crea cuando AqNewAS400FileDetection.java hace startMonitoring()
KK HashMap
KK toMap(
KK .put(
KK .get(
Vector
.clone()
*/

//---------- DirectoryMonitor.java ----------
package DownloadTool00;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

public class AqDirectoryMonitor implements Runnable {
	private File _directory;
	private long _interval;
	private Vector _listeners;
	private boolean _stopped;
	private boolean _daemon;
	private AqConfigAndLog cl;

	public AqDirectoryMonitor(File directory, long interval, String var) { // Constructor // interval = 5000 ; var = var0
		if (directory.isDirectory() == false) {
            System.out.println("File argument must denote a directory\n" + directory + "\n" + interval + "\n" + var);
			throw new IllegalArgumentException("File argument must denote a directory");
		}
		_directory = directory;
		_interval = interval;
		_listeners = new Vector();
		cl = new AqConfigAndLog(var);
	}

	public void addDirectoryChangeListener(AqDirectoryChangeListener dcl) { // interface AqDirectoryChangeListener
		_listeners.add(dcl); // Añade elem a vector
	}
	public void removeDirectoryChangeListener(AqDirectoryChangeListener dcl) {
		_listeners.remove(dcl); // Elimina elem de vector
	}

	private Thread _monitor = null;
	public synchronized void startMonitoring() {
		stopMonitoring();
		_stopped = false;
		_monitor = new Thread(this); // Thread crea instancia antes de setDaemon( y start()
		_monitor.setDaemon(_daemon);
		_monitor.start();
	}
	public synchronized void stopMonitoring() { // Stop Thread
		_stopped = true;
		try {
			if (_monitor != null) { // Thread not started
				_monitor.interrupt(); // Stop Thread
				_monitor.join(); // Espera que el hilo Thread termine stop de ejecutarse
			}
		} catch (InterruptedException e) {
		}
		_monitor = null;
	}

	public void setDaemon(boolean daemon) {
		_daemon = daemon;
		if (_monitor != null) {
			_monitor.setDaemon(daemon); // setDaemon() must be called before the thread is started
		}
	}

	public void run() {
		// File[] prevList = null; // Mio no necesario creo: Si no la siguiente asignacion no borra lo que tenia
		File[] prevList = _directory.listFiles(); // prevList = [C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp] ; _directory=C:\Users\ignac\Documents\Java\DownloadTool00\Files
		HashMap prevMap = toMap(prevList); // prevMap = {C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp=C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini=C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini} ; prevList = [C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp]
		//checkAdditions(prevList, new HashMap());
		initialCheck(prevList);
		while (true) {
			try {
				Thread.sleep(_interval); // espera ; _interval=5000
			} catch (InterruptedException e) {
				// ignore
			}
			if (_stopped == true) {
				return;
			}
			File[] currList = _directory.listFiles(); // listfiles() lista ficheros y directorios del directorio "C:\Users\ignac\Documents\Java\DownloadTool00\Files" ed le mueve "C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp", "C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini", "C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.CSV.TXT" y "C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.INI.TXT" o [C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp]
			HashMap currMap = toMap(currList); // currMap = {C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp=C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini=C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini}
			//long t1 = System.currentTimeMillis();
			checkAdditions(currList, prevMap);
			checkRemovals(prevList, currMap);
			//long t2 = System.currentTimeMillis();
			//System.out.print(" Sweep: " + (t2 - t1) + "ms");
			prevList = currList;
			prevMap = currMap;
		}
	}

	private HashMap toMap(File[] files) { // files = [C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini, C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp]
		int len = files.length;
		HashMap map = new HashMap();
		for (int i = 0; i < len; ++i) {
			map.put(files[i].toString(), files[i]);
		}
		return map; // currMap = {C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp=C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini=C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini}
	}

	private void checkAdditions(File[] curr, HashMap prev) { // curr = [C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp] ; prev = {C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.CSV.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp=C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp, C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.INI.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini=C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini}
		int len = curr.length; // len = 4
		int type = AqDirectoryChangeEvent.FILE_ADDED; // 1
		for (int i = 0; i < len; ++i) {
			if (prev.get(curr[i].toString()) == null) { // Para cada elemento de prev que está en curr crea un EventObject
				AqDirectoryChangeEvent dce = new AqDirectoryChangeEvent(curr[i], type);
				fireNotify(dce);
			}
		}
	}

	private void checkRemovals(File[] prev, HashMap curr) { // prev=[C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp] ; curr = {C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp=C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp, C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT, C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini=C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini}
		int len = prev.length; // len = 4
		int type = AqDirectoryChangeEvent.FILE_REMOVED; // 2
		for (int i = 0; i < len; ++i) {
			if (curr.get(prev[i].toString()) == null) {
				AqDirectoryChangeEvent dce = new AqDirectoryChangeEvent(prev[i], type);
				fireNotify(dce);
			}
		}
	}

	private void initialCheck(File[] curr) {
		int len  = curr.length;
		for (int i = 0 ; i < len ; ++i) {
			if((!(curr[i].getName().endsWith(".INI.TXT")) && !(curr[i].getName().endsWith(".CSV.TXT")) &&
				(!(curr[i].getName().endsWith(".ini.txt")) && !(curr[i].getName().endsWith(".csv.txt"))))) {
				curr[i].delete();
				cl.writeInError("File: " + curr[i].getName() + " deleted on startup");
			}
		}
		checkAdditions(curr, new HashMap()); // OJO Como parametro le pasa un new

	}

	private void fireNotify(AqDirectoryChangeEvent dce) { // dce = DownloadTool00.AqDirectoryChangeEvent[source=C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini] ; dce = DownloadTool00.AqDirectoryChangeEvent[source=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT] ; dce = DownloadTool00.AqDirectoryChangeEvent[source=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.INI.TXT] ; dce = DownloadTool00.AqDirectoryChangeEvent[source=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.CSV.TXT]
		Vector list = (Vector)_listeners.clone(); // _listeners es tipo Vector por lo que se podria eliminar su (Vector) creo
		int len = list.size(); // Numero de elementos del vector ; len = 1
		for (int i = 0; i < len; ++i) {
			AqDirectoryChangeListener dcl = (AqDirectoryChangeListener)list.get(i);
			dcl.directoryChange(dce);
		}
		System.out.println("imc010-list = " + list);
	}

}