/*
 * European Train Control System
 * Copyright (C) 2019-2023 César Benito <cesarbema2009@hotmail.com>
 * Java port (2025)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class ETCSDMISimulation extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ETCSDMISimulation simulation = new ETCSDMISimulation();
            simulation.setVisible(true);
        });
    }

    // Main panel for the simulation
    private static DMIPanel dmiPanel;

    // Flash timer for blinking elements
    private Timer flashTimer;
    private int flashState = 0;

    // List of active windows
    private List<Window> activeWindows = new ArrayList<>();

    // Simulation state
    private boolean evcConnected = false;
    private boolean serieSelected = false;

    public ETCSDMISimulation() {
        setTitle("ETCS DMI Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);

        dmiPanel = new DMIPanel();
        add(dmiPanel);

        // Start the flash timer
        setupFlash();

        // Initialize windows
        Window defaultWindow = new Window();
        activeWindows.add(defaultWindow);

        // Create example UI
        createExampleUI();

        // For simulation testing
        JMenuBar menuBar = new JMenuBar();
        JMenu simulationMenu = new JMenu("Simulation");

        JMenuItem connectEVC = new JMenuItem("Connect EVC");
        connectEVC.addActionListener(e -> {
            evcConnected = true;
            dmiPanel.repaint();
        });

        JMenuItem disconnectEVC = new JMenuItem("Disconnect EVC");
        disconnectEVC.addActionListener(e -> {
            evcConnected = false;
            dmiPanel.repaint();
        });

        JMenuItem selectSerie = new JMenuItem("Select Train Serie");
        selectSerie.addActionListener(e -> {
            serieSelected = true;
            dmiPanel.repaint();
        });

        JMenuItem deselectSerie = new JMenuItem("Deselect Train Serie");
        deselectSerie.addActionListener(e -> {
            serieSelected = false;
            dmiPanel.repaint();
        });

        simulationMenu.add(connectEVC);
        simulationMenu.add(disconnectEVC);
        simulationMenu.add(selectSerie);
        simulationMenu.add(deselectSerie);
        menuBar.add(simulationMenu);
        setJMenuBar(menuBar);
    }

    private void setupFlash() {
        flashTimer = new Timer(250, e -> {
            flashState = (flashState + 1) % 4;
            dmiPanel.repaint();
        });
        flashTimer.start();
    }

    // For demonstration: create a simple example with buttons
    private void createExampleUI() {
        Window mainWindow = activeWindows.get(0);

        // Create layout components
        Component background = new Component(640, 480, null);
        background.setLocation(0, 0);
        background.setBackgroundColor(Color.DARK_BLUE);

        // Add text button example
        TextButton textBtn = new TextButton("Sample Button", 120, 40, () -> {
            System.out.println("Text button pressed");
        }, 14);
        textBtn.setLocation(260, 220);

        // Add to layout with alignment
        Layout layout = mainWindow.layout;
        layout.add(background, new RelativeAlignment(null, 0, 0, 0));
        layout.add(textBtn, new RelativeAlignment(background, 320, 240, 1));

        // Create some additional buttons to demonstrate layout
        TextButton speedBtn = new TextButton("Speed", 100, 40, () -> {
            System.out.println("Speed button pressed");
        }, 14);

        TextButton brakeBtn = new TextButton("Brake", 100, 40, () -> {
            System.out.println("Brake button pressed");
        }, 14);

        layout.add(speedBtn, new ConsecutiveAlignment(textBtn, LEFT | DOWN, 1));
        layout.add(brakeBtn, new ConsecutiveAlignment(textBtn, RIGHT | DOWN, 1));
    }

    // Color definitions
    static class Color {
        int r, g, b;

        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public java.awt.Color toAWTColor() {
            return new java.awt.Color(r, g, b);
        }

        public boolean equals(Color other) {
            return r == other.r && g == other.g && b == other.b;
        }

        // Static color definitions
        public static final Color WHITE = new Color(255, 255, 255);
        public static final Color BLACK = new Color(0, 0, 0);
        public static final Color GREY = new Color(195, 195, 195);
        public static final Color MEDIUM_GREY = new Color(150, 150, 150);
        public static final Color DARK_GREY = new Color(85, 85, 85);
        public static final Color YELLOW = new Color(223, 223, 0);
        public static final Color SHADOW = new Color(8, 24, 57);
        public static final Color RED = new Color(191, 0, 2);
        public static final Color ORANGE = new Color(234, 145, 0);
        public static final Color DARK_BLUE = new Color(3, 17, 34);
        public static final Color PASP_DARK = new Color(33, 49, 74);
        public static final Color PASP_LIGHT = new Color(41, 74, 107);
        public static final Color BLUE = new Color(0, 0, 234);
        public static final Color GREEN = new Color(0, 234, 0);
        public static final Color LIGHT_RED = new Color(255, 96, 96);
        public static final Color LIGHT_GREEN = new Color(96, 255, 96);
        public static final Color MAGENTA = new Color(255, 0, 255);

        public static Color fromETCS(int rawdata) {
            switch (rawdata) {
                case 0: return WHITE;
                case 1: return GREY;
                case 2: return MEDIUM_GREY;
                case 3: return DARK_GREY;
                case 4: return YELLOW;
                case 5: return ORANGE;
                case 6: return RED;
                default: return WHITE;
            }
        }
    }

    // Constants for alignment
    static final int CENTER = 0;
    static final int RIGHT = 1;
    static final int LEFT = 2;
    static final int UP = 4;
    static final int DOWN = 8;

    // Graphics types
    enum GraphicType {
        TEXTURE,
        RECTANGLE,
        LINE,
        CIRCLE,
        SOLID_ARC
    }

    // Base class for all graphic elements
    static abstract class Graphic {
        GraphicType type;

        public Graphic(GraphicType type) {
            this.type = type;
        }

        public abstract void draw(Graphics2D g2d, float x, float y);
    }

    // Rectangle graphic
    static class Rectangle extends Graphic {
        float x, y, sx, sy;
        Color color;

        public Rectangle(float x, float y, float sx, float sy, Color color) {
            super(GraphicType.RECTANGLE);
            this.x = x;
            this.y = y;
            this.sx = sx;
            this.sy = sy;
            this.color = color;
        }

        @Override
        public void draw(Graphics2D g2d, float offsetX, float offsetY) {
            g2d.setColor(color.toAWTColor());
            g2d.fill(new Rectangle2D.Float(x + offsetX, y + offsetY, sx, sy));
        }
    }

    // Line graphic
    static class Line extends Graphic {
        float x1, y1, x2, y2;
        Color color;

        public Line(float x1, float y1, float x2, float y2, Color color) {
            super(GraphicType.LINE);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
        }

        @Override
        public void draw(Graphics2D g2d, float offsetX, float offsetY) {
            g2d.setColor(color.toAWTColor());
            g2d.draw(new Line2D.Float(
                    x1 + offsetX, y1 + offsetY,
                    x2 + offsetX, y2 + offsetY
            ));
        }
    }

    // Circle graphic
    static class Circle extends Graphic {
        float cx, cy, radius;
        Color color;

        public Circle(float cx, float cy, float radius, Color color) {
            super(GraphicType.CIRCLE);
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void draw(Graphics2D g2d, float offsetX, float offsetY) {
            g2d.setColor(color.toAWTColor());
            g2d.fill(new Ellipse2D.Float(
                    cx - radius + offsetX,
                    cy - radius + offsetY,
                    radius * 2,
                    radius * 2
            ));
        }
    }

    // Text graphic
    static class TextGraphic extends Graphic {
        String text;
        float x, y, size;
        float offx, offy;
        float width, height;
        Color color;
        int alignment;
        int aspect;
        BufferedImage tex;

        public TextGraphic() {
            super(GraphicType.TEXTURE);
        }

        @Override
        public void draw(Graphics2D g2d, float offsetX, float offsetY) {
            if (tex != null) {
                g2d.drawImage(tex, (int)(x + offsetX - width/2), (int)(y + offsetY - height/2), null);
            } else {
                g2d.setColor(color.toAWTColor());
                Font font = new Font(Font.SANS_SERIF, (aspect & 1) != 0 ? Font.BOLD : Font.PLAIN, (int)size);
                g2d.setFont(font);

                FontMetrics metrics = g2d.getFontMetrics(font);
                int textWidth = metrics.stringWidth(text);
                int textHeight = metrics.getHeight();

                float drawX = x + offsetX;
                float drawY = y + offsetY + metrics.getAscent() - textHeight/2;

                if ((alignment & LEFT) != 0) drawX = x + offsetX;
                else if ((alignment & RIGHT) != 0) drawX = x + offsetX - textWidth;
                else drawX = x + offsetX - textWidth/2;

                g2d.drawString(text, drawX, drawY);
            }
        }
    }

    // Image graphic
    static class ImageGraphic extends Graphic {
        String path;
        float x, y;
        float width, height;
        BufferedImage tex;

        public ImageGraphic() {
            super(GraphicType.TEXTURE);
        }

        @Override
        public void draw(Graphics2D g2d, float offsetX, float offsetY) {
            if (tex != null) {
                g2d.drawImage(tex, (int)(x + offsetX - width/2), (int)(y + offsetY - height/2), null);
            }
        }
    }

    // Base component class
    class Component {
        float x, y, sx, sy;
        boolean visible = true;
        Color bgColor = Color.DARK_BLUE;
        Color fgColor = Color.WHITE;
        boolean ack = false;
        Runnable pressedAction;
        Runnable display;
        List<Graphic> graphics = new ArrayList<>();
        boolean isButton = false;
        boolean dispBorder = true;
        boolean upType = true;
        boolean delayType = false;
        long firstPressedTime = 0;
        long lastPressedTime = 0;
        int flashStyle = 0;
        float touchUp = 0;
        float touchDown = 0;
        float touchLeft = 0;
        float touchRight = 0;
        String text;
        float textSize;
        Color textColor;

        public Component() {
        }

        public Component(float sx, float sy, Runnable display) {
            this.sx = sx;
            this.sy = sy;
            this.display = display;
        }

        public void clear() {
            graphics.clear();
        }

        public void setPressedAction(Runnable action) {
            this.pressedAction = action;
        }

        public void setAck(Runnable ackAction) {
            this.pressedAction = ackAction;
            this.ack = ackAction != null;
        }

        public void setPressed() {
            if (pressedAction != null) {
                pressedAction.run();
            }
        }

        public void setDisplayFunction(Runnable display) {
            this.display = display;
        }

        public void setSize(float sx, float sy) {
            this.sx = sx;
            this.sy = sy;
        }

        public void setLocation(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public boolean isSensitive() {
            return pressedAction != null;
        }

        protected float getX(float val) {
            return val + x;
        }

        protected float getY(float val) {
            return val + y;
        }

        public void paint(Graphics2D g2d) {
            boolean show = true;
            if (flashStyle != 0) {
                boolean fast = (flashStyle & 1) != 0;
                boolean counter = ((flashStyle & 2) >> 1) != 0;
                show = (fast ? (flashState & 1) : ((flashState >> 1) & 1)) == (counter ? 0 : 1);
            }

            if (show || (flashStyle & 4) == 0) {
                if (!bgColor.equals(Color.DARK_BLUE)) {
                    drawRectangle(g2d, 0, 0, sx, sy, bgColor);
                }

                for (Graphic graphic : graphics) {
                    draw(g2d, graphic);
                }

                if (display != null) {
                    display.run();
                }
            }

            if ((show && flashStyle != 0 && (flashStyle & 4) == 0) || (ack && (flashState & 2) != 0)) {
                drawRectangle(g2d, 0, 0, 2, sy, Color.YELLOW);
                drawRectangle(g2d, sx - 2, 0, 2, sy, Color.YELLOW);
                drawRectangle(g2d, 0, 0, sx, 2, Color.YELLOW);
                drawRectangle(g2d, 0, sy - 2, sx, 2, Color.YELLOW);
            } else if (dispBorder) {
                drawRectangle(g2d, 0, 0, 1, sy - 1, Color.BLACK);
                drawRectangle(g2d, sx - 1, 0, 1, sy - 1, Color.SHADOW);
                drawRectangle(g2d, 0, 0, sx - 1, 1, Color.BLACK);
                drawRectangle(g2d, 0, sy - 1, sx - 1, 1, Color.SHADOW);
            }
        }

        public void draw(Graphics2D g2d, Graphic graphic) {
            if (graphic != null) {
                graphic.draw(g2d, x, y);
            }
        }

        public void drawLine(Graphics2D g2d, float x1, float y1, float x2, float y2, Color c) {
            g2d.setColor(c.toAWTColor());
            g2d.draw(new Line2D.Float(getX(x1), getY(y1), getX(x2), getY(y2)));
        }

        public void drawRectangle(Graphics2D g2d, float x, float y, float w, float h, Color c, int align) {
            g2d.setColor(c.toAWTColor());

            if ((align & LEFT) == 0) x = sx / 2 + x - w / 2;
            if ((align & UP) == 0) y = sy / 2 + y - h / 2;

            g2d.fill(new Rectangle2D.Float(getX(x), getY(y), w, h));
        }

        public void drawRectangle(Graphics2D g2d, float x, float y, float w, float h, Color c) {
            drawRectangle(g2d, x, y, w, h, c, LEFT | UP);
        }

        public void drawCircle(Graphics2D g2d, float radius, float cx, float cy) {
            g2d.fill(new Ellipse2D.Float(
                    getX(cx - radius),
                    getY(cy - radius),
                    radius * 2,
                    radius * 2
            ));
        }

        public void add(Graphic g) {
            graphics.add(g);
        }

        public void addRectangle(float x, float y, float w, float h, Color c, int align) {
            add(new Rectangle(x, y, w, h, c));
        }

        public void addRectangle(float x, float y, float w, float h, Color c) {
            addRectangle(x, y, w, h, c, LEFT | UP);
        }

        public void setBackgroundColor(Color c) {
            bgColor = c;
        }

        public void setForegroundColor(Color c) {
            fgColor = c;
        }

        public void addBorder(Color c) {
            addRectangle(0, 0, 1, sy, Color.MEDIUM_GREY);
            addRectangle(sx - 1, 0, 1, sy, Color.MEDIUM_GREY);
            addRectangle(0, 0, sx, 1, Color.MEDIUM_GREY);
            addRectangle(0, sy - 1, sx, 1, Color.MEDIUM_GREY);
        }

        public void setBorder(Graphics2D g2d, Color c) {
            g2d.setColor(c.toAWTColor());
            g2d.draw(new Rectangle2D.Float(getX(0), getY(0), sx, sy));
        }

        public void addText(String text, float x, float y, float size, Color col, int align, int aspect, float width) {
            if (text == null || text.isEmpty()) return;

            TextGraphic textGraphic = new TextGraphic();
            textGraphic.text = text;
            textGraphic.offx = x;
            textGraphic.offy = y;
            textGraphic.size = size;
            textGraphic.color = col;
            textGraphic.alignment = align;
            textGraphic.aspect = aspect;

            // Calculate position based on alignment
            if ((align & UP) != 0) y = y + textGraphic.height / 2;
            else if ((align & DOWN) != 0) y = (this.sy - y) - textGraphic.height / 2;
            else y = y + this.sy / 2;

            if ((align & LEFT) != 0) x = x + textGraphic.width / 2;
            else if ((align & RIGHT) != 0) x = (this.sx - x) - textGraphic.width / 2;
            else x = x + this.sx / 2;

            textGraphic.x = x;
            textGraphic.y = y;

            add(textGraphic);
        }
    }

    // Button component
     class Button extends Component {
        boolean enabled = true;
        boolean pressed = false;
        boolean showBorder = true;

        public Button() {
            isButton = true;
        }

        public Button(float sx, float sy, Runnable display, Runnable pressed) {
            super(sx, sy, display);
            isButton = true;
            setPressedAction(pressed);
        }

        @Override
        public void paint(Graphics2D g2d) {
            super.paint(g2d);

            if (showBorder) {
                drawRectangle(g2d, 0, 0, 1, sy - 1, Color.BLACK);
                drawRectangle(g2d, sx - 1, 0, 1, sy - 1, Color.SHADOW);
                drawRectangle(g2d, 0, 0, sx - 1, 1, Color.BLACK);
                drawRectangle(g2d, 0, sy - 1, sx - 1, 1, Color.SHADOW);

                if (!pressed) {
                    drawRectangle(g2d, 1, 1, 1, sy - 2, Color.SHADOW);
                    drawRectangle(g2d, sx - 2, 1, 1, sy - 2, Color.BLACK);
                    drawRectangle(g2d, 1, 1, sx - 2, 1, Color.SHADOW);
                    drawRectangle(g2d, 1, sy - 2, sx - 2, 1, Color.BLACK);
                }
            }
        }

        public void setEnabled(boolean val) {
            enabled = val;
        }
    }

    // Icon Button component
     class IconButton extends Button {
        ImageGraphic enabledImage = null;
        ImageGraphic disabledImage = null;
        String enabledPath;
        String disabledPath;

        public IconButton() {
        }

        public IconButton(String enabledPath, float sx, float sy, Runnable pressed, String disabledPath) {
            super(sx, sy, null, pressed);
            this.enabledPath = enabledPath;
            this.disabledPath = disabledPath != null ? disabledPath : "";
        }

        public void setEnabledImage(String path) {
            if (path == null || path.isEmpty()) return;
            enabledPath = path;
            enabledImage = getImage(path);
        }

        public void setDisabledImage(String path) {
            if (path == null || path.isEmpty()) return;
            disabledPath = path;
            disabledImage = getImage(path);
        }

        @Override
        public void paint(Graphics2D g2d) {
            if (enabledImage == null) {
                setEnabledImage(enabledPath);
                if (!disabledPath.isEmpty()) {
                    setDisabledImage(disabledPath);
                }
            }

            super.paint(g2d);

            if (enabled || disabledImage == null) {
                draw(g2d, enabledImage);
            } else {
                draw(g2d, disabledImage);
            }
        }

        private ImageGraphic getImage(String path) {
            ImageGraphic img = new ImageGraphic();
            img.path = path;
            try {
                img.tex = ImageIO.read(new File(path));
                img.width = img.tex.getWidth();
                img.height = img.tex.getHeight();
                img.x = sx / 2;
                img.y = sy / 2;
            } catch (IOException e) {
                System.err.println("Failed to load image: " + path);
            }
            return img;
        }
    }

    // Text Button component
     class TextButton extends Button {
        BufferedImage enabledText = null;
        BufferedImage disabledText = null;
        int size;
        String caption;
        Color disabledColor = Color.DARK_GREY;

        public TextButton() {
        }

        public TextButton(String text, float sx, float sy, Runnable pressed, int size) {
            super(sx, sy, null, pressed);
            this.caption = text;
            this.size = size;
        }

        @Override
        public void paint(Graphics2D g2d) {
            if (enabledText == null) {
                enabledText = createTextImage(caption, size, fgColor);
                disabledText = createTextImage(caption, size, disabledColor);
            }

            super.paint(g2d);

            BufferedImage textImage = enabled ? enabledText : disabledText;
            if (textImage != null) {
                g2d.drawImage(textImage,
                        (int)(x + sx / 2 - textImage.getWidth() / 2),
                        (int)(y + sy / 2 - textImage.getHeight() / 2),
                        null);
            }
        }

        public void rename(String name) {
            caption = name;
            enabledText = null;
            disabledText = null;
        }

        private BufferedImage createTextImage(String text, int size, Color color) {
            if (text == null || text.isEmpty()) return null;

            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, size);
            FontMetrics metrics = new Canvas().getFontMetrics(font);
            int width = metrics.stringWidth(text);
            int height = metrics.getHeight();

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(font);
            g2d.setColor(color.toAWTColor());
            g2d.drawString(text, 0, metrics.getAscent());
            g2d.dispose();

            return image;
        }
    }

    // Layout management classes
    enum AlignType {
        CONSECUTIVE_ALIGN,
        RELATIVE_ALIGN
    }

    static class ComponentAlignment {
        AlignType alignType;
        Component relative;
        int layer;

        public ComponentAlignment(AlignType alignType, Component relative, int layer) {
            this.alignType = alignType;
            this.relative = relative;
            this.layer = layer;
        }
    }

    static class ConsecutiveAlignment extends ComponentAlignment {
        int align;

        public ConsecutiveAlignment(Component rel, int align, int layer) {
            super(AlignType.CONSECUTIVE_ALIGN, rel, layer);
            this.align = align;
        }
    }

    static class RelativeAlignment extends ComponentAlignment {
        float x;
        float y;

        public RelativeAlignment(Component rel, float x, float y, int layer) {
            super(AlignType.RELATIVE_ALIGN, rel, layer);
            this.x = x;
            this.y = y;
        }
    }

    static class LayoutElement {
        Component comp;
        ComponentAlignment alignment;

        public LayoutElement(Component comp, ComponentAlignment alignment) {
            this.comp = comp;
            this.alignment = alignment;
        }
    }

    static class Layout {
        private List<LayoutElement> elements = new ArrayList<>();

        public void add(Component comp, ComponentAlignment alignment) {
            if (comp == null || alignment == null) {
                return;
            }

            elements.add(new LayoutElement(comp, alignment));
            if (alignment.layer == 0) comp.dispBorder = false;
            updateLocations();
        }

        public void bringFront(Component comp) {
            if (comp == null) return;

            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).comp == comp) {
                    LayoutElement e = elements.remove(i);
                    add(e.comp, e.alignment);
                    break;
                }
            }
        }

        public void remove(Component comp) {
            if (comp == null) return;

            elements.removeIf(e -> e.comp == comp);
        }

        public void removeAll() {
            elements.clear();
        }

        public void update(Graphics2D g2d, List<List<Integer>> alreadyDrawn) {
            for (LayoutElement element : elements) {
                Component comp = element.comp;
                boolean paint = true;

                for (List<Integer> vec : alreadyDrawn) {
                    boolean outsideX = comp.x + comp.sx <= vec.get(0) || comp.x >= vec.get(0) + vec.get(2);
                    boolean outsideY = comp.y + comp.sy <= vec.get(1) || comp.y >= vec.get(1) + vec.get(3);

                    if (!outsideX && !outsideY) {
                        paint = false;
                        break;
                    }
                }

                if (paint && comp.visible) {
                    comp.paint(g2d);
                }
            }
        }

        public void updateLocations() {
            for (LayoutElement element : elements) {
                Component c = element.comp;
                ComponentAlignment align = element.alignment;

                if (align.alignType == AlignType.RELATIVE_ALIGN) {
                    RelativeAlignment offset = (RelativeAlignment) align;
                    if (align.relative != null) {
                        c.setLocation(offset.x + align.relative.x - c.sx / 2,
                                offset.y + align.relative.y - c.sy / 2);
                    } else {
                        c.setLocation(offset.x, offset.y);
                    }
                }

                if (align.alignType == AlignType.CONSECUTIVE_ALIGN) {
                    ConsecutiveAlignment offset = (ConsecutiveAlignment) align;
                    int al = offset.align;
                    float x = align.relative.x;
                    float y = align.relative.y;

                    if ((al & RIGHT) != 0) x += align.relative.sx;
                    else if ((al & LEFT) == 0 && (al & DOWN) != 0) x += (align.relative.sx - c.sx) / 2;
                    else if ((al & LEFT) != 0 && (al & DOWN) == 0) x -= c.sx;

                    if ((al & DOWN) != 0) y += align.relative.sy;
                    else if ((al & UP) == 0 && (al & RIGHT) != 0) y += (align.relative.sy - c.sy) / 2;
                    else if ((al & UP) != 0 && (al & RIGHT) == 0) y -= c.sy;

                    c.setLocation(x, y);
                }
            }
        }

        public List<LayoutElement> getElements() {
            return elements;
        }
    }

    // Window class
    static class Window {
        public List<List<Integer>> bounds = new ArrayList<>();
        public Layout layout = new Layout();
        private boolean visible = true;

        public void display(List<List<Integer>> alreadyDrawn) {
            if (visible) {
                layout.update(dmiPanel.getGraphics2D(), alreadyDrawn);
            }
        }

        public void event(boolean pressed, float x, float y) {
            // Handle input events
            if (pressed) {
                for (LayoutElement element : layout.getElements()) {
                    Component comp = element.comp;
                    if (comp.isButton && comp.visible &&
                            x >= comp.x && x <= comp.x + comp.sx &&
                            y >= comp.y && y <= comp.y + comp.sy) {

                        Button btn = (Button) comp;
                        if (btn.enabled) {
                            btn.pressed = true;
                            btn.setPressed();
                        }
                    }
                }
            } else {
                for (LayoutElement element : layout.getElements()) {
                    Component comp = element.comp;
                    if (comp.isButton) {
                        Button btn = (Button) comp;
                        btn.pressed = false;
                    }
                }
            }
        }
    }

    // DMI Panel for rendering
    class DMIPanel extends JPanel {
        private BufferedImage invalidEvcIndicator;
        private BufferedImage invalidSerieIndicator;

        public DMIPanel() {
            setPreferredSize(new Dimension(640, 480));

            // Create indicators
            Font indicatorFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
            invalidEvcIndicator = createTextImage("EVC not connected!", indicatorFont, Color.WHITE);
            invalidSerieIndicator = createTextImage("Train serie not selected!", indicatorFont, Color.WHITE);

            // Add mouse listener for input handling
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleInput(true, e.getX(), e.getY());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    handleInput(false, e.getX(), e.getY());
                }
            });
        }

        private void handleInput(boolean pressed, int x, int y) {
            for (Window window : activeWindows) {
                window.event(pressed, x, y);
            }
            repaint();
        }

        public Graphics2D getGraphics2D() {
            Graphics2D g2d = (Graphics2D) getGraphics();
            if (g2d != null) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            return g2d;
        }

        private BufferedImage createTextImage(String text, Font font, Color color) {
            if (text == null || text.isEmpty()) return null;

            FontMetrics metrics = getFontMetrics(font);
            int width = metrics.stringWidth(text);
            int height = metrics.getHeight();

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(font);
            g2d.setColor(color.toAWTColor());
            g2d.drawString(text, 0, metrics.getAscent());
            g2d.dispose();

            return image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Fill background
            g2d.setColor(Color.DARK_BLUE.toAWTColor());
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // If EVC not connected, show indicator
            if (!evcConnected) {
                if (invalidEvcIndicator != null) {
                    g2d.drawImage(invalidEvcIndicator, 0, 0, null);
                }
                return;
            }

            // If train serie not selected, show indicator
            if (!serieSelected) {
                if (invalidSerieIndicator != null) {
                    g2d.drawImage(invalidSerieIndicator, 0, 20, null);
                }
                return;
            }

            // Display ETCS windows
            List<List<Integer>> alreadyDrawn = new ArrayList<>();
            for (Window window : activeWindows) {
                window.display(alreadyDrawn);
                alreadyDrawn.addAll(window.bounds);
            }
        }
    }
}