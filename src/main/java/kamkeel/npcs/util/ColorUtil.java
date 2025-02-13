package kamkeel.npcs.util;

public class ColorUtil {


    public static int[] colorTableInts = {
         0xFFFFFF , // White
         0xF2B233 , // Light Orange
         0xE680D9 , // Pinkish Purple
         0x99B2F2 , // Light Blue
         0xE6E633 , // Yellow
         0x80CC1A , // Green
         0xF2B2CC , // Light Pink
         0x4D4D4D , // Dark Gray
         0x999999 , // Gray
         0x4D99B2 , // Teal
         0xB266E6 , // Purple
         0x3366CC , // Deep Blue
         0x805D4D , // Brown
         0x668033 , // Olive Green
         0xCC4D4D , // Red
         0x1A1A1A   // Black
    };

    public static float[] hexToRGB(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255.0F;
        float g = ((hex >> 8) & 0xFF) / 255.0F;
        float b = (hex & 0xFF) / 255.0F;
        return new float[]{r, g, b};
    }
}
