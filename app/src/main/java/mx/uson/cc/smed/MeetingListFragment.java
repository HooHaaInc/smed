package mx.uson.cc.smed;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.Calendar;

import mx.uson.cc.smed.util.Junta;
import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.SMEDClient;

/**
 * Created by Jorge on 10/3/2015.
 */
public class MeetingListFragment extends ListFragment {
    LayoutInflater inflater;
    Context contexto;
    MeetingListAdapter adapter;

    public MeetingListFragment(){


    }
    public void setArguments(Bundle B){


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        getActivity().setTitle(R.string.meetings);
        contexto = inflater.getContext();
        getActivity().setTitle(R.string.meetings);
        if(ResourcesMan.getJuntas() == null) {
            ResourcesMan.initJuntas();
            SharedPreferences preferences = getActivity().getSharedPreferences("user", 0);
            new getMeetings(
                    preferences.getInt(SMEDClient.KEY_ID_GROUP, -1),
                    preferences.getInt(SMEDClient.KEY_ID_PARENT, -1) //si es maestro, parentId = -1, entonces se debera obtener todas las juntas del grupo
            ).execute();
        }else{
            adapter = new MeetingListAdapter(inflater.getContext(),R.layout.list_view_row_report_item,
                    ResourcesMan.getJuntas());
            setListAdapter(adapter);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ReadMeetingFragment  rrf = new ReadMeetingFragment();
        Bundle b  = new Bundle();
        b.putSerializable("frag",ReadMeetingFragment.class);
        b.putInt("Position", position);
        MainActivity a = (MainActivity)getActivity();
        try {
            ((MainActivity)getActivity()).changeFragments(b);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }

        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }

    class getMeetings extends AsyncTask<Void,Void,Boolean>{

        public getMeetings(int groupId, int parentId){
            this.groupId = groupId;
            this.parentId = parentId;
        }

        JSONArray juntas;
        JSONObject junta;
        String id_junta,id_padre,id_grupo,fecha,motivo,descripcion,esgrupal;
        int groupId, parentId;

        @Override
        protected Boolean doInBackground(Void... params) {
            ResourcesMan.quitarJuntas();
            JSONObject result = SMEDClient.getAllMeetings(groupId, parentId);

            try{
                juntas = result.getJSONArray("juntas");

                for(int i=0;i<juntas.length();++i){
                    junta = null;
                    junta = juntas.getJSONObject(i);
                    id_junta = junta.getString("id_junta");
                    id_padre = junta.getString("id_padre");
                    id_grupo = junta.getString("id_grupo");
                    fecha = junta.getString("fecha");
                    motivo = junta.getString("motivo");
                    esgrupal = junta.getString("esgrupal");

                    if(esgrupal.equals("1"))
                        ResourcesMan.addJunta(new Junta(
                                motivo,
                                descripcion,
                                Integer.parseInt(id_grupo),
                                Date.valueOf(fecha),
                                true
                        ));
                    if(esgrupal.equals("0"))
                        ResourcesMan.addJunta(new Junta(
                                motivo,
                                descripcion,
                                Integer.parseInt(id_grupo),
                                Date.valueOf(fecha),
                                false
                        ));
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
            adapter = new MeetingListAdapter(contexto,R.layout.list_view_row_report_item,
                    ResourcesMan.getJuntas());
            setListAdapter(adapter);
        }

    }


}
