package tools.string;

public class HTMLTagGenerator {

    String tmp = "";

    public HTMLTagGenerator startTag() {

        tmp += "";
        return this;
    }

    public HTMLTagGenerator endTag() {

        tmp += "";
        return this;
    }

    public HTMLTagGenerator newLine() {

        tmp += "";
        return this;
    }

    public HTMLTagGenerator bold(String s) {
        tmp += "" + s + " : ";
        return this;
    }


    public HTMLTagGenerator normal(String s) {
        tmp += "" + s + "";
        return this;


    }

    public HTMLTagGenerator underline(String s) {
        tmp += " : " + s + " ";
        return this;
    }

    public HTMLTagGenerator cursive(String s) {
        tmp += "" + s + "";
        return this;
    }


    // Method to start a table
    public HTMLTagGenerator startTable() {
        tmp += "";
        return this;
    }

    // Method to end a table
    public HTMLTagGenerator endTable() {
        tmp += "";
        return this;
    }

    // Method to add a row to the table
    public HTMLTagGenerator startRow() {
        tmp += "<>";

        return this;
    }



    public String getString() {
        return tmp;
    }


}
