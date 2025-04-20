package shop.xmz.lol.loratadine.modules.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import shop.xmz.lol.loratadine.modules.Module;

import java.util.function.Supplier;

@Getter
public abstract class Setting<T> {
    protected final String name;
    protected Module present;
    @Setter
    protected T value;

    // 新增可见性条件（默认为可见）
    protected Supplier<Boolean> visibilityCondition = () -> true;

    public Setting(String name, Module present, T value) {
        this.name = name;
        this.present = present;
        present.getSettings().add(this);
        this.value = value;
    }

    // 新增可见性判断方法
    public boolean shouldRender() {
        return visibilityCondition.get();
    }

    // 链式调用设置可见性条件
    public Setting<T> setVisibility(Supplier<Boolean> condition) {
        this.visibilityCondition = condition;
        return this;
    }

    public abstract void toJson(JsonObject object);
    public abstract void formJson(JsonElement element);
}