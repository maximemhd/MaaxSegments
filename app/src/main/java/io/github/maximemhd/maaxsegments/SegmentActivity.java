package io.github.maximemhd.maaxsegments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.List;

public class SegmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segment);

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        String token = sharedPref.getString(getString(R.string.sharedPreferences),"disconnected");

        if(token=="disconnected"){
            ((TextView)findViewById(R.id.textView_connection)).setText("Disconnected");
        }
        else {
            StravaConfig config = null;
            config = StravaConfig.withToken(token)
                    .debug()
                    .build();
            //new async().execute(config);
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
                        .perPage(100)
                        .execute();

            }catch(StravaUnauthorizedException e){
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();

            }catch(StravaAPIException e){
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();
            }finally{
                if(activities==null){
                    return null;
                }else{
                    return activities;

                }
            }

        }

        protected void onProgressUpdate(Integer... progress) {
           /* if (progressDialog == null) {
                progressDialog = new ProgressDialog(SegmentActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            progressDialog.setProgress(progress[0]);*/
            //Toast.makeText(SegmentActivity.this, "Activité "+progress[0], Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(List<Activity> result) {
            if(result==null){
                Toast.makeText(SegmentActivity.this, "Liste NON CHARGÉE", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(SegmentActivity.this, "Liste chargée", Toast.LENGTH_SHORT).show();
                List<String> liste_titres = null;
                for (Activity act : result){
                    liste_titres.add(act.getName());
                }


                ListView listview = (ListView)findViewById(R.id.listSegments);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                 SegmentActivity.this,
                 android.R.layout.simple_list_item_1,
                        liste_titres );

                listview.setAdapter(arrayAdapter);

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

            }catch(StravaUnauthorizedException e){
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();

            }catch(StravaAPIException e){
                Toast.makeText(SegmentActivity.this, "erreur", Toast.LENGTH_SHORT).show();
            }finally{
                if(athlete==null){
                    return null;
                }else{
                    return athlete;

                }
            }



        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Athlete result) {
            //Toast.makeText(Activity_Dashboard.this, "Athlete: "+ result.getFirstName(), Toast.LENGTH_SHORT).show();
            if(result==null){
                ((TextView)findViewById(R.id.textView_athlete)).setText("Need to reconnect");
            }else{
                ((TextView)findViewById(R.id.textView_athlete)).setText("Hello "+result.getFirstName()+" ! You are connected to Strava :-)");
            }


        }
    }
}
