package rabbit.gateway.common.utils;

public class HexUtils {

    private HexUtils() {}

    /**
     * 转16进制
     *
     * @param bytes
     * @return
     */
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String str = Integer.toHexString(b & 0xFF);
            if (str.length() < 2) {
                sb.append(0).append(str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 16 进制转byte[]
     *
     * @param hexStr
     * @return
     */
    public static byte[] toBytes(String hexStr) {
        String hex = hexStr;
        if (1 == hex.length() % 2) {
            hex = "0".concat(hex);
        }
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }

}
