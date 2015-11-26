package mx.uson.cc.smed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.Calendar;

import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.SMEDClient;

public class AddReportActivity extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);
        preferences = getSharedPreferences("user",0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_report, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
    public void submitReport(View V){
        Intent i = this.getIntent();
        //mete aqui al batito que mando al reporte-- posible con el nuevo pull?
        String acusador = "Batito"; //TODO funcion getUserInfo (getNombre)
        TextView tv = (TextView)findViewById(R.id.desc_field);
        String descripcion = tv.getText().toString();
        Calendar GC = Calendar.getInstance();

        //Reporte r = new Reporte(acusador,descripcion,new Date(GC.getTimeInMillis()));

        int id = preferences.getInt(SMEDClient.KEY_ID_STUDENT,0);

        new newReport(id,descripcion,new Date(GC.getTimeInMillis())).execute();
        //TODO ^ poner ahi luego un string con el nombre?
        //i.putExtra("Reporte",r);
        this.setResult(RESULT_OK,i);
        finish();
    }

    //TODO añadir clase Report que extienda de asynctask para llamar el script.
    public class newReport extends AsyncTask<Void,Void,String>{

        private final int mId_alumno;
        private final String mComentario;
        private final Date mFecha;

        public newReport(int id_alumno,String comentario,Date fecha){
            mId_alumno = id_alumno;
            mComentario = comentario;
            mFecha = fecha;
        }

        @Override
        protected String doInBackground(Void... params) {
            return SMEDClient.newReporte(mId_alumno,mComentario,mFecha);
        }

        protected void onPostExecute(String res){
            Toast.makeText(AddReportActivity.this, res, Toast.LENGTH_SHORT).show();
            //TODO seguirle
        }
    }
}
