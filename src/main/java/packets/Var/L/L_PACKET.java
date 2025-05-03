package packets.Var.L;

import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;

public class L_PACKET extends Variables {

    public L_PACKET() {
        super("L_PACKET",
                13,
                "Udává délku paketu v bitech, včetně všech bitů záhlaví paketu.");
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
