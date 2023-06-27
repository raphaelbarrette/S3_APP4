package Layers;

import java.io.IOException;

public class ClientAppInstance {
    PhysicalLayer physicalLayer;
    ApplicationLayer applicationLayer;
    String filename;


    public ClientAppInstance(String filename, String destination_ip, String listening_port, boolean addErrors) throws IOException, InterruptedException {
        this.filename = filename;
        ClientInstanceBuild(destination_ip, listening_port, addErrors);
    }


    public void ClientInstanceBuild(String destination_ip, String listening_port, boolean addErrors) throws IOException {
        TransportLayer transportLayer = TransportLayer.getInstance();
        NetworkLayer networkLayer = NetworkLayer.getInstance();
        DataLinkLayer dataLinkLayer = DataLinkLayer.getInstance();
        physicalLayer = PhysicalLayer.getInstance();
        applicationLayer = ApplicationLayer.getInstance();
        physicalLayer.setLayerUp(dataLinkLayer);
        dataLinkLayer.setLayerUp(networkLayer);
        dataLinkLayer.setLayerdown(physicalLayer);
        networkLayer.setLayerUp(transportLayer);
        networkLayer.setLayerdown(dataLinkLayer);
        transportLayer.setLayerdown(networkLayer);
        transportLayer.setLayerUp(applicationLayer);
        applicationLayer.setLayerdown(transportLayer);

        // set server
        physicalLayer.setReceptionThread(Integer.parseInt(listening_port));
        physicalLayer.DelayError = addErrors ? 10 : -1;
        physicalLayer.delay = 1;
        physicalLayer.setDestPort(25001);
        physicalLayer.setDestAddressIP(destination_ip);
    }


    public void ClientStart() throws IOException, InterruptedException {
        System.out.println("Client Start");
        physicalLayer.start();
        applicationLayer.EnvoyerFichier(filename);
    }
}
