package packets.Var.G;

import packets.Var.Variables;

import java.util.ArrayList;

public class G_TSR extends Variables {

    public G_TSR() {
        super("G_TSR",
                8,
                "Definuje výchozí sklon, který se použije pro dohled TSR, pokud není k dispozici žádný profil sklonu (paket 21).");
    }


    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();

        int i = 0;
        for (i = 0; i < 256; i++)
            s.add(i + "‰");


        return s;

    }

}
