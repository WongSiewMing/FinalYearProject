package Helper;

/*Author : Adelina Tang Chooi & Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Conversion {

    public static String hexToAscii(String hexStr) {

        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String asciiToHex(String asciiStr) {

        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();

    }
}
