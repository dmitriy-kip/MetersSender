package ru.progmatik.meterssender.utils;

import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class HttpsRequest {

    public enum requestState { rqsIdle, rqsLoading, rqsError, rqsComplete }
    private requestState state = requestState.rqsIdle;
    private String xmlResponse = "";
    private String lastError = "";
    private String server;

    public String getServer() {
        return server;
    }

    public requestState getState() {
        return this.state;
    }

    /*
    * Отправка https-запроса
    * */
    public void getResponse(String _server, String app, ArrayMap<String, String> params){
        this.server = _server;
        this.xmlResponse = "";

        String tmpURL = "https://"+_server + app;
        if ( params.size() >  0) {
            tmpURL += "?";
            for ( int i = 0; i < params.size(); i++ ) {
                if ( i > 0 ) {
                    tmpURL += "&";
                }
                tmpURL += Uri.encode(params.keyAt(i)) + "=" + Uri.encode(params.valueAt(i));
            }
        }

        String rqUrl = tmpURL;


        HttpsURLConnection conn = null;
        this.state = requestState.rqsLoading;
        try {
            HttpsTrustManager.allowAllSSL();
            BufferedReader reader;
            URL url = new URL( rqUrl );
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(25000);
            conn.setConnectTimeout(25000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();

            InputStream inputStream = conn.getInputStream();
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line.trim());
            }
            this.xmlResponse += buffer.toString();
            this.state = requestState.rqsComplete;
        } catch (Exception e) {
            //Log.d(TAG, "ошибка внутри DbConnection: " + e.getMessage());
            this.state = requestState.rqsError;
            this.lastError = e.getLocalizedMessage();
            //e.printStackTrace();
        } finally {
            if (conn != null){
                conn.disconnect();
            }
        }
    }


    public ZkhphoneResponse Parse() {
        ZkhphoneResponse response = null;
        XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        String text = "";
        String lastTag = "";
        String parentTag = "";
        ArrayMap<String, String> item = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
            parser.setInput( new StringReader(this.xmlResponse) );

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagname = parser.getName();
                switch (eventType) {

                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("Response")) {
                            response = new ZkhphoneResponse(); //<response></response>
                            parentTag = tagname;
                        } else if (tagname.equalsIgnoreCase("Result") && response != null ) {
                            response.createResult();//<response><result></result></response>
                            parentTag = tagname;
                        } else if (tagname.equalsIgnoreCase("List") && response != null && response.getResult() != null ) {
                            response.getResult().createList();//<response><result><list type="..."></list></result></response>
                            parentTag = tagname;
                            Integer attrsCount = parser.getAttributeCount();
                            if ( attrsCount > 0 ) {
                                for ( Integer i = 0; i < attrsCount; i++ ) {
                                    String attrName = parser.getAttributeName( i );
                                    String attrValue = parser.getAttributeValue(null, attrName );
                                    //keyValueItem attribute = new keyValueItem( attrName, attrValue );
                                    response.getResult().getList().getAttributes().put(attrName, attrValue);
                                }
                            }

                        } else if (tagname.equalsIgnoreCase("item") && response != null && response.getResult() != null && response.getResult().getList() != null ) {
                            item = new ArrayMap<>();
                            Integer attrsCount = parser.getAttributeCount();
                            if ( attrsCount > 0 ) {
                                for ( Integer i = 0; i < attrsCount; i++ ) {
                                    String attrName = parser.getAttributeName( i );
                                    String attrValue = parser.getAttributeValue(null, attrName );
                                    //subItem = new keyValueItem(attrName, attrValue);
                                    item.put(attrName, attrValue);
                                }
                            }
                        } else if (tagname.equalsIgnoreCase("meters") && response != null && response.getResult() != null ) {
                            response.getResult().createList();//<response><result><list type="..."></list></result></response>
                            parentTag = tagname;
                            Integer attrsCount = parser.getAttributeCount();
                            if ( attrsCount > 0 ) {
                                for ( Integer i = 0; i < attrsCount; i++ ) {
                                    String attrName = parser.getAttributeName( i );
                                    String attrValue = parser.getAttributeValue(null, attrName );
                                    //keyValueItem attribute = new keyValueItem( attrName, attrValue );
                                    response.getResult().getList().getAttributes().put(attrName, attrValue);
                                }
                            }

                        } else if (tagname.equalsIgnoreCase("meter") && response != null && response.getResult() != null && response.getResult().getList() != null ) {
                            item = new ArrayMap<>();
                            Integer attrsCount = parser.getAttributeCount();
                            if ( attrsCount > 0 ) {
                                for ( Integer i = 0; i < attrsCount; i++ ) {
                                    String attrName = parser.getAttributeName( i );
                                    String attrValue = parser.getAttributeValue(null, attrName );
                                    //subItem = new keyValueItem(attrName, attrValue);
                                    item.put(attrName, attrValue);
                                }
                            }
                        }
                        lastTag = tagname;

                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText().trim();
                        if ( !text.equals("") ) {
                            if ( lastTag.equalsIgnoreCase("Code") && parentTag.equalsIgnoreCase("Result") ) {
                                response.getResult().setCode( Integer.parseInt(text) );
                            } else if ( lastTag.equalsIgnoreCase("Desc") && parentTag.equalsIgnoreCase("Result") ) {
                                response.getResult().setDesc( text );
                            } else if ( lastTag.equalsIgnoreCase("Session") && parentTag.equalsIgnoreCase("Result") ) {
                                response.getResult().set_session( text );
                            } else if ( lastTag.equalsIgnoreCase("Type") && parentTag.equalsIgnoreCase("List") ) {
                                response.getResult().getList().setType( text );
                            } else if ( parentTag.equalsIgnoreCase("List") && item != null ) {
                                //subItem = new keyValueItem(lastTag, text);
                                //Item.addItem( subItem );
                                item.put(lastTag, text);
                            }
                        }
                        //text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if ((tagname.equalsIgnoreCase("Item")) || (tagname.equalsIgnoreCase("meter")))
                        {
                            response.getResult().getList().addItem(item);
                            item = null;
                        }
                        break;

                    default:  break;
                }


                eventType = parser.next();

            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static class HttpsTrustManager implements X509TrustManager {
        private static TrustManager[] trustManagers;
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

        @Override
        public void checkClientTrusted(
                X509Certificate[] x509Certificates, String s)
                throws java.security.cert.CertificateException {

        }

        @Override
        public void checkServerTrusted(
                X509Certificate[] x509Certificates, String s)
                throws java.security.cert.CertificateException {

        }

        public boolean isClientTrusted(X509Certificate[] chain) {
            return true;
        }

        public boolean isServerTrusted(X509Certificate[] chain) {
            return true;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return _AcceptedIssuers;
        }

        public static void allowAllSSL() {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

            });

            SSLContext context = null;
            if (trustManagers == null) {
                trustManagers = new TrustManager[]{new HttpsTrustManager()};
            }

            try {
                context = SSLContext.getInstance("TLS");
                context.init(null, trustManagers, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }

            HttpsURLConnection.setDefaultSSLSocketFactory(context != null ? context.getSocketFactory() : null);
        }
    }
    //endregion
}
