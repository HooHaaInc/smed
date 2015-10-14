package mx.uson.cc.smed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.SMEDClient;
import mx.uson.cc.smed.util.WifiDirect;

public class GroupConnectionActivity extends AppCompatActivity
        implements WifiDirect.WifiDirectEventListener, ViewPager.OnPageChangeListener {

	WifiDirect direct;
    WiFiFragmentPagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    Button btnContinue;
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
        mPagerAdapter = new WiFiFragmentPagerAdapter(this);
        btnContinue = (Button)findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            }
        });

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

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

    //endregion

    //region WiFiDirect methods

    @Override
    public void onWifiP2PStateChanged(boolean isEnabled) {
        if (isEnabled){
            direct_step = 1;
            if(myInfo.getInt(WifiDirect.EXTRAS_ACCOUNT, -1) == SMEDClient.TEACHER) {
                direct.createGroup(myInfo);
            }
        } else direct_step = 0;
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceChangedAction(WifiP2pDevice device) {
        mDevice = device;
        if (findViewById(R.id.my_name) != null) {
            ((TextView) findViewById(R.id.my_name)).setText(mDevice.deviceName);
            ((TextView) findViewById(R.id.my_status)).setText(getDeviceStatus(mDevice.status));
        }
        if(myInfo.getInt(WifiDirect.EXTRAS_ACCOUNT, -1) == SMEDClient.TEACHER) {
            direct.createGroup(myInfo);
            myInfo.putString(WifiDirect.EXTRAS_DEVICE, device.deviceName);
        }
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
        if(peers.size() > 0 && direct_step == 1) {
            direct_step = 2;
            mPagerAdapter.notifyDataSetChanged();
            this.peers = new ArrayList<>(peers);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            ListView list = ((ListView) findViewById(R.id.list));
            if(list != null) {
                list.setAdapter(new WiFiPeerListAdapter(
                        this, android.R.layout.simple_list_item_1, peers));
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        direct.connect(position);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        progressDialog = ProgressDialog.show(GroupConnectionActivity.this,
                                "Press back to cancel",
                                "Connecting to :" + GroupConnectionActivity.this.peers.get(position).deviceName, //.deviceName
                                true, true);
                    }
                });
            }
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
        if(info.groupFormed && !info.isGroupOwner) {
            direct.requestInfo(this);
            if(progressDialog != null && progressDialog.isShowing())
                progressDialog.setMessage("Getting info...");
        }else {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
        if(direct_step == 2) {
            direct_step = 3;
            mPagerAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onUserDataRead(Bundle read) {
        if(inscrito){
            finish();
            return;
        }
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

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
        new AlertDialog.Builder(this).setView(v).setPositiveButton("Inscribirse",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(GroupConnectionActivity.this, "Inscrito yey", Toast.LENGTH_SHORT).show();
                        if(myInfo.getInt(WifiDirect.EXTRAS_ACCOUNT) == SMEDClient.TEACHER)
                            GroupConnectionActivity.this.finish();
                        else {
                            direct.sendInfo(GroupConnectionActivity.this, myInfo);
                            GroupConnectionActivity.this.inscrito = true;
                        }
                    }
                }).setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //TODO: direct.disconnect();
                    }
                }).create().show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    //endregion

    //region ViewPagerListener

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {


    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //endregion

    //region PagerAdapter

    public static class WiFiFragmentPagerAdapter extends FragmentPagerAdapter {

        public static final int STEP_ONE_FRAGMENT = 0;
        public static final int STEP_TWO_FRAGMENT = 1;
        public static final int DEVICE_LIST_FRAGMENT = 2;
        public static final int DEVICE_DETAIL_FRAGMENT = 3;

        private GroupConnectionActivity activity;

        public WiFiFragmentPagerAdapter(GroupConnectionActivity activity) {
            super(activity.getSupportFragmentManager());
            this.activity = activity;
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
            return activity.direct_step + 1;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

        }
    }

    public static class WiFiDirectFragment extends Fragment {
        public static final String ARG_POSITION = "position";
        int pos;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            pos = getArguments().getInt(ARG_POSITION);
            final GroupConnectionActivity activity = (GroupConnectionActivity)getActivity();
            View v = null;
            switch(pos){
                case WiFiFragmentPagerAdapter.STEP_ONE_FRAGMENT:
                    v = inflater.inflate(R.layout.fragment_wifidirect_one, container, false);
                    if (activity.mDevice != null) {
                        ((TextView) v.findViewById(R.id.my_name)).setText(activity.mDevice.deviceName);
                        ((TextView) v.findViewById(R.id.my_status)).setText(getDeviceStatus(activity.mDevice.status));
                    }
                    break;
                case WiFiFragmentPagerAdapter.STEP_TWO_FRAGMENT:
                    v = inflater.inflate(R.layout.fragment_wifidirect_two, container, false);
                    v.findViewById(R.id.discover_peers_button).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            activity.direct.discoverPeers();
                            if (activity.progressDialog != null && activity.progressDialog.isShowing()) {
                                activity.progressDialog.dismiss();
                            }
                            activity.progressDialog = ProgressDialog.show(activity, "Press back to cancel", "finding peers", true,
                                    true);
                        }
                    });
                    break;
                case WiFiFragmentPagerAdapter.DEVICE_LIST_FRAGMENT:
                    v = inflater.inflate(R.layout.device_list, container, false);


                    if (activity.mDevice != null) {
                        ((TextView) v.findViewById(R.id.my_name)).setText(activity.mDevice.deviceName);
                        ((TextView) v.findViewById(R.id.my_status)).setText(getDeviceStatus(activity.mDevice.status));
                    }
                    break;
                case WiFiFragmentPagerAdapter.DEVICE_DETAIL_FRAGMENT:
                    v = inflater.inflate(R.layout.device_detail, container, false);
                    break;
            }
            return v;
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
                    bottom.setText(getDeviceStatus(device.status));
                }
            }

            return v;

        }
    }

    //endregion

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WifiDirect.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
}