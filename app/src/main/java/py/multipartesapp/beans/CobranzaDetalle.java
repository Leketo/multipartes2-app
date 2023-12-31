package py.multipartesapp.beans;

import java.util.List;

/**
 * Created by Adolfo on 15/01/2016.
 */
public class CobranzaDetalle {

    private String invoice;
    private Integer amount;
    private Integer cashed;
    private Integer charge_id;
    private String nroFactura;

    private List<CobranzaFormaPago> items;


    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getCashed() {
        return cashed;
    }

    public void setCashed(Integer cashed) {
        this.cashed = cashed;
    }

    public Integer getCharge_id() {
        return charge_id;
    }

    public void setCharge_id(Integer charge_id) {
        this.charge_id = charge_id;
    }

    public List<CobranzaFormaPago> getItems() {
        return items;
    }

    public void setItems(List<CobranzaFormaPago> items) {
        this.items = items;
    }

    public String getNroFactura() {
        return nroFactura;
    }

    public void setNroFactura(String nroFactura) {
        this.nroFactura = nroFactura;
    }

    @Override
    public String toString() {
        return "CobranzaDetalle{" +
                "invoice='" + invoice + '\'' +
                ", amount=" + amount +
                ", cashed=" + cashed +
                ", charge_id=" + charge_id +
                ", nroFactura='" + nroFactura + '\'' +
                ", items=" + items +
                '}';
    }
}
