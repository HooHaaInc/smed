package mx.uson.cc.smed;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import mx.uson.cc.smed.util.Junta;

public class AddMeetingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
        /*TimePicker.OnTimeChangedListener*/, View.OnClickListener,
        TimePickerDialog.OnTimeSetListener{

    Date date;
    Time time;

    Button fechaBtn = null;
    EditText titulo;
    EditText desc;
    int padre;


    SimpleDateFormat formatter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meeting);


        fechaBtn = (Button)findViewById(R.id.fecha_calendario);
        String fecha;

        date =  new Date(Calendar.getInstance().getTimeInMillis());
        Locale locale = getResources().getConfiguration().locale;
        formatter = new SimpleDateFormat("MMM dd, E kk:mm", locale);
        fecha = formatter.format(date);
        fechaBtn.setText(fecha);

        fechaBtn.setOnClickListener(this);
        titulo = (EditText) findViewById(R.id.editTitulo);
        desc = (EditText) findViewById(R.id.editDesc);
        findViewById(R.id.back_to_list).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.submit:
                if(!datosValidos())
                    return;
                /// TODO, aqui va el asynctask de la tarea
                Intent i = this.getIntent();
                EditText et = (EditText)findViewById(R.id.editDesc);
                String desc = et.getText().toString();
                et = (EditText)findViewById(R.id.editTitulo);
                String titulo = et.getText().toString();
                et = (EditText)findViewById(R.id.spinner_junta);
                String padre = et.getText().toString();
                boolean juntaGrupal = false;
                if(padre.length() == 0) juntaGrupal = true;
                Calendar GC = new GregorianCalendar();
                
                java.sql.Date fecha =  new java.sql.Date(GC.getTimeInMillis());
                Junta j = new Junta(titulo,desc,0/*ID del padre*/,fecha,juntaGrupal);
                i.putExtra("Junta",j);
                this.setResult(RESULT_OK, i);
                finish();
                break;
            case R.id.back_to_list:
                finish();
                break;
            case R.id.fecha_calendario:
                Log.v("calendario", "entro?");
                Calendar calendar = Calendar.getInstance(v.getResources().getConfiguration().locale);
                calendar.setTime(date);
                new DatePickerDialog(this, this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.delete_meeting:
                Log.v("delete", "entro?");
        }
    }
    public boolean datosValidos(){
        if(TextUtils.isEmpty(titulo.getText())) {
            titulo.setError(getString(R.string.error_title_empty));
            titulo.requestFocus();
            return false;
        }if(TextUtils.isEmpty(desc.getText())){
            desc.setError(getString(R.string.error_desc_empty));
            desc.requestFocus();
            return false;
        }
        java.util.Date now = new java.util.Date();
        Calendar cal = Calendar.getInstance(getResources().getConfiguration().locale);
        cal.setTime(now);
        int year = //cal.get(Calendar.MONTH) < Calendar.JULY ?
                cal.get(Calendar.YEAR);// : (cal.get(Calendar.YEAR) + 1);
        //TODO: remove this v
        String deadEnd = cal.get(Calendar.MONTH) < Calendar.JULY
                ? (year+"-06-01") : (year+"-12-18");
        Date endYear = Date.valueOf(deadEnd); //year+"-07-01" +|-
        if(date.compareTo(new java.util.Date()) < 1 || date.compareTo(endYear) > -1){
            fechaBtn.setError(getString(R.string.error_incorrect_date));
            fechaBtn.requestFocus();
            return false;
        }
        return true;
    }
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if(view.isShown()) {
            this.date = Date.valueOf(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            Log.d("AddMeetingDate", date.toString());
            new TimePickerDialog(this, this, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true).show();
        }
    }
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        if(view.isShown()) {
            long hourInMillis = (hourOfDay * 60 + minute) * 60 * 1000;
            date = new Date(date.getTime() + hourInMillis);

            fechaBtn.setText(formatter.format(date));
        }
    }
}
