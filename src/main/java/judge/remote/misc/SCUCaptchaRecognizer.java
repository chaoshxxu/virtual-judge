package judge.remote.misc;

import java.awt.image.BufferedImage;

public class SCUCaptchaRecognizer {
    
    public static String recognize(BufferedImage img) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int minDiff = 999999;
            char digitAns = 0;
            for (int j = 0; j <= 9; j++) {
                int curDiff = 0;
                for (int y = 0; y < 9; y++) {
                    for (int x = 0; x < 6; x++) {
                        boolean pixel1 = digits[j][y].charAt(x) == '#';
                        boolean pixel2 = img.getRGB(left[i] + x, 1 + y) < -1e7;
                        if (pixel1 != pixel2) {
                            ++curDiff;
                        }
                    }
                }
                if (curDiff < minDiff) {
                    minDiff = curDiff;
                    digitAns = (char) ('0' + j);
                }
            }
            ans.append(digitAns);
        }
        return ans.toString();
    }
    
    private static int left[] = new int[]{4, 12, 20, 28};
    
    private static String[][] digits = new String[][]{
            {
                " #### ",
                " #  # ",
                "#    #",
                "#    #",
                "#    #",
                "#    #",
                "#    #",
                " #  # ",
                " #### "
            },
            {
                "###   ",
                "  #   ",
                "  #   ",
                "  #   ",
                "  #   ",
                "  #   ",
                "  #   ",
                "  #   ",
                "##### "
            },
            {
                " #### ",
                "#   ##",
                "     #",
                "     #",
                "    # ",
                "   #  ",
                "  #   ",
                " #    ",
                "######"
            },
            {
                " #### ",
                "#    #",
                "     #",
                "     #",
                "  ### ",
                "     #",
                "     #",
                "#    #",
                " #### "
            },
            {
                "   ## ",
                "   ## ",
                "  # # ",
                " #  # ",
                " #  # ",
                "#   # ",
                "######",
                "    # ",
                "    # "
            },
            {
                "##### ",
                "#     ",
                "#     ",
                "##### ",
                "    ##",
                "     #",
                "     #",
                "#   ##",
                " #### "
            },
            {
                "  ### ",
                " #   #",
                "#     ",
                "# ### ",
                "##  ##",
                "#    #",
                "#    #",
                " #  ##",
                " #### "        
            },
            {
                "######",
                "     #",
                "    # ",
                "    # ",
                "   #  ",
                "   #  ",
                "  #   ",
                "  #   ",
                " #    "
            },
            {
                " #### ",
                "#    #",
                "#    #",
                "#    #",
                " #### ",
                "#    #",
                "#    #",
                "#    #",
                " #### "   
            },
            {
                " #### ",
                "##  # ",
                "#    #",
                "#    #",
                "##  ##",
                " ### #",
                "     #",
                "#   # ",
                " ###  "
            }
        };
}