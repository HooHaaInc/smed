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

import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.SMEDClient;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment{


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        getActivity().setTitle(R.string.homeworks);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HomeworkFragment  hwf = new HomeworkFragment();

        Tarea tarea = ResourcesMan.getTareas().get(position);

        Bundle b  = new Bundle();
        b.putInt("Id", tarea.id);
        b.putString("Titulo",tarea.getTitulo());
        b.putString("Descripcion", tarea.getDesc());
        b.putString("Materia", tarea.getMateria());


        b.putString("Fecha", tarea.getFecha().toString());
        hwf.setArguments(b);
        ((MainActivity)getActivity()).changeFragments(hwf);
        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }
}
