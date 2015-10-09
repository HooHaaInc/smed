package mx.uson.cc.smed.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiDirect extends BroadcastReceiver
		implements WifiP2pManager.PeerListListener,
		WifiP2pManager.ConnectionInfoListener{

	public static String TAG = WifiDirect.class.toString();
	public static final int SERVER_PORT = 8888;

	public static final String DISCOVER_PEERS = "discover_peers";
	public static final String CONNECT = "connect";
	public static final String ADD_LOCAL_SERVICE = "add_local_service";
	public static final String ADD_SERVICE_REQUEST = "add_service_request";
	public static final String DISCOVER_SERVICES = "discover_services";

	private String action;

	//private WiFiDirectBroadcastReceiver receiver;
	private Context context;
	private final IntentFilter intentFilter = new IntentFilter();
	WifiP2pManager mManager;
	WifiP2pManager.Channel mChannel;
	private List<WifiP2pDevice> peers = new ArrayList<>();
	final HashMap<String, String> buddies = new HashMap<>();

    private WifiDirectEventListener listener;

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


    public void register(){
    	context.registerReceiver(this, intentFilter);
    }

    public void unregister(){
        context.unregisterReceiver(this);
        listener = null;
    }

    public void setIsWifiP2pEnabled(boolean enabled){
        if(listener != null)
            listener.onWifiP2PStateChanged(enabled);
    }

    public void discoverPeers(){

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

    public void setWifiDirectListener(WifiDirectEventListener listener){
        this.listener = listener;
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

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
			new UserDataAsyncTask(this).execute((Void) null);

        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            new UserDataAsyncTask(this,info.groupOwnerAddress.getHostAddress());
        }
    }

    @TargetApi(16)
    public void startRegistration() {
        //  Create a string map containing information about your service.
        Map<String, String> record = new HashMap<>();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "Neto mlp" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if(listener != null)
                    listener.onActionSuccess(ADD_LOCAL_SERVICE);
            }

            @Override
            public void onFailure(int reason) {
                if(listener != null)
                    listener.onActionFailure(ADD_LOCAL_SERVICE, reason);
            }
        });
    }

    @TargetApi(16)
    public void discoverService() {
	    WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
	        @Override
	        /* Callback includes:
	         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
	         * record: TXT record dta as a map of key/value pairs.
	         * device: The device running the advertised service.
	         */

	        public void onDnsSdTxtRecordAvailable(
	                String fullDomain, Map record, WifiP2pDevice device) {
                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                buddies.put(device.deviceAddress, (String) record.get("buddyname"));
                Toast.makeText(context, (String)record.get("buddyname"), Toast.LENGTH_SHORT).show();
	            }
	        };

		WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {

	        public void onDnsSdServiceAvailable(String instanceName, String registrationType,
	                WifiP2pDevice resourceType) {

	                // Update the device name with the human-friendly version from
	                // the DnsTxtRecord, assuming one arrived.
	                resourceType.deviceName = buddies
	                        .containsKey(resourceType.deviceAddress) ? buddies
	                        .get(resourceType.deviceAddress) : resourceType.deviceName;

	                // Add to the custom adapter defined specifically for showing
	                // wifi devices.

                    if(listener != null)
                        listener.onPeerAdded(resourceType);

	                Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
	        }
	    };

	    mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

	    WifiP2pServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel,
                serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        if(listener != null)
                            listener.onActionSuccess(ADD_SERVICE_REQUEST);
                    }

                    @Override
                    public void onFailure(int reason) {
                        if(listener != null)
                            listener.onActionFailure(ADD_SERVICE_REQUEST, reason);
                    }
                });

        
	}

	@SuppressLint("NewApi")
	public void onSuccess(){
        if(listener != null)
            listener.onActionSuccess(action);

		switch(action){
		case DISCOVER_PEERS:
			break;
		case CONNECT:
			break;
		case ADD_LOCAL_SERVICE:
			break;
		case ADD_SERVICE_REQUEST:
			action = DISCOVER_SERVICES;
			mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    if(listener != null)
                        listener.onActionSuccess(DISCOVER_SERVICES);
                }

                @Override
                public void onFailure(int reason) {
                    if(listener != null)
                        listener.onActionFailure(DISCOVER_SERVICES, reason);
                }
            });
			break;
		case DISCOVER_SERVICES:
			break;
		default:
			Log.d(TAG, "Unknown action");
		}
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
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
            //        .findFragmentById(R.id.frag_list);
            //fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
             //       WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            if(listener != null)
                listener.onDeviceChangedAction(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }


    public static class UserDataAsyncTask extends AsyncTask<Void, Void, String>{
        WifiDirect context;
        String hostAddress;

        public UserDataAsyncTask(WifiDirect context){
            this.hostAddress = null;
            this.context = context;
        }

        public UserDataAsyncTask(WifiDirect context, String hostAddress){
            this.hostAddress = null;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            if(hostAddress == null) { //retrieve data
                try {
                    ServerSocket serverSocket = new ServerSocket(8888);
                    Socket client = serverSocket.accept();
                    DataInputStream data = new DataInputStream(client.getInputStream());
                    return data.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }else{ //send data
                Socket socket = new Socket();
                try{
                    socket.bind(null);
                    socket.connect(new InetSocketAddress(hostAddress, 8888), 500);

                    OutputStream output = socket.getOutputStream();
                    DataOutputStream data = new DataOutputStream(output);
                    data.writeUTF("Alumno");
                    data.close();
                    output.close();
                    return "sended shit";
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    if(socket.isConnected())
                        try{
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(context, "Holi" + s, Toast.LENGTH_SHORT).show();
            if(context.listener != null)
                context.listener.onUserDataRead(s);
        }
    }

    public interface WifiDirectEventListener {
        void onWifiP2PStateChanged(boolean isEnabled);
        void onActionSuccess(String action);
        void onActionFailure(String action, int code);
        void onPeersChanged(List<WifiP2pDevice> peers);
        void onPeerAdded(WifiP2pDevice peer);
        void onDeviceChangedAction(Parcelable device);
        void onUserDataRead(String read);
    }
}