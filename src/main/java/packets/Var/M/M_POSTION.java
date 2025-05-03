package packets.Var.M;

import tools.crypto.ArithmeticalFunctions;
import packets.Var.Variables;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static tools.ui.GUIHelper.addLabel;


public class M_POSTION extends Variables {

    public M_POSTION() {
        super("M_POSTION",
                24,
                "Funkce hlášení zeměpisné polohy používá obsah této proměnné jako referenční hodnotu.");
    }

    @Override
    public Component getComponent(String comment) {


        JTextField jTextArea = new JTextField(String.valueOf(ArithmeticalFunctions.bin2Dec(getBinValue())));

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
}
