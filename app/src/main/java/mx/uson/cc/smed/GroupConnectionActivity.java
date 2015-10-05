package mx.uson.cc.smed;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.List;

import mx.uson.cc.smed.util.WifiDirect;

public class GroupConnectionActivity extends Activity
        implements WifiDirect.WifiDirectEventListener {

	WifiDirect direct;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //setContentView(R.layout.main);

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
        Toast.makeText(this, isEnabled ? "WifiP2P enabled" : "WifiP2P disabled",
                Toast.LENGTH_SHORT).show();
        if(isEnabled) direct.discoverPeers();
    }

    @Override
    public void onActionSuccess(String action) {
        Toast.makeText(this,"Sucessful " + action, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionFailure(String action, int code) {
        Toast.makeText(this,"Failed " + action, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeersChanged(List<WifiP2pDevice> peers) {
        if(peers.size() > 0)
            direct.connect(0);
        else Toast.makeText(this, "No peers found", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeerAdded(WifiP2pDevice peer) {

    }

    @Override
    public void onDeviceChangedAction(Parcelable device) {

    }

    @Override
    public void onUserDataRead(String read) {

    }
}