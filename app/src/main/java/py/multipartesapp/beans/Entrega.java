package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 09/10/2015.
 */
public class Entrega extends Bean {

    Integer id;

    String user_id;
    String client_id;
    String order_id;
    String date_delivered;
    String time_delivered;
    String observation;
    String nombre_cliente;
    String nombre_vendedor;

    String estado_envio;


    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        id = getInteger(o, "id");

        user_id = getInteger(o, "user_id").toString();
        client_id = getInteger(o, "client_id").toString();
        order_id = getInteger(o, "order_id").toString();
        date_delivered = getString(o, "date_delivered");
        time_delivered = getString(o, "time_delivered");
        observation = getString(o, "observation");

        nombre_cliente = getString(o, "nombre_cliente");
        nombre_vendedor = getString(o, "nombre_vendedor");
        estado_envio = "ENVIADO";

    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getDate_delivered() {
        return date_delivered;
    }

    public void setDate_delivered(String date_delivered) {
        this.date_delivered = date_delivered;
    }

    public String getTime_delivered() {
        return time_delivered;
    }

    public void setTime_delivered(String time_delivered) {
        this.time_delivered = time_delivered;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getEstado_envio() {
        return estado_envio;
    }

    public void setEstado_envio(String estado_envio) {
        this.estado_envio = estado_envio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Entrega{" +
                "id=" + id +
                ", user_id='" + user_id + '\'' +
                ", client_id='" + client_id + '\'' +
                ", order_id='" + order_id + '\'' +
                ", date_delivered='" + date_delivered + '\'' +
                ", time_delivered='" + time_delivered + '\'' +
                ", observation='" + observation + '\'' +
                ", estado_envio='" + estado_envio + '\'' +
                '}';
    }
}
