package UI.SZIFEditor;



import java.util.Arrays;
import java.util.Optional;

/**
 * Service for generating telegrams
 */
class TelegramGenerator {

    public TelegramGenerator() {
    }

    public String generateBinary(TableRowData row, PacketRepository repository) {
        StringBuilder binary = new StringBuilder();

        // Add header information (bits 0-47)
        binary.append("1"); // Start bit
        binary.append(BinaryConverter.decimalToBinary(row.getValue(0), 7)); // M_VERSION
        binary.append("0"); // Fixed bit
        binary.append(BinaryConverter.decimalToBinary(row.getValue(1), 3)); // N_PIG
        binary.append(BinaryConverter.decimalToBinary(row.getValue(2), 3)); // N_TOTAL
        binary.append(BinaryConverter.decimalToBinary(row.getValue(3), 2)); // M_DUP
        binary.append(BinaryConverter.decimalToBinary(row.getValue(4), 8)); // M_MCOUNT
        binary.append(BinaryConverter.decimalToBinary(row.getValue(5), 10)); // NID_C
        binary.append(BinaryConverter.decimalToBinary(row.getValue(6), 14)); // NID_BG
        binary.append(BinaryConverter.decimalToBinary(row.getValue(7), 1)); // Q_LINK

        // Add packet content (columns 8-16)
        for (int col = 8; col < 17; col++) {
            String packetStr = row.getValue(col);
            if (packetStr == null || packetStr.isEmpty()) {
                continue;
            }

            // Parse packet string
            int bracketIndex = packetStr.indexOf('[');
            if (bracketIndex <= 0) {
                continue;
            }

            String packetName = packetStr.substring(0, bracketIndex);

            // Find packet in repository
            Optional<PacketTemplate> packetOpt = repository.findByName(packetName);
            if (!packetOpt.isPresent()) {
                continue;
            }

            PacketTemplate packetTemplate = packetOpt.get();

            // Extract parameters
            String[] params = new String[10];
            Arrays.fill(params, "0");

            int startIndex = packetStr.indexOf('[');
            int endIndex = packetStr.indexOf(']');

            if (startIndex >= 0 && endIndex > startIndex) {
                String paramsPart = packetStr.substring(startIndex + 1, endIndex);
                String[] paramsArray = paramsPart.split(";");

                for (int i = 0; i < Math.min(paramsArray.length, params.length); i++) {
                    params[i] = paramsArray[i];
                }
            }

            // Process variables
            // In TelegramGenerator.generateBinary method
// Process variables
            // In TelegramGenerator class
// Process variables
            for (PacketVariable var : packetTemplate.getVariables()) {
                if (var.getValue().startsWith("-")) {
                    // Parameter reference
                    int paramIndex = Math.abs(Integer.parseInt(var.getValue())) - 1;
                    if (paramIndex >= 0 && paramIndex < params.length) {
                        String paramValue = params[paramIndex];

                        // Check if it's a template reference
                        if (paramValue != null && paramValue.startsWith("TEMPLATE:")) {
                            // Format is TEMPLATE:TemplateName:VariableName
                            String[] parts = paramValue.split(":", 3);
                            if (parts.length == 3) {
                                String templateName = parts[1];
                                String variableName = parts[2];

                                // Look up the template variable
                                boolean found = false;
                                for (PacketTemplate template : repository.getTemplatePackets()) {
                                    if (template.getName().equals(templateName)) {
                                        for (PacketVariable templateVar : template.getVariables()) {
                                            if (templateVar.getName().equals(var.getName()) &&
                                                    templateVar.getValue().equals(variableName)) {
                                                // Use the template variable's size value
                                                binary.append(BinaryConverter.decimalToBinary(
                                                        String.valueOf(templateVar.getSize()), var.getSize()));
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (found) break;
                                    }
                                }

                                if (!found) {
                                    // Default to zero if template not found
                                    binary.append(BinaryConverter.decimalToBinary("0", var.getSize()));
                                }
                            } else {
                                binary.append(BinaryConverter.decimalToBinary("0", var.getSize()));
                            }
                        } else if (isNumeric(paramValue)) {
                            binary.append(BinaryConverter.decimalToBinary(paramValue, var.getSize()));
                        } else {
                            binary.append(BinaryConverter.decimalToBinary("0", var.getSize()));
                        }
                    }
                } else {
                    // Fixed value
                    binary.append(var.getBinary());
                }
            }
        }

        // Pad with 1s to ensure proper length
        while (binary.length() < 830) {
            binary.append("1");
        }

        return binary.toString();
    }

    public String encodeTelegram(String binaryString) {
        // Convert to hex for storage
        return BinaryConverter.binaryToHex(binaryString);
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}