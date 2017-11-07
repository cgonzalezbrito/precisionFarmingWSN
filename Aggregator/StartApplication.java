/*AGGREGATOR
 * StartApplication.java
 *
 * Created on Nov 25, 2016 10:04:33 PM;
 */

package org.sunspotworld;

import com.sun.spot.util.Utils;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 * 
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */

public class StartApplication extends MIDlet {

    private long spotAddress = 0;

    float tempReception;
    float ecReception;
    float phReception;
    private boolean newDataFlag = false;

    
    RadioTools node = new RadioTools();

    public static final String ID = ("7f00.0101.0000.1001");            //Buscar M?todo dinamico para obtener MAC

    protected void startApp() throws MIDletStateChangeException {

        boolean connected = false;
        String SinkAddress = null;
        System.out.println("Aggregator Node"+ID);

        while (connected == false){
            if (SinkAddress == null){
                SinkAddress = node.SubscriptionProcessNode();
                System.out.println("Sending measurements to:"+SinkAddress);
            } else {
                connected = true;
                node.doSendMeasures(0, 0, 0, SinkAddress);
            }
        }

        node.start();

        while (true) {
            Utils.sleep(1000);
            spotAddress = node.get_spotAddress();
            if (spotAddress != 0){
                
                //node.unicast_communication(spot_address);
            }
            // Ask for broadcast port availability
            if (node.isOpenBroadcastPort() == false){
                //Ask for new data
                newDataFlag = node.get_newDataFlag();
                tempReception = node.get_tempReception();
                ecReception = node.get_ecReception();
                phReception = node.get_phReception();
                //sinkAddress = node.get_spotAddress();
                if (newDataFlag == true) {
                    node.doSendMeasures(tempReception, ecReception, phReception, SinkAddress);
                    System.out.println("Send OK");
                }

            }
        }

    }


    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        
    }

    protected void pauseApp() {
        
    }

        
}
