package py.multipartesapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Cobranza;
import py.multipartesapp.beans.CobranzaDetalle;
import py.multipartesapp.beans.Configuracion;
import py.multipartesapp.beans.Entrega;
import py.multipartesapp.beans.Factura;
import py.multipartesapp.beans.LocationTable;
import py.multipartesapp.beans.Login;
import py.multipartesapp.beans.Pedido;
import py.multipartesapp.beans.PedidoDetalle;
import py.multipartesapp.beans.PrecioCategoria;
import py.multipartesapp.beans.PrecioVersion;
import py.multipartesapp.beans.Producto;
import py.multipartesapp.beans.ProductoFamilia;
import py.multipartesapp.beans.ProductoImagen;
import py.multipartesapp.beans.ProductoSubFamilia;
import py.multipartesapp.beans.RegistroVisita;
import py.multipartesapp.beans.RutaLocation;
import py.multipartesapp.beans.Session;
import py.multipartesapp.beans.StockDTO;
import py.multipartesapp.beans.Usuario;
import py.multipartesapp.beans.*;

/**
 * Created by Adolfo on 19/06/2015.
 */
public class AppDatabase {

    public static final String TAG = AppDatabase.class.getSimpleName();
    //Database
    private static final String DATABASE_NAME = "myDatabase";
    //DB version
    private static final int DATABASE_VERSION = 11;

    private final DictionaryOpenHelper mDatabaseOpenHelper;

    private int mOpenCounter;

    private static AppDatabase instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public AppDatabase(Context context) {
        mDatabaseOpenHelper = new DictionaryOpenHelper(context);
    }

    public AppDatabase(){
        mDatabaseOpenHelper = null;
    }

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new AppDatabase();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized AppDatabase getInstance() {
        if (instance == null) {
            throw new IllegalStateException(AppDatabase.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter--;
        if(mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }

    //selects
    public Login selectLoginActive(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.LOGIN + " WHERE STATUS = ? ", new String[]{ "ACTIVE" } );
        return mappingLogin(c);
    }

    public Session selectUsuarioLogeado(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.SESSION, null );
        return mappingSession(c);
    }

    public Configuracion selectConfiguracionByClave(String clave){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.CONFIGURACION + " WHERE CLAVE = ? ", new String[]{ clave } );
        return mappingConfiguracion(c);
    }

    public List<Cliente> selectAllCliente(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.CLIENTE, null);
        return mappingListCliente(c);
    }

    public List<ProductoFamilia> selectAllProductoFamilia(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PRODUCTO_FAMILIA, null);
        return mappingListProductoFamilia(c);
    }

    public List<ProductoSubFamilia> selectProductoSubFamiliaByIdFamilia(Integer idProductoFamilia){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PRODUCTO_SUB_FAMILIA + " WHERE "
                + AppContract.ProductoSubFamilia.id_familia + "=" +idProductoFamilia, null);
        return mappingListProductoSubFamilia(c);
    }

    public List<ProductoImagen> selectAllProductoImagen(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PRODUCTO_IMAGEN, null);
        return mappingListProductoImagen(c);
    }

    public List<RutaLocation> selectAllRutaLocation(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.RUTA_LOCATION +
                " WHERE status != 'V' ORDER BY "+AppContract.RutaLocation.priority + " DESC ", null);
        return mappingListRutaLocation(c);
    }

    public List<RutaLocation> selectRutaLocationByFilter(String tipo, String entrada, String salida){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String query = "SELECT * FROM "+AppContract.Tables.RUTA_LOCATION + " WHERE 1=1 ";
        if (tipo != null){
            query = query + " AND "+AppContract.RutaLocation.type +"='"+ tipo+"'";
        }
        if (entrada != null){
            if (entrada.equals("null")){
                query = query + " AND "+AppContract.RutaLocation.entrada +" IS NULL";
            }else{
                query = query + " AND "+AppContract.RutaLocation.entrada +"='"+ entrada+"'";
            }
        }
        if (salida != null){
            query = query + " AND "+AppContract.RutaLocation.salida +"='"+ salida+"'";
        }
        query = query + " ORDER BY "+AppContract.RutaLocation.priority + " DESC ";

        Cursor c = db.rawQuery(query, null);
        return mappingListRutaLocation(c);
    }

    public List<Cobranza> selectAllCobranza(String order){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.COBRANZA + " ORDER BY "+AppContract.Cobranza.id + " " +order, null);
        return mappingListCobranza(c);
    }

    public List<Cobranza> selectCobranzaByIdVendedor(Integer id_vendedor, String order){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.COBRANZA
                + " WHERE "+AppContract.Cobranza.user_id + "=" + id_vendedor
                + " ORDER BY "+AppContract.Cobranza.id + " " +order, null);
        return mappingListCobranza(c);
    }

    public Cliente selectClienteById (Integer id_cliente){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.CLIENTE + " WHERE ID_CLIENTE = "+id_cliente;
        String[] whereArgs = {  };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingCliente(c);
    }

    public List<Cliente> selectClienteByNombreCedula(String nombre){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = "SELECT * FROM "+AppContract.Tables.CLIENTE + " WHERE NOMBRE LIKE '%"+nombre+"%' OR RUC LIKE '%"+nombre+"%'";
        Cursor c = db.rawQuery(sql, null);
        return mappingListCliente(c);
    }

    public List<Usuario> selectUsuarioByNombre(String nombre){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = "SELECT * FROM "+AppContract.Tables.USUARIO + " WHERE NAME LIKE '%"+nombre+"%' OR LASTNAME LIKE '%"+nombre+"%'";
        Cursor c = db.rawQuery(sql, null);
        return mappingListUsuario(c);
    }

    public List<ProductoImagen> selectProductoImagenByNombre(Integer idFamilia, Integer idSubfamilia, CharSequence nombre){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        //String sql = "SELECT * FROM "+AppContract.Tables.PRODUCTO_IMAGEN + " WHERE COD_PRODUCTO LIKE '%"+nomre + "%'" ;
        Log.d(TAG, "Ejecutar query con nombre: "+ nombre);
        String sql = "SELECT * FROM "+AppContract.Tables.PRODUCTO_IMAGEN + " I "
                + " WHERE I."+ AppContract.ProductoImagen.m_product_id + " IN ("
                    + " SELECT "+AppContract.Producto.m_product_id+ " FROM " + AppContract.Tables.PRODUCTO + " WHERE 1=1 ";
                        if (idFamilia != null) {
                            sql = sql + " AND " + AppContract.Producto.id_familia + "=" + idFamilia;
                            sql = sql + " AND " + AppContract.Producto.id_subfamilia + "=" + idSubfamilia;
                        }
                if (nombre != null && nombre.length() > 0){
                    sql = sql + " AND ("+AppContract.Producto.name +" LIKE '%"+nombre +"%'";
                    sql = sql + " OR "+AppContract.Producto.codinterno +" LIKE '%"+nombre +"%' )";
                }
                sql = sql + " )";

        //Log.d(TAG, "query: "+sql);
        Cursor c = db.rawQuery(sql, null);
        //Log.d(TAG, "resultado size: "+mappingListProductoImagen(c).size());
        return mappingListProductoImagen(c);
    }

    public Producto selectProductByCodigo (String codigo){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.PRODUCTO + " WHERE "+ AppContract.Producto.codinterno + " =?";
        String[] whereArgs = { codigo };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingProducto(c);
    }

    public List<Producto> selectProductByNombre (String nombre){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.PRODUCTO + " WHERE "+ AppContract.Producto.name + " like '%"+nombre+"%'";
        String[] whereArgs = { nombre };
        Cursor c = db.rawQuery(sql, null);
        return mappingListProducto(c);
    }

    public List<Producto> selectProductByNombreOrCodigo (String nombreOcodigo){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.PRODUCTO + " WHERE "+ AppContract.Producto.name + " like '%"+nombreOcodigo+"%' " +
                " or "+ AppContract.Producto.codinterno + " like '%"+nombreOcodigo+"%' ";
        String[] whereArgs = { nombreOcodigo };
        Cursor c = db.rawQuery(sql, null);
        return mappingListProducto(c);
    }

    public Producto selectProductById (Integer id_producto){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.PRODUCTO + " WHERE "+ AppContract.Producto.m_product_id + " = "+id_producto;
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingProducto(c);
    }


    public List<StockDTO> selectStockPorProducto(String id_producto){

        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.STOCK_PRODUCTO + " WHERE "+ AppContract.StockProducto.m_product_id + " = "+id_producto+"";
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingStockDTO(c);

    }

    public Cobranza selectCobranzaById (Integer id_cobranza){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.COBRANZA + " WHERE "+ AppContract.Cobranza.id + " = "+id_cobranza;
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingCobranza(c);
    }

    public List<CobranzaFormaPago> selectCobranzaFormaPagoByIdCobro (Integer id_cobranza){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.COBRANZA_FORMA_PAGO + " WHERE "+ AppContract.CobranzaFormaPago.idCobranza + " = "+id_cobranza;
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingListListCobranzaFormaPago(c);
    }

    public PrecioVersion selectPrecioVersionByIdAndProducto (Integer idPrecioVersion, Integer idProducto){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.PRECIO_VERSION + " WHERE "+ AppContract.PrecioVersion.id + " = "+idPrecioVersion
                + " AND " + AppContract.PrecioVersion.m_product_id + "=" + idProducto;
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingPrecioVersion(c);
    }

    public PrecioCategoria selectPrecioCategoriaById (Integer idPrecioCategoria){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.PRECIO_CATEGORIA + " WHERE "+ AppContract.PrecioCategoria.m_pricelist_id + " = "+idPrecioCategoria;
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingPrecioCategoria(c);
    }

    public PrecioVersion selectPrecioVersionByProducto (Integer idProducto){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.PRECIO_VERSION + " WHERE "+ AppContract.PrecioVersion.m_product_id + "="+idProducto;
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingPrecioVersion(c);
    }

    public List<Pedido> selectAllPedido(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PEDIDO + " ORDER BY "+AppContract.Pedido.id + " DESC" , null);
        return mappingListPedido(c);
    }
    public List<Pedido> selectPedidoByUser(Integer userId, String order){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PEDIDO + " WHERE " + AppContract.Pedido.user_id + "= " + userId
                +" ORDER BY "+AppContract.Pedido.date_order + " "+ order + ", " + AppContract.Pedido.id + " " + order , null);
        return mappingListPedido(c);
    }

    public List<Pedido> selectPedidoByClienteId(Integer clienteId){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PEDIDO + " WHERE " + AppContract.Pedido.client_id + "= " + clienteId
                +" ORDER BY "+AppContract.Pedido.date_order + " DESC" , null);
        return mappingListPedido(c);
    }

    public List<Pedido> selectPedidoByClienteIdEstadoCobro(Integer clienteId, String estadoCobrado){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PEDIDO + " WHERE " + AppContract.Pedido.client_id + "=" + clienteId
                +" AND ISINVOICED='"+estadoCobrado+"' ORDER BY "+AppContract.Pedido.date_order + " DESC" , null);
        return mappingListPedido(c);
    }

    public List<Factura> selectFacturaByClientId (Integer clienteId){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.FACTURA + " WHERE " + AppContract.Factura.client_id + "=" + clienteId
                +" ORDER BY "+AppContract.Factura.dateinvoiced +" ASC", null);
        return mappingListFactura(c);
    }

    public Factura selectFacturaById (Integer idFactura){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.FACTURA + " WHERE " + AppContract.Factura.id + "=" + idFactura , null);
        return mappingFactura(c);
    }

    public List<Pedido> selectPedidoByClienteName(String cliente){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Log.d(TAG, "inicio query pedido y cliente complejo");
        //Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PEDIDO + " p, CLIENTE c WHERE p.CLIENT_ID = c.ID_CLIENTE AND" +
        //        " c.NOMBRE like '%"+cliente+"%'", null);
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PEDIDO + " p INNER JOIN CLIENTE c ON p.CLIENT_ID = c.ID_CLIENTE WHERE" +
                " c.NOMBRE like '%"+cliente+"%'", null);

        Log.d(TAG, "fin query pedido y cliente. iniciando mapeo de "+c.getCount());
        return mappingListPedido(c);
    }

    public Pedido selectLastPedido(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PEDIDO + " ORDER BY ID DESC LIMIT 1  ", null);
        return mappingPedido(c);
    }

    public Entrega selectLastEntrega(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.ENTREGA + " ORDER BY " + AppContract.Entrega.id +" DESC LIMIT 1  ", null);
        return mappingEntrega(c);
    }

    public List<Producto> selectAllProducto(){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.PRODUCTO, null);
        return mappingListProducto(c);
    }

    public List<LocationTable> selectLocationByEstado (String estado){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.LOCATION+ " WHERE ESTADO_ENVIO=" +"'"+estado+"'", null );
        return mappingListLocation(c);
    }

    public List<RegistroVisita> selectRegistroVisitaByEstado (String estado){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.REGISTRO_VISITA+ " WHERE ESTADO_ENVIO=" +"'"+estado+"'", null );
        return mappingListRegistroVisita(c);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    public List<RutaLocation> selectRutaLocationByEstado (String estado){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.RUTA_LOCATION+ " WHERE ESTADO_ENVIO=" +"'"+estado+"'", null );
        return mappingListRutaLocation(c);
    }
    /////////////////////////////////////////////////////////////////////////////////////////

    public List<Pedido> selectPedidoByEstado (String estado){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.PEDIDO+ " WHERE ESTADO_ENVIO=" +"'"+estado+"'", null );
        return mappingListPedido(c);
    }

    public Usuario selectUsuarioById (Integer idUsuario){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String []whereArgs={String.valueOf(idUsuario)};
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.USUARIO+ " WHERE " +
                AppContract.Usuario.id +"=?", whereArgs);
        return mappingUsuario(c);
    }

    public List<PedidoDetalle> selectPedidoDetalleByPedido (Integer idPedido){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.PEDIDO_DETALLE+ " WHERE ORDER_ID=" +idPedido, null );
        return mappingListPedidoDetalle(c);
    }

    public List<PedidoDetalle> selectAllPedidoDetalle (){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.PEDIDO_DETALLE, null );
        return mappingListPedidoDetalle(c);
    }

    public List<CobranzaDetalle> selectCobranzaDetalleByCobro (Integer idCobranza){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.COBRANZA_DETALLE+ " WHERE CHARGE_ID=" +idCobranza, null );
        return mappingListCobranzaDetalle(c);
    }

    public List<Entrega> selectEntregaByEstado (String estado){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.ENTREGA+ " WHERE ESTADO_ENVIO=" +"'"+estado+"'", null );
        return mappingListEntrega(c);
    }

    public List<Entrega> selectEntregaByIdUser(Integer id_user, String order){

        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+AppContract.Tables.ENTREGA
                + " WHERE "+AppContract.Entrega.user_id + "='" + id_user + "'"
                + " ORDER BY "+AppContract.Entrega.id + " " +order, null);
        return mappingListEntrega(c);
    }

    public List<Cobranza> selectCobranzaByEstado (String estado){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.COBRANZA+ " WHERE ESTADO_ENVIO=" +"'"+estado+"'", null );
        return mappingListCobranza(c);
    }

    public Usuario buscarUsuario (String username, String password){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String sql = " SELECT * FROM " +AppContract.Tables.USUARIO + " WHERE " + AppContract.UsuarioColumns.mail + "='" +username
                + "' AND " +AppContract.UsuarioColumns.password + "='"+password+"';";
        String[] whereArgs = { };
        Cursor c = db.rawQuery(sql, whereArgs);
        return mappingUsuario(c);
    }

    public List<RegistroVisita> selectAllRegistroVisita (String order){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.REGISTRO_VISITA + " order by FECHA_VISITA "+ order +", HORA_VISITA "+ order,
                null );
        return mappingListRegistroVisita(c);
    }

    public List<RegistroVisita> selectRegistroVisitaByNomUser (String usuarioId, String order){
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String [] whereArgs={usuarioId};
        Cursor c = db.rawQuery(" SELECT * FROM " +AppContract.Tables.REGISTRO_VISITA +
                        " WHERE "+AppContract.RegistroVisita.usuario+"=?"+
                " order by FECHA_VISITA "+ order +", HORA_VISITA "+ order,
                whereArgs );
        return mappingListRegistroVisita(c);
    }

    private Login mappingLogin(Cursor cursor){
        Login login = new Login();
        if(cursor.moveToFirst()) {
            login.setUserName(cursor.getString(cursor.getColumnIndex(AppContract.Login.userName)));
            login.setStatus(cursor.getString(cursor.getColumnIndex(AppContract.Login.status)));
            login.setSessionID(cursor.getString(cursor.getColumnIndex(AppContract.Login.sessionID)));

        }
        cursor.close();
        return login;
    }

    private Session mappingSession(Cursor cursor){
        Session session = new Session();
        if(cursor.moveToFirst()) {

            session.setUserId(cursor.getInt(cursor.getColumnIndex(AppContract.Session.userId)));
            session.setEmail(cursor.getString(cursor.getColumnIndex(AppContract.Session.email)));
            //usuario.setAdmin(cursor.get);
            //omitimos por ahora el root y el admin del usuario
        }
        cursor.close();
        return session;
    }

    private Usuario mappingUsuario(Cursor cursor){
        Usuario usuario = new Usuario();
        if(cursor.moveToFirst()) {
            usuario.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Usuario.id)));
            usuario.setName(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.name)));
            usuario.setLastname(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.lastname)));
            usuario.setPassword(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.password)));
            usuario.setState(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.state)));
            usuario.setRole(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.role)));
            usuario.setUserCellphone(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.userCellphone)));
        }
        cursor.close();
        return usuario;
    }

    private Configuracion mappingConfiguracion(Cursor cursor){
        Configuracion configuracion = new Configuracion();
        if(cursor.moveToFirst()) {
            configuracion.setClave(cursor.getString(cursor.getColumnIndex(AppContract.Configuracion.clave)));
            configuracion.setValor(cursor.getString(cursor.getColumnIndex(AppContract.Configuracion.valor)));
        }
        cursor.close();
        return configuracion;
    }

    private Cliente mappingCliente(Cursor cursor){
        Cliente cliente = new Cliente();
        if(cursor.moveToFirst()) {
            cliente.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Cliente.id)));
            cliente.setNombre(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.nombre)));
            cliente.setRuc(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.ruc)));
            cliente.setTelefono(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.telefono)));
            cliente.setDireccion(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.direccion)));
            cliente.setCredito_disponible(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.credito_disponible)));
            cliente.setCheques_pend(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.cheques_pend)));
            cliente.setCredito_extra(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.credito_extra)));
            cliente.setCredito_usado(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.credito_usado)));
            cliente.setFactura_vieja(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.factura_vieja)));
            cliente.setCategoria_precio(cursor.getInt(cursor.getColumnIndex(AppContract.Cliente.categoria_precio)));
            cliente.setPlazomax(cursor.getInt(cursor.getColumnIndex(AppContract.Cliente.plazo_max_cheque)));
        }
        cursor.close();
        return cliente;
    }

    private Producto mappingProducto(Cursor cursor){
        Producto producto = new Producto();
        if(cursor.moveToFirst()) {
            producto.setM_product_id(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.m_product_id)));
            producto.setName(cursor.getString(cursor.getColumnIndex(AppContract.Producto.name)));
            producto.setPrice(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.price)));
            producto.setStock(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.stock)));
            producto.setCodinterno(cursor.getString(cursor.getColumnIndex(AppContract.Producto.codinterno)));
            producto.setIdFamilia(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.id_familia)));
            producto.setIdSubFamilia(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.id_subfamilia)));
        }
        cursor.close();
        return producto;
    }

    public List<StockDTO> mappingStockDTO(Cursor cursor){
        List<StockDTO> listStock= new ArrayList<>();
        StockDTO stockDTO;
        if(cursor.moveToFirst()) {
            do {
//                if (cursor.moveToFirst()) {
                    stockDTO = new StockDTO();

                    Producto producto = new Producto();
                    producto.setM_product_id(cursor.getInt(cursor.getColumnIndex(AppContract.StockProducto.m_product_id)));
                    stockDTO.setProducto(producto);

                    stockDTO.setStock_disponible(cursor.getInt(cursor.getColumnIndex(AppContract.StockProducto.stock_disponible)));

                    LocatorDTO locatorDTO = new LocatorDTO();
                    locatorDTO.setM_locator_id(cursor.getString(cursor.getColumnIndex(AppContract.StockProducto.m_locator_id)));
                    locatorDTO.setM_locator_value(cursor.getString(cursor.getColumnIndex(AppContract.StockProducto.desc_m_locator)));
                    stockDTO.setLocator(locatorDTO);

                    listStock.add(stockDTO);
//                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return listStock;
    }

    private Cobranza mappingCobranza(Cursor cursor){
        Cobranza cobranza = new Cobranza();
        if(cursor.moveToFirst()) {
            cobranza.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.id)));
            cobranza.setClient_id(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.client_id)));
            cobranza.setUser_id(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.user_id)));
            cobranza.setAmount(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.amount)));
            cobranza.setInvoice_number(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.invoice_number)));
            cobranza.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.observation)));
            cobranza.setStatus(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.status)));
            cobranza.setNombre_cliente(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.nombre_cliente)));
            cobranza.setNombre_vendedor(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.nombre_vendedor)));
            cobranza.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.estado_envio)));
        }
        cursor.close();
        return cobranza;
    }

    private List<CobranzaFormaPago> mappingListListCobranzaFormaPago(Cursor cursor){
        List<CobranzaFormaPago> list = new ArrayList<CobranzaFormaPago>();
        CobranzaFormaPago formaPago;
        if(cursor.moveToFirst()) {
            do {
                formaPago = new CobranzaFormaPago();

                formaPago.setPayment_type(cursor.getString(cursor.getColumnIndex(AppContract.CobranzaFormaPago.payment_type)));
                formaPago.setAmount(cursor.getInt(cursor.getColumnIndex(AppContract.CobranzaFormaPago.amount)));
                formaPago.setBank(cursor.getString(cursor.getColumnIndex(AppContract.CobranzaFormaPago.bank)));
                formaPago.setCheck_number(cursor.getString(cursor.getColumnIndex(AppContract.CobranzaFormaPago.check_number)));
                formaPago.setExpired_date(cursor.getString(cursor.getColumnIndex(AppContract.CobranzaFormaPago.expired_date)));
                formaPago.setCheck_name(cursor.getString(cursor.getColumnIndex(AppContract.CobranzaFormaPago.check_name)));
                formaPago.setIscrossed(cursor.getString(cursor.getColumnIndex(AppContract.CobranzaFormaPago.iscrossed)));

                list.add(formaPago);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private Factura mappingFactura(Cursor cursor){
        Factura factura = new Factura();
        if(cursor.moveToFirst()) {
            factura.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.id)));
            factura.setIsactive(cursor.getString(cursor.getColumnIndex(AppContract.Factura.isactive)));
            factura.setOrder_id(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.order_id)));
            factura.setDateinvoiced(cursor.getString(cursor.getColumnIndex(AppContract.Factura.dateinvoiced)));
            factura.setClient_id(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.client_id)));
            factura.setGrandtotal(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.grandtotal)));
            factura.setIspaid(cursor.getString(cursor.getColumnIndex(AppContract.Factura.ispaid)));
            factura.setPend(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.pend)));
            factura.setNroFacturaImprimir(cursor.getString(cursor.getColumnIndex(AppContract.Factura.nroFacturaImprimir)));
        }
        cursor.close();
        return factura;
    }

    private PrecioVersion mappingPrecioVersion(Cursor cursor){
        PrecioVersion precioVersion = new PrecioVersion();
        if(cursor.moveToFirst()) {
            precioVersion.setId(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.id)));
            precioVersion.setM_product_id(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.m_product_id)));
            precioVersion.setPrecio_ventas_inicial(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_ventas_inicial)));
            precioVersion.setPrecio_costo_inicial(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_costo_inicial)));
            precioVersion.setPrecio_publico(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_publico)));
            precioVersion.setPrecio_mayorista_a(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_mayorista_a)));
            precioVersion.setPrecio_mayorista_b(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_mayorista_b)));
            precioVersion.setPrecio_lista(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_lista)));
            precioVersion.setPrecio_vidrieros(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_vidrieros)));
            precioVersion.setPrecio_radiadoritas(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioVersion.precio_radiadoritas)));

        }
        cursor.close();
        return precioVersion;
    }


    private PrecioCategoria mappingPrecioCategoria(Cursor cursor){
        PrecioCategoria precioCategoria = new PrecioCategoria();
        if(cursor.moveToFirst()) {

            precioCategoria.setRowid(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioCategoria.rowid)));
            precioCategoria.setM_pricelist_version_id(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioCategoria.m_pricelist_version_id)));
            precioCategoria.setM_pricelist_id(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioCategoria.m_pricelist_id)));
            precioCategoria.setName(cursor.getString(cursor.getColumnIndex(AppContract.PrecioCategoria.name)));
            precioCategoria.setActive(cursor.getString(cursor.getColumnIndex(AppContract.PrecioCategoria.active)));
            precioCategoria.setAd_client_id(cursor.getInt(cursor.getColumnIndex(AppContract.PrecioCategoria.ad_client_id)));
        }
        cursor.close();
        return precioCategoria;
    }

    private Entrega mappingEntrega(Cursor cursor){
        Entrega entrega = new Entrega();
        if(cursor.moveToFirst()) {
            entrega.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Entrega.id)));
            entrega.setOrder_id(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.order_id)));
            entrega.setClient_id(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.client_id)));
            entrega.setDate_delivered(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.date_delivered)));
            entrega.setTime_delivered(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.time_delivered)));
            entrega.setUser_id(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.user_id)));
            entrega.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.observation)));
            entrega.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.estado_envio)));
            entrega.setNombre_cliente(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.nombre_cliente)));
        }
        cursor.close();
        return entrega;
    }

    private List<Cliente> mappingListCliente(Cursor cursor){
        List<Cliente> list = new ArrayList<Cliente>();
        Cliente cliente;
        if(cursor.moveToFirst()) {
            do {
                cliente = new Cliente();
                cliente.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Cliente.id)));
                cliente.setNombre(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.nombre)));
                cliente.setRuc(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.ruc)));
                cliente.setTelefono(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.telefono)));
                cliente.setDireccion(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.direccion)));

                cliente.setCredito_disponible(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.credito_disponible)));
                cliente.setCheques_pend(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.cheques_pend)));
                cliente.setCredito_extra(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.credito_extra)));
                cliente.setCredito_usado(cursor.getDouble(cursor.getColumnIndex(AppContract.Cliente.credito_usado)));
                cliente.setFactura_vieja(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.factura_vieja)));
                cliente.setPlazomax(cursor.getInt(cursor.getColumnIndex(AppContract.Cliente.plazo_max_cheque)));
                cliente.setCategoria_precio(cursor.getInt(cursor.getColumnIndex(AppContract.Cliente.categoria_precio)));

                list.add(cliente);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<Usuario> mappingListUsuario(Cursor cursor){
        List<Usuario> list = new ArrayList<Usuario>();
        Usuario usuario = new Usuario();
        if(cursor.moveToFirst()) {
            do {
                usuario = new Usuario();
                usuario.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Usuario.id)));
                usuario.setName(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.name)));
                usuario.setLastname(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.lastname)));
                usuario.setPassword(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.password)));
                usuario.setState(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.state)));
                usuario.setRole(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.role)));
                usuario.setUserCellphone(cursor.getString(cursor.getColumnIndex(AppContract.Usuario.userCellphone)));

                list.add(usuario);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<ProductoFamilia> mappingListProductoFamilia(Cursor cursor){
        List<ProductoFamilia> list = new ArrayList<ProductoFamilia>();
        ProductoFamilia p = new ProductoFamilia();
        if(cursor.moveToFirst()) {
            do {
                p = new ProductoFamilia();
                p.setM_product_family_id(cursor.getInt(cursor.getColumnIndex(AppContract.ProductoFamilia.m_product_family_id)));
                p.setValue(cursor.getString(cursor.getColumnIndex(AppContract.ProductoFamilia.value)));
                p.setDescription(cursor.getString(cursor.getColumnIndex(AppContract.ProductoFamilia.description)));
                list.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<ProductoSubFamilia> mappingListProductoSubFamilia(Cursor cursor){
        List<ProductoSubFamilia> list = new ArrayList<ProductoSubFamilia>();
        ProductoSubFamilia p = new ProductoSubFamilia();
        if(cursor.moveToFirst()) {
            do {
                p = new ProductoSubFamilia();
                p.setId(cursor.getInt(cursor.getColumnIndex(AppContract.ProductoSubFamilia.id)));
                p.setValue(cursor.getString(cursor.getColumnIndex(AppContract.ProductoSubFamilia.value)));
                p.setDescription(cursor.getString(cursor.getColumnIndex(AppContract.ProductoSubFamilia.description)));
                p.setId_familia(cursor.getInt(cursor.getColumnIndex(AppContract.ProductoSubFamilia.id_familia)));
                list.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    private List<ProductoImagen> mappingListProductoImagen(Cursor cursor){
        List<ProductoImagen> list = new ArrayList<ProductoImagen>();
        ProductoImagen productoImagen;
        if(cursor.moveToFirst()) {
            do {
                productoImagen = new ProductoImagen();
                productoImagen.setM_product_id(cursor.getInt(cursor.getColumnIndex(AppContract.ProductoImagen.m_product_id)));
                productoImagen.setImg(cursor.getString(cursor.getColumnIndex(AppContract.ProductoImagen.img)));
                productoImagen.setSize(cursor.getString(cursor.getColumnIndex(AppContract.ProductoImagen.size)));

                //si trae tambien nombre del producto
                if (cursor.getColumnIndex(AppContract.Producto.name) != -1){
                    productoImagen.setNombreProducto(cursor.getString(cursor.getColumnIndex(AppContract.Producto.name)));
                }
                list.add(productoImagen);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<RutaLocation> mappingListRutaLocation(Cursor cursor){
        List<RutaLocation> list = new ArrayList<RutaLocation>();
        RutaLocation rutaLocation;
        if(cursor.moveToFirst()) {
            do {
                rutaLocation = new RutaLocation();
                rutaLocation.setId(cursor.getInt(cursor.getColumnIndex(AppContract.RutaLocation.id)));
                rutaLocation.setDate(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.date)));
                rutaLocation.setUser_id(cursor.getInt(cursor.getColumnIndex(AppContract.RutaLocation.user_id)));
                rutaLocation.setLatitude(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.latitude)));
                rutaLocation.setLongitude(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.longitude)));
                rutaLocation.setClient_id(cursor.getInt(cursor.getColumnIndex(AppContract.RutaLocation.client_id)));
                rutaLocation.setZone(cursor.getInt(cursor.getColumnIndex(AppContract.RutaLocation.zone)));
                rutaLocation.setPriority(cursor.getInt(cursor.getColumnIndex(AppContract.RutaLocation.priority)));
                rutaLocation.setStatus(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.status)));
                rutaLocation.setEntrada(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.entrada)));
                rutaLocation.setSalida(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.salida)));
                rutaLocation.setFechaHoraEntrada(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.fechaHoraEntrada)));
                rutaLocation.setFechaHoraSalida(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.fechaHoraSalida)));
                rutaLocation.setType(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.type)));
                rutaLocation.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.observation)));
                rutaLocation.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.RutaLocation.estadoEnvio)));

                list.add(rutaLocation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<Cobranza> mappingListCobranza(Cursor cursor){
        List<Cobranza> list = new ArrayList<Cobranza>();
        Cobranza cobranza;
        if(cursor.moveToFirst()) {
            do {
                cobranza = new Cobranza();
                cobranza.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.id)));
                cobranza.setClient_id(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.client_id)));
                cobranza.setUser_id(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.user_id)));
                cobranza.setAmount(cursor.getInt(cursor.getColumnIndex(AppContract.Cobranza.amount)));
                cobranza.setInvoice_number(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.invoice_number)));
                cobranza.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.observation)));
                cobranza.setStatus(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.status)));
                cobranza.setNombre_cliente(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.nombre_cliente)));
                cobranza.setNombre_vendedor(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.nombre_vendedor)));
                cobranza.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.Cobranza.estado_envio)));

                list.add(cobranza);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    private Pedido mappingPedido(Cursor cursor){
        Pedido pedido = new Pedido();
        if(cursor.moveToFirst()) {
            pedido.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.id)));
            pedido.setAd_client_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.ad_client_id)));
            pedido.setAd_org_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.ad_org_id)));
            pedido.setIsactive(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.isactive)));
            pedido.setDate_order(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.date_order)));
            pedido.setOrder_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.order_id)));
            pedido.setClient_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.client_id)));
            pedido.setUser_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.user_id)));
            pedido.setTotal(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.total)));
            pedido.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.observation)));
            pedido.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.estado_envio)));
        }
        cursor.close();
        return pedido;
    }

    private List<Pedido> mappingListPedido(Cursor cursor){
        List<Pedido> list = new ArrayList<Pedido>();
        Pedido pedido;
        if(cursor.moveToFirst()) {
            do {
                pedido = new Pedido();
                pedido.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.id)));
                pedido.setAd_client_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.ad_client_id)));
                pedido.setAd_org_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.ad_org_id)));
                pedido.setIsactive(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.isactive)));
                pedido.setDate_order(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.date_order)));
                pedido.setOrder_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.order_id)));
                pedido.setClient_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.client_id)));
                pedido.setUser_id(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.user_id)));
                pedido.setTotal(cursor.getInt(cursor.getColumnIndex(AppContract.Pedido.total)));
                pedido.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.observation)));
                pedido.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.estado_envio)));
                pedido.setIsinvoiced(cursor.getString(cursor.getColumnIndex(AppContract.Pedido.isinvoiced)));

                //si trae tambien el nombre del query en el resultado
                if (cursor.getColumnIndex(AppContract.Cliente.nombre) != -1){
                    pedido.setNombre_cliente(cursor.getString(cursor.getColumnIndex(AppContract.Cliente.nombre)));
                }

                list.add(pedido);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<Factura> mappingListFactura(Cursor cursor){
        List<Factura> list = new ArrayList<Factura>();
        Factura factura;
        if(cursor.moveToFirst()) {
            do {
                factura = new Factura();
                factura.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.id)));
                factura.setIsactive(cursor.getString(cursor.getColumnIndex(AppContract.Factura.isactive)));
                factura.setOrder_id(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.order_id)));
                factura.setDateinvoiced(cursor.getString(cursor.getColumnIndex(AppContract.Factura.dateinvoiced)));
                factura.setClient_id(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.client_id)));
                factura.setGrandtotal(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.grandtotal)));
                factura.setIspaid(cursor.getString(cursor.getColumnIndex(AppContract.Factura.ispaid)));
                factura.setPend(cursor.getInt(cursor.getColumnIndex(AppContract.Factura.pend)));
                factura.setNroFacturaImprimir(cursor.getString(cursor.getColumnIndex(AppContract.Factura.nroFacturaImprimir)));

                list.add(factura);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<PedidoDetalle> mappingListPedidoDetalle(Cursor cursor){
        List<PedidoDetalle> list = new ArrayList<PedidoDetalle>();
        PedidoDetalle detalle;
        if(cursor.moveToFirst()) {
            do {
                detalle = new PedidoDetalle();

                detalle.setOrder_id(cursor.getInt(cursor.getColumnIndex(AppContract.PedidoDetalle.id)));
                detalle.setIsactive(cursor.getString(cursor.getColumnIndex(AppContract.PedidoDetalle.isactive)));
                detalle.setProduct_id(cursor.getInt(cursor.getColumnIndex(AppContract.PedidoDetalle.product_id)));
                detalle.setQuantity(cursor.getInt(cursor.getColumnIndex(AppContract.PedidoDetalle.quantity)));
                detalle.setPrice(cursor.getInt(cursor.getColumnIndex(AppContract.PedidoDetalle.price)));
                detalle.setTotal(cursor.getInt(cursor.getColumnIndex(AppContract.PedidoDetalle.total)));
                detalle.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.PedidoDetalle.observation)));

                list.add(detalle);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<CobranzaDetalle> mappingListCobranzaDetalle(Cursor cursor){
        List<CobranzaDetalle> list = new ArrayList<CobranzaDetalle>();
        CobranzaDetalle detalle;
        if(cursor.moveToFirst()) {
            do {
                detalle = new CobranzaDetalle();
                detalle.setInvoice(cursor.getString(cursor.getColumnIndex(AppContract.CobranzaDetalle.invoice)));
                detalle.setCharge_id(cursor.getInt(cursor.getColumnIndex(AppContract.CobranzaDetalle.charge_id)));
                detalle.setCashed(cursor.getInt(cursor.getColumnIndex(AppContract.CobranzaDetalle.cashed)));
                detalle.setAmount(cursor.getInt(cursor.getColumnIndex(AppContract.CobranzaDetalle.amount)));

                list.add(detalle);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<Producto> mappingListProducto(Cursor cursor){
        List<Producto> list = new ArrayList<Producto>();
        Producto producto;
        if(cursor.moveToFirst()) {
            do {
                producto = new Producto();
                producto.setM_product_id(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.m_product_id)));
                producto.setName(cursor.getString(cursor.getColumnIndex(AppContract.Producto.name)));
                producto.setPrice(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.price)));
                producto.setStock(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.stock)));
                producto.setCodinterno(cursor.getString(cursor.getColumnIndex(AppContract.Producto.codinterno)));
                producto.setIdFamilia(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.id_familia)));
                producto.setIdSubFamilia(cursor.getInt(cursor.getColumnIndex(AppContract.Producto.id_subfamilia)));
                list.add(producto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<RegistroVisita> mappingListRegistroVisita(Cursor cursor){
        List<RegistroVisita> list = new ArrayList<RegistroVisita>();
        RegistroVisita registroVisita;
        if(cursor.moveToFirst()) {
            do {
                registroVisita = new RegistroVisita();
                registroVisita.setTipo_visita(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.tipo_visita)));
                registroVisita.setRuc(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.ruc)));
                registroVisita.setCliente(cursor.getInt(cursor.getColumnIndex(AppContract.RegistroVisita.cliente)));
                registroVisita.setLatitude(cursor.getDouble(cursor.getColumnIndex(AppContract.RegistroVisita.latitude)));
                registroVisita.setLongitude(cursor.getDouble(cursor.getColumnIndex(AppContract.RegistroVisita.longitude)));
                registroVisita.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.observation)));
                registroVisita.setStatus(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.status)));
                registroVisita.setUsuario(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.usuario)));
                registroVisita.setFechavisita(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.fechavisita)));
                registroVisita.setHoravisita(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.horavisita)));
                registroVisita.setFecha_prox_visita(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.fecha_prox_visita)));
                registroVisita.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.estado_envio)));
                registroVisita.setNombreUsuario(cursor.getString(cursor.getColumnIndex(AppContract.RegistroVisita.nombre_usuario)));

                list.add(registroVisita);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<Entrega> mappingListEntrega(Cursor cursor){
        List<Entrega> list = new ArrayList<Entrega>();
        Entrega entrega;
        if(cursor.moveToFirst()) {
            do {
                entrega = new Entrega();

                entrega.setId(cursor.getInt(cursor.getColumnIndex(AppContract.Entrega.id)));
                entrega.setOrder_id(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.order_id)));
                entrega.setClient_id(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.client_id)));
                entrega.setDate_delivered(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.date_delivered)));
                entrega.setTime_delivered(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.time_delivered)));
                entrega.setUser_id(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.user_id)));
                entrega.setObservation(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.observation)));
                entrega.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.estado_envio)));
                entrega.setNombre_cliente(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.nombre_cliente)));
                entrega.setNombre_vendedor(cursor.getString(cursor.getColumnIndex(AppContract.Entrega.nombre_vendedor)));

                list.add(entrega);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<LocationTable> mappingListLocation(Cursor cursor){
        List<LocationTable> list = new ArrayList<LocationTable>();
        LocationTable location;
        if(cursor.moveToFirst()) {
            do {
                location = new LocationTable();
                location.setId(cursor.getInt(cursor.getColumnIndex(AppContract.LocationColumns.id)));
                location.setLatitude(cursor.getString(cursor.getColumnIndex(AppContract.LocationColumns.latitude)));
                location.setLongitude(cursor.getString(cursor.getColumnIndex(AppContract.LocationColumns.longitude)));
                location.setDate(cursor.getString(cursor.getColumnIndex(AppContract.LocationColumns.date)));
                location.setTime(cursor.getString(cursor.getColumnIndex(AppContract.LocationColumns.time)));
                location.setId_user(cursor.getString(cursor.getColumnIndex(AppContract.LocationColumns.id_user)));
                location.setEstado_envio(cursor.getString(cursor.getColumnIndex(AppContract.LocationColumns.estado_envio)));

                list.add(location);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    //Insert LOGIN
    public void insertLogin(Login login){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.Login.userName, login.getUserName());
        values.put(AppContract.Login.status, login.getStatus());
        values.put(AppContract.Login.sessionID, login.getSessionID());
        db.insert(AppContract.Tables.LOGIN, null, values);

        Log.d("Valor Insertado", login.toString());
    }

    public void insertOrUpdateCobranzaList (List<Cobranza> cobranzaList) {
        for (Cobranza c: cobranzaList){
            Cobranza tmp = selectCobranzaById(c.getId());
            //insertar
            if (tmp.getId() == null){
                insertCobranza(c);
                //actualizar
            } else {
                updateCobranza(c);
            }
        }
    }


    public void insertOrUpdatePrecioVersionList (List<PrecioVersion> precioVersionListList) {
        for (PrecioVersion c: precioVersionListList){
            PrecioVersion tmp = selectPrecioVersionByIdAndProducto(c.getId(), c.getM_product_id());
            //insertar
            if (tmp.getId() == null){
                insertPrecioVersion(c);
                //actualizar
            } else {
                updatePrecioVersion(c);
            }
        }
    }

    public void insertOrUpdateFacturaList (List<Factura> facturaList) {
        for (Factura c: facturaList){
            Factura tmp = selectFacturaById(c.getId());
            //insertar
            if (tmp.getId() == null){
                insertFactura(c);
            //actualizar
            } else {
                updateFactura(c);
            }
        }
    }

    public void insertOrUpdateProductoList (List<Producto> productoList) {
        Log.d(TAG,"Cant Productos: "+productoList.size());
        for (Producto p: productoList){
            Producto tmp = selectProductById(p.getM_product_id());
            //insertar
            if (tmp.getM_product_id() == null){
                insertProducto(p);

            } else {
                updateProducto(p);
            }

            Log.d(TAG,"Producto"+p.getName()+ " "+p.getM_product_id());
            Log.d(TAG,"Eliminamos el Stock del Producto");
            deleteStockProductoByID(p.getM_product_id());

            //insertamos el nuevo stock
            Log.d(TAG,"Insertar Stock Producto");
            insertarStockProducto(p);
        }
    }

    public void insertOrUpdateClienteList (List<Cliente> clientesList) {
        for (Cliente c: clientesList){
            Cliente tmp = selectClienteById(c.getId());
            //insertar
            if (tmp.getId() == null){
                insertCliente(c);
            //actualizar
            } else {
                updateCliente(c);
            }
        }
    }

    //Insert CLIENTE lista
    public void insertPrecioCategoriaList (List<PrecioCategoria> precioCategoriaList) {
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, AppContract.Tables.PRECIO_CATEGORIA);

        final int rowid = ih.getColumnIndex(AppContract.PrecioCategoria.rowid);
        final int m_pricelist_version_id = ih.getColumnIndex(AppContract.PrecioCategoria.m_pricelist_version_id);
        final int m_pricelist_id = ih.getColumnIndex(AppContract.PrecioCategoria.m_pricelist_id);
        final int name = ih.getColumnIndex(AppContract.PrecioCategoria.name);
        final int active = ih.getColumnIndex(AppContract.PrecioCategoria.active);
        final int ad_client_id = ih.getColumnIndex(AppContract.PrecioCategoria.ad_client_id);

        try {
            for (PrecioCategoria c : precioCategoriaList){
                ih.prepareForInsert();
                ih.bind(rowid, c.getRowid());
                ih.bind(m_pricelist_version_id, c.getM_pricelist_version_id());
                ih.bind(m_pricelist_id, c.getM_pricelist_id());
                ih.bind(name, c.getName());
                ih.bind(active, c.getActive());
                ih.bind(ad_client_id, c.getAd_client_id());
                ih.execute();
            }
        } finally {
            ih.close();
        }
    }


    //Insert CLIENTE lista
    public void insertClienteList (List<Cliente> clientesList) {
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, AppContract.Tables.CLIENTE);

        final int id_index = ih.getColumnIndex(AppContract.Cliente.id);
        final int ruc_index = ih.getColumnIndex(AppContract.Cliente.ruc);
        final int nombre_index = ih.getColumnIndex(AppContract.Cliente.nombre);
        final int direccion_index = ih.getColumnIndex(AppContract.Cliente.direccion);
        final int telefono_index = ih.getColumnIndex(AppContract.Cliente.telefono);

        try {
            for (Cliente c : clientesList){
                ih.prepareForInsert();
                ih.bind(id_index, c.getId());
                ih.bind(ruc_index, c.getRuc());
                ih.bind(nombre_index, c.getNombre());
                ih.bind(direccion_index, c.getDireccion());
                ih.bind(telefono_index, c.getTelefono());
                ih.execute();
            }
        } finally {
            ih.close();
        }
    }

    public void insertClienteLista (List<Cliente> listPrec, Context context){
        //SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.CLIENTE
                + " ( " + AppContract.Cliente.id + ", "
                + AppContract.Cliente.ruc + ", "
                + AppContract.Cliente.nombre + ", "
                + AppContract.Cliente.direccion + ", "
                + AppContract.Cliente.telefono + ", "

                + AppContract.Cliente.credito_disponible + ", "
                + AppContract.Cliente.credito_extra + ", "
                + AppContract.Cliente.credito_usado + ", "
                + AppContract.Cliente.cheques_pend + ", "
                + AppContract.Cliente.factura_vieja + ","
                + AppContract.Cliente.categoria_precio + ","
                + AppContract.Cliente.plazo_max_cheque + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        db.beginTransaction();
        try {
            for (Cliente c : listPrec) {
                insStmt.bindLong(1, c.getId());
                insStmt.bindString(2, c.getRuc());
                insStmt.bindString(3, c.getNombre());

                if (c.getDireccion() != null)
                    insStmt.bindString(4, c.getDireccion());
                if (c.getTelefono()!= null)
                    insStmt.bindString(5, c.getTelefono());
                insStmt.bindDouble(6, c.getCredito_disponible());
                insStmt.bindDouble(7, c.getCredito_extra());
                insStmt.bindDouble(8, c.getCredito_usado());
                insStmt.bindDouble(9, c.getCheques_pend());

                if (c.getFactura_vieja() != null)
                    insStmt.bindString(10, c.getFactura_vieja());

                if (c.getCategoria_precio() != null)
                    insStmt.bindLong(11, c.getCategoria_precio());

                insStmt.bindString(12, c.getPlazomax().toString());
                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }


    public void insertPrecioVersionLista (List<PrecioVersion> listPrecVersion, Context context){
        //SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.PRECIO_VERSION
                + " ( " + AppContract.PrecioVersion.id + ", "
                + AppContract.PrecioVersion.m_product_id + ","
                + AppContract.PrecioVersion.precio_ventas_inicial + ","
                + AppContract.PrecioVersion.precio_costo_inicial + ","
                + AppContract.PrecioVersion.precio_publico + ","
                + AppContract.PrecioVersion.precio_mayorista_a + ","
                + AppContract.PrecioVersion.precio_mayorista_b+ ","
                + AppContract.PrecioVersion.precio_lista+ ","
                + AppContract.PrecioVersion.precio_vidrieros+ ","
                + AppContract.PrecioVersion.precio_radiadoritas + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ? );");

        db.beginTransaction();
        try {
            for (PrecioVersion p : listPrecVersion) {
                insStmt.bindLong(1, p.getId());
                insStmt.bindLong(2, p.getM_product_id());

                if (p.getPrecio_ventas_inicial() != null)
                    insStmt.bindLong(3, p.getPrecio_ventas_inicial());

                insStmt.bindLong(4, p.getPrecio_costo_inicial());
                insStmt.bindLong(5, p.getPrecio_publico());
                insStmt.bindLong(6, p.getPrecio_mayorista_a());
                insStmt.bindLong(7, p.getPrecio_mayorista_b());
                insStmt.bindLong(8, p.getPrecio_lista());

                if (p.getPrecio_vidrieros() != null)
                    insStmt.bindLong(9, p.getPrecio_vidrieros());
                insStmt.bindLong(10, p.getPrecio_radiadoritas());

                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }


    public void insertUsuarioLista (List<Usuario> listUsuario, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.USUARIO
                + " ( " + AppContract.Usuario.id + ", "
                + AppContract.Usuario.mail + ","
                + AppContract.Usuario.name + ","
                + AppContract.Usuario.lastname + ","
                + AppContract.Usuario.password + ","
                + AppContract.Usuario.state + ","
                + AppContract.Usuario.role + ","
                + AppContract.Usuario.userCellphone + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

        db.beginTransaction();
        try {
            for (Usuario u : listUsuario) {
                insStmt.bindLong(1, u.getId());
                insStmt.bindString(2, u.getMail());
                insStmt.bindString(3, u.getName());
                insStmt.bindString(4, u.getLastname());
                insStmt.bindString(5, u.getPassword());
                insStmt.bindString(6, u.getState());
                insStmt.bindString(7, u.getRole());
                if (u.getUserCellphone() != null)
                    insStmt.bindString(8, u.getUserCellphone());

                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //insertar rutalocation
    public void insertRutaLocation (RutaLocation entrega){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();

        /*Entrega ultimaEntrega = selectLastEntrega();
        if (ultimaEntrega.getId() != null)
            entrega.setId(ultimaEntrega.getId()+1);
        else
            entrega.setId(0);*/

        ContentValues values = new ContentValues();
        values.put(AppContract.RutaLocation.id, entrega.getId());
        values.put(AppContract.RutaLocation.client_id, entrega.getClient_id());
        values.put(AppContract.RutaLocation.priority, entrega.getPriority());
        values.put(AppContract.RutaLocation.user_id, entrega.getUser_id());
        values.put(AppContract.RutaLocation.latitude, entrega.getLatitude());
        values.put(AppContract.RutaLocation.longitude, entrega.getLongitude());
        values.put(AppContract.RutaLocation.observation, entrega.getObservation());
        values.put(AppContract.RutaLocation.estadoEnvio, entrega.getEstadoEnvio());
        values.put(AppContract.RutaLocation.date, entrega.getDate());
        values.put(AppContract.RutaLocation.zone, entrega.getZone());
        values.put(AppContract.RutaLocation.status, entrega.getStatus());
        values.put(AppContract.RutaLocation.type, entrega.getType());


        db.insert(AppContract.Tables. RUTA_LOCATION, null, values);
        Log.d("Valor Insertado", entrega.toString());
    }
    ////////////////////////////////////////////////////////////////////////

    public void insertRutaLocationLista (List<RutaLocation> listRutaLocation, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.RUTA_LOCATION
                + " ( " + AppContract.RutaLocation.id + ", "
                + AppContract.RutaLocation.date + ","
                + AppContract.RutaLocation.user_id + ","
                + AppContract.RutaLocation.latitude + ","
                + AppContract.RutaLocation.longitude + ","
                + AppContract.RutaLocation.client_id + ","
                + AppContract.RutaLocation.zone + ","
                + AppContract.RutaLocation.priority + ","
                + AppContract.RutaLocation.status + ","
                + AppContract.RutaLocation.type + ","
                + AppContract.RutaLocation.estadoEnvio + ","
                + AppContract.RutaLocation.observation + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);");

        db.beginTransaction();
        try {
            for (RutaLocation r : listRutaLocation) {
                insStmt.bindLong(1, r.getId());
                insStmt.bindString(2, r.getDate());
                insStmt.bindLong(3, r.getUser_id());

                if (r.getLatitude() != null)
                    insStmt.bindString(4, r.getLatitude());
                if (r.getLongitude() != null)
                    insStmt.bindString(5, r.getLongitude());

                insStmt.bindLong(6, r.getClient_id());

                if(r.getZone()!=null)
                insStmt.bindLong(7, r.getZone());

                if(r.getPriority()!=null)
                insStmt.bindLong(8, r.getPriority());

                insStmt.bindString(9, r.getStatus());

                if (r.getType() != null)
                    insStmt.bindString(10, r.getType());

                if(r.getObservation()!=null) {
                    insStmt.bindString(11, r.getObservation());
                }
                else{
                    insStmt.bindString(11, "");
                }

                insStmt.executeInsert();
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    public void insertCobranzaLista (List<Cobranza> listCobranza, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.COBRANZA
                + " ( " + AppContract.Cobranza.id + ", "
                + AppContract.Cobranza.client_id + ","
                + AppContract.Cobranza.user_id + ","
                + AppContract.Cobranza.amount + ","
                + AppContract.Cobranza.invoice_number + ","
                + AppContract.Cobranza.observation + ","
                + AppContract.Cobranza.status + ","
                + AppContract.Cobranza.nombre_cliente + ","
                + AppContract.Cobranza.nombre_vendedor + ","
                + AppContract.Cobranza.estado_envio + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");


        db.beginTransaction();
        try {
            for (Cobranza c : listCobranza) {
                insStmt.bindLong(1, c.getId());
                if (c.getClient_id() != null)
                    insStmt.bindLong(2, c.getClient_id());
                if (c.getUser_id() != null)
                    insStmt.bindLong(3, c.getUser_id());

                insStmt.bindLong(4, c.getAmount());
                if (c.getInvoice_number() != null)
                    insStmt.bindString(5, c.getInvoice_number());

                insStmt.bindString(6, c.getObservation());
                insStmt.bindString(7, c.getStatus());
                insStmt.bindString(8, c.getNombre_cliente());
                insStmt.bindString(9, c.getNombre_vendedor());
                //insStmt.bindString(11, c.getEstado_envio());

                insStmt.executeInsert();
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }


    //Insert CLIENTE
    public void insertCliente(Cliente cliente){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.Cliente.id, cliente.getId());
        values.put(AppContract.Cliente.nombre, cliente.getNombre());
        values.put(AppContract.Cliente.ruc, cliente.getRuc());
        values.put(AppContract.Cliente.telefono, cliente.getTelefono());
        values.put(AppContract.Cliente.direccion, cliente.getDireccion());
        values.put(AppContract.Cliente.credito_disponible, cliente.getCredito_disponible());
        values.put(AppContract.Cliente.credito_extra, cliente.getCredito_extra());
        values.put(AppContract.Cliente.credito_usado, cliente.getCredito_usado());
        values.put(AppContract.Cliente.cheques_pend, cliente.getCheques_pend());
        values.put(AppContract.Cliente.factura_vieja, cliente.getFactura_vieja());
        values.put(AppContract.Cliente.plazo_max_cheque, cliente.getPlazomax());
        values.put(AppContract.Cliente.categoria_precio, cliente.getCategoria_precio());

        db.insert(AppContract.Tables.CLIENTE, null, values);
        Log.d("Valor Insertado", cliente.toString());
    }

    //Insert PRECIO_VERSION
    public void insertPrecioVersion(PrecioVersion precioVersion){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.PrecioVersion.id, precioVersion.getId());
        values.put(AppContract.PrecioVersion.m_product_id, precioVersion.getM_product_id());
        values.put(AppContract.PrecioVersion.precio_ventas_inicial, precioVersion.getPrecio_ventas_inicial());
        values.put(AppContract.PrecioVersion.precio_costo_inicial, precioVersion.getPrecio_costo_inicial());
        values.put(AppContract.PrecioVersion.precio_publico, precioVersion.getPrecio_publico());
        values.put(AppContract.PrecioVersion.precio_mayorista_a, precioVersion.getPrecio_mayorista_a());
        values.put(AppContract.PrecioVersion.precio_mayorista_b, precioVersion.getPrecio_mayorista_b());
        values.put(AppContract.PrecioVersion.precio_lista, precioVersion.getPrecio_lista());
        values.put(AppContract.PrecioVersion.precio_vidrieros, precioVersion.getPrecio_vidrieros());
        values.put(AppContract.PrecioVersion.precio_radiadoritas, precioVersion.getPrecio_radiadoritas());

        db.insert(AppContract.Tables.PRECIO_VERSION, null, values);
        //Log.d("Valor Insertado", precioVersion.toString());
    }


    //Insert PRODUCTO
    public void  insertProducto(Producto producto){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.Producto.m_product_id, producto.getM_product_id());
        values.put(AppContract.Producto.name, producto.getName());
        values.put(AppContract.Producto.price, producto.getPrice());
        values.put(AppContract.Producto.stock, producto.getStock());
        values.put(AppContract.Producto.codinterno, producto.getCodinterno());
        values.put(AppContract.Producto.id_familia, producto.getIdFamilia());
        values.put(AppContract.Producto.id_subfamilia, producto.getIdSubFamilia());

        db.insert(AppContract.Tables.PRODUCTO, null, values);
        //Log.d("Valor Insertado", precioVersion.toString());


    }

    public void insertarStockProducto(Producto producto){
        Log.d(TAG,"insertar stock de producto"+producto.getM_product_id()+" "+producto.getName());
        SQLiteDatabase db =mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        //Log.d(TAG,producto.getM_product_id()+" - Cant. Stock Producto: ");

        Log.d(TAG,producto.getM_product_id()+" - Cant. Stock Producto: "+producto.getListaStock().size());

        if(producto.getListaStock()!=null){
            for (StockDTO stockDTO :producto.getListaStock()){
                values.put(AppContract.StockProducto.m_product_id,stockDTO.getProducto().getM_product_id());
                values.put(AppContract.StockProducto.desc_m_product_id,stockDTO.getProducto().getName());
                values.put(AppContract.StockProducto.m_locator_id,stockDTO.getLocator().getM_locator_id());
                values.put(AppContract.StockProducto.desc_m_locator,stockDTO.getLocator().getM_locator_value());
                values.put(AppContract.StockProducto.stock_disponible,stockDTO.getStock_disponible());
                db.insert(AppContract.Tables.STOCK_PRODUCTO,null,values);
            }
        }

    }


    //Insert FACTURA
    public void insertFactura(Factura factura){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.Factura.id, factura.getId());
        values.put(AppContract.Factura.isactive, factura.getIsactive());
        values.put(AppContract.Factura.order_id, factura.getOrder_id());
        values.put(AppContract.Factura.dateinvoiced, factura.getDateinvoiced());
        values.put(AppContract.Factura.client_id, factura.getClient_id());
        values.put(AppContract.Factura.grandtotal, factura.getGrandtotal());
        values.put(AppContract.Factura.ispaid, factura.getIspaid());
        values.put(AppContract.Factura.pend, factura.getPend());
        values.put(AppContract.Factura.nroFacturaImprimir, factura.getNroFacturaImprimir());

        db.insert(AppContract.Tables.FACTURA, null, values);
        //Log.d("Valor Insertado", precioVersion.toString());
    }

    //Insert REGISTRO VISITA
    public void insertRegistroVisita(RegistroVisita registroVisita){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.RegistroVisita.tipo_visita, registroVisita.getTipo_visita());
        values.put(AppContract.RegistroVisita.ruc, registroVisita.getRuc());
        values.put(AppContract.RegistroVisita.cliente, registroVisita.getCliente());
        values.put(AppContract.RegistroVisita.latitude, registroVisita.getLatitude());
        values.put(AppContract.RegistroVisita.longitude, registroVisita.getLongitude());
        values.put(AppContract.RegistroVisita.observation, registroVisita.getObservation());
        values.put(AppContract.RegistroVisita.status, registroVisita.getStatus());
        values.put(AppContract.RegistroVisita.usuario, registroVisita.getUsuario());
        values.put(AppContract.RegistroVisita.fechavisita, registroVisita.getFechavisita());
        values.put(AppContract.RegistroVisita.horavisita, registroVisita.getHoravisita());
        values.put(AppContract.RegistroVisita.fecha_prox_visita, registroVisita.getFecha_prox_visita());
        values.put(AppContract.RegistroVisita.estado_envio, registroVisita.getEstado_envio());
        values.put(AppContract.RegistroVisita.ent_sal,registroVisita.getEnt_sal());
        values.put(AppContract.RegistroVisita.nombre_usuario, registroVisita.getNombreUsuario());

        long registro = db.insert(AppContract.Tables.REGISTRO_VISITA, null, values);
        Log.d(TAG,"se insertaron " + registro);
        Log.d("Valor Insertado", registroVisita.toString());
    }

    //Insert ENTREGA
    public void insertEntrega (Entrega entrega){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();

        Entrega ultimaEntrega = selectLastEntrega();
        if (ultimaEntrega.getId() != null)
            entrega.setId(ultimaEntrega.getId()+1);
        else
            entrega.setId(0);

        ContentValues values = new ContentValues();
        values.put(AppContract.Entrega.id, entrega.getId());
        values.put(AppContract.Entrega.client_id, entrega.getClient_id());
        values.put(AppContract.Entrega.order_id, entrega.getOrder_id());
        values.put(AppContract.Entrega.user_id, entrega.getUser_id());
        values.put(AppContract.Entrega.date_delivered, entrega.getDate_delivered());
        values.put(AppContract.Entrega.time_delivered, entrega.getTime_delivered());
        values.put(AppContract.Entrega.observation, entrega.getObservation());
        values.put(AppContract.Entrega.estado_envio, entrega.getEstado_envio());
        values.put(AppContract.Entrega.nombre_cliente, entrega.getNombre_cliente());
        values.put(AppContract.Entrega.nombre_vendedor, entrega.getNombre_vendedor());

        db.insert(AppContract.Tables.ENTREGA, null, values);
        Log.d("Valor Insertado", entrega.toString());
    }

    //Insert COBRANZA
    public void insertCobranza (Cobranza cobranza){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppContract.Cobranza.id, cobranza.getId());
        values.put(AppContract.Cobranza.client_id, cobranza.getClient_id());
        values.put(AppContract.Cobranza.user_id, cobranza.getUser_id());
        values.put(AppContract.Cobranza.amount, cobranza.getAmount());
        values.put(AppContract.Cobranza.invoice_number, cobranza.getInvoice_number());
        values.put(AppContract.Cobranza.observation, cobranza.getObservation());
        values.put(AppContract.Cobranza.status, cobranza.getStatus());
        values.put(AppContract.Cobranza.nombre_cliente, cobranza.getNombre_cliente());
        values.put(AppContract.Cobranza.nombre_vendedor, cobranza.getNombre_vendedor());
        values.put(AppContract.Cobranza.estado_envio, cobranza.getEstado_envio());

        db.insert(AppContract.Tables.COBRANZA, null, values);
        //Log.d("Valor Insertado", cobranza.toString());

    }

    //Insert SESSION
    public void insertSession(Session session){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.Session.userId, session.getUserId());
        values.put(AppContract.Session.email, session.getEmail());
        values.put(AppContract.Session.admin, session.isAdmin());
        values.put(AppContract.Session.root, session.isRoot());

        db.insert(AppContract.Tables.SESSION, null, values);
        Log.d("Valor Insertado", session.toString());
    }

    //Insert LOCATIOM
    public void insertLocation (LocationTable locationTable){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.Location.latitude, locationTable.getLatitude());
        values.put(AppContract.Location.longitude, locationTable.getLongitude());
        values.put(AppContract.Location.date, locationTable.getDate());
        values.put(AppContract.Location.time, locationTable.getTime());
        values.put(AppContract.Location.id_user, locationTable.getId_user());
        values.put(AppContract.Location.estado_envio, locationTable.getEstado_envio());

        db.insert(AppContract.Tables.LOCATION, null, values);
        Log.d("Valor Insertado", locationTable.toString());
    }

    //Insert PRODUCTO LISTA
    public void insertProductoLista (List<Producto> listProducto, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.PRODUCTO
                + " ( " + AppContract.Producto.m_product_id + ", "
                + AppContract.Producto.codinterno + ","
                + AppContract.Producto.name+ ","
                + AppContract.Producto.price+ ","
                + AppContract.Producto.id_familia+ ","
                + AppContract.Producto.id_subfamilia //+ ","
//                + AppContract.Producto.stock
                + ") VALUES (?, ?, ?, ?, ?, ?);");

        db.beginTransaction();
        try {
            for (Producto p : listProducto) {
                insStmt.bindLong(1, p.getM_product_id());
                if (p.getCodinterno() != null){
                    insStmt.bindString(2, p.getCodinterno());
                }
                insStmt.bindString(3, p.getName());
                insStmt.bindLong(4, p.getPrice());
                insStmt.bindLong(5, p.getIdFamilia());
                insStmt.bindLong(6, p.getIdSubFamilia());
//                insStmt.bindLong(7, p.getStock());

                insertarStockProducto(p);

                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }

        //db.close();
    }

    //Insert ENTREGA LISTA
    public void insertEntregaLista (List<Entrega> listEntrega, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.ENTREGA
                + " ( " + AppContract.Entrega.id + ", "
                + AppContract.Entrega.user_id + ","
                + AppContract.Entrega.client_id + ","
                + AppContract.Entrega.order_id + ","
                + AppContract.Entrega.date_delivered + ","
                + AppContract.Entrega.time_delivered + ","
                + AppContract.Entrega.observation + ","
                + AppContract.Entrega.nombre_cliente + ","
                + AppContract.Entrega.nombre_vendedor + ","
                + AppContract.Entrega.estado_envio  + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");


        db.beginTransaction();
        try {
            for (Entrega e : listEntrega) {
                insStmt.bindLong(1, e.getId());
                insStmt.bindString(2, e.getUser_id());
                insStmt.bindString(3, e.getClient_id());
                insStmt.bindString(4, e.getOrder_id());
                insStmt.bindString(5, e.getDate_delivered());
                insStmt.bindString(6, e.getTime_delivered());
                insStmt.bindString(7, e.getObservation());
                insStmt.bindString(8, e.getNombre_cliente());
                insStmt.bindString(9, e.getNombre_vendedor());
                insStmt.bindString(10, e.getEstado_envio());

                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    //Insert PRODUCTO FAMILIA LISTA
    public void insertProductoFamiliaLista (List<ProductoFamilia> listProductoFamilia, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.PRODUCTO_FAMILIA
                + " ( " + AppContract.ProductoFamilia.m_product_family_id + ", "
                + AppContract.ProductoFamilia.value + ","
                + AppContract.ProductoFamilia.description + ") VALUES (?, ?, ?);");

        db.beginTransaction();
        try {
            for (ProductoFamilia p : listProductoFamilia) {

                insStmt.bindLong(1, p.getM_product_family_id());
                insStmt.bindString(2, p.getValue());
                insStmt.bindString(3, p.getDescription());
                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    //Insert REGISTRO VISITA lista
    public void insertRegistroVisitaLista (List<RegistroVisita> listRegistroVisita, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.REGISTRO_VISITA
                + " ( " + AppContract.RegistroVisita.tipo_visita + ", "
                + AppContract.RegistroVisita.ruc + ","
                + AppContract.RegistroVisita.cliente + ","
                + AppContract.RegistroVisita.latitude + ","
                + AppContract.RegistroVisita.longitude + ","
                + AppContract.RegistroVisita.observation + ","
                + AppContract.RegistroVisita.usuario + ","
                + AppContract.RegistroVisita.fechavisita + ","
                + AppContract.RegistroVisita.horavisita + ","
                + AppContract.RegistroVisita.fecha_prox_visita + ","
                + AppContract.RegistroVisita.estado_envio + ","
                + AppContract.RegistroVisita.estado_envio + ","
                + AppContract.RegistroVisita.nombre_usuario + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");


        db.beginTransaction();
        try {
            for (RegistroVisita r : listRegistroVisita) {

                insStmt.bindString(1, r.getTipo_visita());
                insStmt.bindString(2, r.getRuc());
                insStmt.bindLong(3, r.getCliente());
                insStmt.bindDouble(4, r.getLatitude());
                insStmt.bindDouble(5, r.getLongitude());
                insStmt.bindString(6, r.getObservation());

                if (r.getUsuario() != null)
                    insStmt.bindString(7, r.getUsuario());
                insStmt.bindString(8, r.getFechavisita());
                insStmt.bindString(9, r.getHoravisita());
                if(r.getFecha_prox_visita()!=null) {
                    insStmt.bindString(10, r.getFecha_prox_visita());
                }
                insStmt.bindString(11, r.getEstado_envio());

                if (r.getEnt_sal() != null)
                    insStmt.bindString(12, r.getEnt_sal());
                if (r.getNombreUsuario() != null)
                    insStmt.bindString(13, r.getNombreUsuario());

                insStmt.executeInsert();
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    //Insert PRODUCTO IMAGEN LISTA
    public void insertProductoImagenLista (List<ProductoImagen> listProductoImagen, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.PRODUCTO_IMAGEN
                + " ( " + AppContract.ProductoImagen.m_product_id + ", "
                + AppContract.ProductoImagen.img + ","
                + AppContract.ProductoImagen.size + ") VALUES (?, ?, ?);");

        db.beginTransaction();
        try {
            for (ProductoImagen p : listProductoImagen) {
                insStmt.bindLong(1, p.getM_product_id());
                insStmt.bindString(2, p.getImg());
                insStmt.bindString(3, p.getSize());
                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    //Insert PRODUCTO SUB FAMILIA LISTA
    public void insertProductoSubFamiliaLista (List<ProductoSubFamilia> listProductoSubFamilia, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.PRODUCTO_SUB_FAMILIA
                + " ( " + AppContract.ProductoSubFamilia.id + ", "
                + AppContract.ProductoSubFamilia.value + ","
                + AppContract.ProductoSubFamilia.description + ","
                + AppContract.ProductoSubFamilia.id_familia + ") VALUES (?, ?, ?, ?);");

        db.beginTransaction();
        try {
            for (ProductoSubFamilia p : listProductoSubFamilia) {
                insStmt.bindLong(1, p.getId());
                insStmt.bindString(2, p.getValue());
                insStmt.bindString(3, p.getDescription());
                insStmt.bindLong(4, p.getId_familia());
                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    public void insertFacturaLista (List<Factura> listFactura, Context context){

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.FACTURA
                + " ( " + AppContract.Factura.id + ", "
                + AppContract.Factura.isactive + ", "
                + AppContract.Factura.order_id + ", "
                + AppContract.Factura.dateinvoiced + ", "
                + AppContract.Factura.client_id + ", "
                + AppContract.Factura.grandtotal + ", "
                + AppContract.Factura.ispaid + ", "
                + AppContract.Factura.nroFacturaImprimir + ", "
                + AppContract.Factura.pend  + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");

        db.beginTransaction();
        try {
            for (Factura f : listFactura) {
                insStmt.bindLong(1, f.getId());
                insStmt.bindString(2, f.getIsactive());
                insStmt.bindLong(3, f.getOrder_id());
                insStmt.bindString(4, f.getDateinvoiced());
                insStmt.bindLong(5, f.getClient_id());
                insStmt.bindLong(6, f.getGrandtotal());
                insStmt.bindString(7, f.getIspaid());
                if (f.getNroFacturaImprimir() != null)
                    insStmt.bindString(8, f.getNroFacturaImprimir());

                insStmt.bindLong(9, f.getPend());
                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    //Insert PEDIDO
    public void insertPedido (Pedido pedido){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.Pedido.id, pedido.getId());
        values.put(AppContract.Pedido.ad_client_id, pedido.getAd_client_id());
        values.put(AppContract.Pedido.ad_org_id, pedido.getAd_org_id());
        values.put(AppContract.Pedido.isactive, pedido.getIsactive());
        values.put(AppContract.Pedido.date_order, pedido.getDate_order());
        values.put(AppContract.Pedido.order_id, pedido.getOrder_id());
        values.put(AppContract.Pedido.client_id, pedido.getClient_id());
        values.put(AppContract.Pedido.user_id, pedido.getUser_id());
        values.put(AppContract.Pedido.total, pedido.getTotal());
        values.put(AppContract.Pedido.observation, pedido.getObservation());
        values.put(AppContract.Pedido.estado_envio, pedido.getEstado_envio());
        values.put(AppContract.Pedido.isinvoiced, pedido.getIsinvoiced());

        db.insert(AppContract.Tables.PEDIDO, null, values);
        Log.d("Valor Insertado", "id_pedido:"+pedido.getId() + "- estado_envio:"+pedido.getEstado_envio());
    }

    //Insert PRODUCTO_IMAGEN
    public void insertProductoImagen (ProductoImagen productoImagen){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.ProductoImagen.m_product_id, productoImagen.getM_product_id());
        values.put(AppContract.ProductoImagen.img, productoImagen.getImg());
        values.put(AppContract.ProductoImagen.size, productoImagen.getSize());
        values.put(AppContract.ProductoImagen.estado_envio, productoImagen.getEstado_envio());

        db.insert(AppContract.Tables.PRODUCTO_IMAGEN, null, values);
        Log.d("Valor Insertado", "nombre archivo:"+productoImagen.getImg());
    }

    public void insertPedidoLista (List<Pedido> listPedido, Context context){
        //initializeInstance(new AppDatabase.DictionaryOpenHelper(context));
        //SQLiteDatabase db = AppDatabase.getInstance().openDatabase();

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        SQLiteStatement insStmt = db.compileStatement("INSERT INTO "
                + AppContract.Tables.PEDIDO
                + " ( " + AppContract.Pedido.id + ", "
                + AppContract.Pedido.ad_client_id + ","
                + AppContract.Pedido.ad_org_id + ","
                + AppContract.Pedido.isactive + ","
                + AppContract.Pedido.date_order + ","
                + AppContract.Pedido.order_id + ","
                + AppContract.Pedido.client_id + ","
                + AppContract.Pedido.user_id + ","
                + AppContract.Pedido.total + ","
                + AppContract.Pedido.isinvoiced + ","
                + AppContract.Pedido.observation + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        db.beginTransaction();
        try {
            for (Pedido p : listPedido) {
                insStmt.bindLong(1, p.getId());
                insStmt.bindLong(2, p.getAd_client_id());
                insStmt.bindLong(3, p.getAd_org_id());
                insStmt.bindString(4, p.getIsactive());
                if (p.getDate_order() != null)
                    insStmt.bindString(5, p.getDate_order());
                if (p.getOrder_id() != null)
                    insStmt.bindLong(6, p.getOrder_id());
                insStmt.bindLong(7, p.getClient_id());
                insStmt.bindLong(8, p.getUser_id());
                insStmt.bindLong(9, p.getTotal());

                if (p.getIsinvoiced() != null)
                    insStmt.bindString(10, p.getIsinvoiced());

                if (p.getObservation() != null){
                    insStmt.bindString(11, p.getObservation());
                }
                insStmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            //AppDatabase.getInstance().closeDatabase();
        }
    }

    //Insert PEDIDO DETALLE
    public void insertPedidoDetalle (PedidoDetalle pedidoDetalle){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.PedidoDetalle.id, pedidoDetalle.getId());
        values.put(AppContract.PedidoDetalle.isactive, pedidoDetalle.getIsactive());
        values.put(AppContract.PedidoDetalle.product_id, pedidoDetalle.getProduct_id());
        values.put(AppContract.PedidoDetalle.quantity, pedidoDetalle.getQuantity());
        values.put(AppContract.PedidoDetalle.price, pedidoDetalle.getPrice());
        values.put(AppContract.PedidoDetalle.total, pedidoDetalle.getTotal());
        values.put(AppContract.PedidoDetalle.observation, pedidoDetalle.getObservation());
        values.put(AppContract.PedidoDetalle.order_id, pedidoDetalle.getOrder_id());

        db.insert(AppContract.Tables.PEDIDO_DETALLE, null, values);
        //Log.d("Valor Insertado", pedidoDetalle.toString());
    }

    //Insert COBRANZA DETALLE
    public void insertCobranzaDetalle (CobranzaDetalle cobranzaDetalle){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.CobranzaDetalle.amount, cobranzaDetalle.getAmount());
        values.put(AppContract.CobranzaDetalle.cashed, cobranzaDetalle.getCashed());
        values.put(AppContract.CobranzaDetalle.invoice, cobranzaDetalle.getInvoice());
        values.put(AppContract.CobranzaDetalle.charge_id, cobranzaDetalle.getCharge_id());

        db.insert(AppContract.Tables.COBRANZA_DETALLE, null, values);
        //Log.d("Valor Insertado", pedidoDetalle.toString());
    }


    //Insert COBRANZA_FORMA_PAGO
    public void insertCobranzaFormaPago(CobranzaFormaPago formaPago, Integer idCobranza){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.CobranzaFormaPago.idCobranza, idCobranza);
        values.put(AppContract.CobranzaFormaPago.payment_type, formaPago.getPayment_type());
        values.put(AppContract.CobranzaFormaPago.amount, formaPago.getAmount());
        values.put(AppContract.CobranzaFormaPago.bank, formaPago.getBank());
        values.put(AppContract.CobranzaFormaPago.check_number, formaPago.getCheck_number());
        values.put(AppContract.CobranzaFormaPago.expired_date, formaPago.getExpired_date());
        values.put(AppContract.CobranzaFormaPago.check_name, formaPago.getCheck_name());
        values.put(AppContract.CobranzaFormaPago.iscrossed, formaPago.getIscrossed());

        db.insert(AppContract.Tables.COBRANZA_FORMA_PAGO, null, values);
        //Log.d("Valor Insertado", pedidoDetalle.toString());
    }

    // Update
    public void updateCliente (Cliente cliente) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza Cliente : "+cliente.getNombre());

        ContentValues values = new ContentValues();
        values.put(AppContract.Cliente.id, cliente.getId());
        values.put(AppContract.Cliente.nombre, cliente.getNombre());
        values.put(AppContract.Cliente.ruc, cliente.getRuc());
        values.put(AppContract.Cliente.telefono, cliente.getTelefono());
        values.put(AppContract.Cliente.direccion, cliente.getDireccion());
        values.put(AppContract.Cliente.credito_disponible, cliente.getCredito_disponible());
        values.put(AppContract.Cliente.credito_extra, cliente.getCredito_extra());
        values.put(AppContract.Cliente.credito_usado, cliente.getCredito_usado());
        values.put(AppContract.Cliente.cheques_pend, cliente.getCheques_pend());
        values.put(AppContract.Cliente.factura_vieja, cliente.getFactura_vieja());
        values.put(AppContract.Cliente.plazo_max_cheque, cliente.getPlazomax());
        values.put(AppContract.Cliente.categoria_precio, cliente.getCategoria_precio());

        String[] whereArgs = { cliente.getId().toString() };
        int cant_row = db.update(AppContract.Tables.CLIENTE,
                values, "ID_CLIENTE = "+cliente.getId(), null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar CLIENTE. Se actualizo mas de 1 fila: "+cant_row);
        }
    }


    // Update
    public void updatePrecioVersion (PrecioVersion precioVersion) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza PrecioVersion id : "+precioVersion.getId());

        ContentValues values = new ContentValues();
        values.put(AppContract.PrecioVersion.id, precioVersion.getId());
        values.put(AppContract.PrecioVersion.m_product_id, precioVersion.getM_product_id());
        values.put(AppContract.PrecioVersion.precio_ventas_inicial, precioVersion.getPrecio_ventas_inicial());
        values.put(AppContract.PrecioVersion.precio_costo_inicial, precioVersion.getPrecio_costo_inicial());
        values.put(AppContract.PrecioVersion.precio_publico, precioVersion.getPrecio_publico());
        values.put(AppContract.PrecioVersion.precio_mayorista_a, precioVersion.getPrecio_mayorista_a());
        values.put(AppContract.PrecioVersion.precio_mayorista_b, precioVersion.getPrecio_mayorista_b());
        values.put(AppContract.PrecioVersion.precio_lista, precioVersion.getPrecio_lista());
        values.put(AppContract.PrecioVersion.precio_vidrieros, precioVersion.getPrecio_vidrieros());
        values.put(AppContract.PrecioVersion.precio_radiadoritas, precioVersion.getPrecio_radiadoritas());

        String[] whereArgs = { };
        int cant_row = db.update(AppContract.Tables.PRECIO_VERSION,
                values, AppContract.PrecioVersion.id +" = "+precioVersion.getId()
                + " AND " + AppContract.PrecioVersion.m_product_id + " = "+precioVersion.getM_product_id(), null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar PRECIO_VERSION. Se actualizo mas de 1 fila: "+cant_row);
        }
    }

    // Update
    public void updateProducto (Producto producto) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza Producto id : "+producto.getM_product_id());

        ContentValues values = new ContentValues();
        values.put(AppContract.Producto.m_product_id, producto.getM_product_id());
        values.put(AppContract.Producto.name, producto.getName());
        values.put(AppContract.Producto.price, producto.getPrice());
        values.put(AppContract.Producto.stock, producto.getStock());
        values.put(AppContract.Producto.codinterno, producto.getCodinterno());
        values.put(AppContract.Producto.id_familia, producto.getIdFamilia());
        values.put(AppContract.Producto.id_subfamilia, producto.getIdSubFamilia());

        String[] whereArgs = { };
        int cant_row = db.update(AppContract.Tables.PRODUCTO,
                values, AppContract.Producto.m_product_id +" = "+producto.getM_product_id(), null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar PRODUCTO. Se actualizo mas de 1 fila: "+cant_row);
        }

        //db.close();

    }

    // Update
    public void updateFactura (Factura factura) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza Factura id : "+factura.getId());

        ContentValues values = new ContentValues();
        values.put(AppContract.Factura.id, factura.getId());
        values.put(AppContract.Factura.isactive, factura.getIsactive());
        values.put(AppContract.Factura.order_id, factura.getOrder_id());
        values.put(AppContract.Factura.dateinvoiced, factura.getDateinvoiced());
        values.put(AppContract.Factura.client_id, factura.getClient_id());
        values.put(AppContract.Factura.grandtotal, factura.getGrandtotal());
        values.put(AppContract.Factura.ispaid, factura.getIspaid());
        values.put(AppContract.Factura.pend, factura.getPend());
        values.put(AppContract.Factura.nroFacturaImprimir, factura.getNroFacturaImprimir());

        String[] whereArgs = { };
        int cant_row = db.update(AppContract.Tables.FACTURA,
                values, AppContract.Factura.id +" = "+factura.getId(), null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar FACTURA. Se actualizo mas de 1 fila: "+cant_row);
        }
    }


    // Update
    public void updateRegistroVisita (RegistroVisita registroVisita) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza RegistroVisita : ");

        ContentValues values = new ContentValues();

        values.put(AppContract.RegistroVisita.tipo_visita, registroVisita.getTipo_visita());
        values.put(AppContract.RegistroVisita.ruc, registroVisita.getRuc());
        values.put(AppContract.RegistroVisita.cliente, registroVisita.getCliente());
        values.put(AppContract.RegistroVisita.latitude, registroVisita.getLatitude());
        values.put(AppContract.RegistroVisita.longitude, registroVisita.getLongitude());
        values.put(AppContract.RegistroVisita.observation, registroVisita.getObservation());
        values.put(AppContract.RegistroVisita.status, registroVisita.getStatus());
        values.put(AppContract.RegistroVisita.usuario, registroVisita.getUsuario());
        values.put(AppContract.RegistroVisita.fechavisita, registroVisita.getFechavisita());
        values.put(AppContract.RegistroVisita.horavisita, registroVisita.getHoravisita());
        values.put(AppContract.RegistroVisita.fecha_prox_visita, registroVisita.getFecha_prox_visita());
        values.put(AppContract.RegistroVisita.estado_envio, registroVisita.getEstado_envio());

        String[] whereArgs = { registroVisita.getFechavisita(), registroVisita.getHoravisita() };
        int cant_row = db.update(AppContract.Tables.REGISTRO_VISITA,
                values, "FECHA_VISITA=? AND HORA_VISITA=?", whereArgs);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar REGISTRO VISITA. Se actualizo mas de 1 fila: "+cant_row);
        }
    }

    public void updatePedido (Pedido pedido) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza Pedido : id="+pedido.getId() + " - estado: "+pedido.getEstado_envio());

        ContentValues values = new ContentValues();

        values.put(AppContract.Pedido.id, pedido.getId());
        values.put(AppContract.Pedido.ad_client_id, pedido.getAd_client_id());
        values.put(AppContract.Pedido.ad_org_id, pedido.getAd_org_id());
        values.put(AppContract.Pedido.isactive, pedido.getIsactive());
        values.put(AppContract.Pedido.date_order, pedido.getDate_order());
        values.put(AppContract.Pedido.order_id, pedido.getOrder_id());
        values.put(AppContract.Pedido.client_id, pedido.getClient_id());
        values.put(AppContract.Pedido.user_id, pedido.getUser_id());
        values.put(AppContract.Pedido.total, pedido.getTotal());
        values.put(AppContract.Pedido.observation, pedido.getObservation());
        values.put(AppContract.Pedido.estado_envio, pedido.getEstado_envio());

        String[] whereArgs = { "'"+ Integer.toString(pedido.getId()) +"'" };
        int cant_row = db.update(AppContract.Tables.PEDIDO,
                values, "ID="+pedido.getId(), null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar PEDIDO. Se actualizo mas de 1 fila: "+cant_row);
        }
    }

    // Update
    public void updateEntrega (Entrega entrega) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza Entrega : "+ entrega.toString());

        ContentValues values = new ContentValues();
        values.put(AppContract.Entrega.client_id, entrega.getClient_id());
        values.put(AppContract.Entrega.order_id, entrega.getOrder_id());
        values.put(AppContract.Entrega.user_id, entrega.getUser_id());
        values.put(AppContract.Entrega.date_delivered, entrega.getDate_delivered());
        values.put(AppContract.Entrega.time_delivered, entrega.getTime_delivered());
        values.put(AppContract.Entrega.observation, entrega.getObservation());
        values.put(AppContract.Entrega.estado_envio, entrega.getEstado_envio());

        String[] whereArgs = { entrega.getId().toString() };
        int cant_row = db.update(AppContract.Tables.ENTREGA, values, AppContract.Entrega.id+"="+entrega.getId(), null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar Entrega. Se actualizo mas de 1 fila: "+cant_row);
        }
    }

    // Update
    public void updateCobranza (Cobranza cobranza) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        //Log.d(TAG, "Se actualiza Cobranza : ");

        ContentValues values = new ContentValues();
        values.put(AppContract.Cobranza.id, cobranza.getId());
        values.put(AppContract.Cobranza.client_id, cobranza.getClient_id());
        values.put(AppContract.Cobranza.user_id, cobranza.getUser_id());
        values.put(AppContract.Cobranza.amount, cobranza.getAmount());
        values.put(AppContract.Cobranza.invoice_number, cobranza.getInvoice_number());
        values.put(AppContract.Cobranza.observation, cobranza.getObservation());
        values.put(AppContract.Cobranza.status, cobranza.getStatus());
        values.put(AppContract.Cobranza.nombre_cliente, cobranza.getNombre_cliente());
        values.put(AppContract.Cobranza.nombre_vendedor, cobranza.getNombre_vendedor());
        values.put(AppContract.Cobranza.estado_envio, cobranza.getEstado_envio());

        String[] whereArgs = { cobranza.getId().toString() };
        int cant_row = db.update(AppContract.Tables.COBRANZA, values,
                AppContract.Cobranza.id+"="+cobranza.getId(),
                null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar Cobranza. Se actualizo mas de 1 fila: "+cant_row);
        }
    }

    public void updateRutaLocation (RutaLocation rutaLocation) {

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Log.d(TAG, "Se actualiza RutaLocation : "+ rutaLocation.toString());

        ContentValues values = new ContentValues();

        values.put(AppContract.RutaLocation.id, rutaLocation.getId());
        values.put(AppContract.RutaLocation.date, rutaLocation.getDate());
        values.put(AppContract.RutaLocation.user_id, rutaLocation.getUser_id());
        values.put(AppContract.RutaLocation.latitude, rutaLocation.getLatitude());
        values.put(AppContract.RutaLocation.longitude, rutaLocation.getLongitude());
        values.put(AppContract.RutaLocation.client_id, rutaLocation.getClient_id());
        values.put(AppContract.RutaLocation.zone, rutaLocation.getZone());
        values.put(AppContract.RutaLocation.priority, rutaLocation.getPriority());
        values.put(AppContract.RutaLocation.status, rutaLocation.getStatus());
        values.put(AppContract.RutaLocation.observation, rutaLocation.getObservation());
        values.put(AppContract.RutaLocation.entrada, rutaLocation.getEntrada());
        values.put(AppContract.RutaLocation.salida, rutaLocation.getSalida());
        values.put(AppContract.RutaLocation.fechaHoraEntrada, rutaLocation.getFechaHoraEntrada());
        values.put(AppContract.RutaLocation.fechaHoraSalida, rutaLocation.getFechaHoraSalida());
        values.put(AppContract.RutaLocation.estadoEnvio, rutaLocation.getEstadoEnvio());

        String[] whereArgs = { rutaLocation.getId().toString() };
        int cant_row = db.update(AppContract.Tables.RUTA_LOCATION, values, AppContract.RutaLocation.id+"="+rutaLocation.getId(), null);
        if (cant_row != 1){
            //throw new RuntimeException("");
            Log.e(TAG, "Error al actualizar RutaLocation. Se actualizo mas de 1 fila: "+cant_row);
        }
    }


    //Insert CONFIGURACION
    public void insertConfiguracion (Configuracion configuracion){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppContract.Configuracion.clave, configuracion.getClave());
        values.put(AppContract.Configuracion.valor, configuracion.getValor());
        db.insert(AppContract.Tables.CONFIGURACION, null, values);
        Log.d("Valor Insertado", configuracion.toString());
    }


    // Deletes
    public void deleteProductoFamilia(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PRODUCTO_FAMILIA, null, null);
        //db.close();
    }

    // Deletes
    public void deleteEntrega(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.ENTREGA, null, null);
        //db.close();
    }
    // Deletes
    public void deleteRegistroVisita(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.REGISTRO_VISITA, null, null);
        //db.close();
    }

    public void deleteProductoSubFamilia(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PRODUCTO_SUB_FAMILIA, null, null);
        //db.close();
    }

    // Deletes
    public void deleteRutaLocation(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.RUTA_LOCATION, null, null);
        //db.close();
    }

    // Deletes
    public void deleteProductoImagen(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PRODUCTO_IMAGEN, null, null);
        //db.close();
    }

    // Deletes
    public void deleteLogin(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.LOGIN, null, null);
        //db.close();
    }

    // Deletes
    public void deleteCliente(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.CLIENTE, null, null);
        //db.close();
    }

    // Deletes
    public void deleteConfiguracion(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.CONFIGURACION, null, null);
        //db.close();
    }

    // Deletes
    public void deleteConfiguracionLastUpdated(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.CONFIGURACION, " CLAVE!='URL'", null);
        //db.close();
    }


    // Deletes
    public void deleteSession(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.SESSION, null, null);
        //db.close();
    }

    public void deleteUsuario(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.USUARIO, null, null);
        //db.close();
    }

    public void deletePrecioCategoria(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PRECIO_CATEGORIA, null, null);
        //db.close();
    }

    public void deletePrecioVersion(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PRECIO_VERSION, null, null);
        //db.close();
    }

    // Deletes
    public void deleteConfiguracionByClave(String clave){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.CONFIGURACION, "CLAVE=?",  new String[]{clave});
        //db.close();
    }

    // Deletes
    public void deleteLocation(LocationTable locationTable){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int result = db.delete(AppContract.Tables.LOCATION, "LATITUDE='"+locationTable.getLatitude()+"' AND LONGITUDE='"+locationTable.getLongitude()+"'"
                ,null);
       //db.close();
    }

    public void deleteProducto(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PRODUCTO, null, null);
        //db.close();
    }

    public void deletePedido(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PEDIDO, null, null);
        //db.close();
    }

    public void deletePedidoSinEstado(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PEDIDO, null, null);
       // db.close();
    }

    public void deletePedidoDetalle(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.PEDIDO_DETALLE, null, null);
        //db.close();
    }

    public void deletePedidoDetalleByIdPedido(Integer idPedido){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count = db.delete(AppContract.Tables.PEDIDO_DETALLE, AppContract.PedidoDetalle.order_id+"="+idPedido, null);
        Log.d(TAG, "total de detalles eliminados: "+count);
       // db.close();
    }

    public void deleteCobranza(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.COBRANZA, null, null);
        //db.close();
    }

    public void deleteFactura(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.FACTURA, null, null);
        //db.close();
    }

    public void deleteStockProducto(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.STOCK_PRODUCTO, null, null);
    }

    public void deleteStockProductoByID(Integer m_product_id){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.delete(AppContract.Tables.STOCK_PRODUCTO, AppContract.StockProducto.m_product_id+"='"+m_product_id+"'", null);
    }

    //Cuenta las filas de una tabla
    public int count(Cursor c){
        int result = 0;
        if(c != null){
            c.moveToFirst();
            if (c.getCount() > 0 && c.getColumnCount() > 0) {
                result = c.getInt(0);
                Log.d("count",result + "");
                c.close();
                return result;
            } else {
                c.close();
                return result;
            }
        } else{
            return result;
        }
    }

    //Count Cobranza
    public int countCobranza(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.COBRANZA, null);
        return count(cursor);
    }

    //Count Product
    public int countProduct(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.PRODUCTO, null);
        return count(cursor);
    }

    //Count cliente
    public int countCliente(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.CLIENTE, null);
        return count(cursor);
    }

    //Count cliente
    public int countFactura(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.FACTURA, null);
        return count(cursor);
    }

    //Count precio_version
    public int countPrecioVersion(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.PRECIO_VERSION, null);
        return count(cursor);
    }

    //Count pedido
    public int countPedido(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.PEDIDO, null);
        return count(cursor);
    }

    //Count Stock de Producto
    public int countStockProducto(){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.STOCK_PRODUCTO, null);
        return count(cursor);
    }

    //Count Stock de Producto Por ID
    public int countStockProductoByID(int m_product_id){
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + AppContract.Tables.STOCK_PRODUCTO+" where M_PRODUCT_ID="+m_product_id, null);
        return count(cursor);
    }

    /**
     * This creates/opens the database.
     */
    public class DictionaryOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        public SQLiteDatabase mDatabase;

        DictionaryOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creando las tablas..");
            mDatabase = db;

            //TABLA LOGIN
            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.LOGIN + " USING fts3 ("
                            + AppContract.Login.userName + ", "
                            + AppContract.Login.sessionID + ", "
                            + AppContract.Login.status + "); "
            );
            Log.d("Creo tabla","LOGIN");

            //TABLA CLIENTE
            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.CLIENTE + " USING fts3 ("
                            + AppContract.Cliente.id + ", "
                            + AppContract.Cliente.ruc + ", "
                            + AppContract.Cliente.nombre + ", "
                            + AppContract.Cliente.direccion + ", "
                            + AppContract.Cliente.telefono + ", "
                            + AppContract.Cliente.credito_disponible + ", "
                            + AppContract.Cliente.credito_extra + ", "
                            + AppContract.Cliente.credito_usado + ", "
                            + AppContract.Cliente.cheques_pend + ", "
                            + AppContract.Cliente.factura_vieja + ","
                            + AppContract.Cliente.categoria_precio + ","
                            + AppContract.Cliente.plazo_max_cheque+"); "
            );
            Log.d("Creo tabla","CLIENTE");

            //TABLA CONFIGURACION
            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.CONFIGURACION + " USING fts3 ("
                            + AppContract.Configuracion.clave + ", "
                            + AppContract.Configuracion.valor + "); "
            );
            Log.d("Creo tabla","CONFIGURACION");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.REGISTRO_VISITA + " USING fts3 ("
                            + AppContract.RegistroVisita.tipo_visita + ", "
                            + AppContract.RegistroVisita.ruc + ", "
                            + AppContract.RegistroVisita.cliente + ", "
                            + AppContract.RegistroVisita.latitude + ", "
                            + AppContract.RegistroVisita.longitude + ", "
                            + AppContract.RegistroVisita.observation + ", "
                            + AppContract.RegistroVisita.status + ", "
                            + AppContract.RegistroVisita.usuario + ", "
                            + AppContract.RegistroVisita.horavisita + ", "
                            + AppContract.RegistroVisita.estado_envio + ", "
                            + AppContract.RegistroVisita.fecha_prox_visita + ", "
                            + AppContract.RegistroVisita.ent_sal + ", "
                            + AppContract.RegistroVisita.nombre_usuario + ", "
                            + AppContract.RegistroVisita.fechavisita + "); "
            );
            Log.d("Creo tabla","REGISTRO VISITA");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.SESSION + " USING fts3 ("
                            + AppContract.Session.userId + ", "
                            + AppContract.Session.email + ", "
                            + AppContract.Session.root + ", "
                            + AppContract.Session.admin + ", "
                            + AppContract.Session.session + "); "
            );
            Log.d("Creo tabla","SESSION");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.LOCATION + " USING fts3 ("
                            + AppContract.Location.id + " INTEGER primary key autoincrement, "
                            + AppContract.Location.latitude + ", "
                            + AppContract.Location.longitude + ", "
                            + AppContract.Location.date + ", "
                            + AppContract.Location.time + ", "
                            + AppContract.Location.estado_envio + ", "
                            + AppContract.Location.id_user + "); "
            );
            Log.d("Creo tabla","LOCATION");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.USUARIO + " USING fts3 ("
                            + AppContract.Usuario.id + ", "
                            + AppContract.Usuario.mail + ", "
                            + AppContract.Usuario.name + ", "
                            + AppContract.Usuario.lastname + ", "
                            + AppContract.Usuario.password + ", "
                            + AppContract.Usuario.state + ", "
                            + AppContract.Usuario.role + ", "
                            + AppContract.Usuario.userCellphone + "); "
            );
            Log.d("Creo tabla","USUARIO");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.ENTREGA + " USING fts3 ("
                            + AppContract.Entrega.id + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                            + AppContract.Entrega.client_id + ", "
                            + AppContract.Entrega.user_id + ", "
                            + AppContract.Entrega.order_id + ", "
                            + AppContract.Entrega.date_delivered + ", "
                            + AppContract.Entrega.time_delivered + ", "
                            + AppContract.Entrega.observation + ", "
                            + AppContract.Entrega.nombre_cliente + ", "
                            + AppContract.Entrega.nombre_vendedor + ", "
                            + AppContract.Entrega.estado_envio + "); "
            );
            Log.d("Creo tabla","ENTREGA");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PRODUCTO + " USING fts3 ("
                            + AppContract.Producto.m_product_id + ", "
                            + AppContract.Producto.name + ", "
                            + AppContract.Producto.price + ", "
                            + AppContract.Producto.stock+ ", "
                            + AppContract.Producto.id_familia+ ", "
                            + AppContract.Producto.id_subfamilia+ ", "
                            + AppContract.Producto.codinterno + "); "
            );
            Log.d("Creo tabla","PRODUCTO");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PEDIDO + " USING fts3 ("
                            + AppContract.Pedido.id + ", "
                            + AppContract.Pedido.ad_client_id + ", "
                            + AppContract.Pedido.ad_org_id + ", "
                            + AppContract.Pedido.isactive + ", "
                            + AppContract.Pedido.date_order + ", "
                            + AppContract.Pedido.order_id + ", "
                            + AppContract.Pedido.client_id + ", "
                            + AppContract.Pedido.user_id + ", "
                            + AppContract.Pedido.total + ", "
                            + AppContract.Pedido.estado_envio + ", "
                            + AppContract.Pedido.isinvoiced + ", "
                            + AppContract.Pedido.observation + "); "
            );
            Log.d("Creo tabla","PEDIDO");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PEDIDO_DETALLE + " USING fts3 ("
                            + AppContract.PedidoDetalle.id + ", "
                            + AppContract.PedidoDetalle.isactive + ", "
                            + AppContract.PedidoDetalle.product_id + ", "
                            + AppContract.PedidoDetalle.quantity + ","
                            + AppContract.PedidoDetalle.price + ","
                            + AppContract.PedidoDetalle.total + ","
                            + AppContract.PedidoDetalle.observation + ","
                            + AppContract.PedidoDetalle.order_id + "); "
            );
            Log.d("Creo tabla","PEDIDO_DETALLE");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PRECIO_CATEGORIA + " USING fts3 ("
                            + AppContract.PrecioCategoria.rowid + ", "
                            + AppContract.PrecioCategoria.m_pricelist_version_id + ", "
                            + AppContract.PrecioCategoria.m_pricelist_id + ", "
                            + AppContract.PrecioCategoria.name + ", "
                            + AppContract.PrecioCategoria.active + ", "
                            + AppContract.PrecioCategoria.ad_client_id  + "); "
            );
            Log.d("Creo tabla","PRECIO_CATEGORIA");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PRECIO_VERSION+ " USING fts3 ("
                            + AppContract.PrecioVersion.id + ", "
                            + AppContract.PrecioVersion.m_product_id + ", "
                            + AppContract.PrecioVersion.precio_ventas_inicial + ", "
                            + AppContract.PrecioVersion.precio_costo_inicial + ", "
                            + AppContract.PrecioVersion.precio_publico + ", "
                            + AppContract.PrecioVersion.precio_mayorista_a + ", "
                            + AppContract.PrecioVersion.precio_mayorista_b + ", "
                            + AppContract.PrecioVersion.precio_lista + ", "
                            + AppContract.PrecioVersion.precio_vidrieros + ", "
                            + AppContract.PrecioVersion.precio_radiadoritas + "); "

            );
            Log.d("Creo tabla","PRECIO_VERSION");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.RUTA_LOCATION+ " USING fts3 ("
                            + AppContract.RutaLocation.id + ", "
                            + AppContract.RutaLocation.date + ", "
                            + AppContract.RutaLocation.user_id + ", "
                            + AppContract.RutaLocation.latitude + ", "
                            + AppContract.RutaLocation.longitude + ", "
                            + AppContract.RutaLocation.client_id + ", "
                            + AppContract.RutaLocation.zone + ", "
                            + AppContract.RutaLocation.priority + ", "
                            + AppContract.RutaLocation.status + ", "
                            + AppContract.RutaLocation.entrada + ", "
                            + AppContract.RutaLocation.salida + ", "
                            + AppContract.RutaLocation.fechaHoraEntrada + ", "
                            + AppContract.RutaLocation.fechaHoraSalida + ", "
                            + AppContract.RutaLocation.type + ", "
                            + AppContract.RutaLocation.observation + ", "
                            +AppContract.RutaLocation.estadoEnvio+");");

            Log.d("Creo tabla","RUTA_LOCATION");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.COBRANZA+ " USING fts3 ("
                            + AppContract.Cobranza.id + ", "
                            + AppContract.Cobranza.client_id + ", "
                            + AppContract.Cobranza.user_id + ", "
                            + AppContract.Cobranza.amount + ", "
                            + AppContract.Cobranza.invoice_number + ", "
                            + AppContract.Cobranza.observation + ", "
                            + AppContract.Cobranza.status + ", "
                            + AppContract.Cobranza.nombre_cliente + ", "
                            + AppContract.Cobranza.nombre_vendedor + ", "
                            + AppContract.Cobranza.estado_envio + "); "
            );
            Log.d("Creo tabla","COBRANZA");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.COBRANZA_DETALLE+ " USING fts3 ("
                            + AppContract.CobranzaDetalle.amount + ", "
                            + AppContract.CobranzaDetalle.cashed + ", "
                            + AppContract.CobranzaDetalle.invoice + ", "
                            + AppContract.CobranzaDetalle.charge_id +  "); "
            );
            Log.d("Creo tabla","COBRANZA_DETALLE");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PRODUCTO_IMAGEN+ " USING fts3 ("
                            + AppContract.ProductoImagen.m_product_id + ", "
                            + AppContract.ProductoImagen.img + ", "
                            + AppContract.ProductoImagen.estado_envio + ", "
                            + AppContract.ProductoImagen.size + "); "
            );
            Log.d("Creo tabla","PRODUCTO_IMAGEN");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.FACTURA+ " USING fts3 ("
                            + AppContract.Factura.id + ", "
                            + AppContract.Factura.isactive + ", "
                            + AppContract.Factura.order_id + ", "
                            + AppContract.Factura.dateinvoiced + ", "
                            + AppContract.Factura.client_id + ", "
                            + AppContract.Factura.grandtotal + ", "
                            + AppContract.Factura.ispaid + ", "
                            + AppContract.Factura.nroFacturaImprimir + ", "
                            + AppContract.Factura.pend + "); "
            );
            Log.d("Creo tabla","FACTURA");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PRODUCTO_FAMILIA+ " USING fts3 ("
                            + AppContract.ProductoFamilia.m_product_family_id + ", "
                            + AppContract.ProductoFamilia.value + ", "
                            + AppContract.ProductoFamilia.description +  "); "
            );
            Log.d("Creo tabla","PRODUCTO_FAMILIA");

            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.PRODUCTO_SUB_FAMILIA+ " USING fts3 ("
                            + AppContract.ProductoSubFamilia.id + ", "
                            + AppContract.ProductoSubFamilia.value + ", "
                            + AppContract.ProductoSubFamilia.description + ", "
                            + AppContract.ProductoSubFamilia.id_familia +  "); "
            );
            Log.d("Creo tabla","PRODUCTO_SUB_FAMILIA");


            db.execSQL("CREATE TABLE " +AppContract.Tables.STOCK_PRODUCTO+" ("
                    +" id INTEGER PRIMARY KEY AUTOINCREMENT ,"+""
                    +AppContract.StockProducto.m_product_id+","
                    +AppContract.StockProducto.desc_m_product_id+","
                    +AppContract.StockProducto.ad_org_id+","
                    +AppContract.StockProducto.desc_ad_org+","
                    +AppContract.StockProducto.m_locator_id+","
                    +AppContract.StockProducto.desc_m_locator+","
                    +AppContract.StockProducto.stock_disponible+");");

            Log.d("Crear tabla", "STOCK_PRODUCTO");

            db.execSQL("CREATE UNIQUE INDEX idx_stock_producto ON " +AppContract.Tables.STOCK_PRODUCTO+" (id);");


            db.execSQL("CREATE VIRTUAL TABLE " + AppContract.Tables.COBRANZA_FORMA_PAGO+ " USING fts3 ("
                    + AppContract.CobranzaFormaPago.idCobranza + ", "
                    + AppContract.CobranzaFormaPago.payment_type + ", "
                    + AppContract.CobranzaFormaPago.amount + ", "
                    + AppContract.CobranzaFormaPago.bank + ", "
                    + AppContract.CobranzaFormaPago.check_number + ", "
                    + AppContract.CobranzaFormaPago.expired_date + ", "
                    + AppContract.CobranzaFormaPago.check_name + ", "
                    + AppContract.CobranzaFormaPago.iscrossed +  "); "
            );
            Log.d("Creo tabla","COBRANZA_FORMA_PAGO");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.LOGIN);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.CLIENTE);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.CONFIGURACION);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.REGISTRO_VISITA);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.SESSION);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.LOCATION);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.USUARIO);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.ENTREGA);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PRODUCTO);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PEDIDO);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PEDIDO_DETALLE);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PRECIO_CATEGORIA);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PRECIO_VERSION);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.RUTA_LOCATION);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.COBRANZA);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PRODUCTO_IMAGEN);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.COBRANZA_DETALLE);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.FACTURA);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PRODUCTO_FAMILIA);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.PRODUCTO_SUB_FAMILIA);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.STOCK_PRODUCTO);
            db.execSQL("DROP TABLE IF EXISTS " + AppContract.Tables.COBRANZA_FORMA_PAGO);

            onCreate(db);
        }
    }
}
