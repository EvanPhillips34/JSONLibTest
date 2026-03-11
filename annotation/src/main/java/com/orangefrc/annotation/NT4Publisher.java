package com.orangefrc.annotation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import org.ejml.equation.VariableScalar.Type;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.FloatPublisher;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.networktables.StringPublisher;

public class NT4Publisher {
    private static NetworkTableInstance inst = NetworkTableInstance.getDefault();
    private static NetworkTable table = inst.getTable("Tuning");


    public static StringPublisher updateError = table.getStringTopic("UpdateError").publish();
    public static StringPublisher createError = table.getStringTopic("FileCreateError").publish();
    public static StringPublisher readError = table.getStringTopic("FileReadError").publish();
    public static StringPublisher writeError = table.getStringTopic("FileWriteError").publish();

    public static StringPublisher status = table.getStringTopic("Status").publish();


    public NT4Publisher() {
    }

    public void putValues(TypeMirror type) {
    }

    
}
