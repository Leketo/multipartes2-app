package py.multipartes2.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSON {

	
	public static String getStringOrNull(JSONObject o, String key) {
		try {
			if( o.has(key) && o.get(key)!=JSONObject.NULL){
				return o.getString(key);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public static Integer getIntegerOrNull(JSONObject o, String key) {
		try {
			if( o.has(key) && o.get(key)!=JSONObject.NULL){
				return (Integer)o.getInt(key);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public static Double getDoubleOrNull(JSONObject o, String key) {
		try {
			if( o.has(key) && o.get(key)!=JSONObject.NULL){
				return (Double)o.getDouble(key);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static JSONObject getJSONObjectOrNull(JSONObject o, String key) {
		try {
			if (o.has(key) && o.get(key) != JSONObject.NULL) {
				return o.getJSONObject(key);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static boolean getBoolean(JSONObject o, String key, boolean def) {
		try {
			if (o.has(key) && o.get(key) != JSONObject.NULL) {
				return o.getBoolean(key);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return def;

	}

	public static JSONArray getJSONArrayOrNull(JSONObject o, String key) {
		try {
			if (o.has(key) && o.get(key) != JSONObject.NULL) {
				return o.getJSONArray(key);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
