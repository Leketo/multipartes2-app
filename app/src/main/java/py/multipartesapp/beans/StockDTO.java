package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class StockDTO extends Bean {

    private Producto producto;
    private LocatorDTO locator;
    private int stock_disponible;

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public LocatorDTO getLocator() {
        return locator;
    }

    public void setLocator(LocatorDTO locator) {
        this.locator = locator;
    }

    public int getStock_disponible() {
        return stock_disponible;
    }

    public void setStock_disponible(int stock_disponible) {
        this.stock_disponible = stock_disponible;
    }
    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        stock_disponible=getInteger(o,"stock_disponible");

        LocatorDTO locator = new LocatorDTO();

        JSONObject locatorObject= o.getJSONObject("locator");

        locator.setM_locator_value(getString(locatorObject,"m_locator_value"));
        locator.setM_locator_id(getString(locatorObject,"m_locator_id"));
        setLocator(locator);


        JSONObject productoObject =o.getJSONObject("producto");
        Producto producto = new Producto();
        producto.setName(getString(productoObject,"name"));
        producto.setM_product_id(getInteger(productoObject,"m_product_id"));

        setProducto(producto);

    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }


}
