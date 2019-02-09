package py.multipartes2.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import py.multipartes2.utils.MyFormatter;

/**
 * Created by Adolfo on 15/01/2016.
 */
public class Pedido extends Bean {

    Integer id;
    Integer ad_client_id;
    Integer ad_org_id;
    String isactive;
    String date_order;
    Integer order_id;
    Integer client_id;
    Integer user_id;
    Integer total;
    String observation;

    String estado_envio;
    String nombre_cliente;
    String isinvoiced;

    String montoCobrado;

    List<PedidoDetalle> detalles;


    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        id = getInteger(o, "id");
        ad_client_id = getInteger(o, "ad_client_id");
        ad_org_id = getInteger(o, "ad_org_id");
        isactive = getString(o, "isactive");
        date_order = getString(o, "date_order");
        order_id = getInteger(o, "order_id");
        client_id = getInteger(o, "client_id");
        user_id = getInteger(o, "user_id");
        total = getInteger(o, "total");
        observation = getString(o, "observation");
        isinvoiced = getString(o, "isinvoiced");

        //cargar detalle
        JSONArray jsonArray = o.getJSONArray("orderline");
        ArrayList<PedidoDetalle> array = new ArrayList<PedidoDetalle>();

        for( int i = 0; i < jsonArray.length(); i++){
            PedidoDetalle object = (PedidoDetalle) new PedidoDetalle().fromJSON(jsonArray.getJSONObject(i));
            array.add(object);
        }
        setDetalles(array);
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

    public Integer getAd_client_id() {
        return ad_client_id;
    }

    public void setAd_client_id(Integer ad_client_id) {
        this.ad_client_id = ad_client_id;
    }

    public Integer getAd_org_id() {
        return ad_org_id;
    }

    public void setAd_org_id(Integer ad_org_id) {
        this.ad_org_id = ad_org_id;
    }

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }

    public String getDate_order() {
        return date_order;
    }

    public void setDate_order(String date_order) {
        this.date_order = date_order;
    }

    public Integer getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public List<PedidoDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<PedidoDetalle> detalles) {
        this.detalles = detalles;
    }

    public String getEstado_envio() {
        return estado_envio;
    }

    public void setEstado_envio(String estado_envio) {
        this.estado_envio = estado_envio;
    }

    public String getNombre_cliente() {
        return nombre_cliente;
    }

    public void setNombre_cliente(String nombre_cliente) {
        this.nombre_cliente = nombre_cliente;
    }

    public String getIsinvoiced() {
        return isinvoiced;
    }

    public void setIsinvoiced(String isinvoiced) {
        this.isinvoiced = isinvoiced;
    }

    public String getMontoCobrado() {
        return montoCobrado;
    }

    public void setMontoCobrado(String montoCobrado) {
        this.montoCobrado = montoCobrado;
    }

    @Override
    public String toString() {
        String[] splits = date_order.split("-");
        String  fecha = splits[2] + "-" + splits[1] + "-" + splits[0];
        return "Nro. :"+ id + " | "+ fecha + " | Total: "+ MyFormatter.formatearMoneda(total.toString());
    }
}
