package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 09/10/2015.
 */
public class RegistroVisita extends Bean{

    String tipo_visita;
    String ruc;
    Integer cliente;
    Double latitude;
    Double longitude;
    String observation;
    String status;
    String usuario;
    String fechavisita;
    String horavisita;
    String fecha_prox_visita;
    String estado_envio;
    String ent_sal;

    String nombreUsuario;


    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        tipo_visita = getString(o, "tipo_visita");
        ruc = getString(o, "ruc");
        cliente = getInteger(o, "cliente");
        String latitudeString = getString(o, "latitude");
        String longitudeString = getString(o, "longitude");

        latitude = Double.valueOf(latitudeString);
        longitude = Double.valueOf(longitudeString);

        observation = getString(o, "observation");
        status = getString(o, "status");
        //usuario = getString();
        fechavisita = getString(o, "fechavisita");
        horavisita = getString(o, "horavisita");
        fecha_prox_visita = getString(o, "fechaproxvisita") ;
        estado_envio = "ENVIADO";
        //ent_sal = "";
        nombreUsuario = getString(o, "nombreUsuario");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }


    public String getTipo_visita() {
        return tipo_visita;
    }

    public void setTipo_visita(String tipo_visita) {
        this.tipo_visita = tipo_visita;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public Integer getCliente() {
        return cliente;
    }

    public void setCliente(Integer cliente) {
        this.cliente = cliente;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getFechavisita() {
        return fechavisita;
    }

    public void setFechavisita(String fechavisita) {
        this.fechavisita = fechavisita;
    }

    public String getHoravisita() {
        return horavisita;
    }

    public void setHoravisita(String horavisita) {
        this.horavisita = horavisita;
    }

    public String getFecha_prox_visita() {
        return fecha_prox_visita;
    }

    public void setFecha_prox_visita(String fecha_prox_visita) {
        this.fecha_prox_visita = fecha_prox_visita;
    }

    public String getEstado_envio() {
        return estado_envio;
    }

    public void setEstado_envio(String estado_envio) {
        this.estado_envio = estado_envio;
    }

    public String getEnt_sal() {
        return ent_sal;
    }

    public void setEnt_sal(String ent_sal) {
        this.ent_sal = ent_sal;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public String toString() {
        return "RegistroVisita{" +
                "tipo_visita='" + tipo_visita + '\'' +
                ", ruc='" + ruc + '\'' +
                ", cliente=" + cliente +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", observation='" + observation + '\'' +
                ", status='" + status + '\'' +
                ", usuario='" + usuario + '\'' +
                ", fechavisita='" + fechavisita + '\'' +
                ", horavisita='" + horavisita + '\'' +
                ", fecha_prox_visita='" + fecha_prox_visita + '\'' +
                ", estado_envio='" + estado_envio + '\'' +
                ", ent_sal='" + ent_sal + '\'' +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                '}';
    }
}
