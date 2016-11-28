package ch.unifr.diva;

import ch.unifr.diva.exceptions.MethodNotAvailableException;
import ch.unifr.diva.request.DivaServicesRequest;
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
import java.util.LinkedList;
import java.util.List;

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
     * @param url     URL for the HTTP Request
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
     *
     * @param url The url of for the request
     * @return the extracted JSON Object of the response
     */
    public static JSONObject executeGet(String url) {
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
     * @param result        The JSON object return from the POST request
     * @param checkInterval How often to check for new results (in seconds)
     * @param request       The DivaServicesRequest
     * @return The result JSON object
     */
    public static List<JSONObject> getResult(JSONObject result, int checkInterval, DivaServicesRequest request) throws MethodNotAvailableException {
        if (result.has("statusCode") && result.getInt("statusCode") == 404) {
            throw new MethodNotAvailableException("This method is currently not available");
        }
        JSONArray results = result.getJSONArray("results");
        List<JSONObject> response = new LinkedList<>();
        if (request.getCollection().isPresent()) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject res = results.getJSONObject(i);
                String url = res.getString("resultLink");
                JSONObject getResult = getSingleResult(url, checkInterval);
                response.add(getResult);
            }
            return response;
        } else if (request.getImage().isPresent()) {
            //handle single images
            JSONObject correctResult = null;
            for (int i = 0; i < results.length(); i++) {
                JSONObject res = results.getJSONObject(i);
                if (res.getString("md5").equals(request.getImage().get().getMd5Hash())) {
                    correctResult = res;
                }
            }
            String url = correctResult.getString("resultLink");
            JSONObject getResult = getSingleResult(url, checkInterval);
            response.add(getResult);
            return response;
        }
        return null;
    }

    private static JSONObject getSingleResult(String url, int checkInterval) {
        JSONObject getResult = executeGet(url);
        while (!getResult.getString("status").equals("done")) {
            //Result not available yet
            try {
                //Wait 5 seconds and try again
                Thread.sleep(checkInterval * 1000);
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

    private static JSONObject parseEntity(HttpEntity entity) throws IOException {
        InputStream instream = entity.getContent();
        JSONObject result = new JSONObject(convertStreamToString(instream));
        instream.close();
        return result;
    }

}
