package com.example.povilas.povilas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryParser {
    private JSONObject queryResults = null;
    public QueryParser(){} // default empty c-tor
    public QueryParser(JSONObject results){ queryResults = results; }

    public String getQueryWord() throws JSONException {
        return queryResults.getString("word");
    }

    public String getQueryWord(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getString("word");
    }

    public String getPartOfSpeech() throws JSONException {
        String finalResults = "";
        JSONArray results = queryResults.getJSONArray("results");
        JSONObject item = results.getJSONObject(0);
        finalResults = item.getString("partOfSpeech");
        return finalResults;
    }

    public String getPartOfSpeech(String response) throws JSONException {
        String partOfSpeech;
        JSONObject jsonObject = new JSONObject(response);
        JSONArray results = jsonObject.getJSONArray("results");
        // gets the first instance of partOfSpeech
        JSONObject item = results.getJSONObject(0);
        partOfSpeech = item.getString("partOfSpeech");
        return partOfSpeech;
    }

    public String getDefinition() throws JSONException {
        String finalResults = "";
        JSONArray results = queryResults.getJSONArray("results");
        for(int i = 0; i < results.length(); i++){
            JSONObject item = results.getJSONObject(i);
            finalResults += item.getString("definition") + " ";
        }
        return finalResults;
    }

    public String getExamples() throws JSONException{
        String finalExamples = "";
        JSONArray results = queryResults.getJSONArray("results");
        for (int i=0; i < results.length(); i++) {
            JSONObject item = results.getJSONObject(i);
            if (item.has("examples")) {
                finalExamples += item.getString("examples") + " ";
            }
        }
        return finalExamples;
    }
}
