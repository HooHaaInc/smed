package mx.uson.cc.smed;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOGIN = 1;

    FragmentManager fm = getFragmentManager();
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LoginActivity
        SharedPreferences preferences = getSharedPreferences("user", 0);
        if(preferences.getBoolean("login", false)){
            Intent login = new Intent(this, LoginActivity.class);
            startActivityForResult(login, REQUEST_LOGIN);
        }


        //Insertando el fragment
        //FragmentManager fm = getFragmentManager();
        //FragmentTransaction ft = fm.beginTransaction();
        setContentView(R.layout.activity_main);
        if (fm.findFragmentById(R.id.content) == null) {
            MainActivityFragment Tlist = new MainActivityFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.content, Tlist);
            //ft.addToBackStack(null);
            ft.commit();
        }
        fab = (FloatingActionButton)findViewById(R.id.fab_nueva_tarea);
        //((TextView)findViewById(R.id.hello)).setText("Hello "
       //         + getIntent().getExtras().getString("name", ""));
        System.out.println("derp");
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
        }

        return super.onOptionsItemSelected(item);
    }
    public void changeFragments(Fragment f){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, f);
        ft.addToBackStack(null);
        ft.commit();

        fab.hide();
        //setContentView(R.layout.activity_main);


    }
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void goBack(){
        getFragmentManager().popBackStack();
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
}
