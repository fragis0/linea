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
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JPanel paneleditor = new JPanel(new GridLayout(0, 1, 5, 5));
        setSize(500, 171);
        setResizable(false);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        JPanel appearance = new JPanel(new GridLayout(3, 1));
        showLineNumber = new JCheckBox("Show line number");
        antialiasing = new JCheckBox("Anti-aliasing");
        highlightCurrentLine = new JCheckBox("Highlight current line");
        currentLineFading = new JCheckBox("Fade current line highlight");

        JPanel editor = new JPanel(new GridLayout(3, 1));
        folding = new JCheckBox("Code folding");
        wordWrapping = new JCheckBox("Wrap words");
        hyperlinks = new JCheckBox("Allow hyperlinks");
        braces = new JCheckBox("Close braces");

        tabs.addTab("Appearance", panel);
        tabs.addTab("Editor", paneleditor);
        panel.add(showLineNumber);
        panel.add(antialiasing);
        panel.add(highlightCurrentLine);
        panel.add(currentLineFading);

        paneleditor.add(folding);
        paneleditor.add(wordWrapping);
        paneleditor.add(hyperlinks);
        paneleditor.add(braces);
        add(tabs, BorderLayout.PAGE_START);
    }
}
