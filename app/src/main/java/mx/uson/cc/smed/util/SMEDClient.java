package mx.uson.cc.smed.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Ernesto on 25/09/2015.
 */
public class SMEDClient {

    private static final String URL_REGISTER = "http://148.225.83.3/~e5ingsoft2/smed/Registro.php";
    private static final String URL_LOGIN = "http://148.225.83.3/~e5ingsoft2/smed/Login.php";

    public static final String KEY_NAME = "nombre";
    public static final String KEY_LASTNAME1 = "apellido_paterno";
    public static final String KEY_LASTNAME2 = "apellido_materno";
    public static final String KEY_ACCOUNT_TYPE = "tipo_persona";

    public static final String KEY_EMAIL = "correo";
    public static final String KEY_PASSWORD = "clave";

    public static final String RESULT_NEW_USER = "Registro exitoso.";

    public static final String RESULT_OK = "Operación exitosa.";
    public static final String RESULT_WRONG_PASSWORD = "Clave de acceso incorrecta."; //puedes cambiarlos si quieres ... Ok nan :>
    public static final String RESULT_LOGGED_IN = "Informacion correcta.";
    public static final String RESULT_USER404 = "Correo no registrado.";
    public static final String RESULT_USER_ALREADY_EXISTS = "Correo ya registrado.";

    public static String register(String email, String password,
                                  String name, String lastName1,
                                  String lastName2, int accountType){

        final String TAG_SUCESS = "exito";

        HashMap<String,String> datosPersona = new HashMap<>();
        datosPersona.put(SMEDClient.KEY_EMAIL,email);
        datosPersona.put(SMEDClient.KEY_PASSWORD,password);
        datosPersona.put(SMEDClient.KEY_NAME,name);
        datosPersona.put(SMEDClient.KEY_LASTNAME1,lastName1);
        datosPersona.put(SMEDClient.KEY_LASTNAME2,lastName2);
        datosPersona.put(SMEDClient.KEY_ACCOUNT_TYPE,Integer.toString(accountType));

        JSONObject result = SMEDClient.sendPostRequest(URL_REGISTER,datosPersona);

        try {
            Log.v("lel", result.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String res ="";
        //TODO: No mame neto
        try {
            res = result.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;

    }

    public static String login(String email, String password){

        HashMap<String,String> datosPersona = new HashMap<>();
        datosPersona.put(SMEDClient.KEY_EMAIL,email);
        datosPersona.put(SMEDClient.KEY_PASSWORD,password);

        JSONObject result = SMEDClient.sendPostRequest(URL_LOGIN,datosPersona);

        String res="";
        try{
            res = result.getString("message");
        }catch(JSONException e){
            e.printStackTrace();
        }

        return res;
    }

    private static JSONObject sendPostRequest(String requestURL,
                                  HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));

                response = br.readLine();
            }
            else {
                response="Error Registering";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jObj=null;

        try {
            jObj = new JSONObject(response);
            Log.v("lel",response);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jObj;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
