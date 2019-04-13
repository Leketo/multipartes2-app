package py.multipartesapp.beans;

/**
 * Created by Adolfo on 09/12/2015.
 */
public class LocationTable {

    private Integer id;
    private String latitude;
    private String longitude;
    private String date;
    private String time;
    private String id_user;
    private String estado_envio;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
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

    @Override
    public String toString() {
        return "LocationTable{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", id_user='" + id_user + '\'' +
                ", estado_envio='" + estado_envio + '\'' +
                '}';
    }
}
