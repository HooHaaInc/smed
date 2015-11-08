package mx.uson.cc.smed.util;

import java.io.Serializable;
import java.sql.Date;

/**
 * Created by Jorge on 10/2/2015.
 */
public class Reporte implements Serializable {
    String alumno_acusador = "Derp";
    String descripcion;
    Date fecha;
    int ID;
    public Reporte(int ID, String desc,Date fecha){
        this.ID = ID;
        descripcion = desc;
        this.fecha = fecha;
    }
    public String getAcusador(){return alumno_acusador; }
    public String getDescripcion(){ return descripcion; }
    public Date getFecha(){return fecha;}
}
