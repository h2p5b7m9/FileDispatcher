/*
EventObject
*/

package DownloadTool00;


public class AqDirectoryChangeEvent extends java.util.EventObject {
	public static final int FILE_ADDED = 1;
	public static final int FILE_REMOVED = 2;

	private int _type;

	public AqDirectoryChangeEvent(Object source, int type) { // Constructor ; source = C:\Users\ignac\Documents\Java\DownloadTool00\Files\config.ini ; type = 1
		super(source);
		_type = type;
	}

	public int getType() {
		return _type;
	}
}