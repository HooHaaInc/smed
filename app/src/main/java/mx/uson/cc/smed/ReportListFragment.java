package mx.uson.cc.smed;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.Inflater;

import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.SMEDClient;

/**
 * Created by Jorge on 10/3/2015.
 */
public class ReportListFragment extends ListFragment {
    LayoutInflater inflater;
    Context contexto;
    ReportListAdapter adapter;
    public ReportListFragment(){


    }
    public void setArguments(Bundle B){


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        contexto = inflater.getContext();
        getActivity().setTitle(R.string.reports);

        //TODO agregar new GetReportes
        if(ResourcesMan.getReportes() == null) {
            ResourcesMan.initReportes();
            SharedPreferences preferences = getActivity().getSharedPreferences("user", 0);
            new getReports(
                    preferences.getInt(SMEDClient.KEY_ID_GROUP, -1),
                    preferences.getInt(SMEDClient.KEY_ID_PARENT, -1) //si es maestro, parentId = -1, y entonces se deben obtener todos los reportes del grupo
            ).execute();
        }else{
            adapter = new ReportListAdapter(inflater.getContext(),R.layout.list_view_row_report_item,
                    ResourcesMan.getReportes());
            setListAdapter(adapter);
        }

        return super.onCreateView(inflater, container, savedInstanceState);


    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle b  = new Bundle();
        b.putSerializable("frag",ReadReportFragment.class);
        b.putInt("Position", position);
        try {
            ((MainActivity)getActivity()).changeFragments(b, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }

        //
    }

    //TODO agregar nueva clase dinámica que extienda de Asyntask

    class getReports extends AsyncTask<Void,Void,Boolean>{

        public getReports(int groupId, int parentId){
            this.groupId = groupId;
            this.parentId = parentId;
        }

        JSONArray reportes;
        JSONObject reporte;
        String id_reporte,id_alumno,fecha,comentario;
        int groupId, parentId;

        @Override
        protected Boolean doInBackground(Void... params) {
            ResourcesMan.quitarReportes();
            JSONObject result = SMEDClient.getAllReports(groupId, parentId);
            try{
                reportes = result.getJSONArray("reportes");

                for(int i=0;i<reportes.length();++i){
                    reporte = null;
                    reporte = reportes.getJSONObject(i);
                    id_reporte = reporte.getString("id_reporte");
                    id_alumno = reporte.getString("id_alumno");
                    fecha = reporte.getString("fecha");
                    comentario = reporte.getString("comentario");
                    int id = Integer.parseInt(id_alumno);
                    String idA = SMEDClient.getPersonNameByStudentID(id);
                    Log.v("nombre",idA);


                    Reporte r = new Reporte(idA,comentario,Date.valueOf(fecha));
                    ResourcesMan.addReporte(r);
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
            return false;
        }
        protected void onPostExecute(Boolean res){
            adapter = new ReportListAdapter(contexto,R.layout.list_view_row_report_item,
                    ResourcesMan.getReportes());
            setListAdapter(adapter);
        }
    }

}
