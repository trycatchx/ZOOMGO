
package com.dmsys.vlcplayer.interfaces;

public interface IPlaybackSettingsController {
    enum DelayState {OFF, AUDIO, SUBS, SPEED};

    void showAudioDelaySetting();
    void showSubsDelaySetting();
    void showPlaybackSpeedSetting();
    void endPlaybackSetting();
}
