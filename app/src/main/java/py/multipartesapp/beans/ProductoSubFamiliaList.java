package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolfo on 01/08/2016.
 */
public class ProductoSubFamiliaList extends Bean {

    private List<ProductoSubFamilia> list;

    public void setList(List<ProductoSubFamilia> list) {
        this.list = list;
    }


    public List<ProductoSubFamilia> getList() {
        return list;
    }


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<ProductoSubFamilia> array = new ArrayList<ProductoSubFamilia>();

        for( int i = 0; i < jsonArray.length(); i++){
            ProductoSubFamilia object = (ProductoSubFamilia) new ProductoSubFamilia().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
