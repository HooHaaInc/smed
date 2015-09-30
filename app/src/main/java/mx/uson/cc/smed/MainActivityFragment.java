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
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment{
    ArrayList<Tarea> tareas;
   public MainActivityFragment() {
        tareas = new ArrayList<>();
        tareas.add(new Tarea(
            "Gramàticas libres de contexto",
            "S -> Aa", 
            Tarea.COURSE_SPANISH, 
            Date.valueOf("2015-09-29")));
        tareas.add(new Tarea(
            "Lagrangiano",
            "Integral de e^x *(integral de f(t)dt) dx o algo así, no me acuerdo la neta. Pishi ruvalcaba. O Dr calculo, no me acuerdo la neta",
            Tarea.COURSE_MATH, 
            Date.valueOf("2015-10-01")));
        tareas.add(new Tarea(
            "Holi",
            "pusheen",
            Tarea.COURSE_NSCIENCES,
            Date.valueOf("2015-10-10")));
        tareas.add(new Tarea(
            "Tierra",
            "No so piedras, son rocas",
            Tarea.COURSE_GEOGRAFY,
            Date.valueOf("2015-10-5")));
        tareas.add(new Tarea(
            "La Revolucion",
            "Por 10ma vez wi",
            Tarea.COURSE_HISTORY,
            Date.valueOf("2015-10-22")));

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

        Bundle b  = new Bundle();
        b.putString("Titulo",(tareas.get(position)).getTitulo());
        b.putString("Descripcion", (tareas.get(position)).getDesc());
        b.putString("Materia", tareas.get(position).materia);
        b.putString("Fecha", tareas.get(position).fecha.toString());
        hwf.setArguments(b);
        MainActivity a = (MainActivity)getActivity();
        a.changeFragments(hwf);
        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }
}
