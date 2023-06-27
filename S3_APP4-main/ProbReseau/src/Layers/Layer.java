package Layers;

import java.io.IOException;

public abstract class Layer {
    private Layer layerup;
    private Layer layerdown;

    protected abstract void getFromUp(byte[] PDU);
    protected abstract void getFromDown(byte[] PDU) throws TransmissionErrorException, IOException;
    protected void sendDown(byte[] PDU){
        layerdown.getFromUp(PDU);
    }
    protected void sendUp(byte[] PDU) throws TransmissionErrorException, IOException {
        layerup.getFromDown(PDU);
    }
    public void setLayerUp(Layer uplayer){
        layerup = uplayer;
    }
    public void setLayerdown(Layer downlayer){
        layerdown = downlayer;
    }
}
