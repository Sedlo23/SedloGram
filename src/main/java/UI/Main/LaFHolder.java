package UI.Main;

import com.formdev.flatlaf.IntelliJTheme;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A holder for a list of {@link IntelliJTheme.ThemeLaf} objects along with an identifier.
 * <p>
 * This class can be used to group and manage a set of Look-and-Feel themes.
 */
public class LaFHolder {
    /**
     * The list of Look-and-Feel themes.
     */
    private ArrayList<IntelliJTheme.ThemeLaf> themes = new ArrayList<>();

    /**
     * A string identifier for this holder.
     */
    private String identifier;

    /**
     * A public integer field for additional usage.
     */
    public int anInt;

    /**
     * Constructs a new {@code LaFHolder} with the specified identifier.
     *
     * @param identifier the identifier for this holder.
     */
    public LaFHolder(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the list of Look-and-Feel themes.
     *
     * @return the list of {@link IntelliJTheme.ThemeLaf} objects.
     */
    public ArrayList<IntelliJTheme.ThemeLaf> getThemes() {
        return themes;
    }

    /**
     * Sets the list of Look-and-Feel themes.
     *
     * @param themes the list of {@link IntelliJTheme.ThemeLaf} objects.
     */
    public void setThemes(ArrayList<IntelliJTheme.ThemeLaf> themes) {
        this.themes = themes;
    }

    /**
     * Returns the identifier of this holder.
     *
     * @return the identifier string.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier for this holder.
     *
     * @param identifier the new identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaFHolder)) return false;
        LaFHolder that = (LaFHolder) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
