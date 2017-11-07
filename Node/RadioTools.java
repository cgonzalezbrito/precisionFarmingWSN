/*
 * RSUS Project: Pression Farming
 * RadioTools (Node)
 *
 */

package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.NoAckException;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import com.sun.spot.util.IEEEAddress;
import java.io.IOException;
//import java.util.Date;                                                        //Data Time in PDU (Review)
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

/**
 *
 * @author Balat, Contreras, Gonzalez.
 */

public class RadioTools extends Thread {

    private boolean baseStationPresent = false;
    private RadiogramConnection conn = null;
    private Radiogram xdg = null;
    private boolean running = true;
    private boolean connected = false;
    private long spotAddress = 0;
    private long aggAddress = 0;
    private String ID = null;
    private boolean openBroadcastPort = false;
    private boolean newDataFlag = false;

    //private HostGUI guiFrame = null;

    float tempReception;
    float ecReception;
    float phReception;
    String batteryReception;

    private EDemoBoard demo = EDemoBoard.getInstance();
    private ITriColorLED leds[] = demo.getLEDs();

    public RadioTools(){
        System.out.println("Starting...");
        init();
    }

    private void init() {
        RadiogramConnection rcvConn = null;
        try {
            rcvConn = (RadiogramConnection)Connector.open("radiogram://:" + PDUs.BROADCAST_PORT);
            baseStationPresent=true;
            openBroadcastPort = true;
        } catch (Exception ex){
            baseStationPresent=false;
            openBroadcastPort = false;
            System.out.println("Problem connecting to base station" + ex);
        } finally {
            try {
                if (rcvConn != null){
                    rcvConn.close();
                    openBroadcastPort = false;
                }
            } catch (IOException ex) { /*Ignore*/   }
        }
    }

    /*public void setGUI (HostGUI fr){
        guiFrame = fr;
        updateConnectionStatus(connected);
    }*/

    private void updateConnectionStatus (boolean isConnected) {
        String status;
        if (isConnected) {
            status = "Connected OK";
        } else {
            status = "Not Connected";
        }
    }

    public boolean isOpenBroadcastPort () {
        return openBroadcastPort;
    }

    public String SubscriptionProcessNode() {
        leds[0].setColor(LEDColor.RED); leds[0].setOn();
        DatagramConnection subsConn = null;
        RadiogramConnection subsConnRx = null;
        String AggregatorAddress = null;
        
        try {

            subsConn = (DatagramConnection) Connector.open("radiogram://broadcast:" + PDUs.BROADCAST_PORT);
            subsConnRx = (RadiogramConnection)Connector.open("radiogram://:"+PDUs.CONNECTED_PORT);
            Datagram subsdg = subsConn.newDatagram(subsConn.getMaximumLength());
            subsdg.writeByte(PDUs.HELLO); // packet type
            subsConn.send(subsdg); // broadcast it
            subsConnRx.setTimeout(8000);
            Datagram subsdgrx = subsConnRx.newDatagram(subsConnRx.getMaximumLength());

            while (true) {
                subsdgrx.reset();
                subsConnRx.receive(subsdgrx);
                if (subsdgrx.readByte() == PDUs.HELLO_RESPONSE){
                    String addr = subsdgrx.getAddress();
                    IEEEAddress ieeeAddr = new IEEEAddress(addr);
                    long macAddress = ieeeAddr.asLong();
                    System.out.println("Received response from: " + ieeeAddr.asDottedHex());
                    aggAddress = macAddress;
                    AggregatorAddress = addr;
                    break;
                }
            }

        } catch (IOException ex) {
            System.out.println("Error sending HELLO message" + ex.toString());
            AggregatorAddress=null;
        } finally {
            try {
                if (subsConn != null) {
                    subsConn.close();
                }
                if (subsConnRx != null) {
                    subsConnRx.close();
                }
            } catch (IOException ex) {/* ignore */}

        }
       
        leds[0].setColor(LEDColor.RED); leds[0].setOff();

        return AggregatorAddress;
    }

    public void doGetMeasures (int idNode){
        if (conn != null){
                try {
                    xdg.reset();
                    xdg.writeByte(PDUs.GET_MEASURES);
                    xdg.writeByte(idNode);
                    conn.send(xdg);
                } catch (NoAckException nex){
                    connected = false;
                    updateConnectionStatus(connected);
                } catch (IOException ex) {
                   //Other problems
                }
        }
    }

    public void doSetMeasures (float tempValue, float ecValue, float phValue, String AggregatorAddress){
        
        short ACK=0;

        DatagramConnection conntx = null;
        try {
            conntx = (DatagramConnection) Connector.open("radiogram://" + AggregatorAddress + ":" + PDUs.CONNECTED_PORT); //
            Datagram dg = conntx.newDatagram(conntx.getMaximumLength());
            if (conntx != null) {
                try {

                    dg.reset();
                    dg.writeUTF(ID);
                    dg.writeUTF("Time");                                        //Esto tambien falta
                    dg.writeByte(PDUs.GET_MEASURES_RESPONSE);
                    dg.writeFloat(tempValue);
                    dg.writeFloat(ecValue);
                    dg.writeFloat(phValue);
                    dg.writeUTF("Battery");                                     //Esto tambien falta
                    conntx.send(dg);                                            //Si el agregator cae, el node se queda aqui, no muestra error solo se queda
                } catch (NoAckException nex) {
                    
                    connected = false;
                    updateConnectionStatus(connected);
                } catch (IOException ex) {
                    //Other problems
                }
            }
        } catch (IOException ex) {
            System.out.println("Searching another route...");
        
        } finally{
            if (conntx != null){
                try {
                    conntx.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }      
    }

    public void doSendMeasures (float tempValue, float ecValue, float phValue){
        DatagramConnection conntx = null;
        try {
            conntx = (DatagramConnection) Connector.open("radiogram://broadcast:" + PDUs.BROADCAST_PORT); //
            Datagram dg = conntx.newDatagram(conntx.getMaximumLength());
            if (conntx != null) {
                try {
                    dg.reset();
                    dg.writeByte(PDUs.GET_MEASURES_RESPONSE);
                    dg.writeFloat(tempValue);
                    dg.writeFloat(ecValue);
                    dg.writeFloat(phValue);
                    conntx.send(dg);
                } catch (NoAckException nex) {
                    connected = false;
                    updateConnectionStatus(connected);
                } catch (IOException ex) {
                    //Other problems
                }
            }
        } catch (IOException ex) {
            System.out.println("Searching another route...");
        } finally{
            if (conntx != null){
                try {
                    conntx.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void doSetAlarm (byte typeAlarm){
        if (conn != null){
            try {
                xdg.reset();
                xdg.writeByte(PDUs.ALARM);
                xdg.writeFloat(typeAlarm);
                conn.send(xdg);
            } catch (NoAckException nex){
                connected = false;
                updateConnectionStatus(connected);
            } catch (IOException ex) {
                //Other problems
            }
        }

    }

    public void doSendAlarm (int typeAlarm, String AggregatorAddress){
        DatagramConnection conntx = null;
        try {
            conntx = (DatagramConnection) Connector.open("radiogram://" + AggregatorAddress + ":" + PDUs.CONNECTED_PORT); //
            Datagram dg = conntx.newDatagram(conntx.getMaximumLength());
            if (conntx != null) {
                try {
                    dg.reset();
                    dg.writeUTF(ID);
                    dg.writeUTF("Time");                                        //Esto tambien falta
                    dg.writeByte(PDUs.ALARM);
                    dg.writeShort(typeAlarm);
                    conntx.send(dg);
                } catch (NoAckException nex) {
                    connected = false;
                    updateConnectionStatus(connected);
                } catch (IOException ex) {
                    //Other problems
                }
            }
        } catch (IOException ex) {
            System.out.println("Searching another route...");
        } finally{
            if (conntx != null){
                try {
                    conntx.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void doSetLimits (float tempMin, float tempMax,float ecMin,float ecMax, float phMin, float phMax){
        if (conn != null){
            try {
                xdg.reset();
                xdg.writeByte(PDUs.SET_CHANGE_LIMITS);
                xdg.writeFloat(tempMin);
                xdg.writeFloat(tempMax);
                xdg.writeFloat(ecMin);
                xdg.writeFloat(ecMax);
                xdg.writeFloat(phMin);
                xdg.writeFloat(phMax);
                conn.send(xdg);
            } catch (NoAckException nex){
                connected = false;
                updateConnectionStatus(connected);
            } catch (IOException ex) {
                //Other problems
            }
        }
    }

    public void doQuit () {
        sendCmd (PDUs.DISPLAY_SERVER_QUITTING);
        running = false;
    }

    public float get_tempReception() {
        newDataFlag = false;
        return tempReception;
    }

    public float get_ecReception() {
        newDataFlag = false;
        return ecReception;
    }

    public float get_phReception() {
        newDataFlag = false;
        return phReception;
    }

    public boolean get_newDataFlag() {
        return newDataFlag;
    }

    public String getAdress() {
        ID = "7f00.0101.0000.1001";                                      //ESTO AUN NO ESTA SOLUCIONADO
        return(ID);
    }

    public void reconnect () {
        connected = false;
        updateConnectionStatus(connected);
        announceStarting();
    }

    private void sendCmd (byte cmd) {
        if (conn != null){
            try {
                xdg.reset();
                xdg.writeByte(cmd);
                conn.send(xdg);
            } catch (NoAckException nex){
                connected = false;
                updateConnectionStatus(connected);
            } catch (IOException ex){
                //
            }
        }
    }

    public void run () {
        if (baseStationPresent){
            System.out.println("Thread Started ... ");
            hostLoop();
        }
    }

    private void announceStarting () {
        leds[0].setColor(LEDColor.RED); leds[0].setOn();
        DatagramConnection txConn = null;
        try{
            txConn = (DatagramConnection)Connector.open("radiogram://broadcast:" + PDUs.CONNECTED_PORT);
            Datagram txdg = txConn.newDatagram(txConn.getMaximumLength());
            txdg.writeByte(PDUs.HELLO);        // packet type
            txConn.send(txdg);                             // broadcast it
        }catch(Exception ex){
            System.out.println("Error sending message" + ex.toString());
        }finally{
            try {
                if (txConn != null) {
                    txConn.close();
                }
            } catch (IOException ex) { /* ignore */ }
        }
        leds[0].setColor(LEDColor.RED); leds[0].setOff();
    }

    private void waitForSpot () {
        RadiogramConnection rcvConn = null;
        spotAddress = 0;
        System.out.println("Waiting for Spot");
        try {
            rcvConn = (RadiogramConnection)Connector.open("radiogram://:" + PDUs.BROADCAST_PORT);
            openBroadcastPort = true;
            rcvConn.setTimeout(10000);             // timeout in 10 seconds
            Datagram dg = rcvConn.newDatagram(rcvConn.getMaximumLength());
            while (true) {
                try {
                    dg.reset();
                    rcvConn.receive(dg);            // wait until we receive a request
                    if (dg.readByte() == PDUs.HELLO) {       // type of packet
                        String addr = dg.getAddress();
                        IEEEAddress ieeeAddr = new IEEEAddress(addr);
                        long macAddress = ieeeAddr.asLong();
                        System.out.println("Received request from: " + ieeeAddr.asDottedHex());
                        spotAddress = macAddress;
                        send_HELLO_RESPONSE(spotAddress);
                        break;
                    }
                } catch (TimeoutException ex) {
                    announceStarting();
                    System.out.println("No SPOT");
                }
            }
        } catch (Exception ex) {
            System.out.println("Error waiting for remote Spot: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                if (rcvConn != null) {
                    rcvConn.close();
                    openBroadcastPort = false;
                }
            } catch (IOException ex) { /* ignore */ }
        }
    }

    private void send_HELLO_RESPONSE(long spotAdd){
        DatagramConnection txConn = null;
        try{
            txConn = (DatagramConnection) Connector.open("radiogram://" + spotAdd + ":" + PDUs.CONNECTED_PORT); //
            Datagram rdg = txConn.newDatagram(10);
            rdg.reset();
            //rdg.setAddress(dg);
            rdg.writeByte(PDUs.HELLO_RESPONSE);        // packet type
            rdg.writeLong(spotAdd);                   // requestor's ID
            txConn.send(rdg);                        // broadcast it
        } catch (Exception ex) {
            System.out.println("Error sending Hello Response to" + ex.toString());
        } finally {
            try{
                if (txConn != null){
                    txConn.close();
                }
            } catch (IOException ex) { }
        }
    }

    public long get_spotAddress() {
        return spotAddress;
    }

    private void hostLoop () {
        running = true;
        announceStarting();
        while (running) {
            waitForSpot();
            if (spotAddress != 0){
                try {
                    conn = (RadiogramConnection) Connector.open("radiogram://" + spotAddress + ":" + PDUs.CONNECTED_PORT);
                    conn.setTimeout(1000);
                    Radiogram rdg = (Radiogram) conn.newDatagram(conn.getMaximumLength());
                    xdg = (Radiogram)conn.newDatagram(10);
                    connected = true;
                    updateConnectionStatus(connected);

                    while (connected){
                        try{
                            conn.receive(rdg);
                        } catch (TimeoutException ex){
                            continue;
                        }
                        String IDrcv = rdg.readUTF();
                        System.out.println("[MEASURES NODE " + IDrcv +" ]");
                        String Timercv = rdg.readUTF();
                        System.out.println(Timercv);
                        byte packetType = rdg.readByte();
                        switch (packetType) {
                            case PDUs.HELLO:
                                System.out.println("[HELLO] Node added sucessfully");
                                break;
                            case PDUs.HELLO_RESPONSE:
                                System.out.println("[HELLO_RESPONSE] Node added sucessfully");
                                break;
                            case PDUs.GET_MEASURES:
                                System.out.println("[MEASURES] Recived measures");
                                break;
                            case PDUs.GET_MEASURES_RESPONSE:
                                newDataFlag = true;
                                tempReception = rdg.readFloat();
                                ecReception = rdg.readFloat();
                                phReception = rdg.readFloat();
                                batteryReception = rdg.readUTF();
                                System.out.println("Temp: " + tempReception + "\n EC: " + ecReception + "\n pH: " + phReception + "\nBattery Level: " +batteryReception);

                                xdg.reset();
                                xdg.write(PDUs.ACK);
                                conn.send(xdg);

                                break;
                            case PDUs.ALARM:
                                short alarmType = rdg.readShort();
                                System.out.println("ALARM");
                                switch (alarmType){
                                    case PDUs.ALARMTYPE_TEMP_MIN:
                                        System.out.println("FROST");
                                        break;
                                    case PDUs.ALARMTYPE_TEMP_MAX:
                                        System.out.println("FIRE");
                                        break;
                                    case PDUs.ALARMTYPE_EC_MIN:
                                        break;
                                    case PDUs.ALARMTYPE_EC_MAX:
                                        break;
                                    case PDUs.ALARMTYPE_PH_MIN:
                                        break;
                                    case PDUs.ALARMTYPE_PH_MAX:
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case PDUs.SET_CHANGE_LIMITS:
                                System.out.println("[CHANGE_LIMITS] Limits changed succesfully");
                                break;
                            case PDUs.SET_CHANGE_LIMITS_RESPONSE:
                                System.out.println("[CHANGE_LIMITS] Limits changed succesfully");
                                break;
                            default:
                                System.out.println(""+rdg);
                                break;
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("Error comunication with remote Spot: " + ex.toString());
                } finally {
                    try {
                        connected =false;
                        updateConnectionStatus(connected);
                        if (conn != null) {
                            xdg.reset();
                            xdg.writeByte(PDUs.DISPLAY_SERVER_QUITTING);
                            conn.send(xdg);
                            conn.close();
                            conn=null;
                        }
                    } catch (IOException ex){/*Ignore*/}
                }
            }
        }
    }

    void close() {
        try {
            conn.close();
        } catch (IOException ex) {
        }
    }

}
