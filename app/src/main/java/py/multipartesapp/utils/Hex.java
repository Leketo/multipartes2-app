package py.multipartesapp.utils;

import java.io.StringWriter;
import java.math.BigInteger;

public class Hex {

    static private final char hex[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String encode(byte[] dataStr) {
        StringWriter w = new StringWriter();
        for (int i = 0; i < dataStr.length; i++) {
            int b = dataStr[i];
            w.write(hex[((b >> 4) & 0xF)]);
            w.write(hex[((b >> 0) & 0xF)]);
        }
        return w.toString();
    }

    public static byte[] decode(String dataStr) {

        if ((dataStr.length() & 0x01) == 0x01)
            dataStr = new String(dataStr + "0");
        BigInteger cI = new BigInteger(dataStr, 16);
        byte[] data = cI.toByteArray();

        return data;
    } //decode
}