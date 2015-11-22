package mx.uson.cc.smed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import mx.uson.cc.smed.util.Group;

public class FindGroupActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    List<Group> items = new ArrayList<>();
    GroupAdapter adapter;
    ProgressDialog progress;
    int request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

        request = getIntent().getIntExtra("requestCode", -1);
        adapter = new GroupAdapter(this, items);
        ListView list = (ListView)findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        new GroupTask(this).execute();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.REQUEST_STUDENT_LINK){
            if(resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if(request == MainActivity.REQUEST_STUDENT_LINK){
            Intent findStudent = new Intent(this, FindStudentActivity.class);
            findStudent.putExtra("groupId", adapter.getItem(position).getId());
            startActivityForResult(findStudent, request);
            return;
        }

        View convertView = LayoutInflater.from(this).inflate(R.layout.list_item_two_line_with_avatar, null, false);
        ((TextView)convertView.findViewById(R.id.title)).setText(
                adapter.getItem(position).getTeacher());
        ((TextView)convertView.findViewById(R.id.desc)).setText(
                adapter.getItem(position).getShift());
        ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                TextDrawable.builder().buildRound(adapter.getItem(position).getName(), Color.LTGRAY));


        new AlertDialog.Builder(FindGroupActivity.this)
                .setTitle(R.string.sign_to_group_question)
                .setView(convertView)
                .setPositiveButton(R.string.sign_to_group, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: neto, inscribir gente
                        Group g = adapter.getItem(position);
                        Toast.makeText(FindGroupActivity.this,
                                getString(R.string.signed_to)+" "+g.getName(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).setNegativeButton(R.string.action_cancel, null).create().show();
    }

    public static class GroupAdapter extends ArrayAdapter<Group> {


        public GroupAdapter(Context context, List<Group> list) {
            super(context, android.R.layout.simple_list_item_1, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_two_line_with_avatar, parent, false);
            ((TextView)convertView.findViewById(R.id.title)).setText(
                    getItem(position).getTeacher());
            ((TextView)convertView.findViewById(R.id.desc)).setText(
                    getItem(position).getShift());
            ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                    TextDrawable.builder().buildRound(getItem(position).getName(), Color.LTGRAY));
            return convertView;
        }
    }

    public static class GroupTask extends AsyncTask<Void,Void,Boolean> {

        FindGroupActivity activity;

        public GroupTask(FindGroupActivity act){
            activity = act;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //TODO: neto pls
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            activity.items.add(new Group(1, "5A", "Matutino", "Sr Dr Prof Roberto"));
            activity.items.add(new Group(2, "1B", "Vespertino", "MNan"));
            activity.items.add(new Group(3, "4Z", "Nocturno", "Picador Criminal Mutilador PhD"));
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
