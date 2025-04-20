package shop.xmz.lol.loratadine.utils.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

public class StencilUtil implements Wrapper {
    /**
     * Disables the stencil buffer and resets render states.
     */
    public static void dispose() {
        RenderSystem.assertOnRenderThread();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.disableBlend();
    }

    /**
     * Sets the stencil function for erasing parts of the stencil buffer.
     *
     * @param invert Whether to invert the stencil function.
     */
    public static void erase(boolean invert) {
        RenderSystem.stencilFunc(invert ? GL11.GL_EQUAL : GL11.GL_NOTEQUAL, 1, 0xFFFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /**
     * Prepares the stencil buffer for writing.
     *
     * @param renderClipLayer Whether to render the clip layer.
     */
    public static void write(boolean renderClipLayer) {
        checkSetupFBO();
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        RenderSystem.assertOnRenderThread();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFFFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        if (!renderClipLayer) {
            RenderSystem.colorMask(false, false, false, false);
        }
    }

    /**
     * Prepares the stencil buffer for writing with a specific framebuffer.
     *
     * @param fb The framebuffer to write to.
     */
    public static void write(boolean renderClipLayer, RenderTarget fb) {
        checkSetupFBO(fb);
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        RenderSystem.assertOnRenderThread();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFFFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        if (!renderClipLayer) {
            RenderSystem.colorMask(false, false, false, false);
        }
    }

    /**
     * Ensures the framebuffer is properly set up for stencil operations.
     */
    public static void checkSetupFBO() {
        RenderTarget fbo = mc.getMainRenderTarget();
        if (fbo != null && fbo.isStencilEnabled()) {
            setupFBO(fbo);
            WrapperUtils.setDepthBufferId(fbo, -1);
        }
    }

    /**
     * Ensures the given framebuffer is properly set up for stencil operations.
     *
     * @param fbo The framebuffer to set up.
     */
    public static void checkSetupFBO(RenderTarget fbo) {
        if (fbo != null && fbo.isStencilEnabled()) {
            setupFBO(fbo);
            WrapperUtils.setDepthBufferId(fbo, -1);
        }
    }

    /**
     * Configures the given framebuffer for stencil operations.
     *
     * @param framebuffer The framebuffer to configure.
     */
    public static void setupFBO(RenderTarget framebuffer) {
        framebuffer.enableStencil();
    }

    /**
     * Initializes the stencil buffer for writing.
     */
    public static void initStencilToWrite() {
        RenderTarget framebuffer = mc.getMainRenderTarget();
        framebuffer.bindWrite(false);
        checkSetupFBO(framebuffer);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);

        RenderSystem.assertOnRenderThread();
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 1);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        RenderSystem.colorMask(false, false, false, false);
    }

    /**
     * Reads the stencil buffer for rendering.
     *
     * @param ref The stencil reference value (usually 1).
     */
    public static void readStencilBuffer(int ref) {
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, ref, 1);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    /**
     * Disables the stencil buffer.
     */
    public static void uninitStencilBuffer() {
        RenderSystem.assertOnRenderThread();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}