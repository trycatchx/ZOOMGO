package com.dmsys.vlcplayer.db;

/**
 * Created by Administrator on 2017/8/29.
 */

public class PlayRecordBean {
    public boolean subtitle_onoff;// 电视剧/BT
    public String uri;// 视屏URI
    public String subtitle_path;//字幕Uri

    public PlayRecordBean( boolean subtitle_onoff, String uri,String subtitle_path) {

        this.subtitle_onoff = subtitle_onoff;
        this.uri = uri;
        this.subtitle_path = subtitle_path;
    }
}
