package mx.uson.cc.smed;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;

import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.SMEDClient;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOGIN = 1;
    public static final int ADD_HOMEWORK = 2;
    public static final int EDIT_HOMEWORK = 3;
    public static final int REQUEST_CONNECTION = 20;

    //Class<? extends Fragment> currentFragment = MainActivityFragment.class;

    ArrayAdapter adapter;

    FragmentManager fm = getFragmentManager();
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton)findViewById(R.id.fab_nueva_tarea);
        Fragment frag;
        if(savedInstanceState == null) {
            //LoginActivity
            SharedPreferences preferences = getSharedPreferences("user", 0);
            if (!preferences.getBoolean("login", false)) {
                Intent login = new Intent(this, LoginActivity.class);
                startActivityForResult(login, REQUEST_LOGIN);
            }
            frag = new MainActivityFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentLayout, frag)
                    .commit();

            new GetHomework().execute();
        }else{
            frag = fm.findFragmentById(R.id.fragmentLayout);
            if(frag instanceof ListFragment){
                adapter = new HomeworkListAdapter(this,
                        android.R.layout.simple_list_item_1,
                        ResourcesMan.getTareas());
                ((ListFragment) frag).setListAdapter(adapter);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_LOGIN){
            if(resultCode == RESULT_OK){
                Bundle userData = data.getExtras(); //TODO: get all data
                SharedPreferences.Editor preferences = getSharedPreferences("user", 0).edit();
                preferences.putBoolean("login", true);
                preferences.apply();
            }else finish();
        }
        if (requestCode == ADD_HOMEWORK) {
            if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
                Toast.makeText(this, "yay, checa el servidor", Toast.LENGTH_SHORT).show();
                //TODO: Google Cloud
                String titulo = data.getStringExtra("TituloTarea");
                String desc = data.getStringExtra("DescTarea");
                String materia = data.getStringExtra("MateriaTarea");
                Date fecha = (Date) data.getSerializableExtra("FechaTarea");
                ResourcesMan.addTarea(new Tarea(titulo, desc, materia, fecha));
                adapter.notifyDataSetChanged();
            }
        }
        if(requestCode == EDIT_HOMEWORK){
            if(resultCode == RESULT_OK){
                //TODO: Google Cloud
                String titulo = data.getStringExtra("TituloTarea");
                String desc = data.getStringExtra("DescTarea");
                String materia = data.getStringExtra("MateriaTarea");
                Date fecha = (Date) data.getSerializableExtra("FechaTarea");
                int id = data.getIntExtra("Id", -1);
                ResourcesMan.editTarea(new Tarea(id, titulo, desc, materia, fecha));
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } if (id == R.id.action_log_out) {
            SharedPreferences.Editor preferences = getSharedPreferences("user", 0).edit();
            preferences.putBoolean("login", false);
            preferences.apply();

            Intent logout = new Intent(this, LoginActivity.class);
            logout.putExtra("logout", true);
            startActivityForResult(logout, REQUEST_LOGIN);
            return true;
        } if (id == R.id.action_connect) {
            Intent connect = new Intent(this, GroupConnectionActivity.class);
            startActivityForResult(connect, REQUEST_CONNECTION);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void addHomeworkButton(View v){
        Intent i = new Intent(this,AddHomeworkActivity.class);
        startActivityForResult(i, ADD_HOMEWORK);
    }

    public void changeFragments(Fragment f){
        //currentFragment = f.getClass();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentLayout, f);
        ft.addToBackStack(null);
        ft.commit();

        fab.hide();
    }

    public void goBack(){
        fm.popBackStackImmediate();
        adapter = new HomeworkListAdapter(this,
                android.R.layout.simple_list_item_1,
                ResourcesMan.getTareas());
        ((ListFragment)fm.findFragmentById(R.id.fragmentLayout)).setListAdapter(adapter);
        //System.out.println("onBack: "+currentFragment.toString());

        fab.show();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.primaryDark, null));
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.primaryDark));
    }

    public void hideFab(){
        fab.hide();
    }

    class GetHomework extends AsyncTask<Void,Void,Boolean> {

        JSONArray hw = null;

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(!ResourcesMan.initialized) {
                JSONObject result = SMEDClient.getAllHomework();
                try {
                    hw = result.getJSONArray("tareas");

                    for (int i = 0; i < hw.length(); ++i) {
                        JSONObject c = null;
                        c = hw.getJSONObject(i);

                        String id_tarea = c.getString("id_tarea");
                        String id_grupo = c.getString("id_grupo");
                        String titulo = c.getString("titulo");
                        String desc = c.getString("descripcion");
                        String materia = c.getString("materia");
                        String fecha = c.getString("fecha");

                        ResourcesMan.addTarea(new Tarea(
                                Integer.parseInt(id_tarea),
                                titulo,
                                desc,
                                materia,
                                Date.valueOf(fecha)));
                    }
                    return true;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean res) {
            ResourcesMan.initialized = res;
            ListFragment frag = (ListFragment)fm.findFragmentById(R.id.fragmentLayout);
            adapter = new HomeworkListAdapter(MainActivity.this,
                    android.R.layout.simple_list_item_1,
                    ResourcesMan.getTareas());
            frag.setListAdapter(adapter);
        }
    }
}
