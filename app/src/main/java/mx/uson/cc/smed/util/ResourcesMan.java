package mx.uson.cc.smed.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by nancio on 5/10/15.
 */
public class ResourcesMan {
    private static Comparator<Tarea> sorter = new Comparator<Tarea>() {
        @Override
        public int compare(Tarea lhs, Tarea rhs) {
            long now = new java.util.Date().getTime();
            long left = lhs.getFecha().getTime() - now;
            long right = rhs.getFecha().getTime() - now;

            if (left * right > 0) {
                left = -Math.abs(left);
                right = -Math.abs(right);
            }

            return left > right ? -1 : right > left ? 1 : 0;

        }
    };

    private static ArrayList<Tarea> tareas = new ArrayList<>();
    private static ArrayList<Reporte> reportes;
    private static ArrayList<Junta> juntas;
    private static ArrayList<Student> estudiantes = new ArrayList<>();
    static{

        estudiantes.add(new Student(1,"pancho","barraza","mota",null,null,null));
        estudiantes.add(new Student(2,"paco","el","chato",null,null,null));
        estudiantes.add(new Student(3,"jorgito","el","vergasa",null,null,null));
        estudiantes.add(new Student(4,"el","pinche","nan",null,null,null));
    }

    public static void quitarEstudiantes(){estudiantes.clear();}
    public static void initReportes(){ reportes = new ArrayList<>(); }
    public static void initJuntas(){ juntas = new ArrayList<>(); }


    public static ArrayList<Student> getEstudiantes(){
        return estudiantes;
    }
    public static ArrayList<Reporte> getReportes() {
        return reportes;
    }

    public static ArrayList<Junta> getJuntas() {
        return juntas;
    }


    public static void addReporte(Reporte reporte){
        reportes.add(reporte);

    }

    public static void addStudent(Student student){
        estudiantes.add(student);
    }

    public static void addJunta(Junta junta){
        juntas.add(junta);

    }

    public static ArrayList<Tarea> getTareas() {
        return tareas;
    }

    public static int editTarea(Tarea tarea) {
        int i = 0;
        for (; i < tareas.size(); ++i)
            if (tareas.get(i).getId() == tarea.getId())
                break;
        tareas.set(i, tarea);
        Collections.sort(tareas, sorter);
        return tareas.indexOf(tarea);
    }

    public static void addTarea(Tarea tarea) {
        tareas.add(tarea);
        Collections.sort(tareas, sorter);
    }

    public static void quitarTareas(){
        tareas.clear();
    }

    public static void quitarReportes(){
        reportes.clear();
    }

    public static void quitarJuntas(){
        juntas.clear();
    }

    public static void eliminarTarea(Tarea tarea){
        int i=0;
        for(;i<tareas.size();++i)
            if(tareas.get(i).getId() == tarea.getId())
                break;
        tareas.remove(i);
        Collections.sort(tareas,sorter);
    }
}
