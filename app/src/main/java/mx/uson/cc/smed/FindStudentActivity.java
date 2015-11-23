package mx.uson.cc.smed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.SMEDClient;
import mx.uson.cc.smed.util.Student;

public class FindStudentActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    List<Student> items = new ArrayList<>();
    StudentAdapter adapter;
    SharedPreferences preferences;
    ProgressDialog progress;
    FindStudentActivity act;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);
        preferences = getSharedPreferences("user",0);
        act = this;
        adapter = new StudentAdapter(this, items);
        ListView list = (ListView)findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        new StudentTask(this, getIntent().getIntExtra("groupId", -1),0).execute();
        Log.v("GROUPID:", "" + getIntent().getIntExtra("groupId", -1));
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.loading));
        progress.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_find_group, menu);
        SearchView view = (SearchView)menu.findItem(R.id.action_search_group).getActionView();
        view.setIconifiedByDefault(false);
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        View convertView = LayoutInflater.from(this).inflate(R.layout.list_item_single_line_with_avatar, null, false);
        ((TextView)convertView.findViewById(R.id.title)).setText(
                adapter.getItem(position).toString());
        ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                TextDrawable.builder().buildRound(adapter.getItem(position).getInitials(), Color.LTGRAY));


        new AlertDialog.Builder(FindStudentActivity.this)
                .setTitle(R.string.link_to_student_question)
                .setView(convertView)
                .setPositiveButton(R.string.link_to_student, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: neto, inscribir gente
                        Student g = adapter.getItem(position);
                        Toast.makeText(FindStudentActivity.this,
                                getString(R.string.linked_to)+" "+g.getName(), Toast.LENGTH_SHORT).show();
                        //TODO TASK Alumno conectado con papa
                        String id = preferences.getString(SMEDClient.KEY_ID_PARENT,"0");

                        new StudentTask(act, g.getId(),1,Integer.parseInt(id)).execute();

                        setResult(RESULT_OK);
                        finish();
                    }
                }).setNegativeButton(R.string.action_cancel, null).create().show();
    }

    public static class StudentAdapter extends ArrayAdapter<Student> {


        public StudentAdapter(Context context, List<Student> list) {
            super(context, android.R.layout.simple_list_item_1, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_single_line_with_avatar, parent, false);
            ((TextView)convertView.findViewById(R.id.title)).setText(
                    getItem(position).toString());
            ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                    TextDrawable.builder().buildRound(getItem(position).getInitials(), Color.LTGRAY));
            return convertView;
        }
    }

    public static class StudentTask extends AsyncTask<Void,Void,Boolean> {
        JSONArray alumnos;
        FindStudentActivity activity;
        int groupId;
        int mTask=0;
        int id_padre;

        public StudentTask(FindStudentActivity act, int groupId,int task){
            activity = act;
            this.groupId = groupId;
            mTask = task;
        }
        public StudentTask(FindStudentActivity act,int groupId,int task,int id){
            activity = act;
            this.groupId = groupId;
            mTask = task;
            id_padre = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //TODO: neto pls
            if(mTask == 1){
                Log.v("IDS","PADRE: "+id_padre + "  ALUMNO: "+groupId);
                if(SMEDClient.connectParentStudent(groupId,id_padre))
                    Log.v("D:","se hizo?");
                else Log.v("no","no");
                return true;
            }else{
                JSONObject result = SMEDClient.getAllStudentsFromGroup(groupId);

                try{
                    alumnos = result.getJSONArray("alumnos");

                    for(int i=0;i<alumnos.length();++i){
                        JSONObject a = null;
                        a = alumnos.getJSONObject(i);

                        String id_alumno = a.getString("id_alumno");
                        String nombre = a.getString("nombre");
                        String apellido_paterno = a.getString("apellido_paterno");
                        String id_padre = a.getString("id_padre");

                        activity.items.add(new Student(Integer.parseInt(id_alumno),nombre,apellido_paterno,null));
                    }
                    return true;
                }catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            activity.progress.dismiss();
            if(success){
                activity.adapter.notifyDataSetChanged();
            }
        }
    }

}
