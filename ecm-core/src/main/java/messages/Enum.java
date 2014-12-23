package messages;
public class Enum {
    protected byte _enumValue;
    protected Enum(byte enumValue) {
        this._enumValue = enumValue;
    }

    public byte Value() {
        return this._enumValue;
    }
}