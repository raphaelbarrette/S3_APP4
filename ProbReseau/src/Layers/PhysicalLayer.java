package Layers;

import javax.xml.crypto.Data;
import java.net.InetAddress;
import java.net.*;
import java.io.*;

public class PhysicalLayer extends Layer{
    int port = 0;
    InetAddress addressIP = null;
    public int packetenvoyer = 0;
    public int DelayError = -1;
    public int delay = 0;
    protected ReceptionThread thread;


    // singleton of PhysicalLayer
    private static PhysicalLayer instance;
    private PhysicalLayer(){}
    static public PhysicalLayer getInstance(){
        if (instance == null){
            instance = new PhysicalLayer();
        }
        return instance;
    }
    public void setDestAddressIP(InetAddress address){
        try{
            this.addressIP = address;
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }
    public void setDestAddressIP(String addressIP){
        try {
            this.addressIP = InetAddress.getByName(addressIP);
        } catch (UnknownHostException exception){
            exception.printStackTrace();
        }
    }

    public void setDestPort(int port){
        this.port = port;
    }
    public void start(){
        thread.running = true;
        thread.start();
    }
    public void stop(){
        thread.running = false;
        thread.interrupt();
    }
    public boolean threadRunning(){
        return thread.running;
    }
    @Override
    protected void getFromUp(byte[] PDU) {
        DatagramSocket DSocket = null;
        try {
            DSocket = new DatagramSocket();
        } catch (SocketException exception){
            exception.printStackTrace();
        }
        packetenvoyer++;
        if (packetenvoyer == DelayError){
            PDU[10] <<= 1;
        }

        DatagramPacket packet = new DatagramPacket(PDU, PDU.length, addressIP, port);
        try {
            DSocket.send(packet);
            Thread.sleep(delay);
        } catch (IOException | InterruptedException exception){
            exception.printStackTrace();
        }
    }


    @Override
    protected void getFromDown(byte[] PDU) throws TransmissionErrorException {
        sendUp(PDU);
    }
    public void setReceptionThread(int port) throws  IOException{
        this.thread = new ReceptionThread(port, this);
    }

    private class ReceptionThread extends Thread {
        protected  DatagramSocket DSocket = null;
        private PhysicalLayer parent;
        public boolean running = true;

        public ReceptionThread(int port, PhysicalLayer parent) throws IOException{
            super("Layers ReceptionThread" + Math.random());
            DSocket = new DatagramSocket(port);
            this.parent = parent;
        }

        public void run(){
            while (running){
                try {
                    byte[] buffer = new byte[256];

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    DSocket.receive(packet);
                    parent.getFromDown(packet.getData());
                } catch (IOException | TransmissionErrorException excpetion){
                    running = false;
                    DSocket.close();


                }
            }
            DSocket.close();
        }
    }

}
