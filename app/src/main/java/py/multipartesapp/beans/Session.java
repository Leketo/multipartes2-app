package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 06/09/2015.
 */
public class Session extends Bean {

    private Integer userId;
    private String email;
    private boolean root;
    private boolean admin;
    private String session;




    @Override
    public void initWithJson(JSONObject o) throws JSONException {

        userId = getInteger(o, "id");
        //email = getString(o, "mail");
        //root = getBoolean(o, "root", false);
        //admin = getBoolean(o, "admin", false);
        //session = getString(o, "session");
    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }


    @Override
    public String toString() {
        return "Usuario{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", root=" + root +
                ", admin=" + admin +
                ", session='" + session + '\'' +
                '}';
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}