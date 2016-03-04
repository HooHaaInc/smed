package mx.uson.cc.smed;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.Student;
import mx.uson.cc.smed.util.Tarea;

/**
 * Created by Hans on 03/03/2016.
 */
public class StudentListAdapter extends ArrayAdapter<Student> {

    public StudentListAdapter(Context context, int textViewResourceId, List<Student> items) {
        super(context, textViewResourceId, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Student homie = getItem(position);
        String nombrito = homie.getName();
        nombrito += " ";
        nombrito += homie.getLastName1();
        if(homie.getLastName2() != null)
            nombrito += (" " + homie.getLastName2());
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_single_line_with_avatar, parent, false);
        ((TextView)convertView.findViewById(R.id.title)).setText(nombrito);
        ((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(
                TextDrawable.builder().buildRound(homie.getInitials(), Color.LTGRAY));
        return convertView;
    }
}
