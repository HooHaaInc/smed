package mx.uson.cc.smed.util;

import java.io.Serializable;
import java.sql.Date;

/**
 * Created by Jorge on 10/2/2015.
 */
public class Reporte implements Serializable {
    String alumno_acusador;
    String descripcion;
    Date fecha;
    public Reporte(String acusador, String desc,Date fecha){
        alumno_acusador = acusador;
        descripcion = desc;
        this.fecha = fecha;
    }
    public String getAcusador(){return alumno_acusador; }
    public String getDescripcion(){ return descripcion; }
    public Date getFecha(){return fecha;}
}
