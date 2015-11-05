package mx.uson.cc.smed;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.SMEDClient;

/**
 * Created by Jorge on 9/16/2015.
 */
public class HomeworkFragment extends Fragment {
    String desc;
    String titulo;
    String materia;
    Date fecha;
    int id;
    int id_grupo;
    View root;

    /**
     * Solo se le manda una posicion
     * @param b bundle que contiene la posicion
     */
    @Override
    public void setArguments(Bundle b){
        int pos = b.getInt("position");

        setTarea(pos);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Titulo",titulo);
        outState.putString("Descripcion", desc);
        outState.putString("Materia", materia);
        outState.putString("Fecha", fecha != null? fecha.toString() : "");
        outState.putInt("Id", id);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_homework, container, false);
        TextView tv;
        if(container.getId() == R.id.fragmentLayout2){
            root.findViewById(R.id.back_to_list).setVisibility(View.GONE);
        }else{
            root.findViewById(R.id.back_to_list).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity) getActivity()).goBack();
                        }
                    });
        }

        if(savedInstanceState != null){
            titulo = savedInstanceState.getString("Titulo");
            desc = savedInstanceState.getString("Descripcion");
            materia = savedInstanceState.getString("Materia");
            fecha = Date.valueOf(savedInstanceState.getString("Fecha"));
            id = savedInstanceState.getInt("Id");
                ((MainActivity)getActivity()).hideFab();
        }if(titulo == null){
            return root;
        }
        setViews();
        Log.v("id tarea:", Integer.toString(id));
        FloatingActionButton fabMini = (FloatingActionButton)root.findViewById(R.id.edit_homework);
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

        return root;
    }

    /**
     * setea los datos de la tarea y actualiza la vista si es visible
     * @param pos posicion de la tarea en ResourcesMan
     */
    public void setTarea(int pos){
        Tarea tarea = ResourcesMan.getTareas().get(pos);
        titulo = tarea.getTitulo();
        desc = tarea.getDesc();
        materia = tarea.getMateria();
        fecha = tarea.getFecha();
        id = tarea.getId();
        id_grupo = tarea.getGrupo();

        if(root != null)
            setViews();
    }

    /**
     * utileria c:
     */
    private void setViews(){
        View v = root.findViewById(R.id.homework_title_bar);
        FloatingActionButton fabMini = (FloatingActionButton)root.findViewById(R.id.edit_homework);
        if(getActivity().getSharedPreferences("user", 0)
                .getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1) != SMEDClient.TEACHER) {
            fabMini.hide();
        }
        else
            fabMini.setBackgroundTintList(ColorStateList.valueOf(Tarea.getCourseColor(materia)));



        ((MainActivity)getActivity()).setStatusBarColor(Tarea.getCourseColor(materia));

        v.setBackgroundColor(Tarea.getCourseColor(materia));
        TextView tv;
        tv = (TextView) root.findViewById(R.id.titulo);
        tv.setText(this.titulo);
        tv = (TextView) root.findViewById(R.id.desc);
        tv.setText(this.desc);
        tv = (TextView)root.findViewById(R.id.date);

        Locale locale = getResources().getConfiguration().locale;  //cal.setTime(tarea.fecha);
        String strFecha;

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, E", locale);
        strFecha = formatter.format(fecha);
        tv.setText(strFecha);
        tv = (TextView)root.findViewById(R.id.course);
        tv.setText(getString(Tarea.getId(materia)));
    }

}
