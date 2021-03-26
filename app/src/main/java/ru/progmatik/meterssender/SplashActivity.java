package ru.progmatik.meterssender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.progmatik.meterssender.utils.AddressItem;
import ru.progmatik.meterssender.utils.CheckConnection;
import ru.progmatik.meterssender.utils.HttpsRequest;
import ru.progmatik.meterssender.utils.RequestFinishListeners;
import ru.progmatik.meterssender.utils.ZkhphoneLoader;
import ru.progmatik.meterssender.utils.ZkhphoneResponse;

public class SplashActivity extends AppCompatActivity implements RequestFinishListeners {

    private SharedPreferences prefs;
    private List<AddressItem> addresses;
    private ZkhphoneLoader loader = null;
    private ZkhphoneResponse response;
    private String mode="", session="";
    private int i =0;
    private List<String> al;

    public SplashActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SystemClock.sleep(1000);
        getPreferences();
        if (!addresses.isEmpty()){
            al = new ArrayList<>();
            for (AddressItem item : addresses){
                al.add(item.getServer());
            }
            // add elements to al, including duplicates
            Set<String> hs = new HashSet<>();
            hs.addAll(al);
            al.clear();
            al.addAll(hs);
            for (String server: al){

                loader = new ZkhphoneLoader();
                loader.setRequestFinishListeners(SplashActivity.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished

                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("username", getString(R.string.login));
                params.put("userpswd",getString(R.string.password));
                mode = "/auth/";
                loader.setParams(server, mode, params);
                loader.execute();
            }
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void getPreferences(){
        addresses = new ArrayList<>();
        prefs = getSharedPreferences("Progmatik.MS", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("Progmatik.MS.addresses", "FAILURE");
        if (!storedHashMapString.equals("FAILURE")) {
            java.lang.reflect.Type type = new TypeToken<List<AddressItem>>() {
            }.getType();
            Gson gson = new Gson();
            List<AddressItem> testHashMap = gson.fromJson(storedHashMapString, type);
            for (AddressItem value : testHashMap)
                addresses.add(value);
        }
    }


    @Override
    public void requestStarted(HttpsRequest httpsRequest) throws XmlPullParserException {
        if (!CheckConnection.checkConnection(getBaseContext())) {
            Toast.makeText(getBaseContext(), "Отсутствует соединение с интернетом", Toast.LENGTH_SHORT).show();
            loader.cancel(true);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void requestFinished(HttpsRequest httpsRequest) throws XmlPullParserException {
        i +=1;
        response = new ZkhphoneResponse();
        response = httpsRequest.Parse();
        if (response != null) {
            session = response.getResult().get_session();
            String reqServer = httpsRequest.getServer();

            for (AddressItem address : addresses) {
                if (address.getServer().equals(reqServer)) {
                    address.setSession(session);
                }
            }
        }
        if (i == al.size()){
            Gson gson = new Gson();
            String hashMapString = gson.toJson(addresses);
            prefs.edit().putString("Progmatik.MS.addresses", hashMapString).apply();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
