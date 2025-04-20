package shop.xmz.lol.loratadine.utils.animations.impl;

import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;

public class EaseOutExpo extends Animation {
    public EaseOutExpo(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public EaseOutExpo(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        return Math.abs((float) x - 1.0F) < 1.0E-5F ? 1 : 1 - Math.pow(2, -10 * x);
    }
}
