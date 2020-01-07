package com.example.fildas.vocal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public abstract class VocalActivity extends AppCompatActivity {

    private final int SPEECH_RECOGNITION_SUCCESS_OUPUT = 42;

    private RequestQueue queue;
    protected Vocal.CollPrx iceProxy;

    /**
     * Activity on create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createIceProxy();
        this.queue = Volley.newRequestQueue(this);
    }

    /**
     * Create menu actions
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle menu actions
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_speech:
                startSpeechToText();
                return true;
            case R.id.action_index:
                index();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Create ice proxy
     */
    protected void createIceProxy() {
        try {
            com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize();
            String s = String.format("SimplePrinter:tcp -h %1$s -p %2$s", getString(R.string.ip), getString(R.string.port_ice));
            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy(s);
            iceProxy = Vocal.CollPrx.checkedCast(base);
            if(iceProxy == null) {
                throw new Error("Invalid proxy");
            }
        } catch(Exception e) {
            System.out.println("Error creating iceProxy");
        }
    }

    /**
     * Start speech recognition service
     */
    protected void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Say something...");
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_SUCCESS_OUPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                           "Sorry ! Speech recognition is not supported on this device.",
                           Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback for speech recognition activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOGNITION_SUCCESS_OUPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // result: contient différentes versions de la phrase énoncée.
                    // Il s'agit notamment de différences au niveau des
                    // majuscules/minuscules en début de mot.
                    // La casse n'ayant pas un grand intérêt ici, on ne tient compte
                    // que du premier résultat.
                    String speech = result.get(0);
                    sendRequest(speech);
                }
                break;
            }
        }
    }

    /**
     * Send a request to the voice command analyzer
     */
    protected void sendRequest(String speech) {
        String url = String.format("http://%1$s:%2$s/command?vocal=%3$s", getString(R.string.ip), getString(R.string.port_command_analyzer), speech.replaceAll(" ", "+"));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Get the command and the rest of the voice command from the JSONObject
                        String command = "", params = "";
                        try {
                            command = (String) response.get("command");
                            params = (String) response.get("params");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        onVoiceCommandAnalyzed(command, params);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                    }
                });

        queue.add(jsonObjectRequest);
    }

    /**
     * Display the index
     */
    protected abstract void index();

    /**
     * Handle voice commands in the derived Activity
     */
    protected abstract void onVoiceCommandAnalyzed(String command, String params);
}
