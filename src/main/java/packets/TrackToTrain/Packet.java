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
        try {
            Class<?> c = Class.forName(this.getClass().getName());

            // First create with current data
            String currentBinData = this.getBinData();
            Constructor<?> cons = c.getConstructor(String[].class);
            Packet newPacket = (Packet) cons.newInstance(new Object[]{new String[]{currentBinData}});

            // Copy additional properties that might not be in binary data
            newPacket.setIcon(this.getIcon());

            // Copy the JButton reference if needed
            if (this.getjProgressBar() != null) {
                newPacket.setjProgressBar(this.getjProgressBar());
            }

            return newPacket;

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback
            try {
                Class<?> c = Class.forName(this.getClass().getName());
                Constructor<?> cons = c.getConstructor();
                return (Packet) cons.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }


}
