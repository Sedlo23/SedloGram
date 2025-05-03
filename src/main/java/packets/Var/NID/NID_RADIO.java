package packets.Var.NID;

import tools.crypto.ArithmeticalFunctions;
import packets.Var.Variables;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static tools.ui.GUIHelper.addLabel;


public class NID_RADIO extends Variables {

    public NID_RADIO() {
        super("NID_RADIO",
                64,
                "Quoted as a 16 digit decimal number.\n" +
                        "The number is to be entered “left adjusted” starting with the first digit to be dialled. Padding by the special value F shall be added after the least significant digit of the number.\n" +
                        "For further information about NID_RADIO refer to SUBSET-054.");
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

    private void updateBin(JTextField jTextArea) {

        String s = ArithmeticalFunctions.hex2Bin(jTextArea.getText());

        for (int i = s.length(); i < getMaxSize(); i++) {
            s += "1";

        }
        s = s.substring(0, getMaxSize());

        setBinValue(s);
    }


    @Override
    public String getSimpleView() {


        return getName() + ": " + ArithmeticalFunctions.bin2Hex(getBinValue()) + "\n";
    }
}


