package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Adolfo on 15/01/2016.
 */
public class PedidoDetalle extends Bean {

    Integer id;
    String isactive;
    Integer product_id;
    Integer quantity;
    Integer price;
    Integer total;
    String observation;
    Integer order_id;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        id = getInteger(o, "id");
        isactive = getString(o, "isactive");
        product_id = getInteger(o, "product_id");
        quantity = getInteger(o, "quantity");
        price = getInteger(o, "price");
        total = getInteger(o, "total");
        observation = getString(o, "observation");
        order_id = getInteger(o, "order_id");
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

    public Integer getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
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

    public Integer getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    @Override
    public String toString() {
        return "PedidoDetalle{" +
                "id=" + id +
                ", isactive='" + isactive + '\'' +
                ", product_id=" + product_id +
                ", quantity=" + quantity +
                ", price=" + price +
                ", total=" + total +
                ", observation='" + observation + '\'' +
                ", order_id=" + order_id +
                '}';
    }
}
