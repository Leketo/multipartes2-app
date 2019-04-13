package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PedidoList extends Bean {

    private List<Pedido> list;

	public void setList(List<Pedido> list) {
		this.list = list;
	}

	
	public List<Pedido> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<Pedido> array = new ArrayList<Pedido>();

        for( int i = 0; i < jsonArray.length(); i++){
            Pedido object = (Pedido) new Pedido().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
