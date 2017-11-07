/*
 * RSUS Project: Pression Farming
 * PDUS
 *
 */

package org.sunspotworld;

/*
 *
 * @author Balat, Contreras, Gonzalez.
 */
public class PDUs {


    public static final int MEASURES_PORT = 101;
    public static final int ALARMS_PORT = 102;

    public static final String BROADCAST_PORT = "42";
    public static final String CONNECTED_PORT = "43";
    public static final String CONNECTEDSINK_PORT = "44";

    public static final byte LOCATE_DISPLAY_SERVER_REQ = 1;
    public static final byte DISPLAY_SERVER_RESTART = 2;
    public static final byte DISPLAY_SERVER_QUITTING = 3;
    public static final byte DISPLAY_SERVER_AVAIL_REPLY = 101;
    public static final byte MESSAGE_REPLY = 111;

    public final static byte HELLO = 0x01;
    public final static byte HELLO_RESPONSE = 0x02;
    public final static byte ALARM = 0x03;
    public final static byte ALARM_RESPONSE = 0x04;
    public final static byte GET_MEASURES = 0x05;
    public final static byte GET_MEASURES_RESPONSE = 0x06;
    public final static byte SET_CHANGE_LIMITS = 0x07;
    public final static byte SET_CHANGE_LIMITS_RESPONSE = 0x08;
    public final static byte HELLO_SINK = 0x09;
    public final static byte HELLOSINK_RESPONSE = 0x0A;

    public final static byte ACK =0x0F;

    public final static short ALARMTYPE_TEMP_MAX = 0XFF;
    public final static short ALARMTYPE_TEMP_MIN = 0XFE;
    public final static short ALARMTYPE_PH_MAX = 0XFD;
    public final static short ALARMTYPE_PH_MIN = 0XFC;
    public final static short ALARMTYPE_EC_MAX = 0XFB;
    public final static short ALARMTYPE_EC_MIN = 0XFA;

    private byte messageType;

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public byte getMessageType() {
        return this.messageType;
    }
}
