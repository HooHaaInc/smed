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
    private static final String URL_EDIT_HOMEWORK = "http://148.225.83.3/~e5ingsoft2/smed/ActualizarTarea.php";
    private static final String URL_DELETE_HOMEWORK = "http://148.225.83.3/~e5ingsoft2/smed/borrarTarea.php";
    private static final String URL_GET_ALL_REPORTS = "http://148.225.83.3/~e5ingsoft2/smed/ObtenerReportes.php";
    private static final String URL_NEW_REPORT = "http://148.225.83.3/~e5ingsoft2/smed/CrearReporte.php";
    private static final String URL_NEW_MEETING = "http://148.225.83.3/~e5ingsoft2/smed/CrearJunta.php";
    private static final String URL_GET_ALL_MEETINGS = "http://148.225.83.3/~e5ingsoft2/smed/ObtenerJuntas.php";
    private static final String URL_GET_PERSON_NAME_BY_ID = "http://148.225.83.3/~e5ingsoft2/smed/ObtenerNombrePorIDAlumno.php";
    private static final String URL_GET_ALL_GROUPS = "http://148.225.83.3/~e5ingsoft2/smed/ObtenerGrupos.php";
    private static final String URL_ASK_GROUP = "http://148.225.83.3/~e5ingsoft2/smed/solicitudGrupo.php";
    private static final String URL_GET_STUDENTS_FROM_GROUP = "http://148.225.83.3/~e5ingsoft2/smed/obtenerAlumnosDeGrupo.php";
    private static final String URL_CONNECT_PARENT_STUDENT = "http://148.225.83.3/~e5ingsoft2/smed/conectarPadreAlumno.php";
    private static final String URL_CREATE_GROUP = "http://148.225.83.3/~e5ingsoft2/smed/CrearGrupo.php";

    public static final int STUDENT = 1;
    public static final int TEACHER = 2;
    public static final int PARENT = 3;

    public static final String KEY_ID_PERSON = "id_persona";
    public static final String KEY_NAME = "nombre";
    public static final String KEY_LASTNAME1 = "apellido_paterno";
    public static final String KEY_LASTNAME2 = "apellido_materno";
    public static final String KEY_ACCOUNT_TYPE = "tipo_persona";

    public static final String KEY_ID_TEACHER = "id_maestro";

    public static final String KEY_EMAIL = "correo";
    public static final String KEY_PASSWORD = "clave";

    public static final String KEY_ID_HOMEWORK = "id_tarea";
    public static final String KEY_ID_GROUP = "id_grupo";
    public static final String KEY_TITLE = "titulo";
    public static final String KEY_DESCRIPTION = "descripcion";
    public static final String KEY_CLASS = "materia";
    public static final String KEY_DATE = "fecha";
    public static final String KEY_ID_STUDENT = "id_alumno";
    public static final String KEY_COMMENT = "comentario";
    public static final String KEY_GCM = "gcm_regid";

    public static final String KEY_ID_GROUPS = "esgrupal";

    public static final String KEY_ID_PARENT = "id_padre";
    public static final String KEY_MOTIVE = "motivo";

    public static final String KEY_GROUP_NAME = "group_name";
    public static final String KEY_GROUP_KEY = "clave";
    public static final String KEY_TURNO = "turno";

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
                                                  String lastName2, int accountType,String id){

        final String TAG_SUCESS = "exito";

        HashMap<String,String> datosPersona = new HashMap<>();
        datosPersona.put(SMEDClient.KEY_EMAIL,email);
        datosPersona.put(SMEDClient.KEY_PASSWORD,password);
        datosPersona.put(SMEDClient.KEY_NAME,name);
        datosPersona.put(SMEDClient.KEY_LASTNAME1,lastName1);
        datosPersona.put(SMEDClient.KEY_LASTNAME2,lastName2);
        datosPersona.put(SMEDClient.KEY_ACCOUNT_TYPE,Integer.toString(accountType));
        datosPersona.put(SMEDClient.KEY_GCM,id);

        JSONObject result = SMEDClient.sendPostRequest(URL_REGISTER,datosPersona);
        Log.d("SMED", result.toString());

        try {
            datosPersona.put("message", result.getString("message"));
            datosPersona.put(KEY_ID_PERSON, result.getString("id_persona"));
            if(accountType==3){
                datosPersona.put(KEY_ID_PARENT,result.getString("id_padre"));
                Log.v("ID_PADRE", result.getString("id_padre"));
            }else if(accountType==2){
                datosPersona.put(KEY_ID_TEACHER, result.getString("id_maestro"));
            }else if(accountType == 1)
                datosPersona.put(KEY_ID_STUDENT, result.getString("id_alumno"));
            Log.v("ID_PERSONA", result.getString("id_persona"));

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
            datosPersona.put("message",result.getString("message"));
            Log.v("login:",result.toString());
            datosPersona.put(KEY_ID_PERSON,result.getString("id_persona"));
            datosPersona.put(KEY_NAME,result.getString("nombre"));
            datosPersona.put(KEY_LASTNAME1,result.getString("apellido_paterno"));
            datosPersona.put(KEY_LASTNAME2,result.getString("apellido_materno"));
            datosPersona.put(KEY_ACCOUNT_TYPE, result.getString("tipo_persona"));
            if(result.getString("tipo_persona").equals("2")) datosPersona.put(KEY_ID_TEACHER,result.getString("id_maestro"));
            if(result.getString("tipo_persona").equals("1")) datosPersona.put(KEY_ID_STUDENT,result.getString("id_alumno"));
            //TODO: datosPersona.put(KEY_ID_GROUP, result.getString(KEY_ID_GROUP));
            Log.v("ID_ALUMNO_CLIENT", result.getString("id_alumno"));
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
            Log.v("test", res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            res = RESULT_ERROR;
        }

        return res;
    }

    public static String newReporte(int id_alumno,String comentario,Date fecha){
        //supongo que con el id_alumno puedes obtener el del grupo (8
        HashMap<String,String> datosReporte = new HashMap<>();

        datosReporte.put(SMEDClient.KEY_ID_STUDENT, Integer.toString(id_alumno));
        datosReporte.put(SMEDClient.KEY_COMMENT,comentario);
        datosReporte.put(SMEDClient.KEY_DATE,fecha.toString());

        JSONObject result = SMEDClient.sendPostRequest(URL_NEW_REPORT,datosReporte);

        String res = "";

        try{
            res = result.getString("message");
            Log.v("test", res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            res = RESULT_ERROR;
        }
        Log.v("D:",res);
        return res;
    }

    public static String newJunta(int id,String titulo,String desc,Date fecha,boolean x){
        HashMap<String,String> datosJunta = new HashMap<>();

        if(!x){
            datosJunta.put(SMEDClient.KEY_ID_PARENT,Integer.toString(id));
            datosJunta.put(SMEDClient.KEY_ID_GROUPS,"1");
        }else{
            datosJunta.put(SMEDClient.KEY_ID_GROUP,Integer.toString(id));
            datosJunta.put(SMEDClient.KEY_ID_GROUPS,"0");
        }
        datosJunta.put(SMEDClient.KEY_MOTIVE,titulo);
        datosJunta.put(SMEDClient.KEY_DESCRIPTION,desc);
        datosJunta.put(SMEDClient.KEY_DATE,fecha.toString());

        JSONObject result = SMEDClient.sendPostRequest(URL_NEW_MEETING,datosJunta);

        String res = "";

        try{
            res = result.getString("message");
            Log.v("test", res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            res = RESULT_ERROR;
        }
        Log.v("D:",res);
        return res;
    }

    public static String editHomework(int id_tarea,int id_grupo,String titulo,String descripcion,String materia,Date fecha){

        HashMap<String,String> datosTarea = new HashMap<>();

        datosTarea.put(SMEDClient.KEY_ID_HOMEWORK,Integer.toString(id_tarea));
        datosTarea.put(SMEDClient.KEY_ID_GROUP,Integer.toString(id_grupo));
        datosTarea.put(SMEDClient.KEY_DATE,fecha.toString());
        datosTarea.put(SMEDClient.KEY_CLASS,materia);
        datosTarea.put(SMEDClient.KEY_TITLE,titulo);
        datosTarea.put(SMEDClient.KEY_DESCRIPTION,descripcion);

        JSONObject result = SMEDClient.sendPostRequest(URL_EDIT_HOMEWORK,datosTarea);

        String res="";
        try{
            res = result.getString("message");
            Log.v("test", res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            res = RESULT_ERROR;
        }

        return res;

    }

    public static String deleteHomework(int id_tarea){
        HashMap<String,String> datosTarea = new HashMap<>();

        datosTarea.put(SMEDClient.KEY_ID_HOMEWORK,Integer.toString(id_tarea));

        JSONObject result = SMEDClient.sendPostRequest(URL_DELETE_HOMEWORK,datosTarea);

        String res="";
        try{
            res = result.getString("message");
            Log.v("borrandooooo", res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            res = RESULT_ERROR;
        }

        return res;
    }

    public static JSONObject getAllHomework(int groupId){
        HashMap<String,String> params = new HashMap<>();
        params.put(KEY_ID_GROUP, groupId+"");
        JSONObject result = SMEDClient.sendPostRequest(URL_GET_ALL_HOMEWORK,params);

        String res="";
        try{
            Log.v("sendpostrequest",result.getString("tareas"));
            res = result.getString("message");
            Log.v("test",res);
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return result;
    }

    public static String createGroup(int teacherId, String groupName, boolean morningShift){
        HashMap<String,String> params = new HashMap<>();
        params.put(KEY_ID_TEACHER, teacherId+"");
        params.put(KEY_GROUP_KEY, groupName);
        params.put(KEY_TURNO, morningShift? "matutino" : "vespertino");
        JSONObject result = SMEDClient.sendPostRequest(URL_CREATE_GROUP,params);

        String res="";
        try{
            Log.v("test",result.toString());
            Log.v("test", teacherId+"");
            res = result.getString(KEY_ID_GROUP);
            return res;
        }catch(JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject getAllReports(int groupId, int parentId){
        HashMap<String,String> params = new HashMap<>();
        params.put(KEY_ID_GROUP, groupId+"");
        params.put(KEY_ID_PARENT, parentId+"");
        //TODO: si parentId == -1, obtener todos los reportes del grupo
        JSONObject result = SMEDClient.sendPostRequest(URL_GET_ALL_REPORTS,params);

        String res = "";
        try{
            res = result.getString("message");
        }catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject getAllMeetings(int groupId, int parentId){
        HashMap<String,String> params = new HashMap<>();
        params.put(KEY_ID_GROUP, groupId+"");
        params.put(KEY_ID_PARENT, parentId+"");
        JSONObject result = SMEDClient.sendPostRequest(URL_GET_ALL_MEETINGS,params);

        String res = "";
        try{
            res = result.getString("message");
            Log.v("getMeetings",res);
        }catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject getAllGroups(){
        HashMap<String,String> params = new HashMap<>();

        JSONObject result = SMEDClient.sendPostRequest(URL_GET_ALL_GROUPS,params);
        String res = "";
        try{
            res = result.getString("message");
        }catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject getAllStudentsFromGroup(int id_grupo){
        HashMap<String,String> params = new HashMap<>();

        params.put(SMEDClient.KEY_ID_GROUP,Integer.toString(id_grupo));

        JSONObject result = SMEDClient.sendPostRequest(URL_GET_STUDENTS_FROM_GROUP,params);
        String res = "";
        try{
            res = result.getString("message");
        }catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return result;
    }

    public static String getPersonNameByStudentID(int id_alumno){
        HashMap<String,String> params = new HashMap<>();

        params.put(SMEDClient.KEY_ID_PERSON, Integer.toString(id_alumno));

        JSONObject result = SMEDClient.sendPostRequest(URL_GET_PERSON_NAME_BY_ID,params);


        String res = "";
        try{
            res = result.getString("nombre");
            Log.v("",result.getString("nombre"));
        }catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return res;
    }

    public static boolean askForGroup(int id_alumno,int id_grupo){
        HashMap<String,String> params = new HashMap<>();

        params.put(SMEDClient.KEY_ID_STUDENT,Integer.toString(id_alumno));
        params.put(SMEDClient.KEY_ID_GROUP,Integer.toString(id_grupo));

        JSONObject result = SMEDClient.sendPostRequest(URL_ASK_GROUP,params);

        String res = "";
        try{
            res = result.getString("message");
            if(res.equals("Solicitud enviada.")){
                return true;
            }
        }catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return false;
    }

    public static boolean connectParentStudent(int id_alumno,int id_padre){
        HashMap<String,String> params = new HashMap<>();

        params.put(SMEDClient.KEY_ID_STUDENT,Integer.toString(id_alumno));
        params.put(SMEDClient.KEY_ID_PARENT,Integer.toString(id_padre));

        JSONObject result = SMEDClient.sendPostRequest(URL_CONNECT_PARENT_STUDENT,params);

        String res = "";
        try{
            res = result.getString("message");
            if(res.equals("Conexion realizada.")){
                return true;
            }
        }catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return false;
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
