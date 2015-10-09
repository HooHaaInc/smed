package mx.uson.cc.smed;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mx.uson.cc.smed.util.WifiDirect;

public class GroupConnectionActivity extends Activity
        implements WifiDirect.WifiDirectEventListener {

	WifiDirect direct;
    LinearLayout layout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_group_connection);
        layout = (LinearLayout)findViewById(R.id.connection_layout);

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
    public void onWifiP2PStateChanged(boolean isEnabled) {
        TextView tv = new TextView(this);
        tv.setText(isEnabled ? "WifiP2P enabled" : "WifiP2P disabled");
        layout.addView(tv);
        if(isEnabled) direct.discoverPeers();
    }

    @Override
    public void onActionSuccess(String action) {
        TextView tv = new TextView(this);
        tv.setText("Successful "+action);
        layout.addView(tv);
    }

    @Override
    public void onActionFailure(String action, int code) {
        TextView tv = new TextView(this);
        tv.setText("Failed "+action+", reason: "+ code);
        layout.addView(tv);
    }

    @Override
    public void onPeersChanged(List<WifiP2pDevice> peers) {
        if(peers.size() > 0)
            direct.connect(0);
        else{
            TextView tv = new TextView(this);
            tv.setText("No peers found");
            layout.addView(tv);
        }
    }

    @Override
    public void onPeerAdded(WifiP2pDevice peer) {

    }

    @Override
    public void onDeviceChangedAction(Parcelable device) {

    }

    @Override
    public void onUserDataRead(String read) {
        TextView tv = new TextView(this);
        tv.setText("Peer says: " + read);
        layout.addView(tv);
    }
}