package packets.Var.M;

import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class M_TRACKCOND extends Variables {

    public M_TRACKCOND() {
        super("M_TRACKCOND",
                4,
                "Type of track condition");
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "Oblast bez zastavení. Počáteční stav: zastavení povoleno");
        s.set(1, "Zastávka v tunelu. Výchozí stav: žádná oblast zastavení tunelu");
        s.set(2, "Pískání. Výchozí stav: žádný požadavek na pískání");
        s.set(3, "Beznapěťová sekce - stáhni sberač. Výchozí stav: Beznapěťový úsek není");
        s.set(4, "Rádiová díra (zastavení dohledu nad T_NVCONTACT). Počáteční stav: dohlíží na T_NVCONTACT");
        s.set(5, "Vzduchotěsnost. Výchozí stav: žádný požadavek na vzduchotěsnost");
        s.set(6, "Vypněte rekuperační brzdu. Počáteční stav: zapnutá rekuperační brzda");
        s.set(7, "Vypněte brzdu vířivými proudy pro provozní brzdu. Výchozí stav: brzda vířivými proudy pro provozní brzdu zapnuta");
        s.set(8, "Vypněte magnetickou brzdu. Počáteční stav: magnetická brzda je zapnutá.");
        s.set(9, "Sekce bez napájení - vypněte hlavní vypínač. Počáteční stav: není sekce bez napájení");
        s.set(10, "Vypněte brzdu vířivými proudy pro nouzové brzdění. Počáteční stav: brzda vířivými proudy pro nouzovou brzdu zapnuta.");
        s.set(11, "NOT_USED");
        s.set(12, "NOT_USED");
        s.set(13, "NOT_USED");
        s.set(14, "NOT_USED");
        s.set(15, "NOT_USED");


        return s;

    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);


        jComboBox.setPreferredSize(new Dimension(580, 24));
        jComboBox.setMaximumSize(new Dimension(580, 24));
        jComboBox.setMinimumSize(new Dimension(580, 24));


        panel.add(jComboBox);


        return setTitle(panel, "M_TRACKCOND");

    }


}
