package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolfo on 02/03/2016.
 */
public class PrecioCategoriaList extends Bean {

    private List<PrecioCategoria> list;

    public void setList(List<PrecioCategoria> list) {
        this.list = list;
    }


    public List<PrecioCategoria> getList() {
        return list;
    }


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<PrecioCategoria> array = new ArrayList<PrecioCategoria>();

        for( int i = 0; i < jsonArray.length(); i++){
            PrecioCategoria object = (PrecioCategoria) new PrecioCategoria().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
