package shop.xmz.lol.loratadine.utils;

import shop.xmz.lol.loratadine.Loratadine;

public class NativeUtils {
    static {
        System.load(Loratadine.INSTANCE.getResourcesManager().resources.getAbsolutePath() + "\\native_utils.dll");
    }

    public static native void redefineClasses(ClassLoader classLoader, Class<?> targetClass, byte[] newClassBytes, boolean printError, boolean printSuccessful);
    public static native byte[] getClassesBytes(Class<?> clazz);

    public static void redefineClassesNoSuccessfulPrint(Class<?> targetClass, byte[] newClassBytes) {
        ClassLoader classLoader = targetClass.getClassLoader();
        redefineClasses(classLoader, targetClass, newClassBytes, true, false);
    }

    public static void redefineClasses(Class<?> targetClass, byte[] newClassBytes) {
        ClassLoader classLoader = targetClass.getClassLoader();
        redefineClasses(classLoader, targetClass, newClassBytes, true, true);
    }
}