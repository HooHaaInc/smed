package mx.uson.cc.smed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.Student;

public class FindStudentActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    List<Student> items = new ArrayList<>();
    StudentAdapter adapter;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

        adapter = new StudentAdapter(this, items);
        ListView list = (ListView)findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        new StudentTask(this, getIntent().getIntExtra("groupId", -1)).execute();
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.loading));
        progress.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_find_group, menu);
        SearchView view = (SearchView)menu.findItem(R.id.action_search_group).getActionView();
        view.setIconifiedByDefault(false);
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        View convertView = LayoutInflater.from(this).inflate(R.layout.list_item_single_line_with_avatar, null, false);
        ((TextView)convertView.findViewById(R.id.title)).setText(
                adapter.getItem(position).toString());
        ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                TextDrawable.builder().buildRound(adapter.getItem(position).getInitials(), Color.LTGRAY));


        new AlertDialog.Builder(FindStudentActivity.this)
                .setTitle(R.string.link_to_student_question)
                .setView(convertView)
                .setPositiveButton(R.string.link_to_student, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: neto, inscribir gente
                        Student g = adapter.getItem(position);
                        Toast.makeText(FindStudentActivity.this,
                                getString(R.string.linked_to)+" "+g.getName(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).setNegativeButton(R.string.action_cancel, null).create().show();
    }

    public static class StudentAdapter extends ArrayAdapter<Student> {


        public StudentAdapter(Context context, List<Student> list) {
            super(context, android.R.layout.simple_list_item_1, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_single_line_with_avatar, parent, false);
            ((TextView)convertView.findViewById(R.id.title)).setText(
                    getItem(position).toString());
            ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                    TextDrawable.builder().buildRound(getItem(position).getInitials(), Color.LTGRAY));
            return convertView;
        }
    }

    public static class StudentTask extends AsyncTask<Void,Void,Boolean> {

        FindStudentActivity activity;
        int groupId;

        public StudentTask(FindStudentActivity act, int groupId){
            activity = act;
            groupId = groupId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //TODO: neto pls
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            activity.items.add(new Student(1, "Nan", "Montaño", "Valdez"));
            activity.items.add(new Student(2, "El", "Neto", null));
            activity.items.add(new Student(3, "Erick", "Lopez", "F."));
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            activity.progress.dismiss();
            if(success){
                activity.adapter.notifyDataSetChanged();
            }
        }
    }

}
