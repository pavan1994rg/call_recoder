package com.example.call_recoder;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Job_Sheduler_Service extends JobService {
    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;
    MediaRecorder recorder ;
    private static String fileName = null;


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        doBackgroundWork(params);

        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        Log.d(TAG, "run: " + "p");
        new Thread(new Runnable() {
            @Override
            public void run() {

                    Log.d(TAG, "run: " );
                    if (jobCancelled) {
                        return;
                    }

                Looper.prepare();
                    TelephonyManager manager =  (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);

                    manager.listen( new PhoneStateListener(){
                        @Override
                        public void onCallStateChanged(int state, String phoneNumber) {
//                super.onCallStateChanged(state, phoneNumber);

                            if(TelephonyManager.CALL_STATE_IDLE == state){
                                Log.d(TAG, " call stopped ");
//                                rec.stop();
//                                rec.reset();
//                                rec.release();
//                                RecorderStarted = false;
                                if(recorder != null){
                                    recorder.stop();

                                    recorder.release();
                                    recorder = null;
                                }

                                stopSelf();

                                ;
                            }
                            else if ( TelephonyManager.CALL_STATE_OFFHOOK ==  state){
                                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

// Get the current ringer volume as a percentage of the max ringer volume.
                                int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
                                int maxRingerVolume = audio.getStreamMaxVolume(AudioManager.STREAM_RING);
                                double proportion = currentVolume/(double)maxRingerVolume;

// Calculate a desired music volume as that same percentage of the max music volume.
                                int maxMusicVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                int desiredMusicVolume = (int)(proportion * maxMusicVolume);

// Set the music stream volume.
                                audio.setStreamVolume(AudioManager.STREAM_MUSIC, desiredMusicVolume, 0 /*flags*/);
                                fileName = getExternalCacheDir().getAbsolutePath();
                                fileName += "/audiorecordtest.mp3";
                                recorder = new MediaRecorder();
                                Log.d("1", recorder.toString());
                                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
//                                 recorder.setAudioSamplingRate(16000);
                                recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                                recorder.setOutputFile(fileName);
                                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);


                    try {
                        recorder.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    recorder.start();
//                                Toast.makeText(getApplicationContext(),"Call recieved",Toast.LENGTH_SHORT).show()
                                Log.d(TAG, " call recording ");



                            }
                            //Do something after 100ms



                }
            },PhoneStateListener.LISTEN_CALL_STATE);
                Looper.loop();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                Log.d(TAG, "Job finished");
                jobFinished(params, true);
            }

        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }
}
