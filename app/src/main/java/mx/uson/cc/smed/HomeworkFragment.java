package mx.uson.cc.smed;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Jorge on 9/16/2015.
 */
public class HomeworkFragment extends Fragment {
    String desc;
    String titulo;
    String materia;
    Date fecha;
    int id;

    @Override
    public void setArguments(Bundle b){
        titulo = b.getString("Titulo");
        desc = b.getString("Descripcion");
        materia = b.getString("Materia");
        fecha = Date.valueOf(b.getString("Fecha"));
        id = b.getInt("Id");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Titulo",titulo);
        outState.putString("Descripcion", desc);
        outState.putString("Materia", materia);
        outState.putString("Fecha", fecha.toString());
        outState.putInt("Id", id);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        View view = inflater.inflate(R.layout.fragment_homework, container, false);
        TextView tv;

        if(savedInstanceState != null){
            titulo = savedInstanceState.getString("Titulo");
            desc = savedInstanceState.getString("Descripcion");
            materia = savedInstanceState.getString("Materia");
            fecha = Date.valueOf(savedInstanceState.getString("Fecha"));
            id = savedInstanceState.getInt("Id");
            ((MainActivity)getActivity()).hideFab();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getActivity().getWindow().setStatusBarColor(Tarea.getCourseColor(materia));

        View v = view.findViewById(R.id.homework_title_bar);
        v.setBackgroundColor(Tarea.getCourseColor(materia));
        tv = (TextView) view.findViewById(R.id.titulo);
        tv.setText(this.titulo);
        tv = (TextView) view.findViewById(R.id.desc);
        tv.setText(this.desc);
        tv = (TextView)view.findViewById(R.id.date);

        Locale locale = getResources().getConfiguration().locale;  //cal.setTime(tarea.fecha);
        String strFecha;

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, E", locale);
        strFecha = formatter.format(fecha);
        tv.setText(strFecha);
        tv = (TextView)view.findViewById(R.id.course);
        tv.setText(getString(Tarea.getId(materia)));


        view.findViewById(R.id.back_to_list).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).goBack();
                    }
                });

        FloatingActionButton fabMini = (FloatingActionButton)view.findViewById(R.id.edit_homework);
        fabMini.setBackgroundTintList(ColorStateList.valueOf(Tarea.getCourseColor(materia)));
        fabMini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddHomeworkActivity.class);
                i.putExtra("edit", true);
                i.putExtra("title", titulo);
                i.putExtra("desc", desc);
                i.putExtra("course", Tarea.getIndex(materia));
                i.putExtra("date", fecha);
                i.putExtra("id", id);
                getActivity().startActivityForResult(i, MainActivity.EDIT_HOMEWORK);
            }
        });

        return view;
    }

}
