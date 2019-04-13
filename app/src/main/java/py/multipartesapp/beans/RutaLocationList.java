package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class RutaLocationList extends Bean {

    private List<RutaLocation> list;

	public void setList(List<RutaLocation> list) {
		this.list = list;
	}

	
	public List<RutaLocation> getList() {
		return list;
	}


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<RutaLocation> array = new ArrayList<RutaLocation>();

        for( int i = 0; i < jsonArray.length(); i++){
            RutaLocation object = (RutaLocation) new RutaLocation().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
