package shop.xmz.lol.loratadine.modules.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.Setting;

@Getter
public class NumberSetting extends Setting<Number> {
    private final Number maxValue;
    private final Number minValue;
    private final Number step;

    public NumberSetting(String name, Module module, Number value, Number minValue, Number maxValue, Number step) {
        super(name, module, value);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.step = step;

        // 确保初始值也符合step规则
        this.setValue(value);
    }

    /**
     * 设置数值并确保它符合step的规则
     * @param value 要设置的值
     */
    @Override
    public void setValue(Number value) {
        // 确保值在最小值和最大值之间
        double doubleValue = value.doubleValue();
        double min = minValue.doubleValue();
        double max = maxValue.doubleValue();

        doubleValue = Math.max(min, Math.min(max, doubleValue));

        double stepValue = step.doubleValue();

        if (stepValue > 0) {
            double deltaFromMin = doubleValue - min;

            int stepCount = (int) Math.round(deltaFromMin / stepValue);

            doubleValue = min + (stepCount * stepValue);
            doubleValue = Math.max(min, Math.min(max, doubleValue));
        }

        Number newValue;
        if (value instanceof Double) {
            newValue = doubleValue;
        } else if (value instanceof Float) {
            newValue = (float) doubleValue;
        } else if (value instanceof Long) {
            newValue = (long) doubleValue;
        } else if (value instanceof Integer) {
            newValue = (int) doubleValue;
        } else if (value instanceof Short) {
            newValue = (short) doubleValue;
        } else if (value instanceof Byte) {
            newValue = (byte) doubleValue;
        } else {
            newValue = doubleValue;
        }

        // 最后设置值
        super.setValue(newValue);
    }

    public void increment() {
        setValue(getNextValue());
    }

    public void decrement() {
        setValue(getPreviousValue());
    }

    public Number getNextValue() {
        double current = getValue().doubleValue();
        double stepValue = step.doubleValue();
        double next = current + stepValue;

        // 确保不超过最大值
        if (next > maxValue.doubleValue()) {
            return maxValue;
        }
        return getTypeConverted(next);
    }

    /**
     * 获取上一个值（减少一个step）
     */
    public Number getPreviousValue() {
        double current = getValue().doubleValue();
        double stepValue = step.doubleValue();
        double previous = current - stepValue;

        // 确保不低于最小值
        if (previous < minValue.doubleValue()) {
            return minValue;
        }
        return getTypeConverted(previous);
    }

    /**
     * 将double值转换为与当前设置相同类型的Number
     */
    private Number getTypeConverted(double value) {
        Number original = getValue();
        if (original instanceof Double) {
            return value;
        } else if (original instanceof Float) {
            return (float) value;
        } else if (original instanceof Long) {
            return (long) value;
        } else if (original instanceof Integer) {
            return (int) value;
        } else if (original instanceof Short) {
            return (short) value;
        } else if (original instanceof Byte) {
            return (byte) value;
        } else {
            return value;
        }
    }

    @Override
    public void toJson(JsonObject object) {
        Number value = this.getValue();
        if (value instanceof Double || value instanceof Float) {
            if (value.doubleValue() == value.longValue()) {
                object.addProperty(this.getName(), value.longValue());
            } else {
                object.addProperty(this.getName(), value);
            }
        } else {
            object.addProperty(this.getName(), value);
        }
    }

    @Override
    public void formJson(JsonElement element) {
        Number number = element.getAsNumber();
        this.setValue(number); // 自动应用step规则
    }
}