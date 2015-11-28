package mx.uson.cc.smed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.pushbots.push.Pushbots;
//import com.pushbots.push.Pushbots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mx.uson.cc.smed.util.SMEDClient;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends Activity
        implements LoaderCallbacks<Cursor>, //AutoComplete
        GoogleApiClient.ConnectionCallbacks, //Google+
        GoogleApiClient.OnConnectionFailedListener, // ^
        View.OnClickListener,
        Dialog.OnClickListener,
        TextView.OnEditorActionListener{

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private static final int STUDENT = 1;
    private static final int TEACHER = 2;
    private static final int PARENT = 3;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private static String TAG = LoginActivity.class.toString();
    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;
    private Person person;

    /** Is there a ConnectionResult solution in progress? */
    private boolean mIsResolving = false;
    /** Should we automatically resolve ConnectionResults when possible?*/
    private boolean mShouldResolve = false;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mRegisterFormView;
    private EditText mNameView;
    private EditText mLastName1View;
    private EditText mLastName2View;
    private Spinner mAccountType;
    private Button mEmailSignInButton;

    private boolean signIn = true;

    //region Activity Methods Override

    /**
     * Initialize mGoogleApiClient and sets listeners
     * and callbacks (this)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Pushbots.sharedInstance().init(this);
        //Pushbots.sharedInstance().setPushEnabled(true);

        // Find the Google+ sign in button.
        SignInButton mPlusSignInButton = (SignInButton) findViewById(R.id.plus_sign_in_button);
        if (supportsGooglePlayServices()) {
            // Set a listener to connect the user when the G+ button is clicked.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(new Scope(Scopes.PROFILE))
                    .addScope(new Scope(Scopes.EMAIL))
                    .build();

            mPlusSignInButton.setOnClickListener(this);
            //mPlusSignInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        } else {
            // Don't offer G+ sign in if the app's version is too low to support Google Play
            // Services.
            mPlusSignInButton.setVisibility(View.GONE);
            return;
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        //Set listeners
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(this);

        mLastName1View = (EditText) findViewById(R.id.last_name1);
        mLastName2View = (EditText) findViewById(R.id.last_name2);
        mAccountType = (Spinner) findViewById(R.id.account_type);

        mNameView = (EditText) findViewById(R.id.name);
        //TODO: mNameView.setOnEditorActionListener(this);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(this);

        TextView formSwitcher = (TextView) findViewById(R.id.switch_form);
        formSwitcher.setOnClickListener(this);
        mLoginFormView = findViewById(R.id.login_form);
        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Connects to Google+ after selection of the account
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    /**
     * Disconnects on Stop
     */
    @Override
    protected void onStop() {
        super.onStop();
        disconnect();
    }

    //endregion

    //region SignUp & Login

    /**
     * Attempts to sign up the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptSignUp(){
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNameView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String name = mNameView.getText().toString();
        String lastName1 = mLastName1View.getText().toString();
        String lastName2 = mLastName2View.getText().toString();
        int accountType = mAccountType.getSelectedItemPosition() +1 ;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if(TextUtils.isEmpty(name)){
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }
        if(TextUtils.isEmpty(lastName1)){
            mLastName1View.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if(accountType == TEACHER){
                new AlertDialog.Builder(this)
                        .setView(getLayoutInflater().inflate(R.layout.teacher_password_layout, null))
                        .setPositiveButton(R.string.action_sign_up, this)
                        .setNegativeButton(R.string.action_cancel, this)
                        .create().show();
            }else{
                showProgress(true);
                mAuthTask = new UserLoginTask(
                        email,
                        password,
                        name,
                        lastName1,
                        lastName2,
                        accountType);
                mAuthTask.execute((Void) null);
            }
        }
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    public void attemptGooglePlusSignUp(){
        final View atd = getLayoutInflater().inflate(R.layout.account_type_dialog,null);
        new AlertDialog.Builder(this)
                .setView(atd)
                .setPositiveButton(R.string.action_sign_up, this)
                .setNegativeButton(R.string.action_cancel, this)
                .create().show();
        Spinner spinner = (Spinner)atd.findViewById(R.id.d_account_type_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                atd.findViewById(R.id.d_teacher_account_password).setVisibility(
                        position == TEACHER-1 ? View.VISIBLE : View.GONE
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean isEmailValid(String email) {
        int at = email.indexOf("@");
        if(at <= 0) return false;
        int dot = email.substring(at).indexOf(".");
        return dot > 0 && dot < email.substring(at).length()-1;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean checkTeacherPassword(String pass){
        if(pass.equals("SMED2015")) return true;
        else return false;
    }

    /**
     * Shows the progress UI and hides the login form or viceversa.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    //endregion

    //region AutoComplete

    private void populateAutoComplete() {
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
        //Use noting
    }

    /**
     * Initialize the auto complete loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    /**
     * Pass emails in cursos to array list, then pass it to
     * addEmailsToAutoComplete(emails)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    /**
     * Creates and sets new adapter to AutoCompleteEditText mEmailView
     * @param emailAddressCollection all emails
     */
    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }



    /**
     * Emails table data projections for auto complete
     */
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    //endregion

    //region Google+

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);
        mShouldResolve = false;
        String email = null;
        if(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null)
            email = Plus.AccountApi.getAccountName(mGoogleApiClient);

        //showProgress(false);
        mAuthTask = new UserLoginTask(email);
        mAuthTask.execute((Void) null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * If the connection failed, checks if has resolution and solves it
     * (select a Google+ account from a list) via startForResult, then
     * starts the callback onActivityResult. If doesn't has resolution,
     * show login form again and an error
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
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
                showProgress(false);
            }
        }else{
            showProgress(false);
        }
    }

    /**
     * Disconnects the Google+ account
     */
    protected void disconnect(){

        if(mGoogleApiClient.isConnected()){
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }if(mGoogleApiClient.isConnecting()){
            mGoogleApiClient.disconnect();
        }
    }

    //endregion

    //region Listeners

    /**
     * Handles the Sign In/Register Button, Form switcher and Google+ Sign in
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.email_sign_in_button:
                if(signIn)
                    attemptLogin();
                else attemptSignUp();
                break;
            case R.id.switch_form:
                signIn = !signIn;
                mRegisterFormView.setVisibility(!signIn ? View.VISIBLE : View.GONE);
                mEmailSignInButton.setText(!signIn ? R.string.action_sign_up : R.string.action_sign_in);
                ((TextView)v).setText(signIn ? R.string.create_account : R.string.has_account);
                mPasswordView.setImeOptions(!signIn ? EditorInfo.IME_ACTION_NEXT : EditorInfo.IME_ACTION_UNSPECIFIED);
                mPasswordView.setImeActionLabel(signIn ? getString(R.string.action_sign_in_short) : null, R.id.login);
                break;
            case R.id.plus_sign_in_button:
                if(!mGoogleApiClient.isConnected()) {
                    mShouldResolve = true;
                    mGoogleApiClient.connect();
                    showProgress(true);
                }
        }
    }

    /**
     * Handles the dialog's buttons
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        AlertDialog alertDialog = (AlertDialog) dialog;
        switch(which){
            case DialogInterface.BUTTON_POSITIVE:
                if(alertDialog.findViewById(R.id.d_account_type_spinner) == null) {
                    EditText mPassView = (EditText) alertDialog.findViewById(R.id.teacher_password_field);
                    String pass = mPassView.getText().toString();
                    if (!checkTeacherPassword(pass)) {
                        Toast.makeText(this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String email = mEmailView.getText().toString();
                    String password = mPasswordView.getText().toString();
                    String name = mNameView.getText().toString();
                    String lastName1 = mLastName1View.getText().toString();
                    String lastName2 = mLastName2View.getText().toString();
                    int accountType = mAccountType.getSelectedItemPosition() + 1;
                    showProgress(true);
                    mAuthTask = new UserLoginTask(
                            email,
                            password,
                            name,
                            lastName1,
                            lastName2,
                            accountType);
                    mAuthTask.execute((Void) null);
                }else{
                    Spinner accountTypeView = (Spinner)alertDialog.findViewById(R.id.d_account_type_spinner);
                    int accountType = accountTypeView.getSelectedItemPosition() + 1;
                    EditText mPassView = (EditText) alertDialog.findViewById(R.id.teacher_password_field);
                    String pass = mPassView.getText().toString();
                    if (accountType == TEACHER && !checkTeacherPassword(pass)) {
                        Toast.makeText(this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showProgress(true);
                    person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                    String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                    mAuthTask = new UserLoginTask(
                            email,
                            person.getName().getGivenName(),
                            person.getName().getFamilyName(),
                            accountType);
                    mAuthTask.execute((Void) null);
                }
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.cancel();
        }
    }

    /**
     * Handles the keyboard action button
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int id = v.getId();
        switch(id){
            case R.id.password:
                if(actionId == R.id.login || actionId == EditorInfo.IME_NULL)
                    if(signIn) {
                        attemptLogin();
                        return true;
                    }
                break;
            case R.id.name:
                if(actionId == R.id.sign_up || actionId == EditorInfo.IME_NULL) {
                    attemptSignUp();
                    return true;
                }
                break;
        }
        return false;
    }

    //endregion

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, HashMap<String,String>> {

        //FLAGS
        private final int LOGIN = 0;
        private final int REGISTER = 1;
        private final int GOOGLE_PLUS = 2;

        private final String mEmail;
        private final String mPassword;
        private final String mName;
        private final String mLastName1;
        private final String mLastName2;
        private final String mGmcId;
        private final int mAccountType;
        private final int task;

        /**
         * Constructor for Login
         */
        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            mName = null;
            mLastName1 = null;
            mLastName2 = null;
            mAccountType = -1;
            mGmcId = Pushbots.sharedInstance().regID();
            task = LOGIN;
        }

        /**
         * Constructor for Sign Up
         */
        
        UserLoginTask(String email, String password, String name,
                      String apellidoPaterno, String apellidoMaterno,
                      int tipoUsuario) {
            mEmail = email;
            mPassword = password;
            mName = name;
            mLastName1 = apellidoPaterno;
            mLastName2 = apellidoMaterno;
            mAccountType = tipoUsuario;
            Log.v("cuenta:",Integer.toString(mAccountType));
            mGmcId = Pushbots.sharedInstance().regID();
            task = REGISTER;
        }

        /**
         * Constructor for Google+ Login
         */
        UserLoginTask(String email){
            mEmail = email;
            mPassword = "g+";
            mName = null;
            mLastName1 = null;
            mLastName2 = null;
            mAccountType = -1;
            mGmcId = Pushbots.sharedInstance().regID();
            task = GOOGLE_PLUS;
        }

        /**
         * Constructor for Google+ Sign Up
         */
        UserLoginTask(String email, String name, String lastName, int accountType){
            mEmail = email;
            mPassword = "g+";
            mName = name;
            int space = lastName.indexOf(" ");
            mLastName1 = space == -1 ? lastName : lastName.substring(0, space);
            mLastName2 = space == -1 ? "" : lastName.substring(space);
            mAccountType = accountType;
            mGmcId = Pushbots.sharedInstance().regID();
            task = GOOGLE_PLUS|REGISTER;
        }

        @Override
        protected HashMap<String,String> doInBackground(Void... params) {
            switch(task&1){
                case LOGIN:
                    return SMEDClient.login(mEmail,mPassword);
                case REGISTER:
                    //TODO: email, password?
                    return SMEDClient.register(
                            mEmail,
                            mPassword,
                            mName,
                            mLastName1,
                            mLastName2,
                            mAccountType,
                            mGmcId);

            }

            return null;
        }

        @Override
        protected void onPostExecute(final HashMap<String,String> result) {
            mAuthTask = null;
            showProgress(false);

            switch(result.get("message")){
                case SMEDClient.RESULT_LOGGED_IN:
                    finishedLogin(result);
                    Toast.makeText(LoginActivity.this,"Bienvenido "+result.get("nombre"),Toast.LENGTH_SHORT).show();
                    break;
                case SMEDClient.RESULT_WRONG_PASSWORD:
                    if(task == GOOGLE_PLUS) //Login with g+ but registered without it
                        finishedLogin(result);
                    else {
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                    break;
                case SMEDClient.RESULT_USER404:
                    if(task == GOOGLE_PLUS) //Tried login with g+, now try sign up
                        attemptGooglePlusSignUp();
                    else {
                        mEmailView.setError(getString(R.string.error_incorrect_email));
                        mEmailView.requestFocus();
                    }
                    break;
                case SMEDClient.RESULT_USER_ALREADY_EXISTS:
                    mEmailView.setError(getString(R.string.error_email_already_exists));
                    mEmailView.requestFocus();
                    break;
                case SMEDClient.RESULT_NEW_USER:
                    finishedLogin(result);
                    Toast.makeText(LoginActivity.this,"Bienvenido "+result.get("nombre"),Toast.LENGTH_SHORT);
                    //aquí ya tengo el id_persona, con lo cual accedo a sus datos.
                    break;
                default:
                    Toast.makeText(LoginActivity.this,
                            R.string.smed_server_error, Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    //region Misc

    /**
     * Check if the device supports Google Play Services.  It's best
     * practice to check first rather than handling this as an error case.
     *
     * @return whether the device supports Google Play Services
     */
    private boolean supportsGooglePlayServices() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS;
    }

    /**
     * Starts the Main Activity
     */
    private void finishedLogin(HashMap<String, String> result){
        Intent main = new Intent(this, MainActivity.class);
        String specificKey = null;
        int accountType = Integer.parseInt(result.get(SMEDClient.KEY_ACCOUNT_TYPE));
        Log.d("account", accountType+"");
        switch(accountType){
            case SMEDClient.TEACHER:
                specificKey = SMEDClient.KEY_ID_TEACHER;
                break;
            case SMEDClient.PARENT:
                //TODO: no mame
                specificKey = SMEDClient.KEY_ID_PARENT;
                break;
            case SMEDClient.STUDENT:
                specificKey = SMEDClient.KEY_ID_STUDENT;
        }
        getSharedPreferences("user", 0).edit()
                .putBoolean("login", true)
                .putInt(SMEDClient.KEY_ID_PERSON, Integer.parseInt(result.get(SMEDClient.KEY_ID_PERSON)))
                .putString(SMEDClient.KEY_NAME, result.get(SMEDClient.KEY_NAME))
                .putString(SMEDClient.KEY_LASTNAME1, result.get(SMEDClient.KEY_LASTNAME1))
                .putString(SMEDClient.KEY_LASTNAME2, result.get(SMEDClient.KEY_LASTNAME2))
                .putString(SMEDClient.KEY_EMAIL, result.get(SMEDClient.KEY_EMAIL))
                .putInt(SMEDClient.KEY_ACCOUNT_TYPE, accountType)
                .putInt(specificKey, Integer.parseInt(result.get(specificKey)))
                .apply();
        //startActivity(main);
        setResult(RESULT_OK, main);
        finish();
    }

    private void showErrorDialog(ConnectionResult result) {
        Toast.makeText(this, "ERROR: " + result, Toast.LENGTH_SHORT).show();
    }

    //endregion
}

