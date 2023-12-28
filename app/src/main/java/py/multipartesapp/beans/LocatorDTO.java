package py.multipartesapp.beans;

public class LocatorDTO {

    private String m_locator_id;
    private String m_locator_value;
    private String ad_org_id;

    public String getAd_org_id() {
        return ad_org_id;
    }

    public void setAd_org_id(String ad_org_id) {
        this.ad_org_id = ad_org_id;
    }

    public String getM_locator_id() {
        return m_locator_id;
    }

    public void setM_locator_id(String m_locator_id) {
        this.m_locator_id = m_locator_id;
    }

    public String getM_locator_value() {
        return m_locator_value;
    }

    public void setM_locator_value(String m_locator_value) {
        this.m_locator_value = m_locator_value;
    }
}
