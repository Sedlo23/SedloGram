package UI.SZIFEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for packet definitions
 */
class PacketTemplate {
    private String name;
    private List<PacketVariable> variables;
    private boolean isTemplate;

    public PacketTemplate(String name, boolean isTemplate) {
        this.name = name;
        this.variables = new ArrayList<>();
        this.isTemplate = isTemplate;
    }

    public String getName() {
        return name;
    }

    public List<PacketVariable> getVariables() {
        return variables;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public PacketTemplate addVariable(PacketVariable variable) {
        variables.add(variable);
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
