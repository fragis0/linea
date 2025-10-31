import javax.swing.*;
import java.awt.*;

public class settings extends JDialog {
    public JCheckBox showLineNumber;
    public JCheckBox antialiasing;
    public JCheckBox highlightCurrentLine;
    public JCheckBox currentLineFading;

    public JCheckBox folding;
    public JCheckBox wordWrapping;
    public JCheckBox hyperlinks;
    public JCheckBox braces;

    public settings(JFrame parent) {
        super(parent, "Linea settings", true);
        JPanel panel = new JPanel();
        JPanel paneleditor = new JPanel();
        setSize(500, 171);
        setLocationRelativeTo(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        paneleditor.setLayout(new BoxLayout(paneleditor, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTabbedPane tabs = new JTabbedPane();
        showLineNumber = new JCheckBox("Show line number");
        antialiasing = new JCheckBox("Anti-aliasing");
        highlightCurrentLine = new JCheckBox("Highlight current line");
        currentLineFading = new JCheckBox("Fade current line highlight");

        folding = new JCheckBox("Code folding");
        wordWrapping = new JCheckBox("Wrap words");
        hyperlinks = new JCheckBox("Allow hyperlinks");
        braces = new JCheckBox("Close braces");

        tabs.addTab("Appearance", panel);
        tabs.addTab("Editor", paneleditor);
        panel.add(showLineNumber);
        showLineNumber.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(antialiasing);
        antialiasing.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(highlightCurrentLine);
        highlightCurrentLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(currentLineFading);
        currentLineFading.setAlignmentX(Component.LEFT_ALIGNMENT);

        paneleditor.add(folding);
        folding.setAlignmentX(Component.LEFT_ALIGNMENT);
        paneleditor.add(wordWrapping);
        wordWrapping.setAlignmentX(Component.LEFT_ALIGNMENT);
        paneleditor.add(hyperlinks);
        hyperlinks.setAlignmentX(Component.LEFT_ALIGNMENT);
        paneleditor.add(braces);
        braces.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(tabs);
    }
}
