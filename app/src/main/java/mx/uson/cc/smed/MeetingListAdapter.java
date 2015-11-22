package mx.uson.cc.smed;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.Junta;
import mx.uson.cc.smed.util.SMEDClient;

/**
 * Created by Jorge on 10/3/2015.
 */
public class MeetingListAdapter extends ArrayAdapter<Junta> {

    TextDrawable groupAvatar;

    public MeetingListAdapter(Context context, int textViewResourceId, List<Junta> items) {
        super(context, textViewResourceId, items);
        groupAvatar  = TextDrawable.builder().buildRound(
                context.getSharedPreferences("user", 0).getString(SMEDClient.KEY_GROUP_NAME, "null"),
                Color.LTGRAY
        );
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_avatar_with_text_and_icon, parent, false);
        }
        // object item based on the position
        Junta junta = getItem(position);
        if(junta.isJuntaGrupal())
            ((ImageView)convertView.findViewById(R.id.imageViewItem))
                    .setImageDrawable(groupAvatar);
        else {
            String[] split = junta.getCitado().split(" ");
            String initials = "" + split[0].charAt(0) + split[1].charAt(0);
            ((ImageView) convertView.findViewById(R.id.imageViewItem))
                    .setImageDrawable(TextDrawable.builder().buildRound(initials, Color.LTGRAY));
        }
        TextView text = (TextView) convertView.findViewById(R.id.textViewItem);
        text.setText(junta.getTitulo());
        text = (TextView) convertView.findViewById(R.id.dateViewItem);
        SimpleDateFormat form = new SimpleDateFormat("dd-MM");
        text.setText(form.format(junta.getFecha()));
        text = (TextView) convertView.findViewById(R.id.descViewItem);
        text.setText(junta.getDesc());
        return convertView;
    }
}
