package shop.xmz.lol.loratadine.utils.animations.impl;

import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;

public class DecelerateAnimation extends Animation {
    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public DecelerateAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    @Override
    protected double getEquation(double x2) {
        return 1.0 - (x2 - 1.0) * (x2 - 1.0);
    }
}

