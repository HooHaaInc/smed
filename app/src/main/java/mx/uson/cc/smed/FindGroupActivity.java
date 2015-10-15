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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class FindGroupActivity extends AppCompatActivity
        implements WifiDirect.WifiDirectEventListener {

	WifiDirect direct;
    PagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    ArrayList<WifiP2pDevice> peers;

    private Bundle myInfo;
    private int direct_step = 0;
    private WifiP2pDevice mDevice;
    private ProgressDialog progressDialog;
    private boolean inscrito = false;

    //region Activity methods

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_group_connection);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(this);


        mViewPager.setAdapter(mPagerAdapter);

        myInfo = new Bundle();
        SharedPreferences preferences = getSharedPreferences("user", 0);
        myInfo.putString(WifiDirect.EXTRAS_ID, preferences.getString(SMEDClient.KEY_ID_PERSON, "-1"));
        myInfo.putInt(WifiDirect.EXTRAS_ACCOUNT, preferences.getInt(SMEDClient.KEY_ACCOUNT_TYPE, -1));
        myInfo.putString(WifiDirect.EXTRAS_NAME, preferences.getString(SMEDClient.KEY_NAME, "NaN"));
        myInfo.putString(WifiDirect.EXTRAS_LASTNAME1, preferences.getString(SMEDClient.KEY_LASTNAME1, "McGreggor"));
        myInfo.putString(WifiDirect.EXTRAS_LASTNAME2, preferences.getString(SMEDClient.KEY_LASTNAME2, ""));

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

    //endregion

    //region WiFiDirect methods

    @Override
    public void onWifiP2PStateChanged(boolean isEnabled) {
        if (isEnabled){
            direct_step = 1;
        } else direct_step = 0;
    }

    @Override
    public void onDeviceChangedAction(WifiP2pDevice device) {
        mDevice = device;
        if (findViewById(R.id.my_name) != null) {
            ((TextView) findViewById(R.id.my_name)).setText(mDevice.deviceName);
            ((TextView) findViewById(R.id.my_status)).setText(
                    getResources().getStringArray(R.array.device_states)[mDevice.status]);
        }
        myInfo.putString(WifiDirect.EXTRAS_DEVICE, device.deviceName);

    }

    @Override
    public void onActionSuccess(String action) {
    }

    @Override
    public void onActionFailure(String action, int code) {
        Toast.makeText(this, action+" failed, code "+code, Toast.LENGTH_SHORT).show();
    }

    /**
     * call first direct.discoverPeers();
     * @param peers
     */
    @Override
    public void onPeersChanged(List<WifiP2pDevice> peers) {
        if(direct_step == 1) {
            mViewPager.setVisibility(View.GONE);
            this.peers = new ArrayList<>(peers);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            findViewById(R.id.wifi_list).setVisibility(View.VISIBLE);
            Fragment frag = getSupportFragmentManager().findFragmentById(R.id.wifi_list);
            if (frag == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.wifi_list, new DeviceListFragment())
                        .commit();
            } else ((DeviceListFragment) frag).adapter.notifyDataSetChanged();
        }
    }

    /**
     * Call direct.connect(i) first.
     * Confirm peers data, set the group owner
     * @param info
     */
    @Override
    public void onGroupFormed(WifiP2pInfo info) {
        System.out.println("Group formed! c:");
        if(!info.isGroupOwner) {
            direct.requestInfo(this);
            if(progressDialog != null && progressDialog.isShowing())
                progressDialog.setMessage(getString(R.string.direct_getting_info));
            else if(progressDialog == null)
                System.out.println("getting info...");
        }else {
            Toast.makeText(this, "Im the owner wtf", Toast.LENGTH_SHORT).show();
        }
        direct_step = 2;
    }


    @Override
    public void onUserDataRead(Bundle read) {
        if(inscrito){
            finish();
            return;
        }
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        System.out.println("user data read");

        String name = read.getString(WifiDirect.EXTRAS_NAME);
        String lastname1 = read.getString(WifiDirect.EXTRAS_LASTNAME1);
        String lastname2 = read.getString(WifiDirect.EXTRAS_LASTNAME2);
        String group = read.getString(WifiDirect.EXTRAS_GROUP);
        String device = read.getString(WifiDirect.EXTRAS_DEVICE);
        String initials = name != null && lastname1 != null
                ? ""+name.charAt(0)+lastname1.charAt(0)
                : "N/A";
        TextDrawable img = TextDrawable.builder().buildRound(initials, Color.LTGRAY);
        View v = LayoutInflater.from(this).inflate(R.layout.device_detail, null, false);
        ((ImageView)v.findViewById(R.id.user_img)).setImageDrawable(img);
        ((TextView) v.findViewById(R.id.user_name)).setText(name +" "+ lastname1 +" "+ lastname2);
        ((TextView)v.findViewById(R.id.group_name)).setText(group);
        ((TextView)v.findViewById(R.id.device_name)).setText(device);
        new AlertDialog.Builder(this).setView(v).setPositiveButton(R.string.sign_to_group,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(FindGroupActivity.this, R.string.signed_to_group, Toast.LENGTH_SHORT).show();
                        direct.sendInfo(FindGroupActivity.this, myInfo);
                        FindGroupActivity.this.inscrito = true;

                    }
                }).setNegativeButton(R.string.action_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        direct_step = 1;
                        //TODO: direct.disconnect();
                    }
                }).create().show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    //endregion

    //region PagerAdapter

    public static class PagerAdapter extends FragmentPagerAdapter {

        public static final int STEP_ONE_FRAGMENT = 0;
        public static final int STEP_TWO_FRAGMENT = 1;

        public PagerAdapter(FindGroupActivity activity) {
            super(activity.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag;
            frag = new StepFragment();
            Bundle args = new Bundle();
            args.putInt(StepFragment.ARG_POSITION, position);
            frag.setArguments(args);

            return frag;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class StepFragment extends Fragment {
        public static final String ARG_POSITION = "position";
        int pos;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            pos = getArguments().getInt(ARG_POSITION);
            final FindGroupActivity activity = (FindGroupActivity)getActivity();
            View v = null;
            switch(pos){
                case PagerAdapter.STEP_ONE_FRAGMENT:
                    v = inflater.inflate(R.layout.fragment_wifidirect_one, container, false);
                    if (activity.mDevice != null) {
                        ((TextView) v.findViewById(R.id.my_name)).setText(activity.mDevice.deviceName);
                        ((TextView) v.findViewById(R.id.my_status)).setText(
                                getResources().getStringArray(R.array.device_states)[activity.mDevice.status]
                        );
                    }
                    break;
                case PagerAdapter.STEP_TWO_FRAGMENT:
                    v = inflater.inflate(R.layout.fragment_wifidirect_two, container, false);
                    v.findViewById(R.id.discover_peers_button).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            activity.direct.discoverPeers();
                            if (activity.progressDialog != null && activity.progressDialog.isShowing()) {
                                activity.progressDialog.dismiss();
                            }
                            activity.progressDialog = ProgressDialog.show(activity, null, getString(R.string.finding_devices), true,
                                    true);
                        }
                    });
                    break;

            }
            return v;
        }


    }

    public static class DeviceListFragment extends ListFragment {
        FindGroupActivity activity;
        WiFiPeerListAdapter adapter;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            activity = (FindGroupActivity)getActivity();
            View v = inflater.inflate(R.layout.device_list, container,false);
            adapter = new WiFiPeerListAdapter(activity,
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
            activity.direct.connect(position);

            activity.progressDialog = ProgressDialog.show(activity,
                    null,
                    String.format(getString(R.string.connecting_to), activity.peers.get(position).deviceName), //.deviceName
                    true, true);
        }
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private static class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {


        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v =  LayoutInflater.from(getContext()).inflate(R.layout.row_devices, parent, false);
            }
            WifiP2pDevice device = getItem(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getContext().getResources()
                            .getStringArray(R.array.device_states)[device.status]);
                }
            }

            return v;

        }
    }

    //endregion


}