package mx.uson.cc.smed;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.ResourcesMan;

/**
 * Created by Jorge on 10/3/2015.
 * Clase para ver un reporte en particular, mostrando todos sus detalles
 */
public class ReadReportFragment extends Fragment  {
    String acusador;
    String descripcion;
    String fecha;
    int position;
    View root;
    public ReadReportFragment(){


    }
    @Override
    public void setArguments(Bundle b){
        position = b.getInt("Position");
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        TextView tv;

        if(savedInstanceState != null){
            descripcion = savedInstanceState.getString("Descripcion");
            acusador = savedInstanceState.getString("Acusador");
            fecha = savedInstanceState.getString("Fecha");
            ((MainActivity)getActivity()).hideFab();
        }else {
            Reporte reporte = ResourcesMan.getReportes().get(position);
            acusador = reporte.getAcusador();
            descripcion = reporte.getDescripcion();
            Locale locale = getResources().getConfiguration().locale;
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, EEEEEEEEE", locale);
            this.fecha = formatter.format(reporte.getFecha());
        }
        tv = (TextView) view.findViewById(R.id.victim);
        tv.setText(this.acusador);
       // tv = (TextView)view.findViewById(R.id.report_date);
        //tv.setText(this.fecha)111111111111111111;
        tv = (TextView)view.findViewById(R.id.desc);
        tv.setText(this.descripcion);
        view.findViewById(R.id.back_to_list).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).goBack();
                    }
                });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("Acusador",acusador);
        outState.putString("Descripcion",descripcion);
        outState.putString("Fecha",fecha);
    }
}
