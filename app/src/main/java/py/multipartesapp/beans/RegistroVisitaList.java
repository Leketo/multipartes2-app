package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class RegistroVisitaList extends Bean {

    private List<RegistroVisita> list;

	public void setList(List<RegistroVisita> list) {
		this.list = list;
	}
	
	public List<RegistroVisita> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<RegistroVisita> array = new ArrayList<RegistroVisita>();

        for( int i = 0; i < jsonArray.length(); i++){
            RegistroVisita object = (RegistroVisita) new RegistroVisita().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
