package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CobranzaList extends Bean {

    private List<Cobranza> list;

	public void setList(List<Cobranza> list) {
		this.list = list;
	}

	
	public List<Cobranza> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<Cobranza> array = new ArrayList<Cobranza>();

        for( int i = 0; i < jsonArray.length(); i++){
            Cobranza object = (Cobranza) new Cobranza().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
