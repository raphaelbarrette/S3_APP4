package Layers;

import java.io.IOException;

public class NetworkLayer extends Layer{
    private static NetworkLayer instance;
    public static NetworkLayer getInstance(){
        if (instance == null){
            instance = new NetworkLayer();
        }
        return instance;
    }
    @Override
    protected void getFromUp(byte[] PDU) {
        sendDown(PDU);
    }

    @Override
    protected void getFromDown(byte[] PDU) throws TransmissionErrorException, IOException {
        sendUp(PDU);
    }
}
