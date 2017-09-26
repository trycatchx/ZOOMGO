package com.dmsys.vlcplayer.util;

import java.net.URI;
import java.net.URL;

public class UrilTools {
    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
     * instead of '+'.
     */
    public static String encodeUri(String uri) {
        URI mURL = null;
        String ret = null;
        try {
            URL url = new URL(uri);
            mURL = new URI(url.getProtocol(), url.getUserInfo(),
                    url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef());
        } catch (Exception e) {

        }

        if (mURL != null) {
            ret = mURL.toASCIIString();
        }
        return ret;
    }

    public static String encodeUri1(String urlStr) {
        URI mURL = null;
        String ret = null;
        try {
            URL url = new URL(urlStr);
            mURL = new URI(url.getProtocol(), url.getUserInfo(),
                    url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef());
        } catch (Exception e) {

        }

        if (mURL != null) {
            ret = mURL.toASCIIString();
        }
        return ret;


    }
}
