package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FacturaList extends Bean {

    private List<Factura> list;

	public void setList(List<Factura> list) {
		this.list = list;
	}
	
	public List<Factura> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<Factura> array = new ArrayList<Factura>();

        for( int i = 0; i < jsonArray.length(); i++){
            Factura object = (Factura) new Factura().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
