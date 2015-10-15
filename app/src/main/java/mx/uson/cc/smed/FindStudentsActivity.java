package mx.uson.cc.smed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.SMEDClient;
import mx.uson.cc.smed.util.WifiDirect;

public class FindStudentsActivity extends AppCompatActivity
        implements WifiDirect.WifiDirectEventListener{

    WifiDirect direct;
    WiFiFragmentPagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    ArrayList<Bundle> peers = new ArrayList<>();

    private Bundle myInfo;
    private int direct_step = 0;
    private WifiP2pDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_connection);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mPagerAdapter = new WiFiFragmentPagerAdapter(this);

        mViewPager.setAdapter(mPagerAdapter);

        myInfo = new Bundle();
        SharedPreferences preferences = getSharedPreferences("user", 0);
        myInfo.putInt(WifiDirect.EXTRAS_ACCOUNT, preferences.getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1));
        myInfo.putString(WifiDirect.EXTRAS_NAME, preferences.getString(SMEDClient.KEY_NAME, "NaN"));
        myInfo.putString(WifiDirect.EXTRAS_LASTNAME1, preferences.getString(SMEDClient.KEY_LASTNAME1, "McGreggor"));
        myInfo.putString(WifiDirect.EXTRAS_LASTNAME2, preferences.getString(SMEDClient.KEY_LASTNAME2, ""));
        myInfo.putString(WifiDirect.EXTRAS_GROUP, preferences.getString(SMEDClient.KEY_GROUP_NAME, "LCC"));

        direct = new WifiDirect(this);
        direct.setWifiDirectListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        direct.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        direct.unregister();
    }

    @Override
    protected void onDestroy() {
        direct.removeGroup();
        super.onDestroy();
    }

    @Override
    public void onWifiP2PStateChanged(boolean isEnabled) {
        if (isEnabled){
            direct_step = 1;
        } else direct_step = 0;
        //mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActionSuccess(String action) {

    }

    @Override
    public void onActionFailure(String action, int code) {
        Toast.makeText(this, action + " failed, code " + code, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceChangedAction(WifiP2pDevice device) {
        System.out.println("Device changed, status:" + device.status);
        if(mDevice == null) {
            myInfo.putString(WifiDirect.EXTRAS_DEVICE, device.deviceName);
        }
        mDevice = device;
        if (findViewById(R.id.my_name) != null) {
            ((TextView) findViewById(R.id.my_name)).setText(mDevice.deviceName);
            ((TextView) findViewById(R.id.my_status)).setText(getResources().getStringArray(R.array.device_states)[mDevice.status]);
        }

    }

    @Override
    public void onPeersChanged(List<WifiP2pDevice> peers) {

    }

    @Override
    public void onGroupFormed(WifiP2pInfo info) {
        if(!info.isGroupOwner) {
            System.out.println("no vale erga");
            //TODO: no vale
        }else{
            mViewPager.setVisibility(View.GONE);
            findViewById(R.id.wifi_list).setVisibility(View.VISIBLE);
            Fragment frag = getSupportFragmentManager().findFragmentById(R.id.wifi_list);
            if(frag == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.wifi_list, new DeviceListFragment())
                        .commit();
                direct.discoverPeers();
            }
        }
    }

    @Override
    public void onUserDataRead(Bundle read) {
        int i=0;
        for(; i<peers.size(); ++i){
            if(peers.get(i).getInt(WifiDirect.EXTRAS_ID) == read.getInt(WifiDirect.EXTRAS_ID))
                peers.set(i, read);
        }
        if(i == peers.size()) peers.add(read);
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.wifi_list);
        ((DeviceListFragment)frag).adapter.notifyDataSetChanged();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    public static class WiFiFragmentPagerAdapter extends FragmentPagerAdapter {

        public static final int STEP_ONE_FRAGMENT = 0;
        public static final int STEP_TWO_FRAGMENT = 1;

        public WiFiFragmentPagerAdapter(FindStudentsActivity activity) {
            super(activity.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag;
            frag = new WiFiDirectFragment();
            Bundle args = new Bundle();
            args.putInt(WiFiDirectFragment.ARG_POSITION, position);
            frag.setArguments(args);

            return frag;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

    public static class WiFiDirectFragment extends Fragment {
        public static final String ARG_POSITION = "position";
        int pos;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            pos = getArguments().getInt(ARG_POSITION);
            final FindStudentsActivity activity = (FindStudentsActivity)getActivity();
            View v = null;
            switch(pos){
                case WiFiFragmentPagerAdapter.STEP_ONE_FRAGMENT:
                    v = inflater.inflate(R.layout.fragment_wifidirect_one, container, false);
                    if (activity.mDevice != null) {
                        ((TextView) v.findViewById(R.id.my_name)).setText(activity.mDevice.deviceName);
                        ((TextView) v.findViewById(R.id.my_status)).setText(getResources().getStringArray(R.array.device_states)[activity.mDevice.status]);
                    }
                    break;
                case WiFiFragmentPagerAdapter.STEP_TWO_FRAGMENT:
                    v = inflater.inflate(R.layout.fragment_wifidirect_two, container, false);
                    v.findViewById(R.id.discover_peers_button).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            activity.direct.createGroup(activity.myInfo);
                        }
                    });
                    break;
            }
            return v;
        }


    }

    public static class DeviceListFragment extends ListFragment {
        FindStudentsActivity activity;
        StudentListAdapter adapter;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            activity = (FindStudentsActivity)getActivity();
            View v = inflater.inflate(R.layout.device_list, container,false);
            adapter = new StudentListAdapter(activity,
                    android.R.layout.simple_list_item_1, activity.peers);
            setListAdapter(adapter);
            if (activity.mDevice != null) {
                ((TextView) v.findViewById(R.id.my_name)).setText(activity.mDevice.deviceName);
                ((TextView) v.findViewById(R.id.my_status)).setText(
                        getResources().getStringArray(R.array.device_states)[activity.mDevice.status]);
            }
            return v;
        }

        /**
         * le dice a la activity que debe cambiar/actualizar el fragment compañero
         * (HomeworkFragment), le manda un bundle con la posicion de la tarea
         * en ResourcesMan y el fragment compañero
         */
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            activity.peers.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(activity, "Student deleted (8<", Toast.LENGTH_SHORT).show();
            //TODO: meter a la base de datos activity.peers.get(i);
        }
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private static class StudentListAdapter extends ArrayAdapter<Bundle> {


        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public StudentListAdapter(Context context, int textViewResourceId,
                                   List<Bundle> objects) {
            super(context, textViewResourceId, objects);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v =  LayoutInflater.from(getContext()).inflate(R.layout.student_detail, parent, false);
            }
            Bundle student = getItem(position);
            if (student != null) {
                String name = student.getString(WifiDirect.EXTRAS_NAME);
                String lastname1 = student.getString(WifiDirect.EXTRAS_LASTNAME1);
                String lastname2 = student.getString(WifiDirect.EXTRAS_LASTNAME2);
                String device = student.getString(WifiDirect.EXTRAS_DEVICE);
                String initials = name != null && lastname1 != null
                        ? ""+name.charAt(0)+lastname1.charAt(0)
                        : "N/A";
                TextDrawable img = TextDrawable.builder().buildRound(initials, Color.LTGRAY);
                ((ImageView)v.findViewById(R.id.user_img)).setImageDrawable(img);
                ((TextView) v.findViewById(R.id.user_name)).setText(name +" "+ lastname1 +" "+ lastname2);
                ((TextView)v.findViewById(R.id.device_name)).setText(device);
            }

            return v;

        }
    }
}
