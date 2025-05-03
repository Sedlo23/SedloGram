package packets.Var.NID;

import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;

public class NID_PACKET extends Variables {

    public NID_PACKET() {
        super("NID_PACKET",
                8,
                "Používá se v záhlaví každého paketu a umožňuje přijímacímu zařízení identifikovat následující data. Pokud jde o definované hodnoty NID_PACKET, viz \"čísla paketů\" v tabulkách v kapitole 7.4.1.");
    }

    @Override
    public Component getComponent(String comment) {


        Component component = super.getComponent(comment);

        Component[] components = ((JPanel) component).getComponents();

        for (int i = 0; i < components.length; i++) {

            components[i].setEnabled(false);

        }


        return component;

    }


}
