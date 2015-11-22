package mx.uson.cc.smed.util;

import java.io.Serializable;
import java.sql.Date;

/**
 * Created by Jorge on 10/29/2015.
 */
public class Junta implements Serializable {

    String desc;
    String titulo;
    Date fecha;
    String citado = "Papi";
    boolean juntaGrupal;
    int ID;
    public Junta(String titulo, String desc, int id,Date fecha, boolean juntaGrupal){
        this.titulo = titulo;
        this.fecha = fecha;
        this.desc = desc;
        this.ID = id;
        this.juntaGrupal = juntaGrupal;
        if(juntaGrupal) citado = "Todos";
    }

    public Junta(String titulo, String desc, int id,Date fecha, String citado){
        this.titulo = titulo;
        this.fecha = fecha;
        this.desc = desc;
        this.ID = id;
        this.juntaGrupal = citado == null;
        this.citado = !juntaGrupal ? citado : "Todos";
    }
    public int getID(){return ID;}
    public String getDesc(){return desc;}
    public String getTitulo(){return titulo;}
    public Date getFecha(){return fecha;}
    public String getCitado(){return citado;}
    public boolean isJuntaGrupal(){ return juntaGrupal; }

}
