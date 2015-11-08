package mx.uson.cc.smed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.sql.Date;
import java.util.Calendar;

import mx.uson.cc.smed.util.Reporte;

public class AddReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

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
        //mete aqui al batito que mando al reporte-- posible con el nuevo push
        TextView tv = (TextView)findViewById(R.id.desc_field);
        String descripcion = tv.getText().toString();
        Calendar GC = Calendar.getInstance();

        Reporte r = new Reporte(1,descripcion,new Date(GC.getTimeInMillis()));
        i.putExtra("Reporte",r);
        this.setResult(RESULT_OK,i);
        finish();
    }
}
