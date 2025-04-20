package shop.xmz.lol.loratadine.ui.tabgui;

import org.lwjgl.glfw.GLFW;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

public class CheckKeyUtils {
    public static void checkKeyPress() {
        long window = mc.getWindow().getWindow();

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            TabGUI.onKeyPress(GLFW.GLFW_KEY_DOWN);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            TabGUI.onKeyPress(GLFW.GLFW_KEY_UP);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            TabGUI.onKeyPress(GLFW.GLFW_KEY_LEFT);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            TabGUI.onKeyPress(GLFW.GLFW_KEY_RIGHT);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
            TabGUI.onKeyPress(GLFW.GLFW_KEY_ENTER);
        }
    }
}
