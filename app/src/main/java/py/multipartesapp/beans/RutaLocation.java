package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 08/03/2016.
 */
public class RutaLocation extends Bean {

    Integer id;
    String date;
    Integer user_id;
    String latitude;
    String longitude;
    Integer client_id;
    Integer zone;
    Integer priority;
    String status;
    String observation;

    // Y or N
    String entrada;
    String salida;
    String fechaHoraEntrada;
    String FechaHoraSalida;


    String estadoEnvio;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        id = getInteger(o, "id");
        date = getString(o, "date");
        user_id = getInteger(o, "user_id");
        latitude = getString(o, "latitude");
        longitude = getString(o, "longitude");
        client_id = getInteger(o, "client_id");
        zone = getInteger(o, "zone");
        priority = getInteger(o, "priority");
        status = getString(o, "status");
        type = getString(o, "type");
        observation = getString(o, "observation");
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getClient_id() {
        return client_id;
    }

    public void setClient_id(Integer client_id) {
        this.client_id = client_id;
    }

    public Integer getZone() {
        return zone;
    }

    public void setZone(Integer zone) {
        this.zone = zone;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    @Override
    public String toString() {
        return "RutaLocation{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", user_id=" + user_id +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", client_id=" + client_id +
                ", zone=" + zone +
                ", priority=" + priority +
                ", status='" + status + '\'' +
                ", observation='" + observation + '\'' +
                '}';
    }

    public String getEntrada() {
        return entrada;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public String getSalida() {
        return salida;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }

    public String getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(String fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public String getFechaHoraSalida() {
        return FechaHoraSalida;
    }

    public void setFechaHoraSalida(String fechaHoraSalida) {
        FechaHoraSalida = fechaHoraSalida;
    }
}
