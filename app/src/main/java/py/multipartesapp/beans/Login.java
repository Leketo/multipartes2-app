package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 11/06/2015.
 */
public class Login extends Bean {

    private String userName;
    private String accessToken;
    private Boolean success;
    private String status;
    private String sessionID;
    private String domain;
    private String path;
    private String expired;

    public Login(){}

    public Login(JSONObject json)throws JSONException{
        initWithJson(json);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        success = getBoolean(o, "success", false);
        if (!success){
            getError(o);
          return;
        }
        userName = getString(o, "userName");
        accessToken = getString(o, "accessToken");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {

    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public String toString() {
        return "Login{" +
                "userName='" + userName + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", success=" + success +
                ", status='" + status + '\'' +
                ", sessionID='" + sessionID + '\'' +
                '}';
    }
}
