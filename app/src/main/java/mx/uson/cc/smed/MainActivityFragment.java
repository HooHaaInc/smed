package mx.uson.cc.smed;

import android.support.v4.app.ListFragment;
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

    /**
     * le dice a la activity que debe cambiar/actualizar el fragment compañero
     * (HomeworkFragment), le manda un bundle con la posicion de la tarea
     * en ResourcesMan y el fragment compañero
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Bundle b  = new Bundle();
        b.putSerializable("frag", HomeworkFragment.class);
        b.putInt("position", position);

        try {
            ((MainActivity)getActivity()).changeFragments(b);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }
}
