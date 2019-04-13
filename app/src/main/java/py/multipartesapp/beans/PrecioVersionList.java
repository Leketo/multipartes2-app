package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolfo on 02/03/2016.
 */
public class PrecioVersionList extends Bean {

    private List<PrecioVersion> list;

    public void setList(List<PrecioVersion> list) {
        this.list = list;
    }


    public List<PrecioVersion> getList() {
        return list;
    }


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<PrecioVersion> array = new ArrayList<PrecioVersion>();

        for( int i = 0; i < jsonArray.length(); i++){
            PrecioVersion object = (PrecioVersion) new PrecioVersion().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
