package shop.xmz.lol.loratadine.ui.notification;

import lombok.Getter;

import java.awt.*;

@Getter
public enum NotificationType {
    SUCCESS(new Color(0, 255, 0), "A"),
    DISABLE(new Color(255, 0, 0), "B"),
    WARNING(Color.YELLOW, "C"),
    INFO(Color.GRAY, "D");
    private final Color color;
    private final String icon;

    NotificationType(Color color, String icon) {
        this.color = color;
        this.icon = icon;
    }
}
