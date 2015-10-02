package mx.uson.cc.smed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.Date;
import java.util.GregorianCalendar;


public class AddHomeworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_homework);
        Spinner listMaterias = (Spinner)findViewById(R.id.spinner_materia);
        String[] als = getResources().getStringArray(R.array.class_types);
        ArrayAdapter<String> AS = new ArrayAdapter<String>(this,R.layout.spinner_item,als);
        AS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listMaterias.setAdapter(AS);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_homework, menu);
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
    public void butClicked(View v){
        Intent i = this.getIntent();
        TextView titulo = (TextView)findViewById(R.id.editTitulo);

        TextView desc = (TextView)findViewById(R.id.editDesc);
        Spinner list = (Spinner)findViewById(R.id.spinner_materia);
        DatePicker DP = (DatePicker)findViewById(R.id.fecha_calendario);
        System.out.println("DateFecha:");
        System.out.println(DP.getYear() + "--" + DP.getMonth() + "--" + DP.getDayOfMonth());
        GregorianCalendar GC = new GregorianCalendar(DP.getYear(),DP.getMonth(),DP.getDayOfMonth());
        Date D = new java.sql.Date(GC.getTime().getTime());
        System.out.println(D.getTime());
        i.putExtra("TituloTarea", titulo.getText().toString());
        i.putExtra("DescTarea",desc.getText().toString());
        i.putExtra("MateriaTarea", Tarea.getCourseString(list.getSelectedItem().toString()));
        i.putExtra("FechaTarea", D);
        this.setResult(RESULT_OK, i);
        finish();
    }
}
