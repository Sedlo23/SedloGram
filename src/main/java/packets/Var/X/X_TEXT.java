package packets.Var.X;

import tools.crypto.ArithmeticalFunctions;
import tools.string.StringHelper;
import packets.Var.Variables;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tools.ui.GUIHelper.addLabel;


public class X_TEXT extends Variables {


    private String text;

    public X_TEXT() {
        super("X_TEXT",
                8,
                "");

        this.text = "";
    }


    @Override
    public ArrayList<String> getCombo() {
        ArrayList a = super.getCombo();

        for (int i = 0;i<Math.pow(2,getMaxSize());i++)
        {
            a.set(i,intToISO88591Char(i));

        }
        return a;
    }

    @Override
    public Component getComponent(String comment) {


        JTextField jTextArea = new JTextField(text);


        jTextArea.setPreferredSize(new Dimension(350, 40));


        JPanel jPanel = new JPanel();

        jPanel.add(jTextArea);

        jTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                text = (jTextArea.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                text = (jTextArea.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                text = (jTextArea.getText());
            }
        });


        return addLabel(jPanel, getName(), getDescription(), new JLabel());

    }

    @Override
    public String getBinValue() {

        String tmp = ArithmeticalFunctions.dec2XBin(String.valueOf(text.length()), 8);


        for (int i = 0; i < text.length(); i++) {
            try {
                String zz = ArithmeticalFunctions.dec2XBin(String.valueOf((text.substring(i, i + 1).getBytes("ISO-8859-1"))[0]), 8);

                tmp += zz;


            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        }

        return tmp;
    }


    @Override
    public void setBinValue(String binValue) {

        String old = getBinValue();

        this.binValue = binValue;

        String input = binValue.substring(0);

        text = text2Bin(input);

        notifyBinaryChangeListeners(old, binValue);

    }


    @Override
    public Variables initValueSet(String[] s) {


        int size = (int) ArithmeticalFunctions.bin2Dec(StringHelper.TrimAR(s, 8));

        setBinValue(StringHelper.TrimAR(s, size * 8));


        return this;
    }

    String convertToBinary(String input, String encoding) {
        byte[] encoded_input = Charset.forName(encoding)
                .encode(input)
                .array();
        return IntStream.range(0, encoded_input.length)
                .map(i -> encoded_input[i])
                .mapToObj(e -> Integer.toBinaryString(e ^ 255))
                .map(e -> String.format("%1$" + Byte.SIZE + "s", e).replace(" ", "0"))
                .collect(Collectors.joining(" "));
    }


    String text2Bin(String s) {
        String[] ss = s.split("(?=(.{8})+$)");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ss.length; i++) {
            sb.append((char) Integer.parseInt(ss[i], 2));
        }
        return sb.toString();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public static String intToISO88591Char(int intValue) {
        if (intValue >= 0 && intValue <= 255) {
            return String.valueOf((char) intValue);
        } else {
            throw new IllegalArgumentException("Input integer should be in the range 0 to 255 for ISO-8859-1");
        }
    }
    @Override
    public String getSimpleView() {

        String tmp = "L_TEXT:8:" + text.length() + "\n";


        for (int i = 0; i < text.length(); i++) {
            try {
                String zz = ArithmeticalFunctions.dec2XBin(String.valueOf((text.substring(i, i + 1).getBytes("ISO-8859-1"))[0]), 8);

                tmp += "X_TEXT:8:" + ArithmeticalFunctions.bin2Dec(zz) + "\n";
                ;


            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        }


        return tmp;
    }

}
