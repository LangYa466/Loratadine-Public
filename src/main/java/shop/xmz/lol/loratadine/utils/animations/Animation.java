package shop.xmz.lol.loratadine.utils.animations;

import lombok.Getter;
import lombok.Setter;

public abstract class Animation {
    public AnimTimeUtil timerUtil = new AnimTimeUtil();
    @Setter
    protected int duration;
    @Setter
    @Getter
    protected double endPoint;
    @Getter
    protected Direction direction;

    public Animation(int ms, double endPoint) {
        this(ms, endPoint, Direction.FORWARDS);
    }

    public Animation(int ms, double endPoint, Direction direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }

    public static float animate(float target, float current, float speed) {
        if (current == target) return current;

        boolean larger = target > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }

        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }

        if (larger) {
            current += (float) factor;
            if (current >= target) current = target;
        } else if (target < current) {
            current -= (float) factor;
            if (current <= target) current = target;
        }

        return current;
    }

    public boolean finished(Direction direction) {
        return this.isDone() && this.direction.equals(direction);
    }

    public double getLinearOutput() {
        return 1.0 - (double) this.timerUtil.getTime() / (double) this.duration * this.endPoint;
    }

    public void reset() {
        this.timerUtil.reset();
    }

    public boolean isDone() {
        return this.timerUtil.hasTimeElapsed(this.duration);
    }

    public void changeDirection() {
        this.setDirection(this.direction.opposite());
    }

    public void setState(boolean sb) {
        if (sb) {
            this.setDirection(Direction.FORWARDS);
        } else {
            this.setDirection(Direction.BACKWARDS);
        }
    }

    public boolean isState() {
        return this.direction.forwards();
    }

    public boolean isDown() {
        return this.direction.backwards();
    }

    public Animation setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            this.timerUtil.setTime(System.currentTimeMillis() - ((long) this.duration - Math.min(this.duration, this.timerUtil.getTime())));
        }
        return this;
    }

    protected boolean correctOutput() {
        return false;
    }

    public double getOutput() {
        if (this.direction.forwards()) {
            if (this.isDone()) {
                return this.endPoint;
            }
            return this.getEquation((double) this.timerUtil.getTime() / (double) this.duration) * this.endPoint;
        }
        if (this.isDone()) {
            return 0.0;
        }
        if (this.correctOutput()) {
            double revTime = Math.min(this.duration, Math.max(0L, (long) this.duration - this.timerUtil.getTime()));
            return this.getEquation(revTime / (double) this.duration) * this.endPoint;
        }
        return (1.0 - this.getEquation((double) this.timerUtil.getTime() / (double) this.duration)) * this.endPoint;
    }

    protected abstract double getEquation(double var1);
}
