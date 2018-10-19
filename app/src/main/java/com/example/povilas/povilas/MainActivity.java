package com.example.povilas.povilas;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;

import android.provider.SearchRecentSuggestions;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Povilas Petrauskas
 * Created: 2018-10
 * Contacts: Ppetrauskas34@gmail.com
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SearchView searchView;
    private SearchRecentSuggestions suggestions; // searchView history
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button cleanHistory = (Button) findViewById(R.id.cleanHistoryBtn);
        cleanHistory.setOnClickListener(this);
        suggestions = new SearchRecentSuggestions(MainActivity.this,
                SuggestionProvider.AUTHORITY,
                SuggestionProvider.MODE); // defined in SuggestionProvider.java (allows history 2 line display)
        handleIntent(getIntent());
    }

    // checks if google maps activity can be initialized in second window
    public boolean isServicesOK(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(available == ConnectionResult.SUCCESS){
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,
                    available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "Map can't be loaded", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // repeats search on item clicked
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getWordApi(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setMaxWidth(Integer.MAX_VALUE); // takes whole appbar once engaged
        searchView.setIconified(false); // doesn't expand searchview
        searchView.requestFocus(1); // gives focus to a view for hint about what direction focus is heading
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getWordApi(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return newText.length() > 0;  // if input detected (not null)
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }
            @Override
            public boolean onSuggestionClick(int position) {
                // repeats search/searches results based on the word cliked
                CursorAdapter selectedView = searchView.getSuggestionsAdapter();
                Cursor cursor = (Cursor) selectedView.getItem(position);
                int index = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1); // picks row 1 as search text
                searchView.setQuery(cursor.getString(index), true);
                return true;
            }
        });
        return true;
    }

    public void getWordApi(String word){
        final Intent secondScreen = new Intent(this, SecondScreen.class);
        String apiLink = getString(R.string.API_LINK);
        // following code gets word results from WordsApi
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String uri = Uri.parse(apiLink + word)
                .buildUpon()
                .build().toString();
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                QueryParser parser = new QueryParser(); // extracts data from search results
                // saves input, partOfSpeech & current date and time, adds it to suggestion bar
                try {
                    suggestions.saveRecentQuery(parser.getQueryWord(response),
                            getCurrentDateAndTime() + "  " + parser.getPartOfSpeech(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // if google maps can be loaded, go to next screen with search results
                if(isServicesOK()){
                    secondScreen.putExtra("queryResults", response);
                    startActivity(secondScreen);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String apiKey = getString(R.string.API_KEY);
                Map<String, String> params = new HashMap<>();
                params.put("X-Mashape-Key", apiKey);
                params.put("Accept", "application/json");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public String getCurrentDateAndTime(){
        String result;
        // using JODA-TIME lib to get time and date
        DateTimeZone zone = DateTimeZone.forID("Europe/Vilnius");
        DateTime dayTime = new DateTime(zone);
        int day = dayTime.getDayOfMonth();
        int month = dayTime.getMonthOfYear();
        int hours = dayTime.getHourOfDay();
        int minutes = dayTime.getMinuteOfHour();
        result = month + "-" + day + " " + hours + ":" + minutes;
        return result;
    }

    // cleans search history when button is clicked
    @Override
    public void onClick(View v) {
        if(suggestions != null) {
            suggestions.clearHistory();
        }
    }
}