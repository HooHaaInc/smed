package mx.uson.cc.smed;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pushbots.push.Pushbots;

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
    public static final int EDIT_MEETING = 7;
    public static final int CANCEL_MEETING = 8;

    public static final int REQUEST_CONNECTION = 20;
    public static final int REQUEST_GROUP = 21;
    public static final int REQUEST_STUDENT_LINK = 22;

    public int account_type;

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
        fab = (FloatingActionButton) findViewById(R.id.fab_nueva_tarea);
        if (fab != null && account_type != SMEDClient.TEACHER) {
            fab.setVisibility(View.GONE);
            fab = null;
        }
        Fragment frag;

        Pushbots.sharedInstance().init(this);
        Pushbots.sharedInstance().setPushEnabled(true);

        if(findViewById(R.id.fragmentLayout2) != null)
            dobleFragment = true;

        //LoginActivity
        SharedPreferences preferences = getSharedPreferences("user", 0);
        if (!preferences.getBoolean("login", false)) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivityForResult(login, REQUEST_LOGIN);
        }
        if (preferences.getInt(SMEDClient.KEY_ID_GROUP, -1) == -1) {
            //buscar grupo si no está en uno o pertenece al grupo default
            View find = findViewById(R.id.view_find_group);
            switch (account_type) {
                case SMEDClient.TEACHER:
                    createGroup();
                    break;
                case SMEDClient.PARENT:
                    ((TextView) find.findViewById(R.id.find_text)).setText(R.string.no_son);
                    ((Button) find.findViewById(R.id.find_button)).setText(R.string.find_student);
                    find.findViewById(R.id.wifi_button).setVisibility(View.GONE);
                case SMEDClient.STUDENT:
                    find.setVisibility(View.VISIBLE);
            }
            return;
        }

        if(savedInstanceState == null || fm.findFragmentById(R.id.fragmentLayout) == null) { //Se crea por primera vez {
        //start fragment
        frag = new MainActivityFragment();
        fm.beginTransaction()
                .add(R.id.fragmentLayout, frag)
                .commit();
        new GetHomework(this).execute();

        }else{
            frag = fm.findFragmentById(R.id.fragmentLayout);
            if(frag instanceof ListFragment){
            }else{
                getSupportActionBar().hide();
            }
            //TODO: new GetHomework(this).execute();

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
                //TODO: notifyDataChanged
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
                //TODO: notifyDataChanged
            }
        }if(requestCode == REQUEST_GROUP){
            if(resultCode == RESULT_OK){
                //TODO: su solicitud a sido enviada
                recreate();
                /*View find = findViewById(R.id.view_find_group);
                find.setVisibility(View.GONE);
                //start fragment
                fm.beginTransaction()
                        .add(R.id.fragmentLayout, new MainActivityFragment())
                        .commit();
                new GetHomework(this).execute();*/
            }
        }if(requestCode == REQUEST_STUDENT_LINK){
            if(resultCode == RESULT_OK){
                //TODO: su solicitud a sido enviada
                recreate();
                /*View find = findViewById(R.id.view_find_group);
                find.setVisibility(View.GONE);
                //start fragment
                fm.beginTransaction()
                        .add(R.id.fragmentLayout, new MainActivityFragment())
                        .commit();
                new GetHomework(this).execute();*/
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
        if(getSharedPreferences("user",0).getInt(SMEDClient.KEY_ID_GROUP, -1) == -1){
            menu.removeItem(R.id.create);
            menu.removeItem(R.id.action_find_students);
            menu.removeItem(R.id.action_view_report);
            menu.removeItem(R.id.action_view_meeting);
            menu.removeItem(R.id.action_add_meeting);
            menu.removeItem(R.id.action_add_report);
            return true;
        }

        if(!dobleFragment || account_type != SMEDClient.TEACHER)
            menu.removeItem(R.id.create);
        switch(account_type){
            case SMEDClient.TEACHER:
                menu.removeItem(R.id.action_add_report);
                break;
            case SMEDClient.STUDENT:
                menu.removeItem(R.id.action_find_students);
                menu.removeItem(R.id.action_view_report);
                menu.removeItem(R.id.action_view_meeting);
                menu.removeItem(R.id.action_add_meeting);
                break;
            case SMEDClient.PARENT:
                menu.removeItem(R.id.action_find_students);
                menu.removeItem(R.id.action_add_report);
                menu.removeItem(R.id.action_add_meeting);
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
        //TODO: fuck the police
        View find = findViewById(R.id.view_find_group);
        if(find.getVisibility() == View.VISIBLE){
            find.setVisibility(View.GONE);
            Fragment frag = new MainActivityFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentLayout, frag)
                    .commit();
            new GetHomework(this).execute();
            return;
        }

        if (fm.getBackStackEntryCount() > 0 ){
            goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void createGroup() {
        new AlertDialog.Builder(this)
                .setView(R.layout.create_group_dialog)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.action_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressDialog progress = new ProgressDialog(MainActivity.this);
                        progress.show();
                        AlertDialog aDialog = (AlertDialog) dialog;
                        String name = ((EditText) aDialog.findViewById(R.id.group_name)).getText().toString();
                        boolean matutino = ((Spinner) aDialog.findViewById(R.id.shift_spinner)).getSelectedItemPosition() == 0;
                        int teacherId = getSharedPreferences("user", 0).getInt(SMEDClient.KEY_ID_TEACHER, -1);
                        new CreateGroupTask(teacherId, name, matutino, progress).execute();
                    }
                }).create().show();
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
                    (Class<? extends Fragment>)b.getSerializable("frag");
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

            //System.out.println("onBack: "+currentFragment.toString());

            if(fab != null && f instanceof MainActivityFragment)
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
                int groupId = mainActivity.getSharedPreferences("user", 0)
                        .getInt(SMEDClient.KEY_ID_GROUP, -1);
                JSONObject result = SMEDClient.getAllHomework(groupId);
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
            HomeworkListAdapter adapter = new HomeworkListAdapter(mainActivity,
                    android.R.layout.simple_list_item_1,
                    ResourcesMan.getTareas());
            frag.setListAdapter(adapter);
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

    private class CreateGroupTask extends AsyncTask<Void,Void,String> {

        private int teacherId;
        private String groupName;
        private boolean morning;
        private ProgressDialog progressDialog;

        public CreateGroupTask(int tId, String name, boolean matutino, ProgressDialog progress) {
            teacherId = tId;
            groupName = name;
            morning = matutino;
            progressDialog = progress;
        }

        @Override
        protected String doInBackground(Void... params) {
            return SMEDClient.createGroup(teacherId, groupName, morning);
        }

        @Override
        protected void onPostExecute(String groupId) {
            progressDialog.dismiss();
            getSharedPreferences("user", 0).edit()
                    .putInt(SMEDClient.KEY_ID_GROUP, Integer.parseInt(groupId))
                    .apply();
            recreate();
        }
    }
}
