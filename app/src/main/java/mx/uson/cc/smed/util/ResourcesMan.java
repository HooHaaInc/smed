package mx.uson.cc.smed.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import mx.uson.cc.smed.Tarea;

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
    public static boolean initialized = false;


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
}
