package com.daniel.shaker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /**
     * Constants for sensors
     */
    private boolean SHAKED=FALSE;
    //private float shake_treshold = 2.9f;
    private float thd_recover_g=1.0f;
    private float min_shake_g = 2.2f;
    private float max_shake_g = 6f;
    private float min_volume=0.5f;
    private float max_volume=1f;
    private float m,b;


    /**
     * Sensors
     */
    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private ImageView ImageView_shaker;
    private SoundPool soundPool;
    private int[] audio_list=new int[2];
    private int []soundId=new int[2];

    private Animation rotate;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /* ADVERTISEMENT */
        /******************************************************************************************/
        MobileAds.initialize(this, "ca-app-pub-3498967338559704~2439966590");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        /*ACCELEROMETER*/
        /******************************************************************************************/
        /* Calibration for the curve g versus volume */
        m=(max_volume-min_volume)/(max_shake_g-min_shake_g);
        b=min_volume - m*min_shake_g;


        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_FASTEST );

        /* SOUND ENGINE */
        /******************************************************************************************/
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        audio_list[0]=R.raw.sha2;
        soundId[0]= soundPool.load(this,audio_list[0], 1);


        /* BUTTONS */
        /******************************************************************************************/
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_image);
        ImageView_shaker = (ImageView)findViewById(R.id.imageView_shaker);

        /*  */
        ImageView_shaker.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                soundPool.autoPause();
                soundPool.play(soundId[0], min_volume, min_volume, 0, 0, 1);
                ImageView_shaker.startAnimation(rotate);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }


    /* onAccuracyChanged */
    /******************************************************************************************/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /* onSensorChanged */
    /******************************************************************************************/
    @Override
    public void onSensorChanged(SensorEvent event)
    {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        }
    }





    /* detectShake */
    /******************************************************************************************/
    private void detectShake(SensorEvent event) {
        //long now = System.currentTimeMillis();

        float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        Log.d("ACCELEROMETER:", Float.toString(gX) + ";" + Float.toString(gY) + ";" + Float.toString(gZ) + ";" + Double.toString(gForce) + ";");
        if ((gForce > min_shake_g) && (SHAKED == FALSE)) {

            float volume = (float) (m * gForce + b);
            soundPool.autoPause();
            soundPool.play(soundId[0], volume, volume, 0, 0, 1);
            ImageView_shaker.startAnimation(rotate);
            SHAKED = TRUE;
        } else if (gForce < thd_recover_g) {
            SHAKED = FALSE;
        }

    }




}
