package shop.xmz.lol.loratadine.modules.impl.hud;

import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.ClientUtils;

import java.awt.*;

import static shop.xmz.lol.loratadine.utils.render.RenderUtils.renderSpectrumBar;

/**
 * @author DSJ_
 * @since 13/2/2025
 */
public final class SpectrumBar extends Module {
    private final BooleanSetting positionLeft = new BooleanSetting("Left", this, true); // 新增设置，默认在左边
    private final NumberSetting width = new NumberSetting("Width",this,20,20,400,10);
    private final NumberSetting high = new NumberSetting("High",this,50,50,400,10);
    private final NumberSetting speed = new NumberSetting("Speed",this,100,100,500,10);

    private long lastUpdateTime = System.currentTimeMillis();
    private float[] lastSpectrumData = null;
    private float[] currentSpectrumData = null;

    private float[] getSpectrumData() {
        int dataLength = width.getValue().intValue();
        float[] data = new float[dataLength];
        long currentTime = System.currentTimeMillis();
        // 使用speed setting来控制动画进度
        float animationProgress = Math.min(1.0f, (currentTime - lastUpdateTime) / speed.getValue().floatValue());

        // 初始化数据
        if (lastSpectrumData == null || currentSpectrumData == null ||
                lastSpectrumData.length != dataLength || currentSpectrumData.length != dataLength) {
            lastSpectrumData = new float[dataLength];
            currentSpectrumData = new float[dataLength];
            for (int i = 0; i < dataLength; i++) {
                lastSpectrumData[i] = 0;
                currentSpectrumData[i] = generateSmoothValue(i, currentTime);
            }
        }

        // 使用speed setting来控制更新间隔
        if (currentTime - lastUpdateTime >= speed.getValue().floatValue()) {
            lastSpectrumData = currentSpectrumData.clone();
            currentSpectrumData = new float[dataLength];
            for (int i = 0; i < dataLength; i++) {
                currentSpectrumData[i] = generateSmoothValue(i, currentTime);
            }
            lastUpdateTime = currentTime;
            animationProgress = 0;
        }

        // 其余代码保持不变
        for (int i = 0; i < dataLength; i++) {
            float easedProgress = easeInOutCubic(animationProgress);
            float interpolatedValue = lerp(lastSpectrumData[i], currentSpectrumData[i], easedProgress);
            float wave = (float) (Math.sin(currentTime * 0.003 + i * 0.2) * 0.03);
            data[i] = Math.max(0, Math.min(1, interpolatedValue + wave));
        }

        return data;
    }

    // 生成平滑的随机值
    private float generateSmoothValue(int index, long time) {
        // 使用正弦函数创造波浪效果
        float baseValue = (float) Math.random() * 0.6f + 0.2f;
        float smooth = (float) (Math.sin(index * 0.1 + time * 0.001) * 0.1);
        return Math.max(0, Math.min(1, baseValue + smooth));
    }

    // 线性插值
    private float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    // 缓动函数，使动画更加自然
    private float easeInOutCubic(float x) {
        return x < 0.5 ? 4 * x * x * x : 1 - (float)Math.pow(-2 * x + 2, 3) / 2;
    }


    public SpectrumBar() {
        super("SpectrumBar", "频谱条", Category.RENDER);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        float[] spectrumData = getSpectrumData();
        Color baseColor = new Color(255, 255, 255, 128); // 假设基础颜色
        // 动态计算参数
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int inventoryX = (screenWidth - 182) / 2;

        float x = positionLeft.getValue() ? 0 : (screenWidth + 182) / 2; // 根据设置选择位置
        float width = Math.min(inventoryX - x - 5, 1000); // 限制最大宽度
        float height = high.getValue().floatValue();
        float y = screenHeight - high.getValue().floatValue();

        renderSpectrumBar(event.poseStack(),  x, y, width, height, spectrumData, baseColor);
    }

    @Override
    public void onEnable() {
        ClientUtils.log("SpectrumBar 已启动，正在捕获系统音频...");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ClientUtils.log("SpectrumBar 已禁用，停止音频捕获...");
        super.onDisable();
    }
}