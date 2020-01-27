package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Adolfo on 01/04/2016.
 */
public class Factura extends Bean {

    Integer id;
    String isactive;
    Integer order_id;
    String dateinvoiced;
    Integer client_id;
    Integer grandtotal;
    String ispaid;
    Integer pend;
    String nroFacturaImprimir;

    String montoCobrado;

    boolean isSelected;


    private List<CobranzaFormaPago> items;


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        id = getInteger(o, "id");
        isactive = getString(o, "isactive");
        order_id = getInteger(o, "order_id");
        dateinvoiced = getString(o, "dateinvoiced");
        client_id = getInteger(o, "client_id");
        grandtotal = getInteger(o, "grandtotal");
        ispaid = getString(o, "ispaid");
        pend = getInteger(o, "pend");
        nroFacturaImprimir = getString(o, "nrofacturaimprimir");
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

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }

    public Integer getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    public String getDateinvoiced() {
        return dateinvoiced;
    }

    public void setDateinvoiced(String dateinvoiced) {
        this.dateinvoiced = dateinvoiced;
    }

    public Integer getClient_id() {
        return client_id;
    }

    public void setClient_id(Integer client_id) {
        this.client_id = client_id;
    }

    public Integer getGrandtotal() {
        return grandtotal;
    }

    public void setGrandtotal(Integer grandtotal) {
        this.grandtotal = grandtotal;
    }

    public String getIspaid() {
        return ispaid;
    }

    public void setIspaid(String ispaid) {
        this.ispaid = ispaid;
    }

    public Integer getPend() {
        return pend;
    }

    public void setPend(Integer pend) {
        this.pend = pend;
    }

    public String getMontoCobrado() {
        return montoCobrado;
    }

    public void setMontoCobrado(String montoCobrado) {
        this.montoCobrado = montoCobrado;
    }

    public String getNroFacturaImprimir() {
        return nroFacturaImprimir;
    }


    public void setNroFacturaImprimir(String nroFacturaImprimir) {
        this.nroFacturaImprimir = nroFacturaImprimir;
    }

    public List<CobranzaFormaPago> getItems() {
        return items;
    }

    public void setItems(List<CobranzaFormaPago> items) {
        this.items = items;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", isactive='" + isactive + '\'' +
                ", order_id=" + order_id +
                ", dateinvoiced='" + dateinvoiced + '\'' +
                ", client_id=" + client_id +
                ", grandtotal=" + grandtotal +
                ", ispaid='" + ispaid + '\'' +
                ", pend=" + pend +
                ", nroFacturaImprimir='" + nroFacturaImprimir + '\'' +
                ", montoCobrado='" + montoCobrado + '\'' +
                ", isSelected=" + isSelected +
                ", items=" + items +
                '}';
    }
}