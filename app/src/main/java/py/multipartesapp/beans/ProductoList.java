package py.multipartesapp.beans;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ProductoList extends Bean {

    private List<Producto> list;

	public void setList(List<Producto> list) {
		this.list = list;
	}

	
	public List<Producto> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<Producto> array = new ArrayList<Producto>();

        for( int i = 0; i < jsonArray.length(); i++){
            Producto object = (Producto) new Producto().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
