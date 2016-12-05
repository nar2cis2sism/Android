package engine.android.util.secure;

/**
 * A simple Hex encode and decode class
 * 
 * @author Daimon
 * @version N
 * @since 4/1/2012
 */
public final class HexUtil {

    private static final char[] HEX_VALUE = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String encode(byte[] bs) {
        if (bs == null)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder(bs.length * 2);
        for (byte b : bs)
        {
            sb.append(HEX_VALUE[(b & 0xf0) >> 4]);
            sb.append(HEX_VALUE[b & 0x0f]);
        }

        return sb.toString();
    }

    public static byte[] decode(String s) {
        char[] cs = s.toCharArray();
        if (cs.length % 2 != 0)
        {
            throw new IllegalArgumentException("Invalid length of input String");
        }

        int len = cs.length / 2;
        byte[] bs = new byte[len];
        for (int i = 0; i < len; i++)
        {
            bs[i] = (byte) ((char2int(cs[i * 2]) << 4) + char2int(cs[i * 2 + 1]));
        }

        return bs;
    }

    private static int char2int(char c) {
        if ('a' <= c && c <= 'f')
        {
            return c - 'a' + 10;
        }
        else if ('A' <= c && c <= 'F')
        {
            return c - 'A' + 10;
        }
        else if ('0' <= c && c <= '9')
        {
            return c - '0';
        }

        throw new IllegalArgumentException("Invalid Hex character");
    }
}