package shop.xmz.lol.loratadine.event.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method as an event handling method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {
    /**
     * The priority value of the event handling method. Methods with lower values will be executed first.
     *
     * @return The priority value, with a default of 10 if not specified.
     */
    int value() default 10;
}
