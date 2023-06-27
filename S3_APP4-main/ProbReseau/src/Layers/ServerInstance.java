package Layers;

import java.io.IOException;

public class ServerInstance {
    private PhysicalLayer physicalLayer;


    public ServerInstance(String listening_port) throws IOException {
        ServerInstanceBuild(listening_port);
    }


    private void ServerInstanceBuild(String listening_port) throws IOException {
        ApplicationLayer applicationLayer = ApplicationLayer.getInstance();
        TransportLayer transportLayer = TransportLayer.getInstance();
        NetworkLayer networkLayer = NetworkLayer.getInstance();
        DataLinkLayer dataLinkLayer = DataLinkLayer.getInstance();
        physicalLayer = PhysicalLayer.getInstance();
        physicalLayer.setLayerdown(dataLinkLayer);
        dataLinkLayer.setLayerUp(networkLayer);
        dataLinkLayer.setLayerdown(physicalLayer);
        networkLayer.setLayerUp(transportLayer);
        networkLayer.setLayerdown(dataLinkLayer);
        transportLayer.setLayerdown(networkLayer);
        transportLayer.setLayerUp(applicationLayer);
        applicationLayer.setLayerdown(transportLayer);
        // set server
        physicalLayer.setReceptionThread(Integer.parseInt(listening_port));
        physicalLayer.setDestPort(25002);
        physicalLayer.setDestAddressIP("localhost");
    }


    public void StartServer() throws IOException {
        physicalLayer.start();
        System.out.println("Running");
        System.out.println("Q: to kill");
        System.out.println("Then enter");
        while(physicalLayer.threadRunning()) {
            int command = System.in.read();
            switch (command) {
                case 113:
                case 81:
                    System.exit(0);
                    break;
            }
        }
    }
}