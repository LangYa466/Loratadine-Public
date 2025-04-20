package shop.xmz.lol.loratadine.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author DSJ_
 * @description 我只是喜欢SKID
 * @description 算了，烧鸡天天开心
 * @description -----------云边有个大烧鸡
 */
public class CharUtils implements Wrapper {
    // 静态缓存当前动画状态
    private static final float[] moveY = new float[20];
    private static final float[] moveX = new float[20];
    private final DecimalFormat deFormat = new DecimalFormat("##0.00", new DecimalFormatSymbols(Locale.ENGLISH));
    private final List<String> numberList = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".");
    private static boolean initialized = false;

    /**
     * 构造函数
     */
    public CharUtils() {
        if (!initialized) {
            for (int i = 0; i <= 19; i++) {
                moveX[i] = 0F;
                moveY[i] = 0F;
            }
            initialized = true;
        }
    }

    /**
     * 绘制数字字符
     */
    public void renderChar(PoseStack poseStack, float number, float orgX, float orgY, float initX, float initY, float scaleX, float scaleY, boolean shadow, float fontSpeed, int color) {
        // 格式化数字为字符串
        String reFormat = deFormat.format(number);
        FontManager fontManager = Loratadine.INSTANCE.getFontManager();
        TrueTypeFont fontRend =fontManager.zw30;
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // 检查字体是否正确加载
        if (fontRend == null) {
            return;
        }

        float delta = 10;
        int indexY = 0;
        float animX = 0F;

        float cutY = initY + fontRend.getHeight() * ((float) 3 / 4);
        int guiScale = (int)mc.getWindow().getGuiScale();
        int scissorX = (int)(orgX * guiScale);
        int scissorY = (int)((mc.getWindow().getGuiScaledHeight() - (orgY + cutY)) * guiScale);
        int scissorWidth = screenWidth * guiScale;
        int scissorHeight = (int)((cutY - (initY - 4F * scaleY)) * guiScale);

        if (scissorWidth > 0 && scissorHeight > 0) {
            // 玉面剪刀王 开！
            RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        } else {
            fontRend.drawString(poseStack, reFormat, initX, initY, color, shadow);
            fontRend.getStringWidth(reFormat);
            return;
        }

        char[] chars = reFormat.toCharArray();
        for (int indexX = 0; indexX < chars.length; indexX++) {
            char c = chars[indexX];

            if (moveX[indexX] == 0 && animX > 0) {
                moveX[indexX] = animX;
            }

            float speed = Math.max(fontSpeed * 0.025F * delta, 0.5f);
            moveX[indexX] = Animation.animate(animX, moveX[indexX], speed);
            animX = moveX[indexX];

            String charStr = String.valueOf(c);
            int pos = numberList.indexOf(charStr);

            if (pos >= 0) {
                float expectAnim = (fontRend.getHeight() + 2F) * pos;

                if (moveY[indexY] == 0 && pos > 0) {
                    moveY[indexY] = expectAnim * 0.5f; // 从半路开始动画
                }

                float ySpeed = Math.max(fontSpeed * 0.02F * delta, 0.01f);
                moveY[indexY] = Animation.animate(expectAnim, moveY[indexY], ySpeed);

                poseStack.pushPose();
                poseStack.translate(0F, initY - moveY[indexY], 0F);

                // 绘制当前数字以及邻近数字
                for (int index = Math.max(0, pos - 3); index < Math.min(numberList.size(), pos + 4); index++) {
                    String num = numberList.get(index);
                    float height = (fontRend.getHeight() + 2F) * index;

                    // 绘制
                    fontRend.drawString(poseStack,
                            num,
                            initX + moveX[indexX],
                            height,
                            color,
                            shadow
                    );
                }
                poseStack.popPose();
            } else {
                moveY[indexY] = 0F;
                fontRend.drawString(poseStack, charStr, initX + moveX[indexX], initY, color, shadow);
            }
            animX += fontRend.getStringWidth(charStr);
            indexY++;
        }

        // 玉面剪刀王 结束！
        RenderSystem.disableScissor();
    }
}