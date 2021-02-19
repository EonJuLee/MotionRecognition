package com.example.sensortest0219;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor gyroSensor;
    private Sensor accelSensor;
    private SensorManager sensorManager;

    private double timestamp;
    private double dt;
    private double RAD2DGR=180/Math.PI;
    private static final double NS2S = 1.0f/1000000000.0f;
    private float[] gyros={0,0,0}, accels={0,0,0};

    private System1 system1;
    private System2 system2;
    private RuleSystem system;
    private int detSystem=1;

    private Button btnStart,btnEnd,btnReset;
    private TextView textMessage,textI,textS,textZ,textNone,textTotal,textTitle;

    private static String totalMessage = "You have tried ";
    private static String[] motions = {"I","S","Z","Nothing"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this,gyroSensor,sensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,accelSensor,sensorManager.SENSOR_DELAY_GAME);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        btnReset= (Button) findViewById(R.id.btnReset);
        textI=findViewById(R.id.textCountI);
        textS=findViewById(R.id.textCountS);
        textZ=findViewById(R.id.textCountZ);
        textNone=findViewById(R.id.textCountUnknown);
        textTotal=findViewById(R.id.textCountTotal);
        textMessage = (TextView) findViewById(R.id.textMessage);
        textTitle = findViewById(R.id.textTitle);

        system1 = new System1();
        system2 = new System2();
        initSystem();
        setButtonEvent();
    }

    public void setButtonEvent(){
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setVisibility(View.GONE);
                btnEnd.setVisibility(View.VISIBLE);
                startMotion();
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setVisibility(View.VISIBLE);
                btnEnd.setVisibility(View.GONE);
                findGesture();
                updateCount(system.getTotalTried(),system.getDetected());
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initCntArray();
                textMessage.setText("");
                updateCount(system.getTotalTried(),system.getDetected());
            }
        });
    }

    public void findGesture(){
        switch(detSystem){
            case 1:
                showToast("Found "+motions[system1.findGesture()]);
                showMessage("diff : "+system1.getDiff()+"\nxDet : "+system1.getxMoveDir());
                break;
            case 2:
                showToast("Found "+motions[system2.findGesture()]);
                showMessage("xDet : "+system2.getxMoveDir());
                break;
        }
    }

    public void initCntArray(){
        switch(detSystem){
            case 1:
                system1.initCntArray();
                break;
            case 2:
                system2.initCntArray();
                break;
        }
        updateCount(system.getTotalTried(),system.getDetected());
    }

    public void initSystem(){
        switch(detSystem){
            case 1:
                system1.initSystem();
                system=system1;
                break;
            case 2:
                system2.initSystem();
                system=system2;
                break;
        }
        cleanScreen();
    }

    public void startMotion(){
        switch(detSystem){
            case 1:
                system1.startMotion();
                break;
            case 2:
                system2.startMotion();
                break;
        }
    }

    public void applyRule(float[] gyros, float[] accels, double dt){
        switch(detSystem){
            case 1:
                system1.applyRule(gyros,accels,dt);
                break;
            case 2:
                system2.applyRule(gyros,accels,dt);
                break;
        }
    }

    public void cleanScreen(){
        textTitle.setText("System "+detSystem);
        updateCount(system.getTotalTried(),system.getDetected());
        showMessage("");
    }

    public void showMessage(String message){
        textMessage.setText(message);
    }

    public void updateCount(int totalTried, int[] detected){
        textTotal.setText(totalMessage+totalTried+" times! ");
        textI.setText(""+detected[0]);
        textS.setText(""+detected[1]);
        textZ.setText(""+detected[2]);
        textNone.setText(""+detected[3]);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dt=(event.timestamp-timestamp)*NS2S;
        timestamp=event.timestamp;

        if(event.sensor==gyroSensor){
            gyros=event.values;
        }

        if(event.sensor==accelSensor){
            accels=event.values;
        }

        if (dt - timestamp*NS2S != 0) {
            applyRule(gyros,accels,dt);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,gyroSensor,sensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,accelSensor,sensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_rulebase1:
                detSystem=1;
                break;
            case R.id.menu_rulebase2:
                detSystem=2;
                break;
        }
        showToast("Move to System"+detSystem);
        initSystem();
        return super.onOptionsItemSelected(item);
    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}