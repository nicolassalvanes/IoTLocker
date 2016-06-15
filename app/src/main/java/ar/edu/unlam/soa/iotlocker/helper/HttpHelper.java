package ar.edu.unlam.soa.iotlocker.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

    private String charset;
    private String contentType;
    public int connectTimeout;
    public int readTimeout;

    private NetworkInfo networkInfo;

    private HttpHelperCallback httpHelperCallback;
    private String stringResult;

    public enum HttpMethod {
        GET, POST;

        public static HttpMethod getFromName(String name){
            for(HttpMethod value : HttpMethod.values()){
                if(value.name().equals(name)){
                    return value;
                }
            }
            return null;
        }
    }

    public interface HttpHelperCallback {
        void onDataAvailable(String data);
    }

    public HttpHelper(Context context) {
        this(context, "UTF-8", "application/json", 15000, 15000);
    }
    public HttpHelper(Context context, String charset, String contentType,
                      Integer connTimeout, Integer readTimeout) {
        this.charset = charset;
        this.contentType = contentType;
        this.connectTimeout = connTimeout;
        this.readTimeout = readTimeout;

        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        this.networkInfo = connMgr.getActiveNetworkInfo();
    }

    public void doGet(String url, HttpHelperCallback callback) throws HttpException {
        if (this.networkInfo != null && !this.networkInfo.isConnected()) {
            throw new HttpException("Network not available");
        }
        this.httpHelperCallback = callback;
        new HttpAsyncTask().execute(url, HttpMethod.GET.name(), "");
    }

    public void doPost(String url, String data, HttpHelperCallback callback) throws HttpException {
        if (this.networkInfo != null && !this.networkInfo.isConnected()) {
            throw new HttpException("Network not available");
        }
        this.httpHelperCallback = callback;
        new HttpAsyncTask().execute(url, HttpMethod.POST.name(), data);
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                stringResult = connect(params[0], HttpMethod.getFromName(params[1]), params[2]);

            } catch (IOException e) {
                Log.e(this.getClass().getName(), "Unable to retrieve URL {" + params[0] + "} may be invalid.", e);
            }
            return stringResult;
        }

        @Override
        protected void onPostExecute(String stringResult) {
            super.onPostExecute(stringResult);
            httpHelperCallback.onDataAvailable(stringResult);
        }

        private String connect(String url, HttpMethod method, String data) throws IOException {
            Log.d(this.getClass().getName(), " >> " + method.name() + ": " + url + " data: " + data);
            String resultString = null;

            URL u = new URL(url);
            HttpURLConnection conn = null;
            InputStream in = null;
            try {
                conn = (HttpURLConnection) u.openConnection();
                if (contentType != null) {
                    conn.setRequestProperty("Content-Type", contentType);
                }
                conn.setRequestMethod(method.name());
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
                conn.setDoInput(true);
                if (method.equals(HttpMethod.POST)) {
                    conn.setDoOutput(true);
                }
                conn.connect();

                if (method.equals(HttpMethod.POST) && !data.trim().isEmpty()) {
                    OutputStream out = conn.getOutputStream();
                    out.write(data.getBytes(charset));
                    out.flush();
                    out.close();
                }

                int status = conn.getResponseCode();
                if (status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    Log.d(this.getClass().getName(), "Error code: " + status);
                    in = conn.getErrorStream();
                } else {
                    in = conn.getInputStream();
                }

                BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                resultString = stringBuilder.toString();

                Log.d(this.getClass().getName(), " >> " + method.name()
                        + ": " + url
                        + " data: " + data
                        + " result: " + resultString);

            } catch (IOException e) {
                throw e;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (in != null) {
                    in.close();
                }
            }
            return resultString;
        }
    }

    public class HttpException extends Exception{
        public HttpException(String detailMessage) {
            super(detailMessage);
        }
    }
}
