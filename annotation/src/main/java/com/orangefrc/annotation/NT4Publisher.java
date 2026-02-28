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

public class NT4Publisher {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();

    public NT4Publisher() {
    }

    public void putValues(TypeMirror type) {
    }

    
}
