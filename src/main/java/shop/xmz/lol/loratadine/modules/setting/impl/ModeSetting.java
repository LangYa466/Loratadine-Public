package shop.xmz.lol.loratadine.modules.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.Setting;

import java.util.Objects;

@Getter
public class ModeSetting extends Setting<String> {
    private final String[] values;

    public ModeSetting(String name, Module present, String[] values, String value) {
        super(name, present, value);
        this.values = values;
    }

    public boolean is(String mode) {
        return Objects.equals(this.value, mode);
    }

    @Override
    public void toJson(JsonObject object) {
        object.addProperty(this.getName(), this.getValue());
    }

    @Override
    public void formJson(JsonElement element) {
        this.setValue(element.getAsString());
    }
}
