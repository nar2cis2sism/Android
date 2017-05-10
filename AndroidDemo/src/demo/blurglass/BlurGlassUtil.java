package demo.blurglass;

public class BlurGlassUtil {
    
    public static final int MIN_BLUR_RADIUS = 1;
    public static final int MAX_BLUR_RADIUS = 20;
    public static final int DEFAULT_BLUR_RADIUS = 10;
    
    public static final int MIN_DOWNSAMPLING = 1;
    public static final int MAX_DOWNSAMPLING = 6;
    public static final int DEFAULT_DOWNSAMPLING = 5;
    
    public static boolean isBlurRadiusValid(float value) {
        return value >= MIN_BLUR_RADIUS && value <= MAX_BLUR_RADIUS;
    }
    
    public static boolean isDownSamplingValid(float value) {
        return value >= MIN_DOWNSAMPLING && value <= MAX_DOWNSAMPLING;
    }
}