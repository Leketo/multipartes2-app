package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StockList extends Bean {
    List<StockDTO> list;

    public List<StockDTO> getList() {
        return list;
    }

    public void setList(List<StockDTO> list) {
        this.list = list;
    }

    @Override
    public void initWithJson(JSONObject o) throws JSONException {

    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<StockDTO> array = new ArrayList<StockDTO>();

        for( int i = 0; i < jsonArray.length(); i++){
            StockDTO object = (StockDTO) new StockDTO().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
