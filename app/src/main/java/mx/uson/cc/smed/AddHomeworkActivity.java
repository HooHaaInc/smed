package mx.uson.cc.smed;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import mx.uson.cc.smed.util.SMEDClient;
import mx.uson.cc.smed.util.Tarea;


public class AddHomeworkActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener{

    private NewHomework mNewHomework;
    Button fechaBtn = null;
    Button delete;
    EditText titulo;
    EditText desc;
    Spinner list;
    SimpleDateFormat formatter;
    private Date date = null;
    private boolean edit = false;
    private int id;

    @Override
    public void onSaveInstanceState(Bundle saveInstance){
        super.onSaveInstanceState(saveInstance);
        saveInstance.putString("date",date.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_homework);

        fechaBtn = (Button)findViewById(R.id.fecha_calendario);
        titulo = (EditText) findViewById(R.id.editTitulo);
        desc = (EditText) findViewById(R.id.editDesc);
        list = (Spinner) findViewById(R.id.spinner_materia);
        delete = (Button)findViewById(R.id.delete_homework);


        if(getIntent().getBooleanExtra("edit", false)){
            edit = true;
            id = getIntent().getIntExtra("id", -1);
            date = (Date)getIntent().getSerializableExtra("date");
            titulo.setText(getIntent().getStringExtra("title"));
            desc.setText(getIntent().getStringExtra("desc"));
            list.setSelection(getIntent().getIntExtra("course", 0));
        }else {
            int pos = (int)(Math.random()*5);
            list.setSelection(pos);
            setBackground(pos);
            findViewById(R.id.delete_homework).setVisibility(View.GONE);
        }
        if(savedInstanceState != null){
            date = Date.valueOf(savedInstanceState.getString("date"));
        }
        if(date == null) {
            java.util.Date tomorrow = new java.util.Date();
            tomorrow.setTime(tomorrow.getTime() + 1000 * 60 * 60 * 24);
            date = new Date(tomorrow.getTime());
        }

        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setBackground(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Locale locale = getResources().getConfiguration().locale;  //cal.setTime(tarea.fecha);
        String fecha;

        formatter = new SimpleDateFormat("MMM dd, E", locale);
        fecha = formatter.format(date);

        fechaBtn.setText(fecha);

        fechaBtn.setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
        findViewById(R.id.back_to_list).setOnClickListener(this);
        findViewById(R.id.delete_homework).setOnClickListener(this);


    }


    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.submit:
                if(!datosValidos())
                    return;
                /// TODO, el 1 está de prueba porque no hay algo que me diga de qué grupo es el maestro, todavía :>

                if(edit)
                    mNewHomework = new NewHomework(id,
                            1, titulo.getText().toString(),
                            desc.getText().toString(),
                            Tarea.getCourseFromArray(list.getSelectedItemPosition()),
                            date);
                else mNewHomework = new NewHomework(1,
                        titulo.getText().toString(),
                        desc.getText().toString(),
                        Tarea.getCourseFromArray(list.getSelectedItemPosition()),
                        date);

                mNewHomework.execute((Void) null);
                v.setEnabled(false);
                break;
            case R.id.back_to_list:
                finish();
                break;
            case R.id.fecha_calendario:
                Log.v("calendario","entro?");
                Calendar calendar = Calendar.getInstance(v.getResources().getConfiguration().locale);
                calendar.setTime(date);
                new DatePickerDialog(this, this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.delete_homework:
                Log.v("delete","entro?");
                mNewHomework = new NewHomework(id);
                mNewHomework.execute((Void) null);
                Intent i = new Intent(this,MainActivity.class);
                i.putExtra("id", id);
                i.putExtra("fecha",date);
                this.startActivityForResult(i, MainActivity.DELETE_HOMEWORK);
                //TODO: mNewHomework = new NewHomework(tarea_id).execute();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.date = Date.valueOf(year + "-" + (monthOfYear+1) + "-" + dayOfMonth);
        Log.d("AddHomeworkDate", date.toString());
        fechaBtn.setText(formatter.format(date));
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

    public void setBackground(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(
                    Tarea.getCourseColorFromIndex(index));

        findViewById(R.id.homework_title_bar).setBackgroundColor(
                Tarea.getCourseColorFromIndex(index));
    }

    public class NewHomework extends AsyncTask<Void,Void,String>{

        private final int mId_grupo;
        private final String mTitulo;
        private final String mDesc;
        private final String mMateria;
        private final Date mFecha;
        private final boolean mEdit;
        private final int mId;

        /**
         * Create homework constructor
         */
        NewHomework(int id_grupo,String titulo,String desc,String materia,Date fecha){
            mId_grupo = id_grupo;
            mTitulo = titulo;
            mDesc = desc;
            mMateria = materia;
            mFecha = fecha;
            mEdit =false;
            mId = -1;
        }

        /**
         * Edit homework constructor
         */
        NewHomework(int id, int id_grupo,String titulo,String desc,String materia,Date fecha){
            mId_grupo = id_grupo;
            mTitulo = titulo;
            mDesc = desc;
            mMateria = materia;
            mFecha = fecha;
            mEdit = true;
            mId = id;
        }

        NewHomework(int id_tarea){
            mId = id_tarea;
            mId_grupo = -1;
            mTitulo = null;
            mDesc = null;
            mMateria = null;
            mFecha = null;
            mEdit = false;
        }

        @Override
        protected String doInBackground(Void... voids) {
            if(!mEdit && mId_grupo != -1) {
                Log.v("agregando?",":c");
                return SMEDClient.newHomework(1, mTitulo, mDesc, mMateria, mFecha);
            }
            if(mEdit && mId_grupo != -1){
                Log.v("editando?",":c");
                return SMEDClient.editHomework(mId, mId_grupo, mTitulo, mDesc, mMateria, mFecha);
            }
            Log.v("borrando?",":c");
            return SMEDClient.deleteHomework(mId);

        }

        protected void onPostExecute(String res){
            mNewHomework = null;
            Toast.makeText(AddHomeworkActivity.this,res, Toast.LENGTH_SHORT).show();
            Intent i = AddHomeworkActivity.this.getIntent();

            i.putExtra("TituloTarea", mTitulo);
            i.putExtra("DescTarea", mDesc);
            i.putExtra("MateriaTarea", mMateria);
            i.putExtra("FechaTarea", mFecha);
            i.putExtra("Id", mId);
            AddHomeworkActivity.this.setResult(RESULT_OK, i);
            AddHomeworkActivity.this.finish();
        }
    }
}
