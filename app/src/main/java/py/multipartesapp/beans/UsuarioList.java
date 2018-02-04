package py.multipartesapp.beans;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolfo on 26/12/2015.
 */
public class UsuarioList extends Bean {

    private List<Usuario> list;

    public void setList(List<Usuario> list) {
        this.list = list;
    }


    public List<Usuario> getList() {
        return list;
    }


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        Log.d("se llama a initWith Json en usuarioList", "");
        //JSONArray jsonArray = o.getJSONArray("places");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
        JSONArray jsonArray = o;
        ArrayList<Usuario> array = new ArrayList<Usuario>();

        for( int i = 0; i < jsonArray.length(); i++){
            Usuario object = (Usuario) new Usuario().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setList(array);
    }
}
