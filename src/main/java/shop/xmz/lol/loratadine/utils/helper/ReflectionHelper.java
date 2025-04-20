package shop.xmz.lol.loratadine.utils.helper;

import java.io.Serial;
import java.lang.reflect.Field;
/*import java.lang.reflect.Method;*/

import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;

public class ReflectionHelper {
    public static Field findField(Class<?> clazz, String... fieldNames) {
        if (clazz == null || fieldNames == null || fieldNames.length == 0) {
            throw new IllegalArgumentException("Class and fieldNames must not be null or empty");
        }

        Exception failed = null;
        for (Class<?> currentClass = clazz; currentClass != null; currentClass = currentClass.getSuperclass()) {
            for (String fieldName : fieldNames) {
                if (fieldName == null) {
                    continue; // 跳过 null 的字段名
                }
                try {
                    Field f = currentClass.getDeclaredField(fieldName);
                    // Make private/protected fields accessible
                    f.setAccessible(true);

                    if ((f.getModifiers() & java.lang.reflect.Modifier.FINAL) != 0) {
                        // Remove final modifier (JDK 17+) using Unsafe
                        UnsafeUtils.putInt(f, UnsafeUtils.getArrayBooleanBaseOffset(), f.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
                    }

                    return f;
                } catch (Exception e) {
                    failed = e;
                }
            }
        }

        throw new UnableToFindFieldException(failed);
    }

    /*public static Method findMethod(Class<?> clazz, String obfName, String deobfName, Class<?>... parameterTypes) {
        if (clazz == null || (obfName == null && deobfName == null)) {
            throw new IllegalArgumentException("Class and at least one method name must not be null");
        }

        Exception failed = null;

        for (Class<?> currentClass = clazz; currentClass != null; currentClass = currentClass.getSuperclass()) {
            try {
                // 优先尝试使用混淆名查找方法
                Method m = null;
                if (obfName != null) {
                    try {
                        m = currentClass.getDeclaredMethod(obfName, parameterTypes);
                    } catch (NoSuchMethodException e) {
                        // 如果混淆名查找失败，捕获异常并尝试使用非混淆名
                    }
                }

                if (m == null && deobfName != null) {
                    m = currentClass.getDeclaredMethod(deobfName, parameterTypes);
                }

                if (m != null) {
                    // Make private/protected methods accessible
                    m.setAccessible(true);

                    // Remove final modifier (JDK 17+) using Unsafe
                    UnsafeUtils.putInt(m, Unsafe.ARRAY_BOOLEAN_BASE_OFFSET, m.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

                    return m;
                }
            } catch (Exception e) {
                failed = e;
            }
        }

        throw new UnableToFindMethodException(failed);
    }*/

    private static class UnableToFindFieldException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        public UnableToFindFieldException(Exception e) {
            super(e);
        }
    }

/*    private static class UnableToFindMethodException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        public UnableToFindMethodException(Exception e) {
            super(e);
        }
    }*/
}