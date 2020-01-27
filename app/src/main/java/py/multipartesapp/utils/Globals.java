package py.multipartesapp.utils;

import android.graphics.Bitmap;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.List;

import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Cobranza;
import py.multipartesapp.beans.CobranzaFormaPago;
import py.multipartesapp.beans.Factura;
import py.multipartesapp.beans.Pedido;
import py.multipartesapp.beans.PedidoDetalle;
import py.multipartesapp.beans.Producto;
import py.multipartesapp.beans.ProductoImagen;
import py.multipartesapp.beans.RegistroVisita;
import py.multipartesapp.beans.RutaLocation;


/**
 * Created by Adolfo on 04/06/2015.
 */
public class Globals {

    public static int last_request_code;
    public static Boolean isLogged = false;
    public static PedidoDetalle nuevoPedidoDetalle;
    public static List<CobranzaFormaPago> itemCobroList;
    public static Cliente clienteSeleccionadoPedido;
    public static RutaLocation rutaLocationSeleccionada;

    public static List<Factura> invoicesListFiltered;

    public static String ordenPedidos = "DESC";
    public static String ordenCobros = "DESC";
    public static String ordenRegVisitas = "DESC";

    public static String ordenEntregas = "DESC";

    public static CookieStore cookieStore =  new BasicCookieStore();

    public static String jsonImage;
    public static String idProductoSeleccionado;
    public static Bitmap imagenActualProduccto;
    public static List<ProductoImagen> nombresImagenes;

    public static boolean CatalogoDesdePedido;
    public static Producto productoSeleccionadoCatalogo;
    public static Bitmap imagenSeleccionadaCatalogo;


    public static Cliente clienteSeleccionadoRuta;

    public static String accion_pedido;
    public static String accion_RV;
    public static Pedido pedidoSeleccionado;
    public static Cobranza cobranzaSeleccionada;

    public static String accion_cobranza;

    ////////////////
    public static RegistroVisita RV;
    ////////////////
    public static CookieStore getCookieStore() {
        return cookieStore;
    }

    public static void setCookieStore(CookieStore cookieStore) {
        Globals.cookieStore = cookieStore;
    }

    public static int getLast_request_code() {
        return last_request_code;
    }

    public static void setLast_request_code(int last_request_code) {
        Globals.last_request_code = last_request_code;
    }

    public static void setIsLogged(Boolean isLogged) {
        Globals.isLogged = isLogged;
    }

    public static PedidoDetalle getNuevoPedidoDetalle() {
        return nuevoPedidoDetalle;
    }

    public static void setNuevoPedidoDetalle(PedidoDetalle nuevoPedidoDetalle) {
        Globals.nuevoPedidoDetalle = nuevoPedidoDetalle;
    }

    public static Cliente getClienteSeleccionadoPedido() {
        return clienteSeleccionadoPedido;
    }

    public static void setClienteSeleccionadoPedido(Cliente clienteSeleccionadoPedido) {
        Globals.clienteSeleccionadoPedido = clienteSeleccionadoPedido;
    }

    public static RutaLocation getRutaLocationSeleccionada() {
        return rutaLocationSeleccionada;
    }

    public static void setRutaLocationSeleccionada(RutaLocation rutaLocationSeleccionada) {
        Globals.rutaLocationSeleccionada = rutaLocationSeleccionada;
    }

    public static String getJsonImage() {
        return jsonImage;
    }

    public static void setJsonImage(String jsonImage) {
        Globals.jsonImage = jsonImage;
    }

    public static Bitmap getImagenActualProduccto() {
        return imagenActualProduccto;
    }

    public static void setImagenActualProduccto(Bitmap imagenActualProduccto) {
        Globals.imagenActualProduccto = imagenActualProduccto;
    }

    public static List<ProductoImagen> getNombresImagenes() {
        return nombresImagenes;
    }

    public static void setNombresImagenes(List<ProductoImagen> nombresImagenes) {
        Globals.nombresImagenes = nombresImagenes;
    }

    public static String getIdProductoSeleccionado() {
        return idProductoSeleccionado;
    }

    public static void setIdProductoSeleccionado(String idProductoSeleccionado) {
        Globals.idProductoSeleccionado = idProductoSeleccionado;
    }

    public static boolean isCatalogoDesdePedido() {
        return CatalogoDesdePedido;
    }

    public static void setCatalogoDesdePedido(boolean catalogoDesdePedido) {
        CatalogoDesdePedido = catalogoDesdePedido;
    }

    public static Producto getProductoSeleccionadoCatalogo() {
        return productoSeleccionadoCatalogo;
    }

    public static void setProductoSeleccionadoCatalogo(Producto productoSeleccionadoCatalogo) {
        Globals.productoSeleccionadoCatalogo = productoSeleccionadoCatalogo;
    }

    public static Cliente getClienteSeleccionadoRuta() {
        return clienteSeleccionadoRuta;
    }

    public static void setClienteSeleccionadoRuta(Cliente clienteSeleccionadoRuta) {
        Globals.clienteSeleccionadoRuta = clienteSeleccionadoRuta;
    }

    public static String getOrdenPedidos() {
        return ordenPedidos;
    }

    public static void setOrdenPedidos(String ordenPedidos) {
        Globals.ordenPedidos = ordenPedidos;
    }

    public static String getOrdenCobros() {
        return ordenCobros;
    }

    public static void setOrdenCobros(String ordenCobros) {
        Globals.ordenCobros = ordenCobros;
    }

    public static String getOrdenRegVisitas() {
        return ordenRegVisitas;
    }

    public static void setOrdenRegVisitas(String ordenRegVisitas) {
        Globals.ordenRegVisitas = ordenRegVisitas;
    }

    public static String getAccion_pedido() {
        return accion_pedido;
    }

    public static void setAccion_pedido(String accion_pedido) {
        Globals.accion_pedido = accion_pedido;
    }
    //////////////////////////////////////////////////////////////

    public static void setAccion_RV(String accion_RV) {
        Globals.accion_RV = accion_RV;
    }

    //////////////////////////////////////////////////////////////
    public static Pedido getPedidoSeleccionado() {
        return pedidoSeleccionado;
    }

    public static void setPedidoSeleccionado(Pedido pedidoSeleccionado) {
        Globals.pedidoSeleccionado = pedidoSeleccionado;
    }
    ///////////////////////////////

    public static void setVisitaSeleccionado(RegistroVisita RV) {
        Globals.RV = RV;
    }

    ///////////////////////////////

    public static Bitmap getImagenSeleccionadaCatalogo() {
        return imagenSeleccionadaCatalogo;
    }

    public static void setImagenSeleccionadaCatalogo(Bitmap imagenSeleccionadaCatalogo) {
        Globals.imagenSeleccionadaCatalogo = imagenSeleccionadaCatalogo;

    }

    public static String getOrdenEntregas() {
        return ordenEntregas;
    }

    public static void setOrdenEntregas(String ordenEntregas) {
        Globals.ordenEntregas = ordenEntregas;
    }

    public static List<CobranzaFormaPago> getItemCobroList() {
        return itemCobroList;
    }

    public static void setItemCobroList(List<CobranzaFormaPago> itemCobroList) {
        Globals.itemCobroList = itemCobroList;
    }

    public static List<Factura> getInvoicesListFiltered() {
        return invoicesListFiltered;
    }

    public static void setInvoicesListFiltered(List<Factura> invoicesListFiltered) {
        Globals.invoicesListFiltered = invoicesListFiltered;
    }


    public static Cobranza getCobranzaSeleccionada() {
        return cobranzaSeleccionada;
    }

    public static void setCobranzaSeleccionada(Cobranza cobranzaSeleccionada) {
        Globals.cobranzaSeleccionada = cobranzaSeleccionada;
    }

    public static String getAccion_cobranza() {
        return accion_cobranza;
    }

    public static void setAccion_cobranza(String accion_cobranza) {
        Globals.accion_cobranza = accion_cobranza;
    }
}
