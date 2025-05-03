package UI.SZIFEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for storing and retrieving packets
 */
class PacketRepository {
    private List<PacketTemplate> packetTemplates;
    private List<PacketTemplate> templatePacketTemplates;

    public PacketRepository() {
        this.packetTemplates = new ArrayList<>();
        this.templatePacketTemplates = new ArrayList<>();
    }

    public void add(PacketTemplate packetTemplate) {
        // Remove any existing packets with the same name
        packetTemplates.removeIf(p -> p.getName().equals(packetTemplate.getName()));
        templatePacketTemplates.removeIf(p -> p.getName().equals(packetTemplate.getName()));

        // Add to appropriate list
        if (packetTemplate.isTemplate()) {
            templatePacketTemplates.add(packetTemplate);
        } else {
            packetTemplates.add(packetTemplate);
        }
    }

    public void remove(PacketTemplate packetTemplate) {
        packetTemplates.remove(packetTemplate);
        templatePacketTemplates.remove(packetTemplate);
    }

    public List<PacketTemplate> getPackets() {
        return new ArrayList<>(packetTemplates);
    }

    public List<PacketTemplate> getTemplatePackets() {
        return new ArrayList<>(templatePacketTemplates);
    }

    public Optional<PacketTemplate> findByName(String name) {
        for (PacketTemplate packetTemplate : packetTemplates) {
            if (packetTemplate.getName().equals(name)) {
                return Optional.of(packetTemplate);
            }
        }

        for (PacketTemplate packetTemplate : templatePacketTemplates) {
            if (packetTemplate.getName().equals(name)) {
                return Optional.of(packetTemplate);
            }
        }

        return Optional.empty();
    }
}
