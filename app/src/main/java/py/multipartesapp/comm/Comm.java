
package py.multipartesapp.comm;

//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Multimap;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import py.multipartesapp.beans.Bean;
import py.multipartesapp.beans.ClienteList;
import py.multipartesapp.beans.CobranzaList;
import py.multipartesapp.beans.EntregaList;
import py.multipartesapp.beans.FacturaList;
import py.multipartesapp.beans.Login;
import py.multipartesapp.beans.PedidoList;
import py.multipartesapp.beans.PrecioCategoria;
import py.multipartesapp.beans.PrecioCategoriaList;
import py.multipartesapp.beans.PrecioVersionList;
import py.multipartesapp.beans.ProductoFamiliaList;
import py.multipartesapp.beans.ProductoImagenList;
import py.multipartesapp.beans.ProductoList;
import py.multipartesapp.beans.ProductoSubFamiliaList;
import py.multipartesapp.beans.RegistroVisitaList;
import py.multipartesapp.beans.RutaLocationList;
import py.multipartesapp.beans.Session;
import py.multipartesapp.beans.UsuarioList;
import py.multipartesapp.comm.Http.HttpException;
import py.multipartesapp.comm.Http.HttpResponse;
import py.multipartesapp.comm.Http.Logger;
import py.multipartesapp.utils.Globals;
import py.multipartesapp.utils.Hex;


public class Comm extends Application{

    private static Context context;

    public static HashMap<String,String> extras = new HashMap<String,String>();
    private static String password = "elefante_login_num1";
    private static String clientSecret = "zPaBp4G84kQmLbMeG1tcaE4/6N2tMeIu7nzBzKFQbxLrmY/wLg5uK8pprthHqTspsVaBC0YkP7HKbeewdt/UwmD+Sj+PF4trJ/0di1uZe6+/4Ulk0oY9E7X0nlUqmKiyiGABPKGHmVz7kZfd8M6Vr4/zohS5iWtJSJCSVfNujHzHsE+YP9/VDFlCwhZzC6V/kIKk74+lUJDpV/36jlWj0Q==";
    private static MessageDigest _md_sha1;


 //   public  static String URL = "http://192.168.0.138:8080/multipartes/";
   // public  static String URL = "http://192.168.0.138:8080/";
    public  static String URL = "http://app.multipartes.com.py/";

    public void onCreate(){
        super.onCreate();
        Comm.context = getApplicationContext();
    }
    public static Context getAppContext() {
        return Comm.context;
    }

    public Comm(){
    }
    public Comm(HashMap extras){
        this.extras.putAll(extras);
    }

    static {
        if (CookieManager.getDefault() == null){
            CookieHandler.setDefault(new CookieManager());
        }
        try {
            _md_sha1 = MessageDigest.getInstance("sha1");
        } catch (NoSuchAlgorithmException e) {
            // just ignore for now
        }
    }

    public static String generateSign(String tstamp, String deviceId) {
        String ssign = tstamp+","+deviceId+","+clientSecret+","+password;
        return getSHA1HexEncoded(ssign.getBytes());
    }

    public interface CommAccessTokenDelegate{
		public void commAccessTokenInvalid(Comm comm, CommException ex);
	}

	private static final String[][] _classByReq = {

        {CommReq.CommReqLogin, Login.class.getName()},
        {CommReq.CommReqGetAllClients, ClienteList.class.getName()},
        {CommReq.CommReqGetAllClients+"/TODOS", ClienteList.class.getName()},
        {CommReq.CommReqGetUserLoged, Session.class.getName()},
        {CommReq.CommReqGetAllUsers, UsuarioList.class.getName()},
        {CommReq.CommReqGetAllOrders, PedidoList.class.getName()},
        {CommReq.CommReqGetAllProduct, ProductoList.class.getName()},
        {CommReq.CommReqGetAllPrecioCategoria, PrecioCategoriaList.class.getName()},
        {CommReq.CommReqGetAllPrecioVersion+"/TODOS", PrecioVersionList.class.getName()},
        {CommReq.CommReqGetAllRoutes, RutaLocationList.class.getName()},
        {CommReq.CommReqGetAllCobros, CobranzaList.class.getName()},
        {CommReq.CommReqGetAllFacturas, FacturaList.class.getName()},
        {CommReq.CommReqGetAllProductFamily, ProductoFamiliaList.class.getName()},
        {CommReq.CommReqGetAllProductSubFamily, ProductoSubFamiliaList.class.getName()},
		{CommReq.CommReqGetAllProductImages, ProductoImagenList.class.getName()},
		{CommReq.CommReqGetRegistroVisita, RegistroVisitaList.class.getName()},
		{CommReq.CommReqGetAllEntrega, EntregaList.class.getName()},


	};
	private static final Map classByReq = toMap(_classByReq);


    public static class CommException extends Exception {

		private String errCode;
        private String userMessage;

        private static final long serialVersionUID = 1L;

        public CommException(String msg, String errCode, Throwable e) {
            super(msg,e);
            this.errCode = errCode;
        }

        public CommException(String msg, String errCode, Throwable e, String userMessage) {
            super(msg,e);
            this.errCode = errCode;
            this.userMessage = userMessage;
        }

        public String getUserMessage() {
            return userMessage;
        }

        public void setUserMessage(String userMessage) {
            this.userMessage = userMessage;
        }

		public String getErrCode() {
			return errCode;
		}

	}
	
	public static class CommResponse {
		private Bean bean;
		
		private Object object;
		
		public CommResponse(Object object) {
			this.object = object;
		}
		
		public Object getObject() {
			return object;
		}
		
		public CommResponse(Bean bean) {
			super();
			this.bean = bean;
		}

		public Bean getBean() {
			return bean;
		}

	}

	public abstract static class CommDelegate {

		protected CommException exception;
		protected CommResponse response;
		protected Comm comm;
		protected Logger logger;
		protected ICommExecutor executor;
		
		public Logger getLogger() {
			return logger;
		}
		public ICommExecutor getExecutor() {
			return executor;
		}

		public abstract void onError();

		public abstract void onSuccess();

	}

	private static abstract class CommRunnable {

		public CommRunnable() {

		}

		public abstract CommResponse run() throws CommException;

	}

	private String respFormat = "json";
	private String req;

	
//	public enum AppErrorCode {
//		ErrorCodeHttpConnectionFailed;
//	};
	
	public static final String ErrorCodeHttpConnectionFailed = "ErrorCodeHttpConnectionFailed";
	
	private String post(CommDelegate delegate, String req, String url, Map params) throws CommException, JSONException {
		try {
			this.req = req;
			delegate.getLogger().d("Comm","--Start Req = "+req+", Url = "+url+", Params = "+params);
			Http.HttpResponse response = Http.post(url, params);
			delegate.getLogger().d("Comm","--End Req = "+req+", Code = "+response.getCode()+", Response = "+response.getResponse());
			checkResponseForErrors(response);
			return response.getResponse(); 
		} catch (HttpException e) {
			e.printStackTrace();
			throw new CommException("Disculpe, ocurrió un error de conexión. " +
					"Asegurese que tenga una conexión a internet y vuelva a intentar."
					,ErrorCodeHttpConnectionFailed.toString(),e);
        }
    }
    /*
    private String post2 (CommDelegate delegate, String req, String url, Multimap params) throws CommException, JSONException {
        try {
            this.req = req;
            delegate.getLogger().d("Comm","--Start Req = "+req+", Url = "+url+", Params = "+params);
            Http.HttpResponse response = Http.post2(url, params);
            delegate.getLogger().d("Comm","--End Req = "+req+", Code = "+response.getCode()+", Response = "+response.getResponse());
            checkResponseForErrors(response);
            return response.getResponse();
        } catch (HttpException e) {
            e.printStackTrace();
            throw new CommException("Disculpe, ocurrió un error de conexión. " +
                    "Asegurese que tenga una conexión a internet y vuelva a intentar."
                    ,ErrorCodeHttpConnectionFailed.toString(),e);
        }
    }
    */
    private String get(CommDelegate delegate, String req, String url, Map params) throws CommException, JSONException {
        String finalUrl = url;
        try {
            this.req = req;
            if (params.get("path_variable") != null)
            {
                finalUrl = finalUrl + "/" + params.get("path_variable").toString();
                params.remove("path_variable");
            }
            delegate.getLogger().d("Comm","--Start Req = "+req+", Url = "+finalUrl+", Params = "+params);
            HttpResponse response = Http.get(finalUrl, params);
            delegate.getLogger().d("Comm","--End Req = "+req+", Code = "+response.getCode()+", Response = "+response.getResponse());
            checkResponseForErrors(response);
            return response.getResponse();
        } catch (HttpException e) {
            e.printStackTrace();
            throw new CommException("Disculpe, ocurrió un error de conexión. " +
                    "Asegurese que tenga una conexión a internet y vuelva a intentar."
                    ,ErrorCodeHttpConnectionFailed.toString(),e);
        }
    }


	private String post(CommDelegate delegate, String req, String string) throws CommException, JSONException {
		return post(delegate, req,string,null);
	}


	

	private void checkResponseForErrors(HttpResponse response) throws CommException, JSONException {

		int code = response.getCode();

        Globals.setLast_request_code(code);

        if( code<200||code>299 ) {
			JSONObject err = new JSONObject(response.getResponse());
			if (true) { //TODO: ALGUNA CONDICION??
				String errCode = err.getString("code");
				String errMsg = err.getString("msg");
				String userMessage = err.getString("type");
				throw new CommException(errMsg, errCode, null, userMessage);
			}
			throw new CommException("Disculpe, ocurrió un error inesperado", null, null);
		}
		
	}

//	private String getUnexpectedErrMsg() {
//		return "Disculpe los inconvenientes, ocurriÔøΩ un error inesperado";
//	}
//	static Properties messages;
//	static Object mMutex = new Object();

	private String addDebugMsgToErrMsg(JSONObject err, String errCode,String errMsg){
		
		
		String key = "msg";
		errMsg = (errMsg!=null?errMsg+" ":"")+"[code = "+errCode+"], [msg = "+getString(err,key)+"]";
	
		return errMsg;
	}
	
	public static String getString(JSONObject json, String string) {
		try {
			return json.getString(string);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

    private static Map toMap(final Object[][] params) {
        if( params == null ){
            return null;
        }

        Map hash = new HashMap();
        for (int i = 0; i < params.length; i++) {
            Object []param = params[i];
            hash.put(param[0],param[1]);
        }
        return hash;
    }

    /*
	private static Multimap toMap2(final Object[][] params) {
		if( params == null ){
			return null;
		}
        Multimap multimap = ArrayListMultimap.create();
		for (int i = 0; i < params.length; i++) {
			Object []param = params[i];
			//hash.put(param[0],param[1]);
            multimap.put(param[0],param[1]);
		}
		return multimap;
	}
    */
	
	
	private static String[]_justTestOk = {
//		CommReqPasswordChange
		
	};

    public static byte[] getSHA1(byte[] message)
    {
        byte[] ret = null;
        synchronized(_md_sha1) {
            ret = _md_sha1.digest(message);
        }
        return ret;
    }

    public static String getSHA1HexEncoded(byte[] message)
    {
        byte[] ret = getSHA1(message);
        return Hex.encode(ret);
    }

	public void requestPost(final String req, final Object[][] params, final CommDelegate delegate){
		Map hash = toMap(params);
		request(req, hash, delegate);
	}

    /*
    public void requestPost2(final String req, final Object[][] params, final CommDelegate delegate){
        Multimap multimap = toMap2(params);
        request2(req, multimap, delegate);
    }
	*/
	public void request(final String req, final Map params, final CommDelegate delegate){
		
		execute(new CommRunnable() {
			
			public CommResponse run() throws CommException {

				String commandURL = req;
				commandURL = sanitateUrl(commandURL);
				String response;
				Object object = null;
				try {
					response = post(delegate, req, URL+commandURL,params);
					object = objectFromResponse(req,response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if( object instanceof Bean ){
					return new CommResponse((Bean) object);
				} else {
					return new CommResponse(object);
				}
			}			
		}, delegate);
	}
    /*
    public void request2(final String req, final Multimap params, final CommDelegate delegate){

        execute(new CommRunnable() {

            public CommResponse run() throws CommException {

                String commandURL = req;
                commandURL = sanitateUrl(commandURL);
                String response;
                Object object = null;
                try {
                    response = post2(delegate, req, URL+commandURL,params);
                    object = objectFromResponse(req,response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if( object instanceof Bean ){
                    return new CommResponse((Bean) object);
                } else {
                    return new CommResponse(object);
                }
            }
        }, delegate);
    }
    */
    public void requestGet(final String req, final Object[][]params, final CommDelegate delegate){
        Map hash = toMap(params);
        requestGet(req, hash, delegate);
    }

    public void requestGet(final String req, final Map params, final CommDelegate delegate){

        execute(new CommRunnable() {

            public CommResponse run() throws CommException {

                String commandURL = req;
                commandURL = sanitateUrl(commandURL);
                String response;
                Object object = null;
                try {
                    response = get(delegate, req, URL + commandURL, params);
                    object = objectFromResponse(req,response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if( object instanceof Bean ){
                    return new CommResponse((Bean) object);
                } else {
                    return new CommResponse(object);
                }
            }
        }, delegate);
    }
	
	
	private static Object objectFromResponse(String req, String response) {

        String clazzName = (String) classByReq.get(req);
        Boolean contain = classByReq.containsValue(clazzName);
        //if (!classByReq.containsValue(req)){
        if (!contain){
            clazzName = extras.get(req);
        }

		if( clazzName!=null ){
			try {
				Bean b = (Bean) Class.forName(clazzName).newInstance();
				return b.fromJSON(response);
			} catch (Throwable e) {
                //si es de login ignorar nomas
                if (req != "j_spring_security_check")
                    throw new RuntimeException(e);
                else
                    e.printStackTrace();
			}
		}
		try {
			final JSONObject jsonObject = new JSONObject(response);
			return jsonObject.getJSONObject("data");
		} catch (JSONException e) {
			Log.e("ERROR:", "URL no está asociada ninguna clase para Mapear respuesta.");
		}
		return response;
	}
	
	public static String md5(String pass) {
		if( pass == null ){
			return null;
		}
        byte[] bytes = pass.getBytes();
        MessageDigest md;
		try {
			md = MessageDigest.getInstance("md5");
			bytes = md.digest(bytes);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				byte b = bytes[i];
				sb.append(toHexString(b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	private static String toHexString(byte i) {
		byte i1 = (byte) ((i>>4)&0x0f);
		byte i2 = (byte) (i&0x0f);
		return toHexChar(i1)+""+toHexChar(i2);
	}

	private static char toHexChar(byte i) {
		return (char) (i<10?('0'+i):('a'+(i-10)));
	}

	

	private String sanitateUrl(String commandURL) {
		return commandURL.replaceAll(" ", "%20");
	}

	
	
//	private static int count = 1;
	private void execute(final CommRunnable commRunnable,
			final CommDelegate delegate) {
		delegate.comm = this;
		
		delegate.getExecutor().execute(new ICommExecutorTask() {
			private CommException exception;
			private CommResponse response;
			
			public void onPostExecute() {
				if (exception != null) {
					delegate.exception = this.exception;
					delegate.onError();
					return;
				}
				delegate.response = this.response;
				delegate.onSuccess();
			}
			
			
			public void doInBackground() {
				try {
					this.response = commRunnable.run();
				} catch (CommException e) {
					this.exception = e;
					e.printStackTrace();
				}
			}
		});
	}



}