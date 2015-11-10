package mx.uson.cc.smed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import mx.uson.cc.smed.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOGIN = 1;
    public static final int ADD_HOMEWORK = 2;
    public static final int EDIT_HOMEWORK = 3;
    public static final int DELETE_HOMEWORK = 4;
    public static final int ADD_REPORT = 5;
    public static final int ADD_MEETING = 6;

    public static final int REQUEST_CONNECTION = 20;
    public static final int REQUEST_GROUP = 21;
    public static final int REQUEST_STUDENT_LINK = 22;

    public int account_type;

    ArrayAdapter adapter;
    boolean dobleFragment = false;

    FragmentManager fm = getSupportFragmentManager();
    FloatingActionButton fab = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        account_type = getSharedPreferences("user", 0)
                .getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1);
        System.out.println("account_type " + account_type);
        fab = (FloatingActionButton)findViewById(R.id.fab_nueva_tarea);
        if(fab != null && account_type != SMEDClient.TEACHER){
            fab.setVisibility(View.GONE);
            fab = null;
        }
        Fragment frag;
        if(findViewById(R.id.fragmentLayout2) != null)
            dobleFragment = true;
        if(savedInstanceState == null) { //Se crea por primera vez
            //LoginActivity
            SharedPreferences preferences = getSharedPreferences("user", 0);
            if (!preferences.getBoolean("login", false)) {
                Intent login = new Intent(this, LoginActivity.class);
                startActivityForResult(login, REQUEST_LOGIN);
            }
            //buscar grupo si no está en uno o pertenece al grupo default
            if(preferences.getInt(SMEDClient.KEY_ID_GROUP, 1) == 1 &&
                    account_type != SMEDClient.TEACHER){
                View find = findViewById(R.id.view_find_group);
                find.setVisibility(View.VISIBLE);
                if(account_type == SMEDClient.PARENT) {
                    ((TextView)find.findViewById(R.id.find_text)).setText(R.string.no_son);
                    ((Button)find.findViewById(R.id.find_button)).setText(R.string.find_student);
                    find.findViewById(R.id.wifi_button).setVisibility(View.GONE);
                }
            }else {
                //start fragment
                frag = new MainActivityFragment();
                fm.beginTransaction()
                        .add(R.id.fragmentLayout, frag)
                        .commit();
                new GetHomework(this).execute();
            }
        }else{
            if(findViewById(R.id.fragmentLayout2) != null) {
                dobleFragment = true;
            }
            frag = fm.findFragmentById(R.id.fragmentLayout);

            if(frag instanceof ListFragment){
                if(frag instanceof MainActivityFragment){
                    adapter = new HomeworkListAdapter(this,
                            android.R.layout.simple_list_item_1,
                            ResourcesMan.getTareas());
                }
                if(frag instanceof ReportListFragment){
                    adapter = new ReportListAdapter(this,
                            android.R.layout.simple_list_item_1,
                            ResourcesMan.getReportes());
                }
                if(frag instanceof MeetingListFragment){
                    adapter = new MeetingListAdapter(this,
                            android.R.layout.simple_list_item_1,
                            ResourcesMan.getJuntas());

                }
                ((ListFragment) frag).setListAdapter(adapter);
            }else{

                getSupportActionBar().hide();


            }
            if(frag instanceof MainActivityFragment){
                new GetHomework(this).execute();


            }



        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_LOGIN){
            if(resultCode == RESULT_OK){
                recreate();
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
                int pos = ResourcesMan.editTarea(new Tarea(id,1, titulo, desc, materia, fecha));
                //adapter.notifyDataSetChanged();
                ((HomeworkFragment)fm.findFragmentById(!dobleFragment
                        ?R.id.fragmentLayout : R.id.fragmentLayout2)).setTarea(pos);
            }
        }
        if(requestCode == DELETE_HOMEWORK){
            if(resultCode == RESULT_OK){
                //TODO Google Cloud
                int id = data.getIntExtra("Id", -1);
                Date fecha = (Date)data.getSerializableExtra("fecha");
                ResourcesMan.eliminarTarea(new Tarea(id,1,"","","",fecha));
                adapter.notifyDataSetChanged();
            }
        }if(requestCode == REQUEST_GROUP){
            if(resultCode == RESULT_OK){
                //TODO: su solicitud a sido enviada
                    View find = findViewById(R.id.view_find_group);
                    find.setVisibility(View.GONE);
                    //start fragment
                    fm.beginTransaction()
                            .add(R.id.fragmentLayout, new MainActivityFragment())
                            .commit();
                    new GetHomework(this).execute();
            }
        }if(requestCode == REQUEST_STUDENT_LINK){
            if(resultCode == RESULT_OK){
                //TODO: su solicitud a sido enviada
                View find = findViewById(R.id.view_find_group);
                find.setVisibility(View.GONE);
                //start fragment
                fm.beginTransaction()
                        .add(R.id.fragmentLayout, new MainActivityFragment())
                        .commit();
                new GetHomework(this).execute();
            }
        }
        if (requestCode == ADD_REPORT) {
            if (resultCode == RESULT_OK) {
                // Un reporte fue subido. Lo insertaremos a la lista de reportes.
                Reporte a = (Reporte)data.getSerializableExtra("Reporte");
                ResourcesMan.addReporte(a);
            }
        }
        if (requestCode == ADD_MEETING) {
            if (resultCode == RESULT_OK) {
                // Un reporte fue subido. Lo insertaremos a la lista de reportes.
                Junta j = (Junta)data.getSerializableExtra("Junta");
                ResourcesMan.addJunta(j);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(!dobleFragment || account_type != SMEDClient.TEACHER)
            menu.removeItem(R.id.create);
        if(account_type != SMEDClient.TEACHER) {
            menu.removeItem(R.id.action_find_students);
            menu.removeItem(R.id.action_find_parents);
        }
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
            getSharedPreferences("user", 0).edit()
                    .clear()
                    .apply();

            Intent logout = new Intent(this, LoginActivity.class);
            logout.putExtra("logout", true);
            startActivityForResult(logout, REQUEST_LOGIN);
            return true;
        } if (id == R.id.action_find_students) {
            Intent connect = new Intent(this, ConnectToStudentsActivity.class);
            startActivityForResult(connect, REQUEST_CONNECTION);
        } if(id == R.id.create){
            addHomeworkButton(null);
        }if(id == R.id.action_view_report){
            Bundle b = new Bundle();
            b.putSerializable("frag", ReportListFragment.class);
            try {
                changeFragments(b);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }
        if(id == R.id.action_view_meeting){
            Bundle b = new Bundle();
            b.putSerializable("frag", MeetingListFragment.class);
            try {
                changeFragments(b);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }
        if(id == R.id.action_add_report){
            Intent i = new Intent(this,AddReportActivity.class);
            startActivityForResult(i, ADD_REPORT);

        }
        if(id == R.id.action_add_meeting){
            Intent i = new Intent(this,AddMeetingActivity.class);
            startActivityForResult(i, ADD_MEETING);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fm.getBackStackEntryCount() > 0 ){
            goBack();
        } else {
            super.onBackPressed();
        }
    }


    public void setAdapter(ArrayAdapter a){
        adapter = a;

    }
    public void findGroup(View v){
        Intent findGroup = new Intent(this, FindGroupActivity.class);
        int requestCode = account_type == SMEDClient.STUDENT ? REQUEST_GROUP : REQUEST_STUDENT_LINK;
        findGroup.putExtra("requestCode", requestCode);
        startActivityForResult(findGroup, requestCode);
    }

    public void wifidirect(View v){
        Intent connect = new Intent(this, ConnectToGroupActivity.class);
        startActivityForResult(connect, REQUEST_GROUP);
    }

    public void addHomeworkButton(View v){
        Intent i = new Intent(this,AddHomeworkActivity.class);
        startActivityForResult(i, ADD_HOMEWORK);
    }

    /**
     * Cambia o actualiza el fragment de detalles
     * @param b Bundle que contiene la posicion del detalle en
     *          ResourcesMan y el fragment de detalles .class
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NullPointerException
     */
    public void changeFragments(Bundle b) throws IllegalAccessException,
            InstantiationException, NullPointerException {
        if(dobleFragment) {
            fm.findFragmentById(R.id.fragmentLayout2).setArguments(b);
        }else{
            Fragment f;

            Class<? extends Fragment> fragclass =
                    (Class)b.getSerializable("frag");
            f = fragclass.newInstance();
            f.setArguments(b);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragmentLayout, f);
            ft.addToBackStack(null);
            ft.commit();
            if(fab != null)
                fab.hide();
            getSupportActionBar().hide();
        }
    }

    @SuppressLint("NewApi")
    public void goBack(){
        if(!dobleFragment){
            fm.popBackStackImmediate();
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentLayout);
            if(f instanceof MainActivityFragment) {
                adapter = new HomeworkListAdapter(this,
                        android.R.layout.simple_list_item_1,
                        ResourcesMan.getTareas());
            }
            if(f instanceof ReportListFragment) {
                adapter = new ReportListAdapter(this,
                        android.R.layout.simple_list_item_1,
                        ResourcesMan.getReportes());
            }
            ((ListFragment) fm.findFragmentById(R.id.fragmentLayout)).setListAdapter(adapter);
            //System.out.println("onBack: "+currentFragment.toString());

            if(fab != null)
                fab.show();
            setStatusBarColor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    ? getResources().getColor(R.color.primaryDark, null)
                    : getResources().getColor(R.color.primaryDark));
        }
    }

    public void hideFab(){
        if(fab != null)
        fab.hide();
    }

    public void setStatusBarColor(int color){
        if (!dobleFragment && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(color);
    }


    static class GetHomework extends AsyncTask<Void,Void,Boolean> {
        MainActivity mainActivity;

        

        GetHomework(MainActivity ma){
            mainActivity = ma;
        }

        JSONArray hw = null;

        @Override
        protected Boolean doInBackground(Void... voids) {
                ResourcesMan.quitarTareas();
                JSONObject result = SMEDClient.getAllHomework();
                try {
                    hw = result.getJSONArray("tareas");

                    for (int i = 0; i < hw.length(); ++i) {
                        JSONObject c = null;
                        c = hw.getJSONObject(i);

                        String id_tarea = c.getString("id_tarea");
                        Log.v("id tarea?", id_tarea);
                        String id_grupo = c.getString("id_grupo");
                        String titulo = c.getString("titulo");
                        String desc = c.getString("descripcion");
                        String materia = c.getString("materia");
                        String fecha = c.getString("fecha");

                        ResourcesMan.addTarea(new Tarea(
                                Integer.parseInt(id_tarea),
                                Integer.parseInt(id_grupo),
                                titulo,
                                desc,
                                materia,
                                Date.valueOf(fecha)));
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
        protected void onPostExecute(Boolean res) {
            ListFragment frag = (ListFragment)mainActivity.fm.findFragmentById(R.id.fragmentLayout);
            mainActivity.adapter = new HomeworkListAdapter(mainActivity,
                    android.R.layout.simple_list_item_1,
                    ResourcesMan.getTareas());
            frag.setListAdapter(mainActivity.adapter);
            if(mainActivity.dobleFragment){
                Fragment fragm = mainActivity.fm.findFragmentById(R.id.fragmentLayout2);
                if(fragm == null)
                    fragm = new HomeworkFragment();
                Bundle bundle = new Bundle();
                //TODO: if(tareas.size() ==0) "No hay tareas, yey";
                bundle.putInt("position", 0);
                fragm.setArguments(bundle);
                mainActivity.fm.beginTransaction()
                        .add(R.id.fragmentLayout2, fragm)
                        .commit();
            }
        }
    }
}
