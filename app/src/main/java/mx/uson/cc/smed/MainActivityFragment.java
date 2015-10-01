package mx.uson.cc.smed;

import android.app.ListFragment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment{
    ArrayList<Tarea> tareas;
   public MainActivityFragment() {
        tareas = new ArrayList<>();
        tareas.add(new Tarea(
            "Ingeniería",
            "Costos",
            Tarea.COURSE_SPANISH, 
            Date.valueOf("2015-09-30")));
        tareas.add(new Tarea(
            "Lógico",
            "Examen",
            Tarea.COURSE_MATH, 
            Date.valueOf("2015-10-05")));
        tareas.add(new Tarea(
            "Diseño de Sistemas Digitales",
            "Examen",
            Tarea.COURSE_NSCIENCES,
            Date.valueOf("2015-10-07")));
        tareas.add(new Tarea(
            "Estadistica",
            "Nah",
            Tarea.COURSE_GEOGRAFY,
            Date.valueOf("2015-09-29")));
        tareas.add(new Tarea(
            "Japones",
            "Examen",
            Tarea.COURSE_HISTORY,
            Date.valueOf("2015-10-02")));

       Collections.sort(tareas, new Comparator<Tarea>() {
           @Override
           public int compare(Tarea lhs, Tarea rhs) {
               long now = new java.util.Date().getTime();
               long left = lhs.fecha.getTime() - now;
               long right = rhs.fecha.getTime() - now;

               if(left*right > 0 ) {
                   left = -Math.abs(left);
                   right = -Math.abs(right);
               }

               return left > right ? -1 : right > left ? 1 : 0;

           }
       });

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        HomeworkListAdapter adapter = new HomeworkListAdapter(inflater.getContext(),
                android.R.layout.simple_list_item_1,tareas);
        setListAdapter(adapter);
        getActivity().setTitle(R.string.homeworks);
        return super.onCreateView(inflater, container, savedInstanceState);
        //viejo
        //return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        HomeworkFragment  hwf = new HomeworkFragment();

        Tarea tarea = tareas.get(position);

        Bundle b  = new Bundle();
        b.putString("Titulo",tarea.getTitulo());
        b.putString("Descripcion", tarea.getDesc());
        b.putString("Materia", tarea.materia);

        //Calendar cal = Calendar.getInstance();
        Locale locale = getResources().getConfiguration().locale;
        //cal.setTime(tarea.fecha);
        String fecha;// = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
        //fecha += " " + cal.getDisplayName(Calendar.DATE, Calendar.LONG, locale);
        //fecha += " " + cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale);

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, EEEEEEEEE", locale);
        fecha = formatter.format(tarea.fecha);

        b.putString("Fecha", fecha);
        hwf.setArguments(b);
        MainActivity a = (MainActivity)getActivity();
        a.changeFragments(hwf);
        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }
}
