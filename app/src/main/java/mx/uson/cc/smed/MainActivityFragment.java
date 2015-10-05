package mx.uson.cc.smed;

import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import mx.uson.cc.smed.util.SMEDClient;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment{
    ArrayList<Tarea> tareas;

    JSONArray hw = null;

    ArrayList<HashMap<String, String>> listaTareas;

    HomeworkListAdapter adapter;

    public MainActivityFragment() {
        tareas = new ArrayList<>();

        /*tareas.add(new Tarea(
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
            Date.valueOf("2015-10-02")));*/

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
        adapter = new HomeworkListAdapter(inflater.getContext(),
                android.R.layout.simple_list_item_1,tareas);
        setListAdapter(adapter);
        listaTareas = new ArrayList<HashMap<String,String>>();
        new GetHomework().execute();

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
        b.putString("Materia", tarea.getMateria());

        Locale locale = getResources().getConfiguration().locale;  //cal.setTime(tarea.fecha);
        String fecha;

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, EEEEEEEEE", locale);
        fecha = formatter.format(tarea.getFecha());

        b.putString("Fecha", fecha);
        hwf.setArguments(b);
        MainActivity a = (MainActivity)getActivity();
        a.changeFragments(hwf);


        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }
    public void addTarea(Context C, Tarea T){
        tareas.add(T);
        HomeworkListAdapter a = new HomeworkListAdapter(C,android.R.layout.simple_list_item_1,tareas);
        setListAdapter(a);


    }

    class GetHomework extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... voids) {
            if(tareas.isEmpty()) {
                JSONObject result = SMEDClient.getAllHomework();
                try {
                    hw = result.getJSONArray("tareas");

                    for (int i = 0; i < hw.length(); ++i) {
                        JSONObject c = null;
                        c = hw.getJSONObject(i);

                        String id_tarea = c.getString("id_tarea");
                        String id_grupo = c.getString("id_grupo");
                        String titulo = c.getString("titulo");
                        String desc = c.getString("descripcion");
                        String materia = c.getString("materia");
                        String fecha = c.getString("fecha");

                        tareas.add(new Tarea(titulo, desc, materia, Date.valueOf(fecha)));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return "";
        }

        protected void onPostExecute(String res){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
