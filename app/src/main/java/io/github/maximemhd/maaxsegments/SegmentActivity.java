package io.github.maximemhd.maaxsegments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sweetzpot.stravazpot.activity.api.ActivityAPI;
import com.sweetzpot.stravazpot.activity.model.Activity;
import com.sweetzpot.stravazpot.athlete.api.AthleteAPI;
import com.sweetzpot.stravazpot.athlete.model.Athlete;
import com.sweetzpot.stravazpot.common.api.StravaConfig;
import com.sweetzpot.stravazpot.common.api.exception.StravaAPIException;
import com.sweetzpot.stravazpot.common.api.exception.StravaUnauthorizedException;
import com.sweetzpot.stravazpot.common.model.Time;
import com.sweetzpot.stravazpot.segment.model.SegmentEffort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class SegmentActivity extends AppCompatActivity {
    StravaConfig config = null;
    int nbr_act =1;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segment);
        Intent intent = getIntent();

         nbr_act = intent.getIntExtra("nbr",1);
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        String token = sharedPref.getString(getString(R.string.sharedPreferences), "disconnected");

        if (token == "disconnected") {
            ((TextView) findViewById(R.id.textView_connection)).setText("Disconnected");
        } else {

            config = StravaConfig.withToken(token)
                    .debug()
                    .build();
            new async().execute(config);
            new asyncListActivity().execute(config);
        }


    }

    private class asyncListActivity extends AsyncTask<StravaConfig, Integer, List<Activity>> {
        private ProgressDialog progressDialog;

        protected List<Activity> doInBackground(StravaConfig... config) {

            ActivityAPI activityAPI = new ActivityAPI(config[0]);
            List<Activity> activities = null;

            try {
                activities = activityAPI.listMyActivities()
                        .before(null)
                        .after(null)
                        .inPage(1)
                        .perPage(nbr_act)
                        .execute();

            } catch (StravaUnauthorizedException e) {
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();

            } catch (StravaAPIException e) {
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();
            } finally {
                if (activities == null) {
                    return null;
                } else {
                    return activities;

                }
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(SegmentActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            progressDialog.setProgress(progress[0]);
            //Toast.makeText(SegmentActivity.this, "Activité "+progress[0], Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(List<Activity> result) {
            if (result == null) {
                Toast.makeText(SegmentActivity.this, "Liste NON CHARGÉE", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SegmentActivity.this, "Liste chargée", Toast.LENGTH_SHORT).show();
                // List<String> liste_titres = new ArrayList<>();
                List<Integer> liste_id = new ArrayList<>();
                //List<SegmentEffort> liste_segments = new ArrayList<>();
                //List<String> liste_segments_titres = new ArrayList<>();

                for (Activity act : result) {
                    // liste_titres.add(act.getName());
                    liste_id.add(act.getID());
                }

                new async_activity().execute(liste_id);


            }


        }
    }


    private class async extends AsyncTask<StravaConfig, Integer, Athlete> {
        protected Athlete doInBackground(StravaConfig... config) {

            AthleteAPI athleteAPI = new AthleteAPI(config[0]);
            Athlete athlete = null;

            try {
                athlete = athleteAPI.retrieveCurrentAthlete()
                        .execute();

            } catch (StravaUnauthorizedException e) {
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();

            } catch (StravaAPIException e) {
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();
            } finally {
                if (athlete == null) {
                    return null;
                } else {
                    return athlete;

                }
            }


        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Athlete result) {
            //Toast.makeText(Activity_Dashboard.this, "Athlete: "+ result.getFirstName(), Toast.LENGTH_SHORT).show();
            if (result == null) {
                ((TextView) findViewById(R.id.textView_athlete)).setText("Need to reconnect");
            } else {
                ((TextView) findViewById(R.id.textView_athlete)).setText("Hello " + result.getFirstName() + " ! You are connected to Strava :-)");
            }


        }
    }

    private static class MyTaskParams {
        StravaConfig config;
        Integer id;

        MyTaskParams(StravaConfig config, Integer id) {
            this.config = config;
            this.id = id;

        }
    }

    private class async_activity extends AsyncTask<List<Integer>, Integer, List<String>> {
        private ProgressDialog dialog;
        List<Integer> liste_segments_id = new ArrayList<>();

        protected List<String> doInBackground(List<Integer>... monId) {

            ActivityAPI activityAPI = new ActivityAPI(config);
            Activity activity = null;
            Set set = new HashSet<>();


            for (int i = 0; i < monId[0].size(); i++) {
                try {
                    activity = activityAPI.getActivity(monId[0].get(i))
                            .includeAllEfforts(true)
                            .execute();

                } catch (StravaUnauthorizedException e) {
                    Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();

                } catch (StravaAPIException e) {
                    Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();
                } finally {
                    if (activity == null) {
                        return null;
                    } else {

                        Set subset = new HashSet<>();
                        //Set subsetid = new HashSet<>();
                        for (SegmentEffort segeff : activity.getSegmentEfforts()) {
                            subset.add(segeff.getName()+"--"+segeff.getSegment().getID());
                            //subsetid.add(segeff.getID());
                        }
                        set.removeAll(subset);
                        set.addAll(subset);
                       // setid.removeAll(subsetid);
                       // setid.addAll(subsetid);
                    }
                }

            }
            List<String> liste_segments = new ArrayList<>(set);
            return liste_segments;

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(SegmentActivity.this);
            dialog.setMessage("Loading, please wait.");
            dialog.show();
        }


        protected void onPostExecute(List<String> result) {
            //Toast.makeText(Activity_Dashboard.this, "Athlete: "+ result.getFirstName(), Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result == null) {

            } else {
                /*List<String> liste_segments_titres = new ArrayList<>();
                for (SegmentEffort segEff: result){
                   liste_segments_titres.add(segEff.getName());
                }*/
                Collections.sort(result);
                ListView listview = (ListView) findViewById(R.id.listSegments);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        SegmentActivity.this,
                        android.R.layout.simple_list_item_1,
                        result);

                listview.setAdapter(arrayAdapter);

                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        // Clicking on items
                        String[] parts = parent.getItemAtPosition(position).toString().split("--");
                        String mon_id = parts[1];
                        Toast.makeText(SegmentActivity.this, "ID:  "+parts[1], Toast.LENGTH_SHORT).show();
                         String URL = "http://www.strava.com/segments/" + mon_id;
                       /*Intent i = new Intent(SegmentActivity.this, WebActivity.class);
                        i.putExtra("url",URL);
                        startActivity(i);*/
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(URL));
                        startActivity(i);

                    }
                });


            }
        }

    }
}
