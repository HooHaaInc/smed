package mx.uson.cc.smed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import mx.uson.cc.smed.util.Junta;
import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.SMEDClient;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReadMeetingFragment extends Fragment {

    String titulo;
    String desc;
    String citado;
    String fecha;
    int position;
    int id;

    @Override
    public void setArguments(Bundle b){
        position = b.getInt("Position");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Titulo",titulo);
        outState.putString("Descripcion", desc);
        outState.putString("Citado", citado);
        outState.putString("Fecha", fecha != null? fecha.toString() : "");
        outState.putInt("Id", id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting, container, false);
        TextView tv = (TextView) view.findViewById(R.id.titulo);
        if(savedInstanceState != null){
            desc = savedInstanceState.getString("Descripcion");
            titulo = savedInstanceState.getString("Titulo");
            fecha = savedInstanceState.getString("Fecha");
            citado = savedInstanceState.getString("Citado");
            ((MainActivity)getActivity()).hideFab();
        }else {
            final Junta junta = ResourcesMan.getJuntas().get(position);
            titulo = junta.getTitulo();
            desc =  junta.getDesc();
            citado = junta.getCitado();
            Locale locale = getResources().getConfiguration().locale;
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, EEEEEEEEE, kk:mm", locale);
            this.fecha = formatter.format(junta.getFecha());
            view.findViewById(R.id.back_to_list).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity) getActivity()).goBack();
                        }
                    });
            if(getActivity().getSharedPreferences("user", 0)
                    .getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1) == SMEDClient.TEACHER)
                view.findViewById(R.id.edit_meeting).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent edit = new Intent(getActivity(), AddMeetingActivity.class);
                                edit.putExtra("edit", true);
                                edit.putExtra("Titulo", titulo);
                                edit.putExtra("Descripcion", desc);
                                edit.putExtra("Citado", citado);
                                edit.putExtra("Fecha", junta.getFecha().toString());
                                getActivity().startActivityForResult(edit, MainActivity.EDIT_MEETING);
                            }
                        });
            else view.findViewById(R.id.edit_meeting).setVisibility(View.GONE);

        }

        tv.setText(titulo);
        tv = (TextView)view.findViewById(R.id.desc);
        tv.setText(desc);
        tv = (TextView)view.findViewById(R.id.cited);
        tv.setText(citado);
        tv = (TextView)view.findViewById(R.id.date);
        tv.setText(fecha);
        return view;
    }
}
