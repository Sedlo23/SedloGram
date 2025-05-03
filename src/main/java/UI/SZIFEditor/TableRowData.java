package UI.SZIFEditor;


import java.util.Arrays;

/**
 * Model class for table row data
 */
class TableRowData {
    private String[] values;

    public TableRowData() {
        values = new String[EditorTable.COLUMN_NAMES.length];
        Arrays.fill(values, "");
    }

    public void setValue(int index, String value) {
        if (index >= 0 && index < values.length) {
            values[index] = value != null ? value : "";
        }
    }

    public String getValue(int index) {
        if (index >= 0 && index < values.length) {
            return values[index];
        }
        return "";
    }

    public String[] getAllValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Row Data: ");
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && !values[i].isEmpty()) {
                sb.append(EditorTable.COLUMN_NAMES[i]).append("=").append(values[i]).append(", ");
            }
        }
        return sb.toString();
    }
}