package Layers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

public class TransportLayer extends Layer {

    private static TransportLayer instance;

    private final char CODE_START = 'd';
    private final char CODE_END = 'f';
    private final char CODE_NORMAL = ' ';
    private final char CODE_ACK = 'a';
    private final char CODE_RESEND = 'r';
    private final int OFFSET = 11;
    private final int SIZE = 188;
    private final int SIZE_HEADER_POS = 9;
    private final int SEQ_HEADER_POS = 1;
    private int EndSequence = -1;
    private int errors;
    private byte[][] TPDU;
    private Map<Integer, byte[]> receiveBuffer;

    public static TransportLayer getInstance() {
        if (instance == null) {
            instance = new TransportLayer();
        }
        return instance;
    }


    @Override
    protected void getFromUp(byte[] PDU) {
        int count = (int) Math.ceil((double) PDU.length / SIZE);
        TPDU = new byte[count][200];

        for (int i = 0; i < count; i++) {
            int taille = SIZE;
            if (i == count - 1) {
                taille = PDU.length % SIZE;
            }
            arraycopy(PDU, i * SIZE, TPDU[i], OFFSET + 1, taille);

            char code = CODE_NORMAL;
            if (i == 0) {
                code = CODE_START;
            } else if (i == count - 1) {
                code = CODE_END;
            }

            TPDU[i][0] = (byte) code;
            arraycopy(convertIntToASCII(i, 8), 0, TPDU[i], SEQ_HEADER_POS, 8);
            arraycopy(convertIntToASCII(taille, 3), 0, TPDU[i], SIZE_HEADER_POS, 3);

            sendDown(TPDU[i]);
        }

        System.out.println("Transmission fini");

    }

    @Override
    protected void getFromDown(byte[] PDU) throws TransmissionErrorException, IOException {
        byte[] seq_bytes = Arrays.copyOfRange(PDU, SEQ_HEADER_POS, SIZE_HEADER_POS);
        byte[] size_bytes = Arrays.copyOfRange(PDU, SIZE_HEADER_POS, OFFSET + 1);

        char code = (char) PDU[0];
        int seq = convertAsciiToInt(seq_bytes);
        int size = convertAsciiToInt(size_bytes);

        byte[] data_bytes = Arrays.copyOfRange(PDU, OFFSET + 1, OFFSET + 1 + size);
        switch (code) {
            case CODE_START:
                // start of the communication & reset receive Buffer
                EndSequence = -1;
                receiveBuffer = new HashMap<>();
                savePDU(0, data_bytes);
                break;

            case CODE_END:
                EndSequence = seq;
                savePDU(seq, data_bytes);
                break;

            case CODE_NORMAL:
                savePDU(seq, data_bytes);
                break;
            case CODE_ACK:
                break;
            case CODE_RESEND:
                errors++;
                System.out.println("resend packet: " + errors);
                sendDown(TPDU[seq]);
                break;
        }
        if (EndSequence != -1) {
            System.out.println("size " + receiveBuffer.size() + " end sequence " + EndSequence);
            if (receiveBuffer.size() <= EndSequence)
                return;
            int arrayL = (receiveBuffer.size() - 1) * SIZE + receiveBuffer.get(EndSequence).length;
            byte[] passUpBuffer = new byte[arrayL];
            int count = 0;
            for (Map.Entry<Integer, byte[]> key_value : receiveBuffer.entrySet()) {
                arraycopy(key_value.getValue(), 0, passUpBuffer, count, key_value.getValue().length);
                count += key_value.getValue().length;
            }
            System.out.println("ERRRORSSSS:" + errors);
            sendUp(passUpBuffer);
        }
    }


    private void savePDU(int seq, byte[] data_bytes) throws TransmissionErrorException {
        if (seq != 0 && receiveBuffer.get(seq - 1) == null) {
            errors++;
            if (errors >= 3) {
                // TODO: throw error
                throw new TransmissionErrorException("More then 3 error connection will be lost.");
            }
            byte[] rPDU = createResendPDU(seq - 1);
            sendDown(rPDU);
        }
        if (receiveBuffer.get(seq) != null)
            return;
        receiveBuffer.put(seq, data_bytes);
        byte[] ackPDU = createAckPDU(seq);
        sendDown(ackPDU);
    }

    private byte[] createResendPDU(int seq) {
        int taille = 0;
        byte[] resendPDU = new byte[200];

        // Copy data size as ASCII in header
        resendPDU[0] = (byte) CODE_RESEND;       // Assuming sizeof(char) == sizeof(byte)
        arraycopy(convertIntToASCII(seq, 8), 0, resendPDU, SEQ_HEADER_POS, 8); // SEQUENCE
        arraycopy(convertIntToASCII(taille, 3), 0, resendPDU, SIZE_HEADER_POS, 3); // SIZE

        // Send lower Layer
        return resendPDU;
    }

    private byte[] createAckPDU(int seq) {
        byte[] ackPDU = new byte[200];

        // Copy data size as ASCII in header
        ackPDU[0] = (byte) CODE_ACK;       // Assuming sizeof(char) == sizeof(byte)
        arraycopy(convertIntToASCII(seq, 8), 0, ackPDU, SEQ_HEADER_POS, 8); // SEQUENCE
        arraycopy(convertIntToASCII(0, 3), 0, ackPDU, SIZE_HEADER_POS, 3); // SIZE

        // Send lower Layer
        return ackPDU;
    }

    private byte[] convertIntToASCII(int data, int size) {
        String converted = Integer.toString(data);
        byte[] converted2 = converted.getBytes(StandardCharsets.US_ASCII);

        // Copy data with padding on the left
        byte[] newData = new byte[size];
        fill(newData, (byte) '0');
        arraycopy(converted2, 0, newData, size - converted2.length, converted2.length);

        return newData;
    }

    private int convertAsciiToInt(byte[] data) {
        String data_string = new String(data);
        return Integer.parseInt(removeLeadingZeros(data_string));
    }

    public static String removeLeadingZeros(String str) {
        String regex = "^0+(?!$)";
        str = str.replaceAll(regex, "");

        return str;
    }
}

