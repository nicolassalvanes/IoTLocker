package ar.edu.unlam.soa.iotlocker;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AdminActivity extends AppCompatActivity {
    private SensorManager mSensorManager;
    static final int MUESTRAS_POR_SEGUNDO = 10;
    int i = 0;
    float[][] valores = new float[MUESTRAS_POR_SEGUNDO][3];
    TextView textValores;
    TextView textPatron;
    boolean flag_patron;
    String patron = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        textValores = (TextView) findViewById(R.id.textView2);
        textPatron = (TextView) findViewById(R.id.textView3);
        textValores.setText("Empieza");
        flag_patron = false;

        SensorEventListener _SensorEventListener=   new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {


                Sensor mySensor = sensorEvent.sensor;

                if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    int j;
                    int inicial;
                    int medio;
                    int fin;
                    char dir = Character.MIN_VALUE;
                    boolean flag_medio_x = false;
                    boolean flag_medio_y = false;
                    boolean flag_fin = false;
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];
                    valores[i][0] = x;
                    valores[i][1] = y;
                    valores[i][2] = z;

                    i++;
                    if(i == MUESTRAS_POR_SEGUNDO) {
                        i = 0;
                        flag_patron = true;
                    }
                    if(flag_patron && patron.length() <= 10){
                        j = i;
                        fin = j;
                        medio = j;
                        j++;
                        if(j == MUESTRAS_POR_SEGUNDO)
                            j=0;
                        inicial = j;
                        j++;
                        if(j == MUESTRAS_POR_SEGUNDO)
                            j=0;
                        while(flag_medio_x == false && flag_medio_y == false && j != fin){
                            if(valores[j][0] - valores[inicial][0] > 10){
                                flag_medio_x = true;
                                dir = 'I';
                                medio = j;
                            }else if(valores[j][0] - valores[inicial][0] < -10){
                                flag_medio_x = true;
                                dir = 'D';
                                medio = j;
                            }else if(valores[j][2] - valores[inicial][2] < -10){
                                flag_medio_y = true;
                                dir = 'A';
                                medio = j;
                            }else if(valores[j][2] - valores[inicial][2] > 10){
                                flag_medio_y = true;
                                dir = 'F';
                                medio = j;
                            }else{
                                j++;
                                if(j == MUESTRAS_POR_SEGUNDO)
                                    j=0;
                            }
                        }
                        if(flag_medio_x == true){

                            while(flag_fin == false && j != fin){
                                j++;
                                if(j == MUESTRAS_POR_SEGUNDO)
                                    j=0;
                                if((valores[j][0] - valores[medio][0] < -10 && dir == 'I') || (valores[j][0] - valores[medio][0] > 10 && dir == 'D')){
                                    flag_fin = true;
                                }
                            }
                        } else if(flag_medio_y == true){

                            while(flag_fin == false && j != fin){
                                j++;
                                if(j == MUESTRAS_POR_SEGUNDO)
                                    j=0;
                                if((valores[j][2] - valores[medio][2] < -10 && dir == 'F') || (valores[j][2] - valores[medio][2] > 10 && dir == 'A')){
                                    flag_fin = true;
                                }
                            }
                        }
                        if(flag_fin == true){
                            flag_patron = false;
                            i=0;
                            valores = new float[MUESTRAS_POR_SEGUNDO][3];
                            patron += dir;
                            textPatron.setText(patron.replace("I", "<").replace("D", ">").replace("A", "˄").replace("F", "˅"));
                        }

                    }
                    textValores.setText(String.valueOf(i) + " " + String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(z));

                }

            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorManager.registerListener(_SensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000000);
     }
    public void vaciarPatron(View view){
        patron = "";
        textPatron.setText(patron);
    }
    public void aceptarPatron(View view){
        Intent intent = new Intent();
        intent.setAction("com.SEND_PATRON");
        Log.d("DEBUG", "Cerrando...");
        intent.putExtra("patron",patron);
        sendBroadcast(intent);
        finish();
    }
}


