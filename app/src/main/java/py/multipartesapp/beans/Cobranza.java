package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Adolfo on 01/04/2016.
 */
public class Cobranza extends Bean {

    Integer id;
    Integer client_id;
    Integer user_id;
    Integer amount;
    String invoice_number;
    String observation;
    String status;
    String nombre_cliente;
    String nombre_vendedor;
    String estado_envio;

    List<CobranzaDetalle> detalles;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        id = getInteger(o, "id");
        //client_id = getInteger(o, "client_id");
        user_id = getInteger(o, "user_id");
        amount = getInteger(o, "amount");
        invoice_number = getString(o, "receipt_number");
        observation = getString(o, "observation");
        status = getString(o, "status");
        nombre_cliente = getString(o, "nombre_cliente");
        nombre_vendedor = getString(o, "nombre_vendedor");

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

    public Integer getClient_id() {
        return client_id;
    }

    public void setClient_id(Integer client_id) {
        this.client_id = client_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNombre_cliente() {
        return nombre_cliente;
    }

    public void setNombre_cliente(String nombre_cliente) {
        this.nombre_cliente = nombre_cliente;
    }

    public String getNombre_vendedor() {
        return nombre_vendedor;
    }

    public void setNombre_vendedor(String nombre_vendedor) {
        this.nombre_vendedor = nombre_vendedor;
    }

    public String getEstado_envio() {
        return estado_envio;
    }

    public void setEstado_envio(String estado_envio) {
        this.estado_envio = estado_envio;
    }

    public List<CobranzaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<CobranzaDetalle> detalles) {
        this.detalles = detalles;
    }
}