package com.example.povilas.povilas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private QueryParser parser;

    public CustomInfoWindowAdapter(Context context, JSONObject jsonQuery){
        Context mContext = context;
        // assigns a custom window layout
        mWindow = LayoutInflater.from(context).inflate(R.layout.marker_info_window, null);
        parser = new QueryParser(jsonQuery);
    }

    // adds query results to info window
    private void renderWindowText(Marker marker, View view) throws JSONException {
        TextView windowTitle = (TextView)view.findViewById(R.id.infoTitle);
        TextView windowPartOfSpeech = (TextView)view.findViewById(R.id.infoPartOfSpeech);
        TextView windowDefinition = (TextView)view.findViewById(R.id.infoDefinition);
        //windowDefinition.setMovementMethod(new ScrollingMovementMethod()); // does not work with info windows
        String title = parser.getQueryWord();
        String partOfSpeech = parser.getPartOfSpeech();
        String definition = parser.getDefinition();
        windowTitle.setText(title);
        windowPartOfSpeech.setText(partOfSpeech);
        windowDefinition.setText(definition);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        try {
            renderWindowText(marker, mWindow);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        try {
            renderWindowText(marker, mWindow);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mWindow;
    }
}
