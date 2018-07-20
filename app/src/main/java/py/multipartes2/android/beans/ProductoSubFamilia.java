package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 01/08/2016.
 */
public class ProductoSubFamilia extends Bean {

    private Integer id;
    private String value;
    private String description;

    private Integer id_familia;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        id = getInteger(o, "id");
        value = getString(o, "value");
        description = getString(o, "description");

        JSONObject familia = o.getJSONObject("family");
        id_familia = familia.getInt("m_product_family_id");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId_familia() {
        return id_familia;
    }

    public void setId_familia(Integer id_familia) {
        this.id_familia = id_familia;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
