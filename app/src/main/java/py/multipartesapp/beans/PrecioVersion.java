package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 02/03/2016.
 */
public class PrecioVersion extends Bean {

    Integer id;
    Integer m_product_id;

    Integer precio_ventas_inicial;
    Integer precio_costo_inicial;
    Integer precio_publico;
    Integer precio_mayorista_a;
    Integer precio_mayorista_b;
    Integer precio_lista;
    Integer precio_vidrieros;
    Integer precio_radiadoritas;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        id = getInteger(o, "id");
        m_product_id = getInteger(o, "m_product_id");
        precio_ventas_inicial = getInteger(o, "precio_ventas_inicial");
        precio_costo_inicial = getInteger(o, "precio_costo_inicial");
        precio_publico = getInteger(o, "precio_publico");
        precio_mayorista_a = getInteger(o, "precio_mayorista_a");
        precio_mayorista_b = getInteger(o, "precio_mayorista_b");
        precio_lista = getInteger(o, "precio_lista");
        precio_vidrieros = getInteger(o, "precio_vidrieros");
        precio_radiadoritas = getInteger(o, "precio_radiadoritas");
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

    public Integer getM_product_id() {
        return m_product_id;
    }

    public void setM_product_id(Integer m_product_id) {
        this.m_product_id = m_product_id;
    }

    public Integer getPrecio_ventas_inicial() {
        return precio_ventas_inicial;
    }

    public void setPrecio_ventas_inicial(Integer precio_ventas_inicial) {
        this.precio_ventas_inicial = precio_ventas_inicial;
    }

    public Integer getPrecio_costo_inicial() {
        return precio_costo_inicial;
    }

    public void setPrecio_costo_inicial(Integer precio_costo_inicial) {
        this.precio_costo_inicial = precio_costo_inicial;
    }

    public Integer getPrecio_publico() {
        return precio_publico;
    }

    public void setPrecio_publico(Integer precio_publico) {
        this.precio_publico = precio_publico;
    }

    public Integer getPrecio_mayorista_a() {
        return precio_mayorista_a;
    }

    public void setPrecio_mayorista_a(Integer precio_mayorista_a) {
        this.precio_mayorista_a = precio_mayorista_a;
    }

    public Integer getPrecio_mayorista_b() {
        return precio_mayorista_b;
    }

    public void setPrecio_mayorista_b(Integer precio_mayorista_b) {
        this.precio_mayorista_b = precio_mayorista_b;
    }

    public Integer getPrecio_lista() {
        return precio_lista;
    }

    public void setPrecio_lista(Integer precio_lista) {
        this.precio_lista = precio_lista;
    }

    public Integer getPrecio_vidrieros() {
        return precio_vidrieros;
    }

    public void setPrecio_vidrieros(Integer precio_vidrieros) {
        this.precio_vidrieros = precio_vidrieros;
    }

    public Integer getPrecio_radiadoritas() {
        return precio_radiadoritas;
    }

    public void setPrecio_radiadoritas(Integer precio_radiadoritas) {
        this.precio_radiadoritas = precio_radiadoritas;
    }
}
