package py.multipartesapp.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adolfo on 26/12/2015.
 */
public class Usuario extends Bean {
    Integer id;
    String mail;
    String name;
    String lastname;
    String password;
    String state;
    String role;
    String userCellphone;

    @Override
    public void initWithJson(JSONObject o) throws JSONException {
        id = getInteger(o, "id");
        mail = getString(o, "mail");
        name = getString(o, "name");
        lastname = getString(o, "lastname");
        password = getString(o, "password");
        state = getString(o, "state");
        role = getString(o, "role");
        userCellphone = getString(o, "userCellphone");

    }

    @Override
    public void initWithJsonArray(JSONArray o) throws JSONException {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserCellphone() {
        return userCellphone;
    }

    public void setUserCellphone(String userCellphone) {
        this.userCellphone = userCellphone;
    }
}
