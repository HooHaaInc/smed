package mx.uson.cc.smed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import mx.uson.cc.smed.util.Group;
import mx.uson.cc.smed.util.SMEDClient;

public class FindGroupActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    List<Group> items = new ArrayList<>();
    GroupAdapter adapter;
    ProgressDialog progress;
    SharedPreferences preferences;
    int request;
    public static final int GETGROUPS = 1;
    public static final int ASKGROUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

        preferences = getSharedPreferences("user",0);
        request = getIntent().getIntExtra("requestCode", -1);
        adapter = new GroupAdapter(this, items);
        ListView list = (ListView)findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        new GroupTask(this,GETGROUPS,0,0).execute();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.REQUEST_STUDENT_LINK){
            if(resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if(request == MainActivity.REQUEST_STUDENT_LINK){
            Intent findStudent = new Intent(this, FindStudentActivity.class);
            findStudent.putExtra("groupId", adapter.getItem(position).getId());
            startActivityForResult(findStudent, request);
            return;
        }

        View convertView = LayoutInflater.from(this).inflate(R.layout.list_item_two_line_with_avatar, null, false);
        ((TextView)convertView.findViewById(R.id.title)).setText(
                adapter.getItem(position).getTeacher());
        ((TextView)convertView.findViewById(R.id.desc)).setText(
                adapter.getItem(position).getShift());
        ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                TextDrawable.builder().buildRound(adapter.getItem(position).getName(), Color.LTGRAY));


        new AlertDialog.Builder(FindGroupActivity.this)
                .setTitle(R.string.sign_to_group_question)
                .setView(convertView)
                .setPositiveButton(R.string.sign_to_group, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: neto, inscribir gente
                        Group g = adapter.getItem(position);
                        Log.v("ID_GRUPO:", "" + g.getId());   //TODO <--- CONSIGO ID DEL GRUPO
                        String id = preferences.getInt(SMEDClient.KEY_ID_STUDENT,0)+"";
                        Log.v("ID_ALUMNO:",id);
                        new GroupTask(FindGroupActivity.this,ASKGROUP,Integer.parseInt(id),g.getId()).execute();
                        Toast.makeText(FindGroupActivity.this,
                                getString(R.string.signed_to)+" "+g.getName(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        getSharedPreferences("user", 0).edit()
                                .putInt(SMEDClient.KEY_ID_GROUP,g.getId())
                                .apply();
                        finish();
                    }
                }).setNegativeButton(R.string.action_cancel, null).create().show();
    }

    public static class GroupAdapter extends ArrayAdapter<Group> {


        public GroupAdapter(Context context, List<Group> list) {
            super(context, android.R.layout.simple_list_item_1, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_two_line_with_avatar, parent, false);
            ((TextView)convertView.findViewById(R.id.title)).setText(
                    getItem(position).getTeacher());
            ((TextView)convertView.findViewById(R.id.desc)).setText(
                    getItem(position).getShift());
            ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                    TextDrawable.builder().buildRound(getItem(position).getName(), Color.LTGRAY));
            return convertView;
        }
    }

    public static class GroupTask extends AsyncTask<Void,Void,Boolean> {

        private final int mTask;
        private int mId_alumno;
        private int mId_grupo;
        FindGroupActivity activity;
        JSONArray grupos = null;
        public GroupTask(FindGroupActivity act,int task,int id_a,int id_g){
            activity = act;
            mTask = task;
            mId_alumno = id_a;
            mId_grupo = id_g;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            //TODO: neto pls
            if(mTask == GETGROUPS) {
                JSONObject result = SMEDClient.getAllGroups();
                try {
                    grupos = result.getJSONArray("grupos");

                    for (int i = 0; i < grupos.length(); ++i) {
                        JSONObject c = null;
                        c = grupos.getJSONObject(i);

                        String id_grupo = c.getString("id_grupo");
                        Log.v("id grupo?", id_grupo);
                        String id_maestro = c.getString("id_maestro");
                        String clave = c.getString("clave");
                        String turno = c.getString("turno");

                        activity.items.add(new Group(Integer.parseInt(id_grupo), clave, turno, "Maestro con ID: " + id_maestro));
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }else{
                return SMEDClient.askForGroup(mId_alumno,mId_grupo);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            activity.progress.dismiss();
            if(success){
                activity.adapter.notifyDataSetChanged();
                if(mTask == GETGROUPS){
                    activity.getSharedPreferences("user", 0).edit()
                            .putInt(SMEDClient.KEY_ID_GROUP, mId_grupo)
                            .apply();
                }
            }
        }
    }

}
