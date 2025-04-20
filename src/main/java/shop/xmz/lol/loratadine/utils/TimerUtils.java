package shop.xmz.lol.loratadine.utils;


public class TimerUtils {

	public long lastMS = System.currentTimeMillis();

	public TimerUtils() {
		reset();
	}

	public void reset() {
		lastMS = System.currentTimeMillis();
	}

	public void resetMS() {
		this.lastMS = System.nanoTime();
	}


	public boolean hasTimeElapsed(long time, boolean reset) {
		if (System.currentTimeMillis() - lastMS > time) {
			if (reset) reset();
			return true;
		}

		return false;
	}

	public static long randomClickDelay(final int minCPS, final int maxCPS) {
		return (long) ((Math.random() * ((double) 1000 / minCPS - (double) 1000 / maxCPS + 1)) + (double) 1000 / maxCPS);
	}

	public boolean delay(long time) {
		return System.currentTimeMillis() - lastMS >= time;
	}

	public boolean passedS(double s) {
		return getMs(System.nanoTime() - lastMS) >= (long) (s * 1000.0);
	}

	public boolean passedMs(long ms) {
		return getMs(System.nanoTime() - lastMS) >= ms;
	}


	public boolean hasTimeElapsed(long time) {
		return System.currentTimeMillis() - lastMS > time;
	}

	public boolean hasTimeElapsed(double time) {
		return hasTimeElapsed((long) time);
	}

	public long getTime() {
		return System.currentTimeMillis() - lastMS;
	}

	public void setTime(long time) {
		lastMS = time;
	}

	public long getPassedTimeMs() {
		return getMs(System.nanoTime() - lastMS);
	}

	public long getMs(long time) {
		return time / 1000000L;
	}
}