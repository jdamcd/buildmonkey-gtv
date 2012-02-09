package com.jdamcd.buildmonkey;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jdamcd.buildmonkey.pojo.Job;
import com.jdamcd.buildmonkey.pojo.Jobs;
import com.jdamcd.buildmonkey.task.IgnitedAsyncTask;

public class BuildMonkey extends Activity implements OnItemClickListener {

    private static String SERVER_URL = "your Jenkins server URL goes here";
    private static String ENDPOINT = "/api/json?tree=jobs[name,color,url]";
    private static final int UPDATE_INTERVAL = 60 * 1000;

    private GridView builds;
    private ArrayList<Job> jobs;
    
    final Handler handler = new Handler();
    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            new FetchStatusTask(BuildMonkey.this).execute();
            handler.postDelayed(updater, UPDATE_INTERVAL);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        builds = (GridView) findViewById(R.id.list_projects);
        handler.post(updater);
    }
    
    private class FetchStatusTask extends IgnitedAsyncTask<BuildMonkey, Void, Void, Void> {

        private Jobs projects;

        public FetchStatusTask(BuildMonkey context) {
            super(context);
        }

        @Override
        protected Void run(Void... params) throws Exception {
            AndroidHttpClient client = AndroidHttpClient.newInstance("BuildMonkey/1.0");
            HttpResponse response = client.execute(new HttpGet(SERVER_URL + ENDPOINT));
            InputStream stream = response.getEntity().getContent();
            parse(stream);
            client.close();
            return null;
        }

        private void parse(InputStream stream) throws Exception {
            Gson gson = new Gson();
            Reader reader = new InputStreamReader(stream);
            projects = gson.fromJson(reader, Jobs.class);
        }

        @Override
        protected void onSuccess(BuildMonkey context, Void result) {
            builds.setAdapter(new JobAdapter(BuildMonkey.this, projects.getJobs()));
            builds.setOnItemClickListener(context);
            jobs = projects.getJobs();
            updateGlobalStatus();
        }

        @Override
        protected void onError(BuildMonkey context, Exception error) {
            super.onError(context, error);
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show();
        }
    }

    public void updateGlobalStatus() {
        for (Job eachJob : jobs) {
            if (eachJob.getColor().equals("red")) {
                setBackground(R.drawable.bg_red);
                return;
            }
        }
        setBackground(R.drawable.bg_green);
    }

    public void setBackground(int resourceId) {
        findViewById(R.id.main_layout).setBackgroundResource(resourceId);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        startActivity(urlIntent(jobs.get(position).getUrl()));
    }
    
    public static Intent urlIntent(String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

}