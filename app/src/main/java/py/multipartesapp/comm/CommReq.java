package py.multipartesapp.comm;

public class CommReq {

	private static final String App = "multip/api/";

    private static final String Teckel = "erp/api/";

	public static final String CommReqLogin
            = "j_spring_security_check";

    public static final String CommReqGetAllClients
            = App + "client/summary";

    public static final String CommReqSincronizarClientes
            = App + "client/sincronizar";

    public static final String CommReqSendVisita
            = App + "visit/save/params";

    public static final String CommReqGetUserLoged
            = App + "user/current";

    public static final String CommReqSendLocation
            = App + "location/save";

    public static final String CommReqGetAllUsers
            = App + "user/admin/users";

    public static final String CommReqGetAllUsersTeckel
            = Teckel + "users";

    public static final String CommReqSendDelivery
            = App + "delivery/save";

    public static final String CommReqGetAllOrders
            = App + "order/summary";

    public static final String CommReqEnviarPedido=App+"/order/save";

    public static final String CommReqGetAllProduct
            = App + "product/summary";

    public static final String CommReqSincronizarProductos
            = App + "product/sincronizar";

    public static final String CommReqGetAllPrecioCategoria
            = App + "product/price/version/list";

    public static final String CommReqGetAllPrecioVersion
            = App + "product/price/summary";

    public static final String CommReqSincronizarPrecios
            = App + "product/price/summary/new";

    public static final String CommReqGetAllRoutes
            = App + "routes/sincronizar/hoja-ruta/TODOS";

    public static final String CommReqPostUpdateRoute
            = App + "routes/update";

    public static final String CommReqGetAllCobros
            = App + "charge/summary";

    public static final String CommReqGetSendCobros
            = App + "charge/save";

    public static final String CommReqGetAllProductImages
            = App + "product/img/summary";

    public static final String CommReqGetAllFacturas
            = App + "invoice/summary";


    public static final String CommReqSincronizarFacturas
            = App + "invoice/sincronizar";
    public static final String CommReqPostSaveRoute
            = App + "routes/save";

    public static final String CommReqGetAllProductFamily
            = App + "product/family/summary";

    public static final String CommReqGetAllProductSubFamily
            = App + "product/subfamily/summary";

    public static final String CommReqGetProductImageFile
            = App + "product/img/file/";

    public static final String CommReqGetRegistroVisita
            = App + "visit/user/";


    public static final String CommReqGetAllEntrega
            = App + "delivery/summary";

    public static final String CommReqGetStockProducto=App+"product/stock-producto/";

    //Reportes

    public static final String  CommReqConsultaEstadoCuenta="api/report/extracto";

    public static final String CommReqConsultaPedidos="api/report/pedidos";





}
