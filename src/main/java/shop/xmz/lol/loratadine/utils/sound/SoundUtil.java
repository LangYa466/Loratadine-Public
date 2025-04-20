package shop.xmz.lol.loratadine.utils.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundUtil {
    public static void simplePlaySound(String filePath, float volume) {
        new Thread(() -> {
            try {
                File soundFile = new File(filePath);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                // 设置音量
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);

                clip.start();

                // 等待音频播放完成
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

