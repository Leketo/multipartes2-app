package py.multipartes2.utils;

/**
 * Created by edith on 12/11/2017.
 */
public class ListaEntrada {

    private String columna_1;
    private String columna_2;
    private String columna_3;
    private String columna_4;
    private String deviceName;
    private String deviceMacAddress;

    public ListaEntrada(String columna_1, String columna_2){
        this.deviceName = columna_1;
        this.deviceMacAddress = columna_2;
    }

    public String get_columna1() {
        return columna_1;
    }

    public String get_columna2() {
        return columna_2;
    }

    public String get_columna3() {
        return columna_3;
    }

    public String get_columna4() {
        return columna_4;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public void setDeviceMacAddress(String deviceMacAddress) {
        this.deviceMacAddress = deviceMacAddress;
    }
}
