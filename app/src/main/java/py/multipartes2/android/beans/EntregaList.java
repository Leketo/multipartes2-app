package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class EntregaList extends Bean {

    private List<Entrega> list;

	public void setList(List<Entrega> list) {
		this.list = list;
	}
	
	public List<Entrega> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<Entrega> array = new ArrayList<Entrega>();

        for( int i = 0; i < jsonArray.length(); i++){
            Entrega object = (Entrega) new Entrega().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
