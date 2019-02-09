package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 01/08/2016.
 */
public class ProductoFamilia extends Bean {

    private Integer m_product_family_id;
    private String value;
    private String description;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        m_product_family_id = getInteger(o, "m_product_family_id");
        value = getString(o, "value");
        description = getString(o, "description");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {

    }

    public Integer getM_product_family_id() {
        return m_product_family_id;
    }

    public void setM_product_family_id(Integer m_product_family_id) {
        this.m_product_family_id = m_product_family_id;
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

    @Override
    public String toString() {
        return this.description;
    }
}
