package edmt.dev.androidcustomkeyboard;

import android.app.Service;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EDMTKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;

    private  boolean isCaps = false;

    private String CompleteString ="";
    private boolean isEng=false;

    private String translatedResult="";

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard,null);
        keyboard = new Keyboard(this,R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int i, int[] ints) {

        final InputConnection ic = getCurrentInputConnection();
        playClick(i);
        switch (i)
        {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1,0);
            break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));
                break;
            case 1000:
                //Log.d("if English is pressed",translatedResult);
                //ic.commitText(translatedResult,1);
                //translatedResult="";
                isEng=true;
                CompleteString="";
                break;
            case 2000:
                isEng = false;
                CompleteString="";
                break;
            case 32:
                if (!isEng){
                    Log.d("if English is pressed",translatedResult);
                    ic.commitText(translatedResult+" ",1);
                    translatedResult="";
                    CompleteString="";
                }
                else{
                    char code = (char)i;
                    if(Character.isLetter(code) && isCaps)
                        code = Character.toUpperCase(code);
                    ic.commitText(String.valueOf(code),1);
                }
            default:
                char code = (char)i;
                if(Character.isLetter(code) && isCaps)
                    code = Character.toUpperCase(code);
                String text= String.valueOf(code);
                if (isEng==false){
                    CompleteString+=text;

                    String url="https://www.googleapis.com/language/translate/v2?key=AIzaSyBGhAh-nZqcgkfPjcayxqZTSEKEtHhVDhA&source=en&target=hi&callback=translateText&q="+CompleteString;
                    url = new String(url.trim().replace(" ","%20"));
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    //Log.e("inside onResponse","reached");

                                    try{

                                        Log.d("new response",response.substring(30));
                                        JSONObject jsonObject = new JSONObject(response.substring(30));
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                        JSONArray jsonArray = jsonObject1.getJSONArray("translations");
                                        JSONObject jsonObject2 = jsonArray.getJSONObject(0);
                                        translatedResult=jsonObject2.getString("translatedText");
                                        //ic.commitText(translatedResult,1);
                                        //translated[0] =jsonArray.getJSONObject(0).getString("translatedText");
                                        Log.d("text",translatedResult);

                                    }catch(JSONException e){
                                        //progressDialog.dismiss();
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "try again", Toast.LENGTH_SHORT).show();

                                    }


                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

                    //Requesting using volley
                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    requestQueue.add(stringRequest);
                }

                else
                    ic.commitText(String.valueOf(code),1);
        }

    }

    private void playClick(int i) {

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(i)
        {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onText(CharSequence charSequence) {
        Log.d("input typed",charSequence.toString());
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

}
