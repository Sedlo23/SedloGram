package UI.DP;

// DockingFrameworkApp.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DockingFrameworkApp extends JFrame {
    private DockingContainer dockingContainer;

    public DockingFrameworkApp() {
        setTitle("Modern Docking Framework");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize components
        initializeUI();

        // Add sample panels
        addSamplePanels();
    }

    private void initializeUI() {
        // Set modern menu bar
        setJMenuBar(new ModernMenuBar());

        // Create main docking container
        dockingContainer = new DockingContainer();
        add(dockingContainer, BorderLayout.CENTER);

        // Set frame background
        getContentPane().setBackground(new Color(30, 30, 30));
    }

    private void addSamplePanels() {
        // Create sample panels with different content
        DockingPanel panel1 = new DockingPanel("Project Explorer");
        JTree tree = new JTree();
        tree.setBackground(new Color(30, 30, 30));
        tree.setForeground(Color.WHITE);
        panel1.setContent(new JScrollPane(tree));

        DockingPanel panel2 = new DockingPanel("Properties");
        JTable table = new JTable(5, 2);
        table.setBackground(new Color(30, 30, 30));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 60));
        panel2.setContent(new JScrollPane(table));

        DockingPanel panel3 = new DockingPanel("Console");
        JTextArea console = new JTextArea();
        console.setBackground(new Color(30, 30, 30));
        console.setForeground(new Color(0, 255, 0));
        console.setFont(new Font("Consolas", Font.PLAIN, 12));
        console.setText("> Ready\n> ");
        panel3.setContent(new JScrollPane(console));

        DockingPanel panel4 = new DockingPanel("Editor");
        JTextArea editor = new JTextArea();
        editor.setBackground(new Color(30, 30, 30));
        editor.setForeground(Color.WHITE);
        editor.setFont(new Font("Consolas", Font.PLAIN, 14));
        panel4.setContent(new JScrollPane(editor));

        // Add panels to different zones
        dockingContainer.addPanel(panel1, DockingContainer.DockingZone.LEFT);
        dockingContainer.addPanel(panel2, DockingContainer.DockingZone.RIGHT);
        dockingContainer.addPanel(panel3, DockingContainer.DockingZone.BOTTOM);
        dockingContainer.addPanel(panel4, DockingContainer.DockingZone.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DockingFrameworkApp app = new DockingFrameworkApp();

            // Add fade-in animation on startup
            //app.setOpacity(0.0f);
            app.setVisible(true);

            Timer fadeIn = new Timer(20, null);
            fadeIn.addActionListener(new ActionListener() {
                float opacity = 0.0f;

                @Override
                public void actionPerformed(ActionEvent e) {
                    opacity += 0.05f;
                    if (opacity >= 1.0f) {
                        opacity = 1.0f;
                        fadeIn.stop();
                    }
                    app.setOpacity(opacity);
                }
            });
            fadeIn.start();
        });
    }
}