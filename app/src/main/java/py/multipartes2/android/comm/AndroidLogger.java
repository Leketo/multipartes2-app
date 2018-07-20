package py.multipartes2.comm;

import android.util.Log;

import py.multipartes2.comm.Http.Logger;

public class AndroidLogger implements Logger {

	public void d(String tag, String format) {
//		System.out.println("DEBUG: "+tag+", "+format);
		Log.d(tag, format);
	}

	public void e(String tag, String message, Throwable e) {
		Log.e(tag, message,e);
//		System.out.println("ERROR: "+tag+", msg"+ message);
//		e.printStackTrace();

	}

}
