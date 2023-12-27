package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://172.20.10.2:1883";
    private static final String TAG = "MainActivity";


    Set<String> availableParkingSpaces = new TreeSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MyApp","I am here");
        System.out.println("Connecting to....");
        connect();

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    subscribe("PARKING-AVAILABILITY-1");
                    subscribe("PARKING-AVAILABILITY-2");
                    subscribe("PARKING-AVAILABILITY-3");
                } else {
                    System.out.println("Connected to: " + serverURI);
                    subscribe("PARKING-AVAILABILITY-1");
                    subscribe("PARKING-AVAILABILITY-2");
                    subscribe("PARKING-AVAILABILITY-3");
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws
                    Exception {
                String parkingSpotAvailabeFromBackend = new String(message.getPayload());
                //System.out.println("Incoming message: " + parkingSpotAvailabeFromBackend);

                if(topic.equalsIgnoreCase("PARKING-AVAILABILITY-1")){
                    availableParkingSpaces.add("1");
                }
                if(topic.equalsIgnoreCase("PARKING-AVAILABILITY-2")){
                    availableParkingSpaces.add("2");
                }
                if(topic.equalsIgnoreCase("PARKING-AVAILABILITY-3")){
                    availableParkingSpaces.add("15");
                }
                handleParkingAvailability(parkingSpotAvailabeFromBackend);
                //availableParkingSpaces.add("Space "+newMessage.trim());
                     /* add code here to interact with elements
                     (text views, buttons)
                     using data from newMessage
                     */
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    private void connect(){
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), SERVER_URI,
                        clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " +
                            SERVER_URI);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscription successful to topic: " + topic);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    System.out.println("Failed to subscribe to topic: " + topic);

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void unsubscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken1 =client.unsubscribe(topic);
            subToken1.waitForCompletion(6500);
            subToken1.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Unsubscribed successfully: " + topic);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    System.out.println("Failed to unsubscribe to topic: " + topic);

                }

            });


        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndBookNearestAvailableSpot() {
        // Implement logic to calculate distances from Sensor 1 to Sensor 2 and Sensor 1 to Sensor 15
        // Then, book the nearest available parking spot among Sensor 2 and Sensor 15

        // Placeholder coordinates for sensors and user in Stockholm city (replace with actual coordinates)
        double userLatitude = 59.3293; // Stockholm city
        double userLongitude = 18.0686;

        double sensor1Latitude = 59.3312; // Sensor 1 - Closest to the user
        double sensor1Longitude = 18.0625;

        double sensor2Latitude = 59.3298; // Sensor 2
        double sensor2Longitude = 18.0645;

        double sensor15Latitude = 59.3214; // Sensor 15
        double sensor15Longitude = 18.0737;

        // Calculate distances from Sensor 1 to Sensor 2 and Sensor 1 to Sensor 15
        double distanceToSensor2 = calculateDistance(sensor1Latitude, sensor1Longitude, sensor2Latitude, sensor2Longitude);
        double distanceToSensor15 = calculateDistance(sensor1Latitude, sensor1Longitude, sensor15Latitude, sensor15Longitude);

        // Book the nearest available parking spot
        if ((distanceToSensor2 < distanceToSensor15) && availableParkingSpaces.contains("2")) {
            bookParkingSpot("sensor2"); // Replace with the actual logic to book sensor 2
            availableParkingSpaces.remove("2");
            unsubscribe("PARKING-AVAILABILITY-2");
        } else if(availableParkingSpaces.contains("15")) {
            bookParkingSpot("sensor15"); // Replace with the actual logic to book sensor 15
            availableParkingSpaces.remove("15");
            unsubscribe("PARKING-AVAILABILITY-3");
        }
        else{
            System.out.println("All Slots filled");
            //Subscribe to all three topics again.
        }
    }



    private void bookParkingSpot(String sensorId) {
        // Implement logic to book the parking spot with the given sensorId
        // This can be a placeholder, and you may need to replace it with the actual booking logic
        // Example: Display a message or send a request to book the parking spot
        System.out.println("Booking parking spot: " + sensorId);

    }

    private void handleParkingAvailability(String parkingSpotAvailabeFromBackend) {
        // Implement logic to check availability and book the nearest available parking spot
        // Assume payload contains availability information, e.g., "available" or "unavailable"
        if (parkingSpotAvailabeFromBackend.equals("1") && availableParkingSpaces.contains("1")) {
            // Parking spot is available, implement booking logic here
            bookParkingSpot("sensor1"); // Replace with the actual logic to book sensor 1
            availableParkingSpaces.remove("1");
            unsubscribe("PARKING-AVAILABILITY-1");

        } else {
            // Parking spot is not available, calculate distances and book the nearest available spot
            calculateAndBookNearestAvailableSpot();
        }


    }





    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Implement logic to calculate distances between coordinates
        // Using the Haversine formula for simplicity
        double earthRadius = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c * 1000; // Distance in meters
    }
}