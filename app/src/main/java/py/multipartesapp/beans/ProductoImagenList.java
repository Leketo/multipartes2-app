package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolfo on 01/08/2016.
 */
public class ProductoImagenList extends Bean {

    private List<ProductoImagen> list;

    public void setList(List<ProductoImagen> list) {
        this.list = list;
    }
    public List<ProductoImagen> getList() {
        return list;
    }


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<ProductoImagen> array = new ArrayList<ProductoImagen>();

        for( int i = 0; i < jsonArray.length(); i++){
            ProductoImagen object = (ProductoImagen) new ProductoImagen().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
