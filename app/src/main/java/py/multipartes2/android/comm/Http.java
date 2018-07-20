package py.multipartes2.comm;


//import com.google.common.collect.Multimap;

import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
        import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;


        import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

        import java.security.InvalidParameterException;
import java.util.ArrayList;
        import java.util.List;
import java.util.Map;

import py.multipartes2.utils.Globals;


public class Http {


	public interface Logger {
		void d(String string, String format);
		void e(String string, String message, Throwable e);
	}
	
	static {

        logger = new Logger() {
			public void e(String string, String message, Throwable e) {
			}
			public void d(String string, String format) {
			}
		};
	}
	
	private static Logger logger;
	public static void setLogger(Logger logger) {
		Http.logger = logger;
	}
	
	static {
		// HTTP connection reuse which was buggy pre-froyo
	    //if (Integer.parseInt(Build.VERSION.SDK) <= Build.VERSION_CODES.ECLAIR) {
	        System.setProperty("http.keepAlive", "false");
	    //}
	}
	
	
	public static class HttpException extends Exception{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public HttpException(Throwable t) {
			super(t);
		}
		
	}
	public static final int RequestTypeHttpPost = 0;
	public static final int RequestTypeHttpGet = 1;

//	public static String get(String url) throws HttpException {
//		return getContent(RequestType.HTTP_GET, url, null);
//	}

//	public static String get(String url, Hashtable<String, String> params) throws HttpException{
//		return getContent(RequestType.HTTP_GET, url, params);
//	}

	public static HttpResponse get(String url, Map params) throws HttpException, JSONException{
        return getContent(RequestTypeHttpGet, url, params);
	}

	public static HttpResponse post(String url, Map params) throws HttpException, JSONException{
		return getContent(RequestTypeHttpPost, url, params);
	}

    /*
    public static HttpResponse post2(String url, Multimap params) throws HttpException, JSONException{
        return getContent2(RequestTypeHttpPost, url, params);
    }
    */

//	public static byte[] getContentByteArray(String url)throws HttpException{
//		HttpConnectionFactory factory = new HttpConnectionFactory(url);
//		while( true ) {
//			try {
//				HttpConnection connection = factory.getNextConnection();
//				try {
//					connection.setRequestMethod("GET");
//					connection.setRequestProperty("Content-type","application/x-www-form-urlencoded");										
//								
//					
//					InputStream is = connection.openInputStream();
//					//do something with the input stream
//					
//					byte[] result = streamToByteArray(is);					
//
//					if(true) {
//						return result;
//					}
//				}catch(IOException e) {
//					//Log the error or store it for displaying to
//					//the end user if no transports succeed
//					throw new HttpException(HttpException.COMMUNICATION_EXCEPTION, HttpException.TIME_OUT);
//				}
//			}catch( NoMoreTransportsException e ) {
//				throw new HttpException(HttpException.COMMUNICATION_EXCEPTION, HttpException.CONECTION_METHOD_EXAUSTED);
//			}
//		}				
//		
//	}
	
	private static HttpResponse getContent(int method, String url, Map params) throws HttpException, JSONException {
//		logger.d("HttpConnManager", String.format("Calling %s [%s] begins", method, url));
		switch (method) {
		case RequestTypeHttpPost:
			HttpResponse postResult;
			postResult = getPostContent(url, params); 
//			logger.d("HttpConnManager", String.format("Calling [%s] ends", url));
			return postResult;
		case RequestTypeHttpGet:
            HttpResponse getResult;
            getResult = getGetContent(url, params);
            return getResult;
//			String getResult;
//			getResult = getGetContent(url, params); 
//			logger.d("HttpConnManager", String.format("Calling [%s] ends", url));
//			logger.d("HttpConnManager", String.format("result", getResult));
//			return getResult;
		default:
			throw new InvalidParameterException("method "+method+" is invalid");
		}
	}

    /*
    private static HttpResponse getContent2(int method, String url, Multimap params) throws HttpException, JSONException {
//		logger.d("HttpConnManager", String.format("Calling %s [%s] begins", method, url));
        switch (method) {
            case RequestTypeHttpPost:
                HttpResponse postResult;
                postResult = getPostContent2(url, params);
//			logger.d("HttpConnManager", String.format("Calling [%s] ends", url));
                return postResult;

//			String getResult;
//			getResult = getGetContent(url, params);
//			logger.d("HttpConnManager", String.format("Calling [%s] ends", url));
//			logger.d("HttpConnManager", String.format("result", getResult));
//			return getResult;
            default:
                throw new InvalidParameterException("method "+method+" is invalid");
        }
    }
	*/
	public interface HttpResponse{
		public int getCode();
		public String getResponse();
	}

	private static HttpResponse getPostContent(String url, Map params) throws HttpException, JSONException{
	    //HttpClient httpclient = new DefaultHttpClient();
	    //HttpPost httppost = new HttpPost(url);

        DefaultHttpClient client = new DefaultHttpClient();
        client.setCookieStore(Globals.cookieStore);


		HttpPost post = new HttpPost(url);
        //post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		if( params!=null ){
//			JSONObject jParams = new JSONObject();
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            Object []keys = params.keySet().toArray();

            for (int i = 0; i < keys.length; i++) {
                String k = (String) keys[i];
                Object o = params.get(k);
                // formparams.add(new BasicNameValuePair("full_name", "value1"));
                nameValuePairs.add(new BasicNameValuePair(k, (String)o));
            }
			
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
//			try {
//				post.setEntity(new StringEntity(jParams.toString()));
//			} catch (UnsupportedEncodingException e) {
//				throw new RuntimeException(e);
//			}
			
		}
//		post.addHeader("Content-Type", "application/json; charset=ISO-8859-1");
		
		Throwable t = null;
		try {
			org.apache.http.HttpResponse resp = client.execute(post);

            /* TEmporal Adolfo */
            List<Cookie> cookies = Globals.cookieStore.getCookies();
            if (cookies.isEmpty()) {
                System.out.println("Cookie None en POST");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    System.out.println("======Cookie POST- " + cookies.get(i).toString());
                }
            }

            StatusLine status = resp.getStatusLine();
			final int code = status.getStatusCode();
			final String r = inputStreamToString(resp.getEntity().getContent());

			//Si hay un error con los paquetes de TIGO, tratar como 404
			if (code == 200 && r.contains("Portal Movil Tigo")){
				return new HttpResponse() {
					public String getResponse() {return r;}
					public int getCode() {return 404;}
				};
			}


			return new HttpResponse() {
				public String getResponse() {return r;}
				public int getCode() {return code;}
			};

		} catch (ClientProtocolException e) {
			t = e;
		} catch (IOException e) {
			t = e;
		} finally {
			if( t!=null ){
				t.printStackTrace();
				throw new HttpException(t);
			}
				
		}
		return null;
		
		
	}

    /*
    private static HttpResponse getPostContent2 (String url, Multimap params) throws HttpException, JSONException{
        //HttpClient httpclient = new DefaultHttpClient();
        //HttpPost httppost = new HttpPost(url);

        HttpClient client = new DefaultHttpClient();
//		client = WebClientDevWrapper.wrapClient(client);
        HttpPost post = new HttpPost(url);

        if( params!=null ){
//			JSONObject jParams = new JSONObject();

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            Object []keys = params.keySet().toArray();

            for (int i = 0; i < keys.length; i++) {
                String k = (String) keys[i];
                //Object o = params.get(k);
                Collection<String> list = params.get(k);
                for (String value : list){
                    nameValuePairs.add(new BasicNameValuePair(k, value));
                }
                // formparams.add(new BasicNameValuePair("full_name", "value1"));
            }

            try {
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
//			try {
//				post.setEntity(new StringEntity(jParams.toString()));
//			} catch (UnsupportedEncodingException e) {
//				throw new RuntimeException(e);
//			}

        }
//		post.addHeader("Content-Type", "application/json; charset=ISO-8859-1");

        Throwable t = null;
        try {
            org.apache.http.HttpResponse resp = client.execute(post);
            StatusLine status = resp.getStatusLine();
            final int code = status.getStatusCode();
            final String r = inputStreamToString(resp.getEntity().getContent());
            return new HttpResponse() {
                public String getResponse() {return r;}
                public int getCode() {return code;}
            };


        } catch (ClientProtocolException e) {
            t = e;
        } catch (IOException e) {
            t = e;
        } finally {
            if( t!=null ){
                t.printStackTrace();
                throw new HttpException(t);
            }

        }
        return null;


    }
	*/
	private static HttpResponse getGetContent(String url, Map<String, String> params) throws HttpException{


	    //HttpClient httpclient = new DefaultHttpClient();
	    String queryString = "";
        //HttpClient client = new DefaultHttpClient();
        DefaultHttpClient client = new DefaultHttpClient();
        client.setCookieStore(Globals.cookieStore);


        if(params != null && params.size()>0){
    		int i = 0;
    		queryString = "?";
	    	for(String key : params.keySet()){
	    		queryString = queryString + key + "=" + params.get(key);
	    		i++;
		        if(i<params.size()){
		        	queryString = queryString + "&";
		        }

	    	}
    	}
        HttpGet httpGet = new HttpGet(url+queryString);
        //httpGet.setParams();

        Throwable t = null;
        try {
            /* TEmporal Adolfo */
            List<Cookie> cookies = Globals.cookieStore.getCookies();
            if (cookies.isEmpty()) {
                //System.out.println("Cookie None GET");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    //System.out.println("======Cookie GET- " + cookies.get(i).toString());
                }
            }

            // Create local HTTP context
            HttpContext localContext = new BasicHttpContext();
            // Bind custom cookie store to the local context
            localContext.setAttribute(ClientContext.COOKIE_STORE, Globals.cookieStore);

            org.apache.http.HttpResponse resp = client.execute(httpGet,localContext);


            StatusLine status = resp.getStatusLine();
            final int code = status.getStatusCode();
            final String r = inputStreamToString(resp.getEntity().getContent());

			//Si hay un error con los paquetes de TIGO, tratar como 404
			if (code == 200 && r.contains("Portal Movil Tigo")){
				return new HttpResponse() {
					public String getResponse() {return r;}
					public int getCode() {return 404;}
				};
			}

            return new HttpResponse() {
                public String getResponse() {return r;}
                public int getCode() {return code;}
            };


        } catch (ClientProtocolException e) {
            t = e;
        } catch (IOException e) {
            t = e;
        } finally {
            if( t!=null ){
                t.printStackTrace();
                throw new HttpException(t);
            }

        }
        return null;

	}
	
	// Fast Implementation
	private static String inputStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    // Read response until the end
	    try {
			while ((line = rd.readLine()) != null) {				
			    total.append(line); 
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    // Return full string	    
	    return total.toString() ;
	}


}
