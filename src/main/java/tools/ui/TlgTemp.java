package tools.ui;

import packets.Interfaces.IPacket;
import packets.TrackToTrain.PH;
import tools.crypto.ArithmeticalFunctions;
import tools.crypto.CalculatorMD4;
import tools.string.StringHelper;

import javax.swing.*;
import java.security.MessageDigest;

import static tools.ui.GUIHelper.loadTelegram;
import static tools.string.StringHelper.*;

/**
 * Represents a temporary telegram data holder.
 * <p>
 * This class stores a telegram string and its name, and automatically loads the telegram
 * into a list model of packets upon construction. The {@code getTlg()} method rebuilds
 * a binary representation from all loaded packets, while {@code toString()} produces a
 * formatted string based on the header packet (PH).
 */
public class TlgTemp {
    private String name;
    private String tlg;

    /**
     * An extra telegram string that, if set, triggers MD4 digest computation.
     */
    public String entlg = "";

    /**
     * The list model that holds all {@link IPacket} objects created from this telegram.
     */
    public DefaultListModel<IPacket> defaultListModel = new DefaultListModel<>();

    /**
     * Constructs a new telegram temporary holder.
     *
     * @param name the name of the telegram
     * @param tlg  the telegram data in hex format
     */
    public TlgTemp(String name, String tlg) {
        this.name = name;
        this.tlg = tlg;
        // Convert the telegram from hex to binary and load packets into the model.
        String[] ss = new String[]{ArithmeticalFunctions.hex2Bin(tlg)};
        loadTelegram(defaultListModel, ss);
    }

    /**
     * Returns the name of this telegram.
     *
     * @return the telegram name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the telegram name.
     *
     * @param name the new name for this telegram
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Concatenates and returns the binary data of all packets stored in the model.
     *
     * @return the concatenated binary data as a string
     */
    public String getTlg() {
        String s = "";
        for (int i = 0; i < defaultListModel.getSize(); i++) {
            IPacket packet = defaultListModel.get(i);

        }

        for (int i =0;i<defaultListModel.getSize(); i++)
        {
            s+= defaultListModel.get(i).getBinData();

        }

        int targetLength = (s.length() <= 210) ? 210 : 830;

        StringBuilder sb = new StringBuilder(s);
        // Append '1' until the string reaches the target length
        while (sb.length() < targetLength) {
            sb.append('1');
        }
        sb.append('0');
        sb.append('0');
        s= sb.toString();

        return sb.toString();
    }

    /**
     * Sets the telegram data.
     *
     * @param tlg the new telegram data
     */
    public void setTlg(String tlg) {
        this.tlg = tlg;
    }

    /**
     * Returns a formatted string representation of this telegram based on the header (PH).
     * <p>
     * This method extracts key fields from the PH packet and formats them as a string.
     * Additionally, if the {@code entlg} field is not empty, an MD4 digest is computed.
     *
     * @return a formatted string representation of the telegram
     */
    @Override
    public String toString() {
        // Assumes the first packet in the model is a PH instance.
        PH ph = (PH) defaultListModel.get(0);

        // Build display name from PH fields.
        String displayName =
                StringHelper.padLeft(String.valueOf(ph.getNid_c().getDecValue()), 3, '0') + "_" +
                        StringHelper.padLeft(String.valueOf(ph.getNid_bg().getDecValue()), 5, '0') + "_" +
                        StringHelper.padLeft(String.valueOf(ph.getN_pig().getDecValue()), 1, '0') + "_" +
                        StringHelper.padLeft(String.valueOf(ph.getM_mcount().getDecValue()), 3, '0');

        // If the extra telegram string is set, compute an MD4 digest.
        if (!entlg.isEmpty()) {
            try {
                MessageDigest md = new CalculatorMD4();
                md.update(splitBinaryStringToByteArray(ArithmeticalFunctions.hex2Bin(entlg)));
                byte[] digest = md.digest();
                String hexDigest = bytesToHex(digest);

                // For example, extract parts of the digest (currently only part1 and part2 are used)
                String part1 = hexDigest.substring(0, 8);
                String part2 = hexDigest.substring(8, 16);
                // String part3 and part4 are computed but not used:
                // String part3 = hexDigest.substring(16, 24);
                // String part4 = hexDigest.substring(24, 32);
                String result = part1 + "-" + part2;
                // The result variable can be used for additional display if needed.
            } catch (Exception e) {
                // Handle potential exceptions in digest computation.
                e.printStackTrace();
            }
        }
        return displayName;
    }
}
