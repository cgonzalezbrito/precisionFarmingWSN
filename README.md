# precisionFarmingWSN
Wireless Sensor Network for Precision Farming unsing SUN Spot Motes.

http://www.sunspotdev.org/ Important Note:

    Oracle has officlally stopped selling and supporting Sun SPOTs. The sunspotworld.com website has been turned off and no further
    developement or support of Sun SPOTs can be expected from Oracle.

Educational Project whose objetive is the deployment of a Wireless Senser Network for Precision Farming
to measure the following variables:

 -  Temperature.
 -  Electrical Conductivity (EC).
 -  Hydric Soil Balance.
 -  Nutrients.
 -  PH.


1. ARCHITECTURE:

Node   Node                  Node     Node
    Agg----------Sink-------------Agg
Node   Node       |           Node     Node
                  |
                  PC
                  
2. REQUIREMENTS

The following requirements are needed for the correct performance of Precision Farming: 
 -  The motes measure different environmental parameters (alarm for differents parameters are no periodical): 
   -  Temperature. 
     -  Temperature alarm: activated when present thresholds are exceeded fully. 
   -  Electrical conductivity (EC):
     -  EC alarm: activated when present thresholds are exceeded fully.
   -  Hydric soil balance (HSB):
     -  HSB  alarm: activated when present thresholds are exceeded fully.
   -  Nutrients measurements (N):
     -  N alarm: activated when present thresholds are exceeded fully.
   -  pH:
     -  pH: alarm: activated when present thresholds are exceeded fully.
   -  Motes Battery level read:
     -  Low battery alarm: activated when battery level is under 25%.
 -  Commanding sensors to make something: irrigation control, change Interval of measures or threshold values, select a variable to be displayed on the LED vector 
 -  The system alarms might be able to be stopped locally, remotely or automatically. 
 -  The system might be able to interrupt resources supply in case of alarm or by remote request. 
 -  Use of buttons of the sensor: Stop/Start the irrigation.
 -  Use of the LEDs for display the variable (humidity, ph, ec, etc.) percentage. 
 -  Storing each sensor configuration (future implementation). 
 -  By using the sink, the WMS will be configured with different alarm thresholds and sampling periods (future implementation). 
 -  An external server will store all the information from WSN (future implementation).
 
 3. LIMITATIONS
 
 -  The system must be implemented with SunSpots.
 -  The nodes are restricted to three sensing capabilities: temperature, accelerometer and luminosity. 
 -  The nodes are restricted to two direct actuator capabilities: LED indicators and speaker. They can also send analog and digital signals in order to simulate the use of external actuator devices. 
 -  The nodes are not waterproof.
