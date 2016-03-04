package mx.uson.cc.smed;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import mx.uson.cc.smed.util.Junta;
import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.SMEDClient;
import mx.uson.cc.smed.util.Student;

/**
 * Created by Hans on 03/03/2016.
 */
public class GroupListFragment extends ListFragment {
    Context contexto;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        contexto = inflater.getContext();

        new getStudents(contexto.getSharedPreferences("user",0).getInt(SMEDClient.KEY_ID_GROUP,-1)).execute();
        StudentListAdapter adapter = new StudentListAdapter(inflater.getContext(),R.layout.list_item_single_line_with_avatar,ResourcesMan.getEstudiantes());
        setListAdapter(adapter);
        getActivity().setTitle(R.string.students);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle b  = new Bundle();
        ArrayList<Student> s = ResourcesMan.getEstudiantes();
        b.putSerializable("frag", ReadStudentFragment.class);
        b.putSerializable("estudiante", s.get(position));
        try {
            ((MainActivity)getActivity()).changeFragments(b, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
    }

    private class getStudents extends AsyncTask<Void,Void,Boolean>{
        StudentListAdapter adapter;
        JSONArray estudiantes;
        JSONObject estudiante;
        String id_persona,id_padre,id_alumno,nombre,apellido_paterno, apellido_materno,numPadre,correoPadre;
        int id_grupo;

        public getStudents(int id){
            id_grupo = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ResourcesMan.quitarEstudiantes();
            JSONObject result = SMEDClient.getAllStudentsFromGroup(id_grupo);
            try{
                estudiantes = result.getJSONArray("alumnos");

                for(int i=0;i<estudiantes.length();++i){
                    estudiante = null;
                    estudiante = estudiantes.getJSONObject(i);
                    id_persona = estudiante.getString("id_persona");
                    id_alumno = estudiante.getString("id_alumno");
                    nombre = estudiante.getString("nombre");
                    apellido_paterno = estudiante.getString("apellido_paterno");
                    //apellido_materno = estudiante.getString("apellido_materno");
                    id_padre = estudiante.getString("id_padre");

                    ResourcesMan.addStudent(new Student(Integer.parseInt(id_alumno),nombre,apellido_paterno,apellido_materno,id_padre,numPadre,correoPadre));

                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            adapter = new StudentListAdapter(contexto,R.layout.list_item_single_line_with_avatar,ResourcesMan.getEstudiantes());
            setListAdapter(adapter);
        }
    }
}
