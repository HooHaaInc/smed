package mx.uson.cc.smed.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class WifiDirect extends BroadcastReceiver
		implements WifiP2pManager.PeerListListener,
		WifiP2pManager.ConnectionInfoListener{

	public static String TAG = WifiDirect.class.toString();
	public static final int SERVER_PORT = 8888;

	public static final String DISCOVER_PEERS = "discover_peers";
    public static final String STOP_DISCOVER_PEERS = "stop_discover_peers";
	public static final String CONNECT = "connect";
    public static final String CREATE_GROUP = "create_group";
    public static final String REMOVE_GROUP = "remove_group";

    public static final String EXTRAS_ACCOUNT = "account";
    public static final String EXTRAS_NAME = "name";
    public static final String EXTRAS_LASTNAME1 = "lastname1";
    public static final String EXTRAS_LASTNAME2 = "lastname2";
    public static final String EXTRAS_GROUP = "group";
    public static final String EXTRAS_DEVICE = "device";

	private boolean wasConnected = false;

	private Context context;
	private final IntentFilter intentFilter = new IntentFilter();
	WifiP2pManager mManager;
	WifiP2pManager.Channel mChannel;
	private List<WifiP2pDevice> peers = new ArrayList<>();
	final HashMap<String, String> buddies = new HashMap<>();
    private boolean isWifiP2PEnabled;
    private Bundle serverResponse;
    private UserDataAsyncTask serverTask;

    private WifiDirectEventListener listener;
    private WifiP2pInfo info;

    public WifiDirect(Context context){

		this.context = context;
		//  Indicates a change in the Wi-Fi P2P status.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

	    // Indicates a change in the list of available peers.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

	    // Indicates the state of Wi-Fi P2P connectivity has changed.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

	    // Indicates this device's details have changed.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

	    mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
    	mChannel = mManager.initialize(context, context.getMainLooper(), null);

        //discoverPeers();
	}


    public void setWifiDirectListener(WifiDirectEventListener listener){
        this.listener = listener;
    }

    public void register(){
    	context.registerReceiver(this, intentFilter);
    }

    public void unregister(){
        context.unregisterReceiver(this);
        listener = null;
        if(serverTask != null) serverTask.cancel(false);
    }

    public void setIsWifiP2pEnabled(boolean enabled){
        isWifiP2PEnabled = enabled;
        if(listener != null)
            listener.onWifiP2PStateChanged(enabled);
    }

    public void discoverPeers(){
        //if(!isWifiP2PEnabbled) nope
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if(listener != null)
                        listener.onActionSuccess(DISCOVER_PEERS);
            }

            @Override
            public void onFailure(int reason) {
                if(listener != null)
                    listener.onActionFailure(DISCOVER_PEERS, reason);
            }
        });
    }

    @TargetApi(16)
    public void stopPeerDiscovery(){
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (listener != null)
                    listener.onActionSuccess(STOP_DISCOVER_PEERS);
            }

            @Override
            public void onFailure(int reason) {
                if (listener != null)
                    listener.onActionFailure(STOP_DISCOVER_PEERS, reason);
            }
        });
    }


	@Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        // Out with the old, in with the new.
        peers.clear();
        peers.addAll(peerList.getDeviceList());



        // If an AdapterView is backed by this data, notify it
        // of the change.  For instance, if you have a ListView of available
        // peers, trigger an update.
        //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if(listener != null)
            listener.onPeersChanged(peers);
        if (peers.size() == 0) {
            Log.d(TAG, "No devices found");
        }
    }

    public boolean connect(int i) {
        // Picking the first device found on the network.
        if(peers.size() <= i) return false;
        WifiP2pDevice device = peers.get(i);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if(listener != null)
                    listener.onActionSuccess(CONNECT);
            }

            @Override
            public void onFailure(int reason) {
                if(listener != null)
                    listener.onActionFailure(CONNECT, reason);
            }
        });

        return true;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        System.out.println("Almost connect");
        // InetAddress from WifiP2pInfo struct.
        //InetAddress groupOwnerAddress = InetAddress.(info.groupOwnerAddress.getHostAddress());
        this.info = info;
        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            serverTask = new UserDataAsyncTask(this, serverResponse);
            serverTask.execute();
			//new UserDataAsyncTask(this).execute((Void) null);

        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
        }
        if(listener != null)
            listener.onGroupFormed(info);
    }

    public void createGroup(Bundle serverResponse){
        this.serverResponse = serverResponse;
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if(listener != null)
                    listener.onActionSuccess(CREATE_GROUP);
            }

            @Override
            public void onFailure(int reason) {
                if(listener != null)
                    listener.onActionFailure(CREATE_GROUP, reason);
            }
        });
    }

    public void removeGroup(){
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if(listener != null)
                    listener.onActionSuccess(REMOVE_GROUP);
            }

            @Override
            public void onFailure(int reason) {
                if(listener != null)
                    listener.onActionFailure(REMOVE_GROUP, reason);
            }
        });
    }

    public void sendInfo(Activity activity, Bundle bundle){
        new UserDataAsyncTask(this, bundle, info.groupOwnerAddress.getHostAddress()).execute();
    }

    public void requestInfo(Activity activity){
        sendInfo(activity, new Bundle());
    }


	@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                setIsWifiP2pEnabled(true);
            } else {
                setIsWifiP2pEnabled(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

	        // Request available peers from the wifi p2p manager. This is an
	        // asynchronous call and the calling activity is notified with a
	        // callback on PeerListListener.onPeersAvailable()
	        if (mManager != null) {
	            mManager.requestPeers(mChannel, this);
	        }
    		Log.d(TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP

                mManager.requestConnectionInfo(mChannel, this);
                wasConnected = true;
            }else if(wasConnected){
                if(listener != null)
                    listener.onDisconnected();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
            //        .findFragmentById(R.id.frag_list);
            //fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
             //       WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            if(listener != null)
                listener.onDeviceChangedAction((WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }


    public static class UserDataAsyncTask extends AsyncTask<Void, Bundle, Bundle>{
        static int SOCKET_TIMEOUT = 5000;

        WifiDirect context;
        Bundle bundle;
        String host;

        public UserDataAsyncTask(WifiDirect context, Bundle bundle){
            this.context = context;
            this.bundle = bundle;
            this.host = null;
        }

        public UserDataAsyncTask(WifiDirect context, Bundle bundle, String host){
            this.context = context;
            this.bundle = bundle;
            this.host = host;
        }

        private Bundle doServer(){
            try{
                ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                while(!isCancelled()) {
                    Log.d(TAG, "Opening server socket - ");
                    Socket client = serverSocket.accept();
                    Log.d(TAG, "Server socket - " + client.isConnected());

                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    Log.d(TAG, "Initialized OutputStream");
                    Set<String> set = bundle.keySet();
                    HashMap<String,String> map = new HashMap<>();
                    if(!set.isEmpty()) {
                        map.put(EXTRAS_ACCOUNT, "" + bundle.getInt(EXTRAS_ACCOUNT));
                        set.remove(EXTRAS_ACCOUNT);
                        for (String key : set)
                            map.put(key, bundle.getString(key));
                    }
                    oos.writeObject(map);
                    Log.d(TAG, "Wrote bundle");

                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    Log.d(TAG, "Initialized InputStream");
                    HashMap<String, String> read = (HashMap<String,String>)ois.readObject();
                    set = read.keySet();
                    Bundle b = new Bundle();
                    if(!set.isEmpty()) {
                        b.putInt(EXTRAS_ACCOUNT, Integer.parseInt(read.get(EXTRAS_ACCOUNT)));
                        set.remove(EXTRAS_ACCOUNT);
                        for (String key : set)
                            b.putString(key, read.get(key));
                    }
                    publishProgress(b);
                    Log.d(TAG, "Progress published");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private Bundle doClient(){
            Socket socket = new Socket();
            try {
                Log.d(TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, SERVER_PORT)), SOCKET_TIMEOUT);
                Log.d(TAG, "Client socket - " + socket.isConnected());

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                HashMap<String, String> read = (HashMap<String,String>)ois.readObject();
                Set<String> set = read.keySet();
                Bundle b = new Bundle();
                if(!set.isEmpty()) {
                    b.putInt(EXTRAS_ACCOUNT, Integer.parseInt(read.get(EXTRAS_ACCOUNT)));
                    set.remove(EXTRAS_ACCOUNT);
                    for (String key : set)
                        b.putString(key, read.get(key));
                }
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                set = bundle.keySet();
                HashMap<String,String> map = new HashMap<>();
                if(!set.isEmpty()) {
                    map.put(EXTRAS_ACCOUNT, "" + bundle.getInt(EXTRAS_ACCOUNT));
                    set.remove(EXTRAS_ACCOUNT);
                    for (String key : set)
                        map.put(key, bundle.getString(key));
                }
                oos.writeObject(map);
                Log.d(TAG, "Client: Data written");
                return b;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Give up
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected Bundle doInBackground(Void... params) {
            if(host != null)
                return doClient();
            else return doServer();
        }

        @Override
        protected void onProgressUpdate(Bundle... values) {
            super.onProgressUpdate(values);
            if(context.listener != null && !values[0].isEmpty())
                context.listener.onUserDataRead(values[0]);
        }

        @Override
        protected void onPostExecute(Bundle result) {
            super.onPostExecute(result);
            if(context.listener != null && result != null)
                context.listener.onUserDataRead(result);
        }
    }

    public interface WifiDirectEventListener {
        void onWifiP2PStateChanged(boolean isEnabled);
        void onActionSuccess(String action);
        void onActionFailure(String action, int code);
        void onPeersChanged(List<WifiP2pDevice> peers);
        void onDeviceChangedAction(WifiP2pDevice device);
        void onGroupFormed(WifiP2pInfo info);
        void onUserDataRead(Bundle result);
        //void onServerResponse(HashMap<String, String> response);
        //void onClientResponse(HashMap<String, String> response);
        void onDisconnected();
    }
}