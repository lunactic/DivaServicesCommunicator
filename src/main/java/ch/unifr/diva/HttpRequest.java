package ch.unifr.diva;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 08.10.2015.
 */
public class HttpRequest {

    /**
     * Executes a post request and returns the body json object
     *
     * @param url URL for the HTTP Request
     * @param payload the JSON payload to send
     * @return the extracted JSON response
     */
    public static JSONObject executePost(String url, JSONObject payload) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        try {
            StringEntity se = new StringEntity(payload.toString());
            se.setContentType("application/json");
            post.setEntity(se);
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return parseEntity(entity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * executes a GET request
     * @param url The url of for the request
     * @return the extracted JSON Object of the response
     */
    public static JSONObject executeGet(String url){
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // A Simple JSON Response Read
                return parseEntity(entity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }

    /**
     * Gets a result JSON object from the exeuction response
     * This method will run GET requests in a 5 second interval until the result is available
     *
     *
     * @param result The JSON object return from the POST request
     * @param index the result file to retrieve
     * @return The result JSON object
     */
    public static JSONObject getResult(JSONObject result, int index){
        JSONArray results = result.getJSONArray("results");
        String url = results.getJSONObject(index).getString("resultLink");
        JSONObject getResult = executeGet(url);
        while(!getResult.getString("status").equals("done")){
            //Result not available yet
            try {
                //Wait 5 seconds and try again
                Thread.sleep(5000);
                getResult = executeGet(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return getResult;
    }

    /**
     * Converts an input stream to a json string
     *
     * @param is
     * @return
     */
    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static JSONObject parseEntity(HttpEntity entity) throws IOException{
        InputStream instream = entity.getContent();
        JSONObject result = new JSONObject(convertStreamToString(instream));
        instream.close();
        return result;
    }

}
