package mx.uson.cc.smed;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pushbots.push.Pushbots;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.concurrent.ExecutionException;

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
        setContentView(R.layout.activity_coordinator_layout);
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
        /*
        try{
            if(preferences.getInt(SMEDClient.KEY_ACCOUNT_TYPE,-1) == 2) {
                Object result = new getGroupID(preferences.getInt(SMEDClient.KEY_ID_TEACHER, -1), preferences.getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1)).execute().get();
            }else if(preferences.getInt(SMEDClient.KEY_ACCOUNT_TYPE,-1) == 3){
                Object result = new getGroupID(preferences.getInt(SMEDClient.KEY_ID_PARENT, -1), preferences.getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1)).execute().get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        */

        new getGroupID(preferences.getInt(SMEDClient.KEY_ID_PARENT, -1), preferences.getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1)).execute();

        setupNavigation();

        Log.v("YAAAA",preferences.getInt(SMEDClient.KEY_ID_GROUP, -1)+"");

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

    public void addGroup(){
        SharedPreferences preferences = getSharedPreferences("user",0);
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
    }

    private void setupNavigation() {
        //actionbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);


        //header
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        SharedPreferences preferences = getSharedPreferences("user", 0);
        View profile = navigationView.getHeaderView(0);
        String name = preferences.getString(SMEDClient.KEY_NAME, "Derp") + " " +
                preferences.getString(SMEDClient.KEY_LASTNAME1, "Gay");
        if(preferences.getString(SMEDClient.KEY_LASTNAME2, null) != null)
            name += " " + preferences.getString(SMEDClient.KEY_LASTNAME2, "Man");
        String[] nms = name.split(" ");
        String initials = "" + nms[0].charAt(0) + nms[1].charAt(0) +
                (nms.length > 2 && nms[2].length() > 0 ? nms[2].charAt(0) : "");
        String group = preferences.getString(SMEDClient.KEY_GROUP_NAME, "1Z");
        profile.setBackgroundColor(Color.rgb(0xfb, 0x8c, 0x00)); //naranjita smed
        ((ImageView) profile.findViewById(R.id.icon)).setImageDrawable(
                TextDrawable.builder().buildRound(initials, Color.LTGRAY));
        ((TextView)profile.findViewById(R.id.title)).setText(name);
        ((TextView)profile.findViewById(R.id.desc)).setText(group);

        //menu
        Menu menu = navigationView.getMenu();
        final DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                drawer.closeDrawer(GravityCompat.START);
                //noinspection SimplifiableIfStatement
                if (id == R.id.action_settings) {
                    return true;
                }
                if (id == R.id.action_log_out) {
                    getSharedPreferences("user", 0).edit()
                            .clear()
                            .apply();

                    Intent logout = new Intent(MainActivity.this, LoginActivity.class);
                    logout.putExtra("logout", true);
                    startActivityForResult(logout, REQUEST_LOGIN);
                    return true;
                }
                if (id == R.id.action_find_students) {
                    Intent connect = new Intent(MainActivity.this, ConnectToStudentsActivity.class);
                    startActivityForResult(connect, REQUEST_CONNECTION);
                }
                if (id == R.id.create) {
                    addHomeworkButton(null);
                }
                if (id == R.id.action_view_report) {
                    Bundle b = new Bundle();
                    b.putSerializable("frag", ReportListFragment.class);
                    b.putSerializable("frag2", ReadReportFragment.class);
                    b.putBoolean("list", true);
                    try {
                        changeFragments(b, null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }

                }
                if (id == R.id.action_view_meeting) {
                    Bundle b = new Bundle();
                    b.putSerializable("frag", MeetingListFragment.class);
                    b.putSerializable("frag2", ReadMeetingFragment.class);
                    b.putBoolean("list", true);
                    try {
                        changeFragments(b, null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }

                }
                if (id == R.id.action_view_homework) {
                    Bundle b = new Bundle();
                    b.putSerializable("frag", MainActivityFragment.class);
                    b.putSerializable("frag2", HomeworkFragment.class);
                    b.putBoolean("list", true);
                    try {
                        changeFragments(b, null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
                if (id == R.id.action_add_report) {
                    Intent i = new Intent(MainActivity.this, AddReportActivity.class);
                    startActivityForResult(i, ADD_REPORT);

                }
                if (id == R.id.action_add_meeting) {
                    Intent i = new Intent(MainActivity.this, AddMeetingActivity.class);
                    startActivityForResult(i, ADD_MEETING);

                }
                return true;
            }
        });
        if(getSharedPreferences("user",0).getInt(SMEDClient.KEY_ID_GROUP, -1) == -1){
            menu.removeItem(R.id.create);
            menu.removeItem(R.id.action_find_students);
            menu.removeItem(R.id.action_view_report);
            menu.removeItem(R.id.action_view_meeting);
            menu.removeItem(R.id.action_add_meeting);
            menu.removeItem(R.id.action_add_report);
            menu.removeItem(R.id.action_view_homework);
            return;
        }

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
                //Reporte a = (Reporte)data.getSerializableExtra("Reporte");
                //ResourcesMan.addReporte(a);
            }
        }
        if (requestCode == ADD_MEETING) {
            if (resultCode == RESULT_OK) {
                // Un reporte fue subido. Lo insertaremos a la lista de reportes.
                //Junta j = (Junta)data.getSerializableExtra("Junta");
                //ResourcesMan.addJunta(j);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!dobleFragment) return false;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.removeItem(R.id.action_find_students);
        menu.removeItem(R.id.action_view_report);
        menu.removeItem(R.id.action_view_meeting);
        menu.removeItem(R.id.action_add_meeting);
        menu.removeItem(R.id.action_add_report);
        menu.removeItem(R.id.action_view_homework);
        menu.removeItem(R.id.action_log_out);
        menu.removeItem(R.id.action_settings);
        if(getSharedPreferences("user",0).getInt(SMEDClient.KEY_ID_GROUP, -1) == -1){
            menu.removeItem(R.id.create);

            return true;
        }

        if(account_type != SMEDClient.TEACHER)
            menu.removeItem(R.id.create);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.create){
            addHomeworkButton(null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        /*/TODO: fuck the police
        View find = findViewById(R.id.view_find_group);
        if(find.getVisibility() == View.VISIBLE){
            find.setVisibility(View.GONE);
            Fragment frag = new MainActivityFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentLayout, frag)
                    .commit();
            new GetHomework(this).execute();
            return;
        }*/
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer);
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if (fm.getBackStackEntryCount() > 0 )
            goBack();
        else super.onBackPressed();
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
                        getSharedPreferences("user",0).edit().putString(SMEDClient.KEY_GROUP_NAME,name);
                        //TODO Ahí ta la forma pedorra de poner el grupo, una chila Nan? 8)
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
    public void changeFragments(Bundle b, View sharedElement) throws IllegalAccessException,
            InstantiationException, NullPointerException {
        Fragment f;

        Class<? extends Fragment> fragclass =
                (Class<? extends Fragment>)b.getSerializable("frag");
        if(dobleFragment) {
            if(b.getBoolean("list", false)){
                if(fm.findFragmentById(R.id.fragmentLayout).getClass().equals(fragclass)){
                    return;
                }
                Class<? extends Fragment> fragclass2 =
                        (Class<? extends Fragment>)b.getSerializable("frag2");
                f = fragclass.newInstance();
                Fragment f2 = fragclass2.newInstance();
                fm.beginTransaction()
                        .replace(R.id.fragmentLayout, f)
                        .replace(R.id.fragmentLayout2, f2)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
            }else {
                f = fragclass.newInstance();
                f.setArguments(b);
                fm.beginTransaction()
                        .replace(R.id.fragmentLayout2, f)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
            }
        }else{

            if(fm.findFragmentById(R.id.fragmentLayout).getClass().equals(fragclass)){
                return;
            }
            f = fragclass.newInstance();
            f.setArguments(b);
            //f.setSharedElementEnterTransition(¿Transition?);
            FragmentTransaction ft = fm.beginTransaction();

            ft.replace(R.id.fragmentLayout, f);
            //ft.setTransitionStyle(1)
            /*if(sharedElement != null) {
                //ft.addSharedElement(sharedElement, b.getString("transitionName"));
                //ft.setTransitionStyle(R.transition.homework_selected_transition);
            }*/
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);
            ft.commit();

            if(fab != null) {
                if (f instanceof MainActivityFragment) {
                    if (account_type == SMEDClient.TEACHER)
                        fab.show();
                } else fab.hide();
            }
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
            Log.v("id",groupId+"");
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
                    .putString(SMEDClient.KEY_GROUP_NAME, groupName)
                    .putInt(SMEDClient.KEY_ID_GROUP, Integer.parseInt(groupId))
                    .apply();
            recreate();
        }
    }

    private class getGroupID extends AsyncTask<Void,Void,String>{
        private int id,tipo;
        public getGroupID(int id_persona,int tipo_usuario){
            id = id_persona;
            tipo = tipo_usuario;
        }

        @Override
        protected String doInBackground(Void... params) {
            return Integer.toString(SMEDClient.getGroupID(id,tipo));
        }

        @Override
        protected void onPostExecute(String s) {
            SharedPreferences preferences = getSharedPreferences("user",0);
            Log.v("nana", s);
            if(preferences.getInt(SMEDClient.KEY_ID_GROUP,-2) == -2)
                preferences.edit().putInt(SMEDClient.KEY_ID_GROUP,Integer.parseInt(s)).apply();
            addGroup();
        }
    }
}
