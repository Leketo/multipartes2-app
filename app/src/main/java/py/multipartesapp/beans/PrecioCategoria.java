package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 02/03/2016.
 */
public class PrecioCategoria extends Bean {


    Integer rowid;
    Integer m_pricelist_version_id; //idPrecioVersion
    Integer m_pricelist_id;
    String name;
    String active;
    Integer ad_client_id;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        rowid = getInteger(o, "rowid");
        m_pricelist_version_id = getInteger(o, "m_pricelist_version_id");
        m_pricelist_id = getInteger(o, "m_pricelist_id");
        name = getString(o, "name");
        active = getString(o, "active");
        ad_client_id = getInteger(o, "ad_client_id");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }

    public Integer getRowid() {
        return rowid;
    }

    public void setRowid(Integer rowid) {
        this.rowid = rowid;
    }

    public Integer getM_pricelist_version_id() {
        return m_pricelist_version_id;
    }

    public void setM_pricelist_version_id(Integer m_pricelist_version_id) {
        this.m_pricelist_version_id = m_pricelist_version_id;
    }

    public Integer getM_pricelist_id() {
        return m_pricelist_id;
    }

    public void setM_pricelist_id(Integer m_pricelist_id) {
        this.m_pricelist_id = m_pricelist_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Integer getAd_client_id() {
        return ad_client_id;
    }

    public void setAd_client_id(Integer ad_client_id) {
        this.ad_client_id = ad_client_id;
    }
}
