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
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Ernesto on 25/09/2015.
 */
public class SMEDClient {

    private static final String URL_REGISTER = "http://148.225.83.3/~e5ingsoft2/smed/Registro.php";
    private static final String URL_LOGIN = "http://148.225.83.3/~e5ingsoft2/smed/Login.php";
    private static final String URL_NEW_HOMEWORK = "http://148.225.83.3/~e5ingsoft2/smed/CrearTarea.php";
    private static final String URL_GET_ALL_HOMEWORK = "http://148.225.83.3/~e5ingsoft2/smed/ObtenerTareas.php";

    public static final String KEY_NAME = "nombre";
    public static final String KEY_LASTNAME1 = "apellido_paterno";
    public static final String KEY_LASTNAME2 = "apellido_materno";
    public static final String KEY_ACCOUNT_TYPE = "tipo_persona";

    public static final String KEY_EMAIL = "correo";
    public static final String KEY_PASSWORD = "clave";

    public static final String KEY_ID_HOMEWORK = "id_tarea";
    public static final String KEY_ID_GROUP = "id_grupo";
    public static final String KEY_TITLE = "titulo";
    public static final String KEY_DESCRIPTION = "descripcion";
    public static final String KEY_CLASS = "materia";
    public static final String KEY_DATE = "fecha";

    public static final String RESULT_NEW_USER = "Registro exitoso.";
    public static final String RESULT_OK = "Operación exitosa.";
    public static final String RESULT_WRONG_PASSWORD = "Clave de acceso incorrecta."; //puedes cambiarlos si quieres ... Ok nan :>
    public static final String RESULT_LOGGED_IN = "Informacion correcta.";
    public static final String RESULT_USER404 = "Correo no registrado.";
    public static final String RESULT_USER_ALREADY_EXISTS = "Correo ya registrado.";
    public static final String RESULT_ERROR = "Error en la conexión";
    public static final String RESULT_NEW_HOMEWORK_CREATED = "Tarea creada.";

    public static HashMap<String,String> register(String email, String password,
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
            datosPersona.put("message",result.getString("message"));
            datosPersona.put("id_persona",result.getString("id_persona"));
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
        return datosPersona;

    }

    public static HashMap<String,String> login(String email, String password){

        HashMap<String,String> datosPersona = new HashMap<>();
        datosPersona.put(SMEDClient.KEY_EMAIL,email);
        datosPersona.put(SMEDClient.KEY_PASSWORD, password);

        JSONObject result = SMEDClient.sendPostRequest(URL_LOGIN,datosPersona);



        String res="";
        try{
            Log.v("login:",result.getString("nombre"));
            datosPersona.put("message",result.getString("message"));
            datosPersona.put("id_persona",result.getString("id_persona"));
            datosPersona.put("nombre",result.getString("nombre"));
            datosPersona.put("apellido_paterno",result.getString("apellido_paterno"));
            datosPersona.put("apellido_materno",result.getString("apellido_materno"));
            datosPersona.put("tipo_persona",result.getString("tipo_persona"));
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return datosPersona;
    }

    public static String newHomework(int id_grupo,String titulo,String descripcion,String materia, Date fecha){

        HashMap<String,String> datosTarea = new HashMap<>();

        datosTarea.put(SMEDClient.KEY_ID_GROUP,Integer.toString(id_grupo));
        datosTarea.put(SMEDClient.KEY_DATE,fecha.toString());
        datosTarea.put(SMEDClient.KEY_CLASS,materia);
        datosTarea.put(SMEDClient.KEY_TITLE,titulo);
        datosTarea.put(SMEDClient.KEY_DESCRIPTION,descripcion);

        JSONObject result = SMEDClient.sendPostRequest(URL_NEW_HOMEWORK,datosTarea);

        String res="";
        try{
            res = result.getString("message");
            Log.v("test",res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            res = RESULT_ERROR;
        }

        return res;
    }

    public static JSONObject getAllHomework(){
        HashMap<String,String> params = new HashMap<>();

        JSONObject result = SMEDClient.sendPostRequest(URL_GET_ALL_HOMEWORK,params);

        String res="";
        try{
            res = result.getString("message");
            Log.v("test",res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return result;
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
            Log.v("sendpostrequest",response);
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
