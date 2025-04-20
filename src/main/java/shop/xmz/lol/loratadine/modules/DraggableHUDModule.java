package shop.xmz.lol.loratadine.modules;

import lombok.Getter;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;

/**
 * 可拖动HUD组件的基类
 * 提供通用的拖拽逻辑和位置管理功能
 */
public abstract class DraggableHUDModule extends Module {
    // 位置设置和实际位置
    protected final NumberSetting xPercentSetting, yPercentSetting;
    @Getter
    protected float x = 0, y = 0, originalX = 0, originalY = 0;

    // 直接缓存具体的像素位置，避免百分比转换导致的精度问题
    protected float cachedPixelX = 0, cachedPixelY = 0;
    protected boolean useDirectPosition = false;

    // 拖拽支持
    protected boolean dragging = false;
    protected float dragX, dragY;

    // 屏幕和位置状态
    protected int lastScreenWidth = 0, lastScreenHeight = 0;
    protected boolean needsPositionUpdate = true;
    protected boolean isBeingDragged = false;

    // 组件尺寸
    protected float width, height;

    @Getter
    protected float coordAlpha = 0f;     // 坐标显示透明度(0-1)

    @Getter
    protected float coordScale = 0.7f;   // 坐标显示缩放

    @Getter
    protected float currentCoordX = 0f;  // 当前坐标X位置
    protected float targetCoordX = 0f;   // 目标坐标X位置
    protected boolean isCoordOnLeft = false; // 坐标是否显示在左侧

    // 动画参数
    protected static final float FADE_SPEED = 0.08f;    // 透明度渐变速度
    protected static final float SCALE_SPEED = 0.03f;   // 缩放动画速度
    protected static final float POSITION_SPEED = 0.12f; // 位置动画速度

    /**
     * 构造函数
     */
    public DraggableHUDModule(String name, String description, float width, float height) {
        super(name, description, Category.RENDER);
        this.width = width;
        this.height = height;
        this.xPercentSetting = new NumberSetting("X Percent", this, 0, 0, 100, 1);
        this.yPercentSetting = new NumberSetting("Y Percent", this, 0, 0, 100, 1);
    }

    /**
     * 更新组件位置
     */
    protected void updatePosition() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 检查屏幕尺寸变化
        if (screenWidth != lastScreenWidth || screenHeight != lastScreenHeight) {
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;
            needsPositionUpdate = true;
            useDirectPosition = false; // 屏幕大小变化后重新计算
        }

        // 如果正在拖动或使用直接位置，使用缓存的像素位置
        if (isBeingDragged || useDirectPosition) {
            x = cachedPixelX;
            y = cachedPixelY;
        } else {
            // 否则使用百分比设置计算位置，使用double值保留精度
            float expectedX = (float)(screenWidth * (xPercentSetting.getValue().doubleValue() / 100.0));
            float expectedY = (float)(screenHeight * (yPercentSetting.getValue().doubleValue() / 100.0));

            // 如果需要更新位置
            if (needsPositionUpdate || Math.abs(expectedX - x) > 0.5f || Math.abs(expectedY - y) > 0.5f) {
                x = expectedX;
                y = expectedY;
                // 更新缓存的像素位置
                cachedPixelX = x;
                cachedPixelY = y;
                needsPositionUpdate = false;
            }
        }

        // 更新坐标显示动画
        updateCoordinateAnimations();
    }

    /**
     * 更新坐标显示的所有动画效果
     */
    private void updateCoordinateAnimations() {
        // 更新透明度和缩放
        if (dragging) {
            // 渐显效果
            coordAlpha = lerp(coordAlpha, 1.0f, FADE_SPEED);
            // 缩放增大效果
            coordScale = lerp(coordScale, 1.0f, SCALE_SPEED);
        } else {
            // 渐隐效果
            coordAlpha = lerp(coordAlpha, 0.0f, FADE_SPEED);
            // 缩放缩小效果
            coordScale = lerp(coordScale, 0.7f, SCALE_SPEED);
        }

        // 更新坐标显示位置
        float[] position = calculateCoordinatePosition();
        targetCoordX = position[0];

        // 根据显示位置判断是左侧还是右侧
        boolean shouldBeOnLeft = isCoordinateShouldBeOnLeft();

        // 如果位置发生改变，初始化动画
        if (shouldBeOnLeft != isCoordOnLeft) {
            isCoordOnLeft = shouldBeOnLeft;
        }

        // 平滑移动到目标位置
        currentCoordX = lerp(currentCoordX, targetCoordX, POSITION_SPEED);
    }

    /**
     * 线性插值函数，使动画更平滑
     */
    private float lerp(float start, float end, float amount) {
        return start + amount * (end - start);
    }

    /**
     * 判断坐标是否应该显示在左侧
     */
    private boolean isCoordinateShouldBeOnLeft() {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        // 如果靠近右边缘，显示在左侧
        return x + width + 70 > screenWidth;
    }

    /**
     * 根据像素位置更新百分比设置
     */
    protected void updatePercentFromPosition() {
        if (mc == null || mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 边界检查
        float safeX = Math.max(0, Math.min(x, screenWidth - width));
        float safeY = Math.max(0, Math.min(y, screenHeight - height));

        if (Math.abs(x - safeX) > 0.5f || Math.abs(y - safeY) > 0.5f) {
            x = safeX;
            y = safeY;
            cachedPixelX = x;
            cachedPixelY = y;
        }

        // 更新百分比设置，但保留直接像素位置
        xPercentSetting.setValue((x / screenWidth) * 100.0);
        yPercentSetting.setValue((y / screenHeight) * 100.0);
        useDirectPosition = true; // 标记使用直接像素位置
    }

    public void setX(float x) {
        this.x = x;
        this.cachedPixelX = x;
        useDirectPosition = true;
    }
    public void setY(float y) {
        this.y = y;
        this.cachedPixelY = y;
        useDirectPosition = true;
    }

    // 位置保存和恢复
    public void saveOriginalPosition() {
        originalX = x;
        originalY = y;
    }

    public void restoreOriginalPosition() {
        x = originalX;
        y = originalY;
        cachedPixelX = x;
        cachedPixelY = y;
        useDirectPosition = true;
    }

    public void applyPosition() { updatePercentFromPosition(); }

    /**
     * 计算坐标显示的理想位置
     */
    public float[] calculateCoordinatePosition() {
        float coordX, coordY;

        // 判断显示位置，避开屏幕边缘
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // 默认在右侧显示
        if (!isCoordinateShouldBeOnLeft()) {
            coordX = x + width + 5;
        } else {
            // 在左侧显示
            coordX = x - 65;
        }

        coordY = y + height / 2;

        return new float[]{coordX, coordY};
    }

    /**
     * 在CompactClickGUI中处理鼠标点击事件
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        boolean isOverComponent = isHovering(x, y, width, height, mouseX, mouseY);
        if (isOverComponent) {
            dragging = true;
            isBeingDragged = true;
            dragX = (float) mouseX - x;
            dragY = (float) mouseY - y;
            saveOriginalPosition();

            // 初始化坐标显示位置，避免闪烁
            if (currentCoordX == 0) {
                float[] pos = calculateCoordinatePosition();
                currentCoordX = pos[0];
                targetCoordX = pos[0];
            }

            return true;
        }
        return false;
    }

    /**
     * 在CompactClickGUI中处理鼠标释放事件
     */
    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            isBeingDragged = false;
            updatePercentFromPosition();
            return true;
        }
        return false;
    }

    /**
     * 在CompactClickGUI中处理鼠标拖动事件
     */
    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            // 计算新位置
            float newX = (float) (mouseX - dragX);
            float newY = (float) (mouseY - dragY);

            // 边界检查
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            newX = Math.max(0, Math.min(newX, screenWidth - width));
            newY = Math.max(0, Math.min(newY, screenHeight - height));

            this.x = newX;
            this.y = newY;
            // 更新缓存的像素位置
            this.cachedPixelX = newX;
            this.cachedPixelY = newY;
            needsPositionUpdate = false;
            useDirectPosition = true;

            return true;
        }
        return false;
    }

    /**
     * 检查是否应该绘制拖动高亮
     */
    public boolean shouldRenderDragHighlight() {
        return dragging;
    }

    /**
     * 检查鼠标是否在特定区域上方
     */
    protected boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}