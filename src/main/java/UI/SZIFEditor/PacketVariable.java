package UI.SZIFEditor;

/**
 * Model class for variables within packets
 */
class PacketVariable {
    private String name;
    private int size;
    private String value;
    boolean forceShow;

    public PacketVariable(String name, int size, String value, boolean forceShow) {
        this.name = name;
        this.size = size;
        this.value = value;
        this.forceShow = forceShow;
    }

    // Constructor that includes a friendly name for template values
    public PacketVariable(String name, int size, String friendlyName, int actualValue) {
        this.name = name;
        this.size = actualValue; // Store the actual value in the size field for templates
        this.value = friendlyName; // Store the friendly name in the value field
        this.forceShow = false;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getValue() {
        return value;
    }

    public String getBinary() {
        if (value.contains("F")) {
            return BinaryConverter.hexToBinary(value);
        } else {
            return BinaryConverter.decimalToBinary(value, size);
        }
    }
}