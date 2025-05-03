package packets.Var.NID;

import tools.crypto.ArithmeticalFunctions;
import packets.Var.Variables;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static tools.ui.GUIHelper.addLabel;

public class NID_MN extends Variables {

    public NID_MN() {
        super("NID_MN",
                24,
                "The NID_MN identifies the GSM-R network the calling mobile station has to register with. The NID_MN consists of up to 6 digits which are entered left adjusted into the data field, the leftmost digit is the digit to be dialled first. In case the NID_MN is shorter than 6 digits, the remaining space is to be filled with special character “F”. For further information about NID_MN refer to Subset-54.");
    }

    private void updateBin(JTextField jTextArea) {
        String s = ArithmeticalFunctions.hex2Bin(jTextArea.getText());

        for (int i = s.length(); i < getMaxSize(); i++) {
            s += "1";

        }
        s = s.substring(0, getMaxSize());

        setBinValue(s);
    }

    @Override
    public Component getComponent(String comment) {


        JTextField jTextArea = new JTextField(ArithmeticalFunctions.bin2Hex(getBinValue()).replaceAll("F", ""));


        jTextArea.setPreferredSize(new Dimension(150, 20));

        JPanel jPanel = new JPanel();

        jPanel.add(jTextArea);

        jTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateBin(jTextArea);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateBin(jTextArea);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateBin(jTextArea);
            }
        });


        return addLabel(jPanel, getName(), getDescription(), new JLabel());

    }

    @Override
    public String getSimpleView() {
        return getName() + ": " + ArithmeticalFunctions.bin2Hex(getBinValue()) + "\n";
    }
}


