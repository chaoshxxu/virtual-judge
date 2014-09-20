package judge.tool;

import java.util.Random;

public class RandomUtil {
    
    private static Random random = new Random();
    
    public static String getRandomString(int length, String charset) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(charset.charAt(random.nextInt(charset.length())));
        }
        return stringBuilder.toString();
    }

}
