package py.multipartes2.comm;

import org.json.JSONException;
import org.json.JSONObject;

import py.multipartes2.comm.Comm.CommDelegate;
import py.multipartes2.comm.Http.Logger;

public class CommDelegateAndroid extends CommDelegate {

	public void onError() {
		try {
			printResponse();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exception.printStackTrace();
	}
	
	public ICommExecutor getExecutor() {
		if( executor == null ){
			executor = new AndroidExecutor();
		}
		return executor;
	}
	
	public Logger getLogger() {
		if( logger == null ){
			logger = new AndroidLogger();
		}
		return logger;
	}

	public void onSuccess() {
		try {
			printResponse();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printResponse() throws JSONException {
		Object o = response.getObject();
		if( o!=null && o instanceof JSONObject ){
			System.out.println(((JSONObject)o).toString(3));	
		} else {
		System.out.println(response);
		}
	}

}
