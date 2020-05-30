package com.example.mpi_ii_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.security.ProviderInstaller;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class GitHubActivity extends AppCompatActivity {

    private ListView gitHubRepos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_hub);

        gitHubRepos = findViewById(R.id.gitHubRepos);

        new APIRequest().execute("https://api.github.com/users/KristapsNaglis/repos");
    }

    private class APIRequest extends AsyncTask<String,Void,String> {
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Searching for public GitHub repositories", Toast.LENGTH_SHORT).show();
            Log.e("KR-APP", "Searching for public GitHub repositories");
        }

        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String response) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                Log.i("JSON", "Num: " + jsonArray.length());
                readAllFiles(jsonArray, gitHubRepos);

            } catch (Exception e) {
                Log.e("JSON", "Parsing error");
            }
        }
    }

    public String readJSONFeed(String address) {
        // For Android devices with API X or lower because of ssl handshake errors (KitKat has this)
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (Exception e) {
            Log.e("APP", "Error getting newer security provider: " + e.toString());
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            SSLEngine engine = sslContext.createSSLEngine();
        } catch (Exception e) {
            Log.e("APP", "Error assigning new ssl: " + e.toString());
        }

        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            Log.d("APP", "Incorrect address: " + e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.d("APP", "Cannot open connection to url '" + url + "': " + e);
        }
        try {
            InputStream content = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            Log.d("APP", "Cannot get content from url '" + url + "': " + e);
        } finally {
            urlConnection.disconnect();
        }
        return stringBuilder.toString();
    }

    public void readAllFiles(JSONArray jsonArray, ListView listView){
        try {
            listView = gitHubRepos;
            List<String> fileListArrayMain = new ArrayList<String>();

            for (int i = 0; i < jsonArray.length(); i++) {
                fileListArrayMain.add(jsonArray.getJSONObject(i).getString("name"));
            }

            ArrayAdapter<String> arrayAdapterMain = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1 , fileListArrayMain);

            listView.setAdapter(arrayAdapterMain);
        } catch (Exception e) {
            Log.d("INFO", "No repos could be assigned to list");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hamburger_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        // Left as switch not an if because of future expansions
        switch (item.getItemId()) {
            case R.id.map:
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                return true;
            default:
                return true;
        }
    }
}
