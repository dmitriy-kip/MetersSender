package ru.progmatik.meterssender.utils;

import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;

import org.xmlpull.v1.XmlPullParserException;

import java.util.List;

public class ZkhphoneLoader extends AsyncTask {

    private HttpsRequest request = new HttpsRequest();
    private ArrayMap<String, String> params;
    private String app;
    private String server;
    private RequestFinishListeners requesFinishListeners;

    public void setParams( String _server,  String _app, ArrayMap<String, String> _params)
    {
        this.server = _server;
        this.app = _app;
        this.params = _params;
    }
    public ArrayMap<String, String> getParams()
    {
        return this.params;
    }

    public String getMode(){
        return this.app;
    }

    public void setRequestFinishListeners(RequestFinishListeners requesFinishListeners)
    {
        this.requesFinishListeners = requesFinishListeners;
    }


    @Override
    protected void onPreExecute() {

        if (requesFinishListeners != null)
            try {
                requesFinishListeners.requestStarted(this.request);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
    }


    @Override
    protected Object doInBackground(Object[] o) {
        request.getResponse(server, app, params);
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {

        if (requesFinishListeners != null)
            try {
                requesFinishListeners.requestFinished(this.request);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

    }
}

