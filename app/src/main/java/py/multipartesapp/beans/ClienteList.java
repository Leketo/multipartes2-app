package py.multipartesapp.beans;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ClienteList extends Bean {

    private List<Cliente> list;

	public void setList(List<Cliente> list) {
		this.list = list;
	}

	
	public List<Cliente> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        Log.d("se llama a initWith Json en clienteList", "");
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<Cliente> array = new ArrayList<Cliente>();

        for( int i = 0; i < jsonArray.length(); i++){
            Cliente object = (Cliente) new Cliente().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }

    @Override
    public String toString() {
        return "ClienteList{" +
                "list=" + list +
                '}';
    }
}
