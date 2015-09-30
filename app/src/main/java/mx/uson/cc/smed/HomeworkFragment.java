package mx.uson.cc.smed;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jorge on 9/16/2015.
 */
public class HomeworkFragment extends Fragment {
    String desc;
    String titulo;
    String materia;
    String fecha;
    public HomeworkFragment(){


    }
    @Override
    public void setArguments(Bundle b){
        titulo = b.getString("Titulo");
        desc = b.getString("Descripcion");
        materia = b.getString("Materia");
        fecha = b.getString("Fecha");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Titulo",titulo);
        outState.putString("Descripcion", desc);
        outState.putString("Materia", materia);
        outState.putString("Fecha", fecha);
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
            fecha = savedInstanceState.getString("Fecha");
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
        tv.setText(fecha);
        tv = (TextView)view.findViewById(R.id.course);
        tv.setText(getString(Tarea.getId(materia)));


        view.findViewById(R.id.back_to_list).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).goBack();
            }
        });

        return view;
    }
}
