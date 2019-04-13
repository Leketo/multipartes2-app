package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 06/09/2015.
 */
public class Cliente extends Bean {

    private Integer id;
    private String ruc;
    private String nombre;
    private String direccion;
    private String telefono;

    private Integer categoria_precio;

    private Double credito_disponible;
    private Double credito_extra;
    private Double credito_usado;
    private Double cheques_pend;
    private String factura_vieja;
    private Integer plazo_max_cheque;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        id = getInteger(o, "id");
        ruc = getString(o, "ruc");
        nombre = getString(o, "nombre");
        direccion = getString(o, "direccion");
        telefono = getString(o, "telefono");
        credito_disponible = getDouble(o, "credito_disponible");
        credito_extra = getDouble(o, "credito_extra");
        credito_usado = getDouble(o, "credito_usado");
        cheques_pend = getDouble(o, "cheques_pend");
        factura_vieja = getString(o, "factura_vieja");
        plazo_max_cheque = getInteger(o, "plazo_max_cheque");
        categoria_precio = getInteger(o, "categoria_precio");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }

    @Override
    public String toString() {
        return nombre + " - " + ruc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Double getCredito_disponible() {
        return credito_disponible;
    }

    public void setCredito_disponible(Double credito_disponible) {
        this.credito_disponible = credito_disponible;
    }

    public Double getCredito_extra() {
        return credito_extra;
    }

    public void setCredito_extra(Double credito_extra) {
        this.credito_extra = credito_extra;
    }

    public Double getCredito_usado() {
        return credito_usado;
    }

    public void setCredito_usado(Double credito_usado) {
        this.credito_usado = credito_usado;
    }

    public Double getCheques_pend() {
        return cheques_pend;
    }

    public void setCheques_pend(Double cheques_pend) {
        this.cheques_pend = cheques_pend;
    }

    public String getFactura_vieja() {
        return factura_vieja;
    }

    public void setFactura_vieja(String factura_vieja) {
        this.factura_vieja = factura_vieja;
    }

    public Integer getPlazomax() {
        return plazo_max_cheque;
    }

    public void setPlazomax(Integer plazomax) {
        this.plazo_max_cheque = plazomax;
    }

    public Integer getCategoria_precio() {
        return categoria_precio;
    }

    public void setCategoria_precio(Integer categoria_precio) {
        this.categoria_precio = categoria_precio;
    }
}