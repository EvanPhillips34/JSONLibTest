package com.orangefrc.annotation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.FloatPublisher;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.Publisher;

public class NT4Publisher {
    NetworkTableInstance inst;
    NetworkTable table;
    Map<Name, Publisher> pubMap;

    DatagramSocket socket;



    public NT4Publisher(Map<Name, TypeMirror> map) throws IOException{
        socket = new DatagramSocket(3400);
        byte[] buffer = "TYPE TESTING".getBytes();
        InetAddress address = InetAddress.getByName("10.30.26.5");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        inst = NetworkTableInstance.getDefault();
        table = inst.getTable("JSONTable");

        for(Map.Entry<Name, TypeMirror> entry : map.entrySet()) {
            TypeMirror type = entry.getValue();
            Name key = entry.getKey();

            if(type.toString() == "double") {
                pubMap.put(key, table.getDoubleTopic(key.toString()).publish());
            }
        }
    }

    public void putValues() {
        for(Map.Entry<Name, Publisher> entry : pubMap.entrySet()) {
            
        }
    }

    
}
