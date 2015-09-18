package mx.uson.cc.smed;

/**
 * Created by Jorge on 9/15/2015.
 * Clase tareita nomas pa calar
 */
public class Tarea {

    String titulo;
    String desc;

    public Tarea(String titulo, String desc){
        this.titulo = titulo;
        this.desc = desc;
    }
    public String getTitulo(){
            return titulo;


    }
    public String getDesc(){
        return desc;

    }
}
