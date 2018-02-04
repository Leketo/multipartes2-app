package py.multipartesapp.db;

import android.provider.BaseColumns;

import py.multipartesapp.beans.Cobranza;
import py.multipartesapp.beans.PrecioCategoria;

/**
 * Created by Adolfo on 19/06/2015.
 */
public class AppContract {

    public interface Tables {
        String LOGIN = "LOGIN";
        String CLIENTE = "CLIENTE";
        String CONFIGURACION = "CONFIGURACION";
        String REGISTRO_VISITA = "REGISTROVISITA";
        String SESSION = "SESSION1";
        String LOCATION = "LOCATION";
        String USUARIO = "USUARIO";
        String ENTREGA = "ENTREGA";
        String PRODUCTO = "PRODUCTO";
        String PEDIDO = "PEDIDO";
        String PEDIDO_DETALLE = "PEDIDO_DETALLE";
        String PRECIO_CATEGORIA = "PRECIO_CATEGORIA";
        String PRECIO_VERSION = "PRECIO_VERSION";
        String RUTA_LOCATION = "RUTA_LOCATION";
        String COBRANZA = "COBRANZA";
        String PRODUCTO_IMAGEN = "PRODUCTO_IMAGEN";
        String COBRANZA_DETALLE = "COBRANZA_DETALLE";
        String FACTURA = "FACTURA";
        String PRODUCTO_FAMILIA = "PRODUCTO_FAMILIA";
        String PRODUCTO_SUB_FAMILIA = "PRODUCTO_SUB_FAMILIA";
    }

    interface LoginColumns {
        String userName = "USER_NAME";
        String status = "STATUS";
        String sessionID = "SESSION_ID";
    }

    interface ClienteColumns {
        String id = "ID_CLIENTE";
        String ruc = "RUC";
        String nombre = "NOMBRE";
        String direccion = "DIRECCION";
        String telefono = "TELEFONO";
        String credito_disponible = "CREDITO_DISPONIBLE";
        String credito_extra = "CREDITO_EXTRA";
        String credito_usado = "CREDITO_USADO";
        String cheques_pend = "CHEQUES_PEND";
        String factura_vieja = "FACTURA_VIEJA";
        String plazo_max_cheque="PLAZO_MAX_CHEQUE";
        String categoria_precio = "CATEGORIA";
    }

    interface ConfiguracionColumns {
        String clave = "CLAVE";
        String valor = "VALOR";
    }

    interface RegistroVisitaColumns {
        String tipo_visita = "TIPO_VISITA";
        String ruc = "RUC";
        String cliente = "CLIENTE";
        String latitude = "LATITUDE";
        String longitude = "LONGITUDE";
        String observation = "OBSERVATION";
        String status = "STATUS1";
        String usuario = "USUARIO";
        String fechavisita = "FECHA_VISITA";
        String horavisita = "HORA_VISITA";
        String fecha_prox_visita = "FECHA_PROX_VISITA";
        String estado_envio = "ESTADO_ENVIO";
        String ent_sal="ENT_SAL";
        String nombre_usuario = "NOMBRE_USUARIO";
    }

    interface LocationColumns {
        String id = "ID_LOCATION";
        String id_user = "ID_USER";
        String latitude = "LATITUDE";
        String longitude = "LONGITUDE";
        String date = "DATE1";
        String time = "TIME1";
        String estado_envio = "ESTADO_ENVIO";
    }

    interface SessionColumns {
        String userId = "USER_ID";
        String email =  "EMAIL";
        String root =  "ROOT";
        String admin = "ADMIN";
        String session =  "SESSION";
    }

    interface UsuarioColumns {
        String id = "ID_USUARIO";
        String mail = "MAIL";
        String name = "NAME";
        String lastname = "LASTNAME";
        String password = "PASSWORD";
        String state = "STATE";
        String role = "ROLE";
        String userCellphone = "USERCELLPHONE";
    }

    interface EntregaColumns {
        String id = "_id";
        String user_id = "USER_ID";
        String client_id = "CLIENT_ID";
        String order_id = "ORDER_ID";
        String date_delivered = "DATE_DELIVERED";
        String time_delivered = "TIME_DELIVERED";
        String observation = "OBSERVATION";
        String estado_envio = "ESTADO_ENVIO";
        String nombre_cliente = "NOMBRE_CLIENTE";
        String nombre_vendedor = "NOMBRE_VENDEDOR";

    }

    interface ProductoColumns {
        String m_product_id = "PRODUCT_ID";
        String name = "NAME";
        String price = "PRICE";
        String stock = "STOCK";
        String codinterno = "COD_INTERNO";
        String id_familia = "ID_FAMILIA";
        String id_subfamilia = "ID_SUBFAMILIA";
    }

    interface PedidoColumns {
        String id = "ID";
        String ad_client_id = "AD_CLIENT_ID";
        String ad_org_id = "AD_ORG_ID";
        String isactive = "ISACTIVE";
        String date_order = "DATE_ORDER";
        String order_id = "ORDER_ID";
        String client_id = "CLIENT_ID";
        String user_id = "USER_ID";
        String total = "TOTAL";
        String observation = "OBSERVATION";
        String isinvoiced = "ISINVOICED";
        String estado_envio = "ESTADO_ENVIO";
    }

    interface PedidoDetalleColumns {
        String id = "ID";
        String isactive = "ISACTIVE";
        String product_id = "PRODUCT_ID";
        String quantity = "QUANTITY";
        String price = "PRICE";
        String total = "TOTAL";
        String observation = "OBSERVATION";
        String order_id = "ORDER_ID";
    }

    interface PrecioCategoriaColumns {
        String rowid = "ROWID";
        String m_pricelist_version_id = "PRICELIST_VERSION_ID"; //idPrecioVersion
        String m_pricelist_id = "PRICE_LIST_ID";
        String name = "NAME";
        String active = "ACTIVE";
        String ad_client_id = "CLIENT_ID";
    }

    interface PrecioVersionColumns {
        String id = "ID";
        String m_product_id = "M_PRODUCT_ID";
        String precio_ventas_inicial = "PRECIO_VENTA_INICIAL";
        String precio_costo_inicial = "PRECIO_COSTO_INICIAL";
        String precio_publico = "PRECIO_PUBLICO";
        String precio_mayorista_a = "PRECIO_MAYORISTA_A";
        String precio_mayorista_b = "PRECIO_MAYORISTA_B";
        String precio_lista = "PRECIO_LISTA";
        String precio_vidrieros = "PRECIO_VIDRIEDORS";
        String precio_radiadoritas = "PRECIO_RADIADORITAS";
    }

    interface RutaLocationColumns {
        String id = "ID_RUTA_LOCATION";
        String date = "DATE";
        String user_id = "USER_ID";
        String latitude = "LATITUDE";
        String longitude = "LONGITUDE";
        String client_id = "CLIENT_ID";
        String zone = "ZONE";
        String priority = "PRIORITY";
        String status = "STATUS";
        String observation = "OBSERVATION";
        String entrada = "ENTRADA";
        String salida = "SALIDA";
        String type = "TYPE";
        String fechaHoraEntrada = "FECHA_HORA_ENTRADA";
        String fechaHoraSalida = "FECHA_HORA_SALIDA";
    }

    interface CobranzaColumns {
        String id = "ID_COBRANZA";
        String client_id = "CLIENT_ID";
        String user_id = "USER_ID";
        String amount = "AMOUNT";
        String invoice_number = "INVOCE_NUMBER";
        String observation = "OBSERVATION";
        String status = "STATUS";
        String nombre_cliente = "NOMBRE_CLIENTE";
        String nombre_vendedor = "NOMBRE_VENDEDOR";
        String estado_envio = "ESTADO_ENVIO";
    }

    private interface ProductoImagenColumns {
        String m_product_id = "M_PRODUCT_ID";
        String img = "IMG";
        String size = "SIZE";
        String estado_envio = "ESTADO_ENVIO";
    }

    private interface CobranzaDetalleColumns {
        String invoice = "INVOICE";
        String amount = "AMOUNT";
        String cashed = "CASHED";
        String charge_id = "CHARGE_ID";
    }

    private interface FacturaColumns {
        String id = "ID_FACTURA";
        String isactive = "ISACTIVE";
        String order_id = "ORDER_ID";
        String dateinvoiced = "DATEINVOICED";
        String client_id = "CLIENT_ID";
        String grandtotal = "GRANDTOTAL";
        String ispaid = "ISPAID";
        String pend = "PEND";
        String nroFacturaImprimir = "NROFACTURAIMPRIMIR";
    }

    private interface ProductoFamiliaColumns {
        String m_product_family_id = "M_PRODUCT_ID";
        String value = "VALUE";
        String description = "DESCRIPTION";
    }

    private interface ProductoSubFamiliaColumns {
        String id = "ID";
        String value = "VALUE";
        String description = "DESCRIPTION";
        String id_familia = "ID_FAMILIA";
    }

    // LOGIN
    public static class Login implements LoginColumns, BaseColumns {
    }

    // CLIENTE
    public static class Cliente implements ClienteColumns, BaseColumns {
    }

    // CONFIGURACION
    public static class Configuracion implements ConfiguracionColumns, BaseColumns {
    }

    // CONFIGURACION
    public static class RegistroVisita implements RegistroVisitaColumns, BaseColumns {
    }

    // SESSION
    public static class Session implements SessionColumns, BaseColumns {
    }

    // LOCATION
    public static class Location implements LocationColumns, BaseColumns {
    }

    // USUARIO
    public static class Usuario implements UsuarioColumns, BaseColumns {
    }

    // ENTREGA
    public static class Entrega implements EntregaColumns, BaseColumns {
    }

    // PRODUCTO
    public static class Producto implements ProductoColumns, BaseColumns {
    }

    // PEDIDO
    public static class Pedido implements PedidoColumns, BaseColumns {
    }

    // PEDIDO_DETALLE
    public static class PedidoDetalle implements PedidoDetalleColumns, BaseColumns {
    }

    // PRECIO CATEGORIA
    public static class PrecioCategoria implements PrecioCategoriaColumns, BaseColumns {
    }

    // PRECIO_VERSION
    public static class PrecioVersion implements PrecioVersionColumns, BaseColumns {
    }

    // RUTA_LOCATION
    public static class RutaLocation implements RutaLocationColumns, BaseColumns {
    }

    // COBRANZA
    public static class Cobranza implements CobranzaColumns, BaseColumns {
    }

    // PRODUCTO_IMAGEN
    public static class ProductoImagen implements ProductoImagenColumns, BaseColumns {
    }

    // COBRANZA_DETALLE
    public static class CobranzaDetalle implements CobranzaDetalleColumns, BaseColumns {
    }

    //FACTURA
    public static class Factura implements FacturaColumns, BaseColumns {}

    //PRODUCTO_FAMILIA
    public static class ProductoFamilia implements ProductoFamiliaColumns, BaseColumns {}

    //PRODUCTO_SUB_FAMILIA
    public static class ProductoSubFamilia implements ProductoSubFamiliaColumns, BaseColumns {}

}
