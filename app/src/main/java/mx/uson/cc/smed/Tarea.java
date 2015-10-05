package mx.uson.cc.smed;

import android.content.Context;
import android.graphics.Color;

import java.sql.Date;

/**
 * Created by Jorge on 9/15/2015.
 * Clase tareita nomas pa calar
 */
public class Tarea {

    public static final String COURSE_SPANISH = "espanol";
    public static final String COURSE_MATH = "matematicas";
    public static final String COURSE_NSCIENCES = "ciencias_naturales";
    public static final String COURSE_GEOGRAFY = "geografia";
    public static final String COURSE_HISTORY = "historia";
    public static final String COURSE_UNKNOWN = "unknown";

    public static final int COLOR_SPANISH = Color.rgb(0xf4, 0x43, 0x36);
    public static final int COLOR_MATH = Color.rgb(0x3f, 0x51, 0xb5);
    public static final int COLOR_NSCIENCES = Color.rgb(0x00, 0xbc, 0xd4);
    public static final int COLOR_GEOGRAFY = Color.rgb(0x4c, 0xaf, 0x50);
    public static final int COLOR_HISTORY = Color.rgb(0xff, 0xc1, 0x07);

    public static int getCourseColor(String course){
        switch(course){
            case COURSE_SPANISH: return COLOR_SPANISH;
            case COURSE_MATH: return COLOR_MATH;
            case COURSE_NSCIENCES: return COLOR_NSCIENCES;
            case COURSE_GEOGRAFY: return COLOR_GEOGRAFY;
            case COURSE_HISTORY: return COLOR_HISTORY;
        }
        return Color.GRAY;
    }

    public static int getId(String course){
        switch(course){
            case COURSE_SPANISH: return R.string.spanish;
            case COURSE_MATH: return R.string.math;
            case COURSE_NSCIENCES: return R.string.nsciences;
            case COURSE_GEOGRAFY: return R.string.geography;
            case COURSE_HISTORY: return R.string.history;
        }
        return R.string.unknown;
    }

    public static String getCourseFromArray(int index){
        switch(index){
            case 0:  return COURSE_SPANISH;
            case 1: return COURSE_MATH;
            case 2: return COURSE_NSCIENCES;
            case 3: return COURSE_GEOGRAFY;
            case 4: return COURSE_HISTORY;
        }
        return null;
    }

    public static int getCourseColorFromIndex(int index){
        switch(index){
            case 0: return COLOR_SPANISH;
            case 1: return COLOR_MATH;
            case 2: return COLOR_NSCIENCES;
            case 3: return COLOR_GEOGRAFY;
            case 4: return COLOR_HISTORY;
        }
        return Color.GRAY;
    }

    public static boolean isUnknown(String course){
        switch (course){
            case COURSE_SPANISH:
            case COURSE_MATH:
            case COURSE_NSCIENCES:
            case COURSE_GEOGRAFY:
            case COURSE_HISTORY:
                return false;
        }
        return true;
    }

    public static int getIndex(String course){
        switch (course){
            case COURSE_SPANISH: return 0;
            case COURSE_MATH: return 1;
            case COURSE_NSCIENCES: return 2;
            case COURSE_GEOGRAFY: return 3;
            case COURSE_HISTORY: return 4;
        }
        return -1;
    }

    int id = -1;
    String titulo;
    String desc;
    String materia;
    Date fecha;

    public Tarea(String titulo, String desc, String materia,Date fecha){
        this.titulo = titulo;
        this.desc = desc;
        this.materia = materia;
        this.fecha = fecha;
    }

    public Tarea(int id, String titulo, String desc, String materia,Date fecha){
        this.titulo = titulo;
        this.desc = desc;
        this.materia = materia;
        this.fecha = fecha;
        this.id = id;
    }

    public String getTitulo(){
            return titulo;


    }
    public String getDesc(){
        return desc;

    }
    public String getMateria(){
        return materia;

    }
    public Date getFecha(){
        return fecha;

    }
}
