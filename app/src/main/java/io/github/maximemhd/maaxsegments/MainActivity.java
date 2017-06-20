package io.github.maximemhd.maaxsegments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.sweetzpot.stravazpot.athlete.api.AthleteAPI;
import com.sweetzpot.stravazpot.athlete.model.Athlete;
import com.sweetzpot.stravazpot.authenticaton.api.AccessScope;
import com.sweetzpot.stravazpot.authenticaton.api.AuthenticationAPI;
import com.sweetzpot.stravazpot.authenticaton.api.StravaLogin;
import com.sweetzpot.stravazpot.authenticaton.model.AppCredentials;
import com.sweetzpot.stravazpot.authenticaton.model.LoginResult;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginActivity;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginButton;
import com.sweetzpot.stravazpot.common.api.AuthenticationConfig;
import com.sweetzpot.stravazpot.common.api.StravaConfig;
import com.sweetzpot.stravazpot.common.api.exception.StravaAPIException;
import com.sweetzpot.stravazpot.common.api.exception.StravaUnauthorizedException;

import static com.sweetzpot.stravazpot.authenticaton.api.ApprovalPrompt.AUTO;

public class MainActivity extends AppCompatActivity {

    static{
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private static final int RQ_LOGIN = 1001;
    int nb_activity =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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
            new async().execute(config);
        }

        //login button
        StravaLoginButton loginButton = (StravaLoginButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        Button getButton = (Button) findViewById(R.id.getListButton);
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
        //Set the minimum value of NumberPicker
        np.setMinValue(0);
        //Specify the maximum value/number of NumberPicker
        np.setMaxValue(200);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        np.setWrapSelectorWheel(true);

        //Set a value change listener for NumberPicker
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                nb_activity = newVal;
            }
        });
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SegmentActivity.class);
                intent.putExtra("nbr",nb_activity);
                startActivity(intent);
            }
        });


    }

    private void login() {
        Intent intent = StravaLogin.withContext(this)
                .withClientID(13966)
                .withRedirectURI("http://maaxrun.pythonanywhere.com")
                .withApprovalPrompt(AUTO)
                .withAccessScope(AccessScope.VIEW_PRIVATE_WRITE)
                .makeIntent();
        startActivityForResult(intent, RQ_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RQ_LOGIN && resultCode == RESULT_OK && data != null) {
            Log.d("Strava code", data.getStringExtra(StravaLoginActivity.RESULT_CODE));
            //Toast.makeText(this, data.getStringExtra(StravaLoginActivity.RESULT_CODE), Toast.LENGTH_LONG).show();
            new codeToToken().execute(data.getStringExtra(StravaLoginActivity.RESULT_CODE));

        }
    }

    private class codeToToken extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... code) {
            AuthenticationConfig config = AuthenticationConfig.create()
                    .debug()
                    .build();
            AuthenticationAPI api = new AuthenticationAPI(config);
            LoginResult result = api.getTokenForApp(AppCredentials.with(13966, "a67b6efd42d941633fd631b35df2d22ae9b566c1"))
                    .withCode(code[0])
                    .execute();
            return result.getToken().toString();
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            //showDialog("Downloaded " + result + " bytes");
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();

            SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(
                    getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.sharedPreferences), result);
            editor.commit();
            StravaConfig config = StravaConfig.withToken(result)
                    .debug()
                    .build();
            new async().execute(config);


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
                Toast.makeText(MainActivity.this, "erreur", Toast.LENGTH_SHORT).show();

            }catch(StravaAPIException e){
                Toast.makeText(MainActivity.this, "erreur", Toast.LENGTH_SHORT).show();
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
                StravaLoginButton loginButton = (StravaLoginButton) findViewById(R.id.login_button);
                loginButton.setVisibility(View.GONE);
                ((TextView)findViewById(R.id.textView_connection)).setText("Connected");
                Button getButton = (Button) findViewById(R.id.getListButton);
                getButton.setVisibility(View.VISIBLE);

                NumberPicker numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
                numberPicker.setVisibility(View.VISIBLE);

                TextView info = (TextView) findViewById(R.id.textInfo);
                info.setVisibility(View.VISIBLE);
                //U+1F603 (emoji)
            }


        }
    }
}
