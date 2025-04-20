package shop.xmz.lol.loratadine.utils.math;


import lombok.experimental.UtilityClass;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;

@UtilityClass
public class MathUtils {
    public static final float PI = (float) Math.PI;

    public static boolean approximatelyEquals(float a, float b) {
        return Math.abs(b - a) < 1.0E-5F;
    }

    public double roundToPlace(double value, int place){
        if (place < 0) return value;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(place, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double clamp_double(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }

    public static int clamp_int(int num, int min, int max) {
        return num < min ? min : (Math.min(num, max));
    }

    public static double getRandomInRange(double min, double max) {
        SecureRandom random = new SecureRandom();
        return min == max ? min : random.nextDouble() * (max - min) + min;
    }

    public static int getRandomNumber(int n1, int n2) {
        if (n1 == n2) return n1;
        int max = Math.max(n1, n2);
        int min = Math.min(n1, n2);
        return getRandomNumberUsingNextInt(min, max);
    }

    public float wrapAngleTo180(float angle) {
        angle %= 360.0F; // 将角度限制在 [-360, 360] 范围内
        if (angle >= 180.0F) {
            angle -= 360.0F; // 将大于180度的角度转换为负数
        }
        if (angle < -180.0F) {
            angle += 360.0F; // 将小于-180度的角度转换为正数
        }
        return angle;
    }

    public static int getRandomNumberUsingNextInt(int min, int max) {
        java.util.Random random = new java.util.Random();
        return random.nextInt(max - min) + min;
    }

    public static float lerp(float min, float max, float delta) {
        return min + (max - min) * delta;
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, interpolationValue).intValue();
    }

    public static float getAdvancedRandom(float min, float max) {
        SecureRandom random = new SecureRandom();

        long finalSeed = System.nanoTime();

        for (int i = 0; i < 3; ++i) {
            long seed = (long) (Math.random() * 1_000_000_000);

            seed ^= (seed << 13);
            seed ^= (seed >>> 17);
            seed ^= (seed << 15);

            finalSeed += seed;
        }

        random.setSeed(finalSeed);

        return random.nextFloat() * (max - min) + min;
    }

    public static float getRandomFloat(float max, float min) {
        SecureRandom random = new SecureRandom();
        return random.nextFloat() * (max - min) + min;
    }

    public static boolean equals(float a, float b) {
        return Math.abs(a - b) < 10E-5;
    }
}
