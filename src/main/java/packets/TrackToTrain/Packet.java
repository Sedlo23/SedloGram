package packets.TrackToTrain;

import packets.Interfaces.IPacket;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;

public abstract class Packet implements IPacket {

    Component f;

    ImageIcon icon;

    public JButton getjProgressBar() {
        return jProgressBar;
    }

    public void setjProgressBar(JButton jProgressBar) {
        this.jProgressBar = jProgressBar;
    }

    JButton jProgressBar = new JButton();


    public Component getPacketComponentFinal() {

        if (f == null)
            f = getPacketComponent();

        return f;
    }

    public Packet deepCopy() {

        Class<?> c = null;
        Object object = null;

        try {
            c = Class.forName(this.getClass().getName());
            Constructor<?> cons = c.getConstructor();
            object = cons.newInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }


        return (Packet) object;

    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }


}
