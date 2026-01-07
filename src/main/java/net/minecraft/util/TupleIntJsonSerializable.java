package net.minecraft.util;

public class TupleIntJsonSerializable {
    private int integerValue;
    private IJsonSerializable jsonSerializableValue;

    public int getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(int integerValueIn) {
        integerValue = integerValueIn;
    }

    public <T extends IJsonSerializable> T getJsonSerializableValue() {
        return (T) jsonSerializableValue;
    }

    public void setJsonSerializableValue(IJsonSerializable jsonSerializableValueIn) {
        jsonSerializableValue = jsonSerializableValueIn;
    }
}
