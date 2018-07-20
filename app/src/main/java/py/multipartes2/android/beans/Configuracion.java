package py.multipartes2.beans;

/**
 * Created by Adolfo on 06/09/2015.
 */
public class Configuracion{

    private String clave;
    private String valor;

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }


    @Override
    public String toString() {
        return "{ clave:"+clave+ ", valor:"+valor+"}";
    }
}