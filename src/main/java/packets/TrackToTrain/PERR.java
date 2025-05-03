package packets.TrackToTrain;

import packets.Var.NID.NID_PACKET;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;

import javax.swing.*;
import java.awt.*;

public class PERR extends Packet {

    NID_PACKET nid_packet;



    public PERR() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("FF")});

    }

    public PERR(String[] d) {


        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);

        setIcon((GUIHelper.getImageIconFromResources("icons8-alert-80")));
    }


    public Component getPacketComponent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);


        jPanel.add(this.nid_packet.getComponent());

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        return jPanel1;

    }

    public String getHexData() {
        return ArithmeticalFunctions.bin2Hex(getBinData());
    }

    public String getBinData() {
        String tmp = "";

        tmp = getBinDataPrivately(tmp);


        return tmp;

    }

    private String getBinDataPrivately(String tmp) {

        tmp += (this.nid_packet.getFullData());

        return tmp;
    }

    @Override
    public String getSimpleView() {

        String tmp = "";
        tmp += (this.nid_packet.getSimpleView());


        return tmp;
    }

    @Override
    public Component getGraphicalVisualization() {
        return null;
    }


    @Override
    public String toString() {
        return new HTMLTagGenerator().startTag()
                .bold(getClass().getSimpleName())
                .cursive(" ERROR ")

                .endTag()
                .getString();
    }


}
