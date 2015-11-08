package mx.uson.cc.smed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import mx.uson.cc.smed.util.Junta;

/**
 * Created by Jorge on 10/3/2015.
 */
public class MeetingListAdapter extends ArrayAdapter<Junta> {

    public MeetingListAdapter(Context context, int textViewResourceId, List<Junta> items) {
        super(context, textViewResourceId, items);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_row_meeting_item, parent, false);
        }
        // object item based on the position
        Junta junta = getItem(position);
        TextView text = (TextView) convertView.findViewById(R.id.meeting_title);
        text.setText(junta.getTitulo());
        text = (TextView) convertView.findViewById(R.id.meeting_date);
        SimpleDateFormat form = new SimpleDateFormat("dd-MM");
        text.setText(form.format(junta.getFecha()));
        return convertView;
    }
}
