package Layers;

import java.io.IOException;

public class TransportLayer extends Layer{

    private static TransportLayer instance;

    private final char CODE_START = 'd';
    private final char CODE_END = 'f';
    private final char CODE_NORMAL
    @Override
    protected void getFromUp(byte[] PDU) {

    }

    @Override
    protected void getFromDown(byte[] PDU) throws TransmissionErrorException, IOException {

    }
}
