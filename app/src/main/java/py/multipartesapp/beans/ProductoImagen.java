package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 18/06/2016.
 */
public class ProductoImagen extends Bean{

    private Integer m_product_id;
    private String img;
    private String size;
    private String estado_envio;

    private String nombreProducto;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        m_product_id = getInteger(o, "m_product_id");
        img = getString(o, "img");
        size = getString(o, "size");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }

    public Integer getM_product_id() {
        return m_product_id;
    }

    public void setM_product_id(Integer m_product_id) {
        this.m_product_id = m_product_id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getEstado_envio() {
        return estado_envio;
    }

    public void setEstado_envio(String estado_envio) {
        this.estado_envio = estado_envio;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
}
