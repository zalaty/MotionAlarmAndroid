package com.zalaty.motionalarm;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.widget.RelativeLayout;

public class AlarmThread extends Thread{

    RelativeLayout mmCurrentLayout;
    ToneGenerator toneG;

    public AlarmThread(RelativeLayout currentLayout){
        mmCurrentLayout = currentLayout;
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    }

    @Override
    public void run() {
        mmCurrentLayout.setBackgroundColor(Color.red(R.color.colorRed));
/*        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);*/
        try {
            for (int i = 0; i <= 10; i++) {
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
                //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
                //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
                sleep(1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
        //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
        //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
    }

    public void setWhite(){
        //mmCurrentLayout.setBackgroundColor(Color.red(R.color.colorWhite));
    }
}
