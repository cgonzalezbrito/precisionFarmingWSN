
package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.radio.*;
import com.sun.spot.peripheral.NoAckException;
import com.sun.spot.peripheral.NoRouteException;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.util.IEEEAddress;

import java.io.*;
import java.util.Arrays;
import javax.microedition.io.*;

import javax.swing.*;

/**
 * Simple example class to locate a remote service (on a SPOT), to connect to it
 * and send it a variety of commands. In this case to set or calibrate the SPOT's
 * accelerometer and to return a stream of accelerometer telemetry information.
 *
 * @author Ron Goldman<br>
 * Date: May 2, 2006
 */
public class RadioTools extends Thread{

    private boolean baseStationPresent = false;
    private RadiogramConnection conn = null;
    private Radiogram xdg = null;
    private boolean running = true;
    private boolean connected = false;
    private long aggAddress = 0;

    private long timeStampOffset = -1;

    //private GraphView graphView = null;
    private HostGUI guiFrame = null;
    //Almacena las MAC de los aggregators
    String[] arrayMAC = {};

    /**
     * Create a new AccelerometerListener to connect to the remote SPOT over the radio.
     */
    public RadioTools () {
        System.out.println("Starting RadioTools...");
        //init();
    }

    /**
     * Connect to base station & other initialization.
     */
    private void init () {
        RadiogramConnection rcvConn = null;
        try {
            rcvConn = (RadiogramConnection)Connector.open("radiogram://:" + PDUs.BROADCAST_PORT);
            System.out.println("Open Broadcast Port: " + PDUs.BROADCAST_PORT );
            //baseStationPresent = true;
            //System.out.println("connecting to base station: " );

        } catch (Exception ex) {
            //baseStationPresent = false;
            System.out.println("Problem open broadcast port: " + ex);
        } finally {
            try {
                if (rcvConn != null) {
                    rcvConn.close();
                    System.out.println("Broadcast Port Closed");
                }
            } catch (IOException ex) { /* ignore */ }
        }
    }


    /**
     * Specify the GUI window that shows whether connected to a remote SPOT.
     *
     * @param fr the HostGUI that will be used to display the connection status to the remote SPOT
     */
    public void setGUI (HostGUI fr) {
        guiFrame = fr;

        if (guiFrame != null) {
            //System.out.println("no null ");
            //final HostGUI fr = guiFrame;
        }
        else
            System.out.println("null ");
    }

    /**
     *
     *
     */
    void SubscriptionProcessNode() {
        sendCmd( PDUs.HELLO );
    }


    /**
     * Send
     *
     * @param idNode identificaction node
     */
    public void doGetMeasures (int idNode) {

        //if (conn != null) {
            try {
                conn = (RadiogramConnection)Connector.open("radiogram://broadcast:" + PDUs.BROADCAST_PORT);

                Radiogram rdg = (Radiogram)conn.newDatagram(conn.getMaximumLength());
                xdg = (Radiogram)conn.newDatagram(10); // we never send more than 1 or 2 bytes

                System.out.println("PIDE MEDIDAS NODO " + idNode);
                xdg.reset();
                xdg.writeByte( PDUs.GET_MEASURES );
                xdg.writeByte(idNode);
                conn.send(xdg);
            } catch (NoAckException nex) {
                System.out.println("Error sending GET_MEASURES message" + nex.toString());
                //connected = false;
                //updateConnectionStatus(connected);
            } catch (IOException ex) {
                // ignore any other problems
            }
        //}
    }

    /**
     * Send a
     *
     * @param tempValue, ecValue and phValue
     */
    public void doSetMeasures (float tempValue, float ecValue, float phValue) {

        if (conn != null) {
            try {
                xdg.reset();
                xdg.writeByte( PDUs.GET_MEASURES_RESPONSE );
                xdg.writeFloat( tempValue );
                xdg.writeFloat( ecValue );
                xdg.writeFloat( phValue );
                conn.send(xdg);
            } catch (NoAckException nex) {
                System.out.println("Error sending GET_MEASURES_RESPONSE message" + nex.toString());
                //connected = false;
                //updateConnectionStatus(connected);
            } catch (IOException ex) {
                // ignore any other problems
            }
        }
    }

    /**
     *
     *
     * @param typeAlarm
     *
     */
    public void doSetAlarm (byte typeAlarm) {

        if (conn != null) {
            try {
                xdg.reset();
                xdg.writeByte( PDUs.ALARM );
                xdg.writeByte( typeAlarm );
                conn.send(xdg);
            } catch (NoAckException nex) {
                System.out.println("Error sending ALARM message" + nex.toString());
                //connected = false;
                //updateConnectionStatus(connected);
            } catch (IOException ex) {
                // ignore any other problems
            }
        }
    }

    /**
     * Send a
     *
     * @param TODO
     */
    public void doSetLimits (float tempMin, float tempMax, float phMin, float phMax, float ecMin, float ecMax) {

        //if (conn != null) {
            try {
                conn = (RadiogramConnection)Connector.open("radiogram://broadcast:" + PDUs.BROADCAST_PORT);
                xdg = (Radiogram)conn.newDatagram(conn.getMaximumLength()); // we never send more than 1 or 2 bytes

                System.out.println("CAMBIA LIMITES");
                xdg.reset();
                xdg.writeByte( PDUs.SET_CHANGE_LIMITS );
                xdg.writeFloat( tempMin );
                xdg.writeFloat( tempMax );
                xdg.writeFloat( phMin );
                xdg.writeFloat( phMax );
                xdg.writeFloat( ecMin );
                xdg.writeFloat( ecMax );
                conn.send(xdg);
            } catch (NoAckException nex) {
                //connected = false;
                //updateConnectionStatus(connected);
            } catch (IOException ex) {
                // ignore any other problems
            }
        //}
    }

    /**
     * Send a simple command request to the remote SPOT.
     *
     * @param cmd the command requested
     */
    private void sendCmd (byte cmd) {
        if (conn != null) {
            try {
                System.out.println("SEND COMMAND");
                xdg.reset();
                xdg.writeByte(cmd);
                conn.send(xdg);
            } catch (NoAckException nex) {
                //connected = false;
                //updateConnectionStatus(connected);
            } catch (IOException ex) {
                // ignore any other problems
            }
        }
    }

        /**
     * Stop running. Also notify the remote SPOT that we are no longer listening to it.
     */
    public void doQuit () {
        //sendCmd(DISPLAY_SERVER_QUITTING);
        running = false;
    }

    /**
     * Main runtime loop to connect to a remote SPOT.
     * Do not call directly. Call start() instead.
     */
    public void run () {
        System.out.println("SinkHostApp Reader Thread Started ...");
        hostLoop();
    }

    /**
     * Main runtime loop to connect to a remote SPOT.
     * Do not call directly. Call start() instead.
     */
    private int getArea(String addres){
        int index = Arrays.asList(arrayMAC).indexOf(addres);

        return index;
    }

    /**
     * Wait for a remote SPOT to request a connection.
     */
    private void waitForSpot () {
        if (guiFrame != null) {
            System.out.println("no null 3 ");
            final HostGUI fr = guiFrame;
        }
        else
            System.out.println("null 3");

        RadiogramConnection rcvConn = null;
        aggAddress = 0;
        System.out.println("Waiting for AGG");
        try {
            rcvConn = (RadiogramConnection)Connector.open("radiogram://:" + PDUs.BROADCAST_PORT);
            rcvConn.setTimeout(10000);             // timeout in 10 seconds
            Datagram dg = rcvConn.newDatagram(rcvConn.getMaximumLength());
            while (true) {
                try {
                    dg.reset();
                    rcvConn.receive(dg);            // wait until we receive a request

                   if (dg.readByte() == PDUs.HELLO_SINK) {       // type of packet
                        String addr = dg.getAddress();
                        IEEEAddress ieeeAddr = new IEEEAddress(addr);
                        long macAddress = ieeeAddr.asLong();
                        System.out.println("Received request from: " + ieeeAddr.asDottedHex());
                        aggAddress = macAddress;
                        send_HELLO_RESPONSE(aggAddress);
                        break;
                    }
                    
                    /*byte packetType = dg.readByte();
                    System.out.println("RECIBO: " + getMessageName(packetType) );
                    //guiFrame.log("RECIBO: dfdfsgsdfgsdg" );

                    switch (packetType) {
                        case PDUs.GET_MEASURES_RESPONSE:
                            String addr = dg.getAddress();

                            //Segun la MAC identificamos el numero de area
                            int area = this.getArea(addr);

                            //leemos las medidas
                            String tempReception    = String.valueOf(dg.readFloat());
                            String ecReception      = String.valueOf(dg.readFloat());
                            String phReception      = String.valueOf(dg.readFloat());

                            guiFrame.log("RECEIVE INFO FROM: " + addr );
                            //escribo valores recibidos en la tabla
                            guiFrame.displayInfo( area, addr, tempReception, ecReception, phReception );

                            System.out.println("[MEASURES NODE]" +   //TODO: cambiar nombre nodo
                                    "N? Area: " + area + "\n" +
                                    "ID: "      + addr + "\n" +
                                    "Temp: "    + tempReception + "\n" +
                                    "EC: "      + ecReception + "\n" +
                                    "pH: "      + phReception );

                            break;
                        case PDUs.ALARM:
                            //Si es alarma leemos de que tipo es
                            byte alarmType = dg.readByte();
                            //TODO: escribir en el log la info de la alarma
                            String addr2 = dg.getAddress();
                            System.out.println("ALARMA " + getMessageName(alarmType) + " from " + addr2 );
                            guiFrame.logAlarm("ALARMA " + getMessageName(alarmType) + " from " + addr2 );
                            break;
                        default:
                            break;
                    }
                */} catch (TimeoutException ex) {
                    //announceStarting();
                    System.out.println("No AGGREGATOR");
                }catch (Exception ex) {
                    System.out.println("Error communicating with HostSink: " + ex.toString());
                }
            }
        } catch (Exception ex) {
            System.out.println("Error waiting for remote Spot: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                if (rcvConn != null) {
                    rcvConn.close();
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
            rdg.writeByte(PDUs.HELLOSINK_RESPONSE);        // packet type
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

    /**
     * Main receive loop. Receive a packet sent by remote SPOT and handle it.
     */
    private void hostLoop() {

        if (guiFrame != null) {
            System.out.println("no null 2 ");
            final HostGUI fr = guiFrame;
        }
        else
            System.out.println("null 2");

        running = true;
        while (running) {
            waitForSpot();   // connect to a Spot with accelerometer telemetry to display
            if (aggAddress != 0) {
                try {

                    conn = (RadiogramConnection)Connector.open("radiogram://" + aggAddress + ":" + PDUs.CONNECTED_PORT);
                    conn.setTimeout(10000);             // timeout every second
                    Radiogram rdg = (Radiogram)conn.newDatagram(conn.getMaximumLength());
                    xdg = (Radiogram)conn.newDatagram(10); // we never send more than 1 or 2 bytes

                    connected = true;

                    while (connected) {
                        try {
                            rdg.reset();
                            conn.receive(rdg);            // wait until we receive a reply
                        } catch (TimeoutException ex) {
                            continue;
                        }

                        byte packetType = rdg.readByte();
                        System.out.println("RECIBO: " + getMessageName(packetType));
                        guiFrame.log("RECIBO: " + getMessageName(packetType));
                        switch (packetType) {
                            case PDUs.HELLO:
                                System.out.println("[HELLO] Node added succesfully.");
                                break;
                            case PDUs.HELLO_RESPONSE:
                                System.out.println("[HELLO_RESPONSE] Node added succesfully.");
                                break;
                            case PDUs.GET_MEASURES:
                                System.out.println("[MEASURES] Received measures.");
                                break;
                            case PDUs.MESSAGE_REPLY:
                                String str = rdg.readUTF();
                                System.out.println("Message from sensor: " + str);
                                guiFrame.log( str );
                                break;
                            case PDUs.GET_MEASURES_RESPONSE:
                                String addr = rdg.getAddress();
                                //Segun la MAC identificamos el numero de area
                                int area = this.getArea(addr);
                                //leemos las medidas
                                String tempReception    = String.valueOf(rdg.readFloat());
                                String ecReception      = String.valueOf(rdg.readFloat());
                                String phReception      = String.valueOf(rdg.readFloat());
                                guiFrame.log("RECEIVE INFO FROM: " + addr );
                                //escribo valores recibidos en la tabla
                                guiFrame.displayInfo( area, addr, tempReception, ecReception, phReception );
                                System.out.println("[MEASURES NODE\n]" +   //TODO: cambiar nombre nodo
                                        "N? Area: " + area + "\n" +
                                        "ID: "      + addr + "\n" +
                                        "Temp: "    + tempReception + "\n" +
                                        "EC: "      + ecReception + "\n" +
                                        "pH: "      + phReception );
                                break;
                            case PDUs.ALARM:
                                //Si es alarma leemos de que tipo es
                                short alarmType = rdg.readByte();
                                //TODO: escribir en el log la info de la alarma
                                String addr2 = rdg.getAddress();
                                guiFrame.logAlarm("ALARMA " + getMessageName((byte) alarmType) + " from " + addr2 );
                                switch (alarmType) {
                                    case PDUs.ALARMTYPE_TEMP_MIN:

//                                    TODO: escribir en el log la info de la alarma
//                                    String addr2 = rdg.getAddress();
//                                    guiFrame.logAlarm("ALARMA " + getMessageName(alarmType) + " from " + addr2 );
                                        break;
                                    case PDUs.ALARMTYPE_TEMP_MAX:
                                        break;
                                    case PDUs.ALARMTYPE_PH_MIN:
                                        break;
                                    case PDUs.ALARMTYPE_PH_MAX:
                                        break;
                                    case PDUs.ALARMTYPE_EC_MIN:
                                        break;
                                    case PDUs.ALARMTYPE_EC_MAX:
                                        break;
                                }
                                break;
                            case PDUs.SET_CHANGE_LIMITS:
                                System.out.println("[CHANGE_LIMITS] Limits changed succesfully.");
                                break;
                            case PDUs.SET_CHANGE_LIMITS_RESPONSE:
                                System.out.println("[CHANGE_LIMITS] Limits changed succesfully.");
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Error communicating with remote Aggregator: " + ex.toString());
                } finally {
                    try {
                        //connected = false;
                        //updateConnectionStatus(connected);
                        if (conn != null) {
                            xdg.reset();
                            //xdg.writeByte(DISPLAY_SERVER_QUITTING);        // packet type
                            conn.send(xdg);                                // broadcast it
                            conn.close();
                            conn = null;
                        }
                    } catch (IOException ex) { /* ignore */ }
                }
            }
        }
    }


    /**
     * Returns the message name of a given message type.
     *
     * @param byte Type of the message
     * @return Name of a message of the given type
     */
    public String getMessageName(short type){
        switch(type){
            case PDUs.HELLO:
                return "HELLO MESSAGE";
            case PDUs.HELLO_RESPONSE:
                return "HELLO RESPONSE";
            case PDUs.GET_MEASURES:
                return "GET MEASURES";
            case PDUs.GET_MEASURES_RESPONSE:
                return "MEASURES RESPONE";
            case PDUs.ALARM:
                return "ALARM";
            case PDUs.ALARMTYPE_TEMP_MIN:
                return "TEMP MIN";
            case PDUs.ALARMTYPE_TEMP_MAX:
                return "TEMP MAX";
            case PDUs.ALARMTYPE_PH_MIN:
                return "PH MIN";
            case PDUs.ALARMTYPE_PH_MAX:
                return "PH MAX";
            case PDUs.ALARMTYPE_EC_MIN:
                return "EC MIN";
            case PDUs.ALARMTYPE_EC_MAX:
                return "EC MAX";
            case PDUs.SET_CHANGE_LIMITS:
                return "CHANGE LIMITS";
            case PDUs.SET_CHANGE_LIMITS_RESPONSE:
                return "CHANGE LIMITS RESPONSE";
            case PDUs.MESSAGE_REPLY:
                return "MESSAGE FROM SENSOR: ";
            default:
                return "Not Recognized Message: " + type;
        }
    }
}
