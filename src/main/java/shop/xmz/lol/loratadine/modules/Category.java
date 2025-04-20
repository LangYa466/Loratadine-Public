package shop.xmz.lol.loratadine.modules;

public enum Category {
    COMBAT("Combat","战斗类"),
    MOVEMENT("Movement","移动类"),
    PLAYER("Player","玩家类"),
    RENDER("Render","渲染类"),
    MISC("Misc","杂项类"),
    SETTING("Setting","设置");

    public final String name;
    public final String cnName;

    Category(String name, String cnName) {
        this.name = name;
        this.cnName = cnName;
    }
}
