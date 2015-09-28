package mx.uson.cc.smed.util;

import android.util.Log;

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
public class RegisterClass {

    private static final String URL_REGISTER = "http://148.225.83.3/~e5ingsoft2/smed/RegistrarPersona.php";

    public static final String KEY_NAME = "nombre";
    public static final String KEY_LASTNAME1 = "apellido_paterno";
    public static final String KEY_LASTNAME2 = "apellido_materno";
    public static final String KEY_ACCOUNT_TYPE = "tipo_persona";

    public static final String RESULT_OK = "success";
    public static final String RESULT_WRONG_PASSWORD = "wrong_password"; //puedes cambiarlos si quieres
    public static final String RESULT_USER404 = "user_not_found";

    public static String register(String email, String password,
                                  String name, String lastName1,
                                  String lastName2, int accountType){

        final String TAG_SUCESS = "exito";

        HashMap<String,String> datosPersona = new HashMap<>();

        datosPersona.put(RegisterClass.KEY_NAME,name);
        datosPersona.put(RegisterClass.KEY_LASTNAME1,lastName1);
        datosPersona.put(RegisterClass.KEY_LASTNAME2,lastName2);
        datosPersona.put(RegisterClass.KEY_ACCOUNT_TYPE,Integer.toString(accountType));

        String result = RegisterClass.sendPostRequest(URL_REGISTER,datosPersona);

        Log.v("lel", result);

        //TODO: No mame neto
        return result;

    }

    public static String login(String email, String password){
        //TODO: neeeeto holi (8
        return RESULT_OK;
    }

    private static String sendPostRequest(String requestURL,
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

        return response;
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
