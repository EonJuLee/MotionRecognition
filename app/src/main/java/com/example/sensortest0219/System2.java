package com.example.sensortest0219;

import android.util.Log;

public class System2 extends RuleSystem{

    public double []squareGyro = {0,0,0};
    public double []squareAccel = {0,0,0};

    public static int ARRAY_LENGTH = 3;
    public static int border = 3;

    public System2(){
        super();
        initSystem();
    }

    @Override
    public void initSystem() {
        super.initSystem();
        initSquareArray();
    }

    @Override
    public void startMotion() {
        super.startMotion();
        initSquareArray();
    }

    public void initSquareArray(){
        for(int i=0;i<ARRAY_LENGTH;i++){
            squareGyro[i]=squareAccel[i]=0.0;
        }
    }

    public void applyRule(float []gyros, float []accels, double dt){
        for(int i=0;i<ARRAY_LENGTH;i++){
            filterNoise(gyros[i]);
            filterNoise(accels[i]);
        }
        sumSquare(gyros,accels);
        checkDirX(accels[AXIS_X],dt);
    }

    public double filterNoise(double value){
        return (value <=border && value >=-border)? 0:value;
    }

    public void sumSquare(float [] gyro, float [] accel){
        for(int i=0;i<ARRAY_LENGTH;i++){
            squareAccel[i]+=accel[i]*accel[i];
            squareGyro[i]+=gyro[i]*gyro[i];
        }
    }

    public int findMaxSquare(double[] array){
        int maxIndex = 0;
        double maxValue = array[0];
        for(int i=0;i<ARRAY_LENGTH;i++){
            if(maxValue<array[i]){
                maxValue=array[i];
                maxIndex=i;
            }
        }
        return maxIndex;
    }

    public int findGesture(){
        int gesture = UNDEFINED;
        int maxIndexOfGyro = findMaxSquare(squareGyro);
        int maxIndexOfAccel = findMaxSquare(squareAccel);
        xMoveDir=xMoveDir&7;
        if(maxIndexOfAccel==AXIS_Z && maxIndexOfGyro==AXIS_X){
            gesture = GESTURE_I;
        }
        else if(xMoveDir==2){
            gesture = GESTURE_S;
        }
        else if(xMoveDir==5){
            gesture = GESTURE_Z;
        }
        else{
            gesture = ERROR_FOUND;
        }
        totalTried++;
        detected[gesture]++;
        return gesture;
    }
}
