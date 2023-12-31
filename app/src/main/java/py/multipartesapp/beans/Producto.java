package py.multipartesapp.beans;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolfo on 15/01/2016.
 */
public class Producto extends Bean {

    Integer m_product_id;
    String name;
    Integer price;
    Integer stock;
    String codinterno;

    Integer idFamilia;
    Integer idSubFamilia;
    List<StockDTO> listaStock;

    Integer stock_casa_central;
    Integer stock_cde;
    Integer stock_vidrios;


    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        JSONArray jsonArray = o.getJSONArray("listaStock");
        ArrayList<StockDTO> array = new ArrayList<StockDTO>();
        Log.d("Tamaño List --------> ", jsonArray.length() + "");
        for( int i = 0; i < jsonArray.length(); i++){ //jsonArray.length()
            StockDTO stockObject = (StockDTO) new StockDTO().fromJSON(jsonArray.getJSONObject(i));
            array.add(stockObject);
        }
        setListaStock(array);

        m_product_id = getInteger(o, "m_product_id");
        name = getString(o, "name");
        price = getInteger(o, "price");
        stock = getInteger(o, "stock");
        codinterno = getString(o, "codinterno");
        idFamilia = getInteger(o, "m_product_family_id");
        idSubFamilia = getInteger(o, "m_product_subfamily_id");

        stock_casa_central = getInteger(o, "stockCasaCentral");
        stock_cde = getInteger(o, "stockCde");
        stock_vidrios = getInteger(o, "stockVidrios");


        //////////////////////

//
//        for( int i = 0; i < jsonArray.length(); i++){
//            StockDTO object = (StockDTO) new StockDTO().fromJSON(jsonArray.getJSONObject(i));
//            array.add(object);
//        }
//        setListaStock(array);
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }

    public Integer getM_product_id() {
        return m_product_id;
    }

    public void setM_product_id(Integer m_product_id) {
        this.m_product_id = m_product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCodinterno() {
        return codinterno;
    }

    public void setCodinterno(String codinterno) {
        this.codinterno = codinterno;
    }

    public Integer getIdFamilia() {
        return idFamilia;
    }

    public void setIdFamilia(Integer idFamilia) {
        this.idFamilia = idFamilia;
    }

    public Integer getIdSubFamilia() {
        return idSubFamilia;
    }

    public void setIdSubFamilia(Integer idSubFamilia) {
        this.idSubFamilia = idSubFamilia;
    }

    @Override
    public String toString() {
        return name + " - " + codinterno;
    }

    public List<StockDTO> getListaStock() {
        return listaStock;
    }

    public void setListaStock(List<StockDTO> listaStock) {
        this.listaStock = listaStock;
    }

    // Setter para stock_casa_central
    public void setStockCasaCentral(Integer stock_casa_central) {
        this.stock_casa_central = stock_casa_central;
    }

    // Getter para stock_casa_central
    public Integer getStockCasaCentral() {
        return stock_casa_central;
    }

    // Setter para stock_cde
    public void setStockCde(Integer stock_cde) {
        this.stock_cde = stock_cde;
    }

    // Getter para stock_cde
    public Integer getStockCde() {
        return stock_cde;
    }

    // Setter para stock_vidrios
    public void setStockVidrios(Integer stock_vidrios) {
        this.stock_vidrios = stock_vidrios;
    }

    // Getter para stock_vidrios
    public Integer getStockVidrios() {
        return stock_vidrios;
    }

}
