package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolfo on 01/08/2016.
 */
public class ProductoFamiliaList extends Bean {

    private List<ProductoFamilia> list;

    public void setList(List<ProductoFamilia> list) {
        this.list = list;
    }


    public List<ProductoFamilia> getList() {
        return list;
    }


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<ProductoFamilia> array = new ArrayList<ProductoFamilia>();

        for( int i = 0; i < jsonArray.length(); i++){
            ProductoFamilia object = (ProductoFamilia) new ProductoFamilia().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
