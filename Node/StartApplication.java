/*
 * RSUS Project: Pression Farming
 *
 * Node: StartApplication.java
 *
 * Created on Nov 16, 2016 3:51:05 PM;
 */

package org.sunspotworld;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.io.IScalarInput;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.ITemperatureInput;
import com.sun.spot.util.Utils;

import java.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;



/**
 * @author Balat, Contreras, Gonzalez.
 */

public class StartApplication extends MIDlet {

    private EDemoBoard demo = EDemoBoard.getInstance();
    private ITemperatureInput temp = demo.getADCTemperature();
    private IScalarInput scalars[] = demo.getScalarInputs();
    private ISwitch sw = EDemoBoard.getInstance().getSwitches()[EDemoBoard.SW1];        //Por ahora no tiene sentido

    //private String ID="";

    int time_press_buttom=0;

    RadioTools node = new RadioTools();
   
    protected void startApp() throws MIDletStateChangeException {

        int i = 0;                              //counter
        int temp_value = 0;                     //temp
        int ec_value = 0;                       //EC
        float ph_value = 0;                     //Ph

        boolean ALARM = false;
        boolean TimeToSend = false;

        String AggregatorAddress = null;
        
        int last_temp_value=init_node();         // A temp measurement and load last_temp_value variable

        //IEEEAddress ID = new IEEEAddress("7f00.0101.0000.1001");                //  PENDIENTE ID -  SABER QUE MAC TIENE EL NODO

        String ID = node.getAdress();

        System.out.println("NODE: " + ID);



        while (true)  {

            Utils.sleep(10);                    //Step 0,1s (100)
            i++;

            /*
             *  TEMPERETURA MEASURMENT
             */
            if((i%60)==0){                      //Temp measurment/ 1 min (600)

                try {
                    temp_value = get_temp(last_temp_value);
                    if (temp_value <= 0xF9){
                        last_temp_value=temp_value;
                        ALARM=false;
                    }else{
                        ALARM=true;
                    }
                } catch (IOException ex) {
                }
            }
            /*
             * EC & PH
             */
            if(i==300){             //EC & PH measurment/ 15 min (9000)
                i=0;
                try {
                    ec_value = get_ec();
                    ph_value = get_ph(temp_value);
                    TimeToSend=true;
                    System.out.println("Temp:" + temp_value + "\nEC: " + ec_value + "\nPh:" + ph_value);
                } catch (IOException ex) {
                }
            }
            try {
                switchPressed();
            } catch (IOException ex) {
            }

            /*
             *  Subscription
             */
            if (AggregatorAddress == null){
                AggregatorAddress = node.SubscriptionProcessNode();
                System.out.println("Sending measurements to:"+AggregatorAddress);
            } else {
                if (ALARM){
                    node.doSendAlarm(temp_value, AggregatorAddress);
                    System.out.println("ALARM");
                }
                if (TimeToSend){
                    TimeToSend=false;

                    node.doSetMeasures(temp_value, ec_value, ph_value, AggregatorAddress);

                    
                }
            }
        }

    }
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }

    private int init_node() {
        int temp_value = 0;
        try {
            temp_value = (int) temp.getCelsius();
        } catch (IOException ex) {
        }
        return temp_value;
    }

    private int get_temp(int last_temp_value) throws IOException {
        int temp_value=(int) temp.getCelsius();   
        if(((temp_value-last_temp_value)>=7)||temp_value>=47){
            return PDUs.ALARMTYPE_TEMP_MAX;                                       //FIRE
        }else if(((last_temp_value-temp_value)>=7)&&temp_value<= 0){
            return PDUs.ALARMTYPE_TEMP_MIN;                                      //HELADA
        }
        return temp_value;
    }

    private int get_ec() throws IOException {
        int ec_value = (int) scalars[0].getValue() / 64;
        if(ec_value >= 16){ec_value=16;}
        if(ec_value <= 0){ec_value=0;}
        return ec_value;
    }

    private int get_ph (int temp_value) throws IOException {
        float ph_value = ((int) scalars[1].getValue());   // Measurment= digital((signal - 600mv)*(-2.5))
        ph_value = (float) (ph_value * 3 / (-2557.5) + 0.6);   //ph_value from -600 to 600 mv
        ph_value = (float) ((ph_value*1000)/(0.198*(temp_value+273.15))+7);
        if(ph_value >= 14){ph_value=14;}
        if(ph_value <= 0){ph_value=0;}
        return (int) ph_value;
    }


    private void switchPressed() throws IOException {
        //Utils.sleep(1);        //Step 0,1s (In Main Loop)
        if (sw.isClosed()){
            time_press_buttom++;
        }else {time_press_buttom=0;}
        //System.out.println(time_press_buttom);
        if (time_press_buttom>=500){
            //node.SubscriptionProcessNode();
            
        }
    }


}
