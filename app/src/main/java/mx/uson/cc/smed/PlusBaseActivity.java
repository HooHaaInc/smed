package mx.uson.cc.smed;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

/**
 * clase base de LoginActivity, aqui esta la logica de g+.
 * implementa algunos listeners para las conecciones
 */
public abstract class PlusBaseActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    /** Solo sirve para logger*/
    private static String TAG = PlusBaseActivity.class.toString();
    /** request code: no importa mucho el valor, solo es identificador*/
    private static final int RC_SIGN_IN = 0;
    /** coneccion con la cuenta*/
    private GoogleApiClient mGoogleApiClient;
    /** datos de la persona*/
    private Person person;

    /** Is there a ConnectionResult solution in progress? */
    private boolean mIsResolving = false;
    /** Should we automatically resolve ConnectionResults when possible?*/
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //inicializa, añade escuchadores, api a referenciar, tipo de info
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + ":" + resultCode
                + ":" + data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode != RESULT_OK){
                mShouldResolve = false;
            }
            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
        //showLoadingUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailes:" + connectionResult);
        if(!mIsResolving && mShouldResolve){
            if(connectionResult.hasResolution()){
                try{
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                }catch(IntentSender.SendIntentException e){
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            }else{
                showErrorDialog(connectionResult);
                hideLoadingUI();
            }
        }else{
            hideLoadingUI();
            showSignedOutUI();
        }
    }

    @Override
    public void onConnected(Bundle bundle){
        Log.d(TAG, "onConnected: " + bundle);
        mShouldResolve = false;
        if(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null){
            person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        }
        hideLoadingUI();
        showSignedInUi(person != null ? person.getDisplayName() : "");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.plus_sign_in_button){
            onSignInClicked();
        }
        //Disconnect
        if(v.getId() == R.id.plus_disconnect_button){
            disconnect();
        }
        //Switch account
    }

    private void onSignInClicked() {
        if(!mGoogleApiClient.isConnected()) {
            mShouldResolve = true;
            mGoogleApiClient.connect();
            showLoadingUI();
        }
        //mStatusTextView.setText(R.string.signing_in);
    }

    protected void disconnect(){

        if(mGoogleApiClient.isConnected()){
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }if(mGoogleApiClient.isConnecting()){
            mGoogleApiClient.disconnect();
        }
    }

    protected GoogleApiClient getApiClient(){
        return mGoogleApiClient;
    }

    protected abstract void showErrorDialog(ConnectionResult result);
    protected abstract void showSignedOutUI();
    protected abstract void showSignedInUi(String name);
    protected abstract void showLoadingUI();
    protected abstract void hideLoadingUI();
}
