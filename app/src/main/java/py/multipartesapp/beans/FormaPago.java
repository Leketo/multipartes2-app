package py.multipartesapp.beans;

/**
 * Created by Adolfo on 21/09/2015.
 */
public class FormaPago {

    private String codigo;
    private String descripcion;

    public FormaPago(String codigo, String descripcion){
        this.codigo=codigo;
        this.descripcion=descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion+"\n";
    }
}
