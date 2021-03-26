package ru.progmatik.meterssender.utils;

import org.xmlpull.v1.XmlPullParserException;

public interface RequestFinishListeners {
    void requestStarted(HttpsRequest httpsRequest) throws XmlPullParserException;
    void requestFinished(HttpsRequest httpsRequest) throws XmlPullParserException;
}
