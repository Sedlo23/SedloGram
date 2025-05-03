package UI.SZIFEditor;

/**
 * Model class for telegram data
 */
class TelegramData {
    private String encodedData;
    private String name;

    public TelegramData( String encodedData, String name) {
        this.encodedData = encodedData;
        this.name = name;
    }

    public String getEncodedData() {
        return encodedData;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {

            return name;

    }
}
