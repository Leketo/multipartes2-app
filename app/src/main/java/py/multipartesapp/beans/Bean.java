package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import py.multipartes2.utils.JSON;


public abstract class Bean implements Serializable{

    protected String msg;
    protected String type;
    protected String code;

    protected void getError (JSONObject o){
        type = getString(o, "type");
        code = getString(o, "code");
        msg = getString(o, "msg");
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    protected String getString(JSONObject o, String key) {
		return JSON.getStringOrNull(o, key);
	}
	
	protected JSONObject getJSONObject(JSONObject o, String key) {

		return JSON.getJSONObjectOrNull(o,key);
	}
	
	protected Integer getInteger(JSONObject o, String key) {
		return JSON.getIntegerOrNull(o, key);
	}
	
	protected Double getDouble(JSONObject o, String key) {
		return JSON.getDoubleOrNull(o, key);
	}
	
	protected boolean getBoolean(JSONObject o, String string, boolean def) {
		return JSON.getBoolean(o,string,def);
	}

	public Bean fromJSON(String response) {
		try {
            if (response == "")
                return null;

            //si llego un array
            if ( response.charAt(0) == '[' ){
                initWithJsonArray(new JSONArray(response));
                return this;
            }

			JSONObject o = new JSONObject(response);
			return fromJSON(o);
		} catch (JSONException e) {
            //atrapar y no hacer nada
            e.printStackTrace();
            return null;
            //throw new RuntimeException(e);
		}
	}
	
	public Bean fromJSON(JSONObject response) {
		try {
			initWithJson(response);		
			return this;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public abstract void initWithJson(JSONObject o) throws JSONException;

    public abstract void initWithJsonArray(JSONArray o) throws JSONException;



}
