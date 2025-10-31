import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

public class linea {

    private static File currentFile = null;
    private static int fontSize;
    private static UndoManager undoManager;
    public static settings settingswindow;

    public static void main(String[] args) {
        Properties syntaxes = new Properties();
        Properties config = new Properties();
        final Map<String, String> syntaxMap = new HashMap<>();
        boolean antialiasing;
        boolean hyperlinks;
        boolean highlight;
        boolean highlightfade;
        boolean wrapping;
        boolean linenumber;
        boolean braces;
        boolean folding;

        try (FileInputStream in = new FileInputStream("config.properties")) {
            config.load(in);
            fontSize = Integer.parseInt(config.getProperty("fontsize", "15"));
            antialiasing = Boolean.parseBoolean(config.getProperty("antialiasing", "true"));
            hyperlinks = Boolean.parseBoolean(config.getProperty("hyperlinks", "false"));
            highlight = Boolean.parseBoolean(config.getProperty("highlight", "true"));
            highlightfade = Boolean.parseBoolean(config.getProperty("highlightfade", "false"));
            wrapping = Boolean.parseBoolean(config.getProperty("wrapping", "false"));
            linenumber = Boolean.parseBoolean(config.getProperty("linenumber", "true"));
            braces = Boolean.parseBoolean(config.getProperty("braces", "true"));
            folding = Boolean.parseBoolean(config.getProperty("folding", "true"));
        } catch (IOException e) {
            fontSize = 15;
            antialiasing = true;
            hyperlinks = false;
            highlight = true;
            highlightfade = false;
            wrapping = false;
            linenumber = true;
            braces = true;
            folding = true;
            saveConfig(config);
        }

        try (FileInputStream inn = new FileInputStream("syntax.properties")) {
            syntaxes.load(inn);
            for (String key : syntaxes.stringPropertyNames()) {
                syntaxMap.put(key, syntaxes.getProperty(key));
            }
        } catch (IOException e) {
            int option = JOptionPane.showOptionDialog(null, "Syntaxes are not found! Fallback will be set to plain text (text/plain). You can install the syntax from GitHub by clicking OK.", "Linea", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[]{"OK", "Ignore"}, "OK");
            if (option == 0) {
                try (InputStream in = new URI("https://raw.githubusercontent.com/fragis0/linea/main/syntax.properties").toURL().openStream()) {
                    Files.copy(in, Paths.get("syntax.properties"), StandardCopyOption.REPLACE_EXISTING);
                    JOptionPane.showMessageDialog(null, "Syntax file installed successfully! Closing on OK.", "Linea", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                } catch (Exception ee) {
                    JOptionPane.showMessageDialog(null, "Can't download syntax file. Check your internet connection.", "Linea", JOptionPane.ERROR_MESSAGE);
                    ee.printStackTrace();
                    System.exit(1);
                }
            }
        }

        undoManager = new UndoManager();

	    JFrame frame = new JFrame("Linea");

        settingswindow = new settings(frame);
        settingswindow.antialiasing.setSelected(antialiasing);
        settingswindow.hyperlinks.setSelected(hyperlinks);
        settingswindow.highlightCurrentLine.setSelected(highlight);
        settingswindow.currentLineFading.setSelected(highlightfade);
        settingswindow.wordWrapping.setSelected(wrapping);
        settingswindow.showLineNumber.setSelected(linenumber);
        settingswindow.braces.setSelected(braces);
        settingswindow.folding.setSelected(folding);

        settingswindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                saveConfig(config);
                JOptionPane.showMessageDialog(frame, "To see the changes, restart the editor later.", "Linea", JOptionPane.WARNING_MESSAGE);
            }
        });

        settingswindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        setFont(new Font("Sans Serif", Font.PLAIN, fontSize));

        RSyntaxTextArea textwrite = new RSyntaxTextArea();

        textwrite.setSelectedTextColor(UIManager.getColor("TextArea.selectionForeground"));
        textwrite.setSelectionColor(UIManager.getColor("TextArea.selectionBackground"));
        textwrite.setCurrentLineHighlightColor(UIManager.getColor("TextArea.selectionBackground"));
        textwrite.setHighlightCurrentLine(highlight);
        textwrite.setFadeCurrentLineHighlight(highlightfade);
        textwrite.setCodeFoldingEnabled(folding);
        textwrite.setCloseCurlyBraces(braces);
        textwrite.setAntiAliasingEnabled(antialiasing);
        textwrite.setLineWrap(wrapping);
        textwrite.setHyperlinksEnabled(hyperlinks);

        textwrite.getDocument().addUndoableEditListener(undoManager);
        undoManager.maxBufferSize = 10;

        if (args.length > 0) {
            String filename = args[0];
            File file = new File(filename);

            if (file.exists()) {
                try {
                    String content = Files.readString(file.toPath());
                    textwrite.setText(content);
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(frame, "Error reading file!", "Linea", JOptionPane.OK_CANCEL_OPTION);
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "File not found!", "Linea", JOptionPane.WARNING_MESSAGE);
            }
        }

        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu view = new JMenu("View");
        JMenu edit = new JMenu("Edit");
        JMenu help = new JMenu("Help");

        JMenuItem save = new JMenuItem("Save", null);
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        JMenuItem open = new JMenuItem("Open", null);
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        JMenuItem exit = new JMenuItem("Exit", null);
        JMenuItem settingsitem = new JMenuItem("Settings", null);
        JMenuItem syntaxchooser = new JMenuItem("Change syntax", null);

        JMenuItem zoomin = new JMenuItem("Zoom in", null);
        zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
        JMenuItem zoomout = new JMenuItem("Zoom out", null);
        zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));

        JMenuItem undo = new JMenuItem("Undo", null);
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        JMenuItem redo = new JMenuItem("Redo", null);
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));

        JMenuItem report = new JMenuItem("Report issue", null);
        JMenuItem about = new JMenuItem("About", null);

        file.add(save);
        file.add(open);
        file.addSeparator();
        file.add(exit);

        view.add(zoomin);
        view.add(zoomout);

        edit.add(undo);
        edit.add(redo);
        edit.add(settingsitem);
        edit.add(syntaxchooser);

        help.add(report);
        help.addSeparator();
        help.add(about);

        open.addActionListener(e -> {
            File openedFile = openFile(frame);

            assert openedFile != null;
            if (openedFile.exists()) {
                try {
                    String content = Files.readString(openedFile.toPath());
                    textwrite.setText(content);
                    undoManager.reset();
                    String name = openedFile.getName();
                    int lastDot = name.lastIndexOf(".");
                    String ext = (lastDot == -1) ? "" : name.substring(lastDot + 1);
                    textwrite.setSyntaxEditingStyle(syntaxMap.getOrDefault(ext, "text/plain"));
                } catch (IOException ex) {
                    JOptionPane.showConfirmDialog(frame, "Error reading file!", "Linea", JOptionPane.OK_CANCEL_OPTION);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "File not found!", "Linea", JOptionPane.WARNING_MESSAGE);
            }
        });

        save.addActionListener(e -> {
            File fileItem = currentFile != null ? currentFile : saveFile(frame);
            if (fileItem != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileItem))) {
                    writer.write(textwrite.getText());
                    currentFile = fileItem;
                    String name = currentFile.getName();
                    int lastDot = name.lastIndexOf(".");
                    String ext = (lastDot == -1) ? "" : name.substring(lastDot + 1);
                    textwrite.setSyntaxEditingStyle(syntaxMap.getOrDefault(ext, "text/plain"));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving file!", "Linea", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        settingsitem.addActionListener(e -> settingswindow.setVisible(true));

        syntaxchooser.addActionListener(e -> {
            String syntax = JOptionPane.showInputDialog("Enter syntax (ex. c, java, html, plain, asm, etc)");
            textwrite.setSyntaxEditingStyle("text/" + syntax);
        });

        zoomout.addActionListener(e -> {
            fontSize = Math.max(5, fontSize - 5);
            textwrite.setFont(new Font("Sans Serif", Font.PLAIN, fontSize));
            saveConfig(config);
        });
        
        zoomin.addActionListener(e -> {
            fontSize = Math.min(50, fontSize + 5);
            textwrite.setFont(new Font("Sans Serif", Font.PLAIN, fontSize));
            saveConfig(config);
        });

        exit.addActionListener(e -> System.exit(0));

        report.addActionListener(e -> {
            try {
                String url = "https://github.com/fragis0/linea/issues/new";
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            } catch (java.io.IOException ex) {
                System.out.println(ex.getMessage());
            }
        });

        about.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Linea is a simple Java text editing software with syntax highlighting.", "Linea", JOptionPane.INFORMATION_MESSAGE));

        undo.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        });

        redo.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        });

        menubar.add(file);
        menubar.add(view);
        menubar.add(edit);
        menubar.add(help);
        frame.setJMenuBar(menubar);

        RTextScrollPane scroll = new RTextScrollPane(textwrite);
        scroll.setLineNumbersEnabled(linenumber);
        textwrite.setSyntaxEditingStyle("text/plain");

        frame.getContentPane().add(scroll, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public static void saveConfig(Properties config) {
        try (FileOutputStream out = new FileOutputStream("config.properties")) {
            config.setProperty("antialiasing", Boolean.toString(settingswindow.antialiasing.isSelected()));
            config.setProperty("hyperlinks", Boolean.toString(settingswindow.hyperlinks.isSelected()));
            config.setProperty("highlight", Boolean.toString(settingswindow.highlightCurrentLine.isSelected()));
            config.setProperty("highlightfade", Boolean.toString(settingswindow.currentLineFading.isSelected()));
            config.setProperty("wrapping", Boolean.toString(settingswindow.wordWrapping.isSelected()));
            config.setProperty("linenumber", Boolean.toString(settingswindow.showLineNumber.isSelected()));
            config.setProperty("braces", Boolean.toString(settingswindow.braces.isSelected()));
            config.setProperty("folding", Boolean.toString(settingswindow.folding.isSelected()));

            config.setProperty("fontsize", Integer.toString(fontSize));
            config.store(out, " settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSyntaxes(Properties config, Map<String, String> syntaxmap) {
        try (FileOutputStream out = new FileOutputStream("syntax.properties")) {
            config.putAll(syntaxmap);
            config.store(out, " syntax mapping");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setFont(Font font) {

        UIManager.put("TextArea.font", font);
    }

    public static File openFile(JFrame fr) {

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(fr);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    public static File saveFile(JFrame fr) {

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(fr);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    public static class UndoManager extends AbstractUndoableEdit implements UndoableEditListener {
        private String lastEditName = null;
        private final List<MergeComponentEdit> edits = new ArrayList<>(32);
        private MergeComponentEdit current;
        private int pointer = -1;
        private int maxBufferSize = 50;

        private final List<ChangeListener> changeListeners = new ArrayList<>(8);

        public void reset() {
            edits.clear();
            pointer = -1;
            lastEditName = null;
            current = null;
            fireStateChanged();
        }

        public void addChangeListener(ChangeListener changeListener) {
            changeListeners.add(changeListener);
        }

        public void removeChangeListener(ChangeListener changeListener) {
            changeListeners.remove(changeListener);
        }

        public void undoableEditHappened(UndoableEditEvent e) {
            UndoableEdit edit = e.getEdit();
            if (edit instanceof AbstractDocument.DefaultDocumentEvent event) {
                try {
                    int start = event.getOffset();
                    int len = event.getLength();
                    if (start + len > event.getDocument().getLength()) {
                        createCompoundEdit();
                    } else {

                        String text = event.getDocument().getText(start, len);
                        boolean isNeedStart = false;
                        if (current == null) {
                            isNeedStart = true;
                        } else if (text.contains(" ")) {
                            isNeedStart = true;
                        } else if (lastEditName == null || !lastEditName.equals(edit.getPresentationName())) {
                            isNeedStart = true;
                        }

                        while (pointer < edits.size() - 1) {
                            edits.removeLast();
                            isNeedStart = true;
                        }
                        if (isNeedStart) {
                            createCompoundEdit();
                        }

                    }
                    current.addEdit(edit);
                    lastEditName = edit.getPresentationName();
                    fireStateChanged();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }

        public void createCompoundEdit() {
            if (current == null || current.getLength() > 0) {
                current = new MergeComponentEdit();
                if (edits.size() >= maxBufferSize) {
                    edits.removeFirst();
                    if (pointer > 0) {
                        pointer--;
                    } else {
                        pointer = -1;
                    }
                }
                edits.add(current);
                pointer = edits.size() - 1;
            }
        }

        public void undo() throws CannotUndoException {
            if (!canUndo()) {
                throw new CannotUndoException();
            }

            MergeComponentEdit u = edits.get(pointer);
            u.undo();
            pointer--;

            fireStateChanged();
        }

        public void redo() throws CannotUndoException {
            if (!canRedo()) {
                throw new CannotUndoException();
            }

            pointer++;
            MergeComponentEdit u = edits.get(pointer);
            u.redo();

            fireStateChanged();
        }

        public boolean canUndo() {
            return pointer >= 0;
        }

        public boolean canRedo() {
            return !edits.isEmpty() && pointer < edits.size() - 1;
        }

        protected void fireStateChanged() {
            if (changeListeners.isEmpty()) {
                return;
            }
            ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener listener : changeListeners) {
                listener.stateChanged(evt);
            }
        }

        protected static class MergeComponentEdit extends CompoundEdit {
            boolean isUnDone = false;

            public int getLength() {
                return edits.size();
            }

            public void undo() throws CannotUndoException {
                super.undo();
                isUnDone = true;
            }

            public void redo() throws CannotUndoException {
                super.redo();
                isUnDone = false;
            }

            public boolean canUndo() {
                return !edits.isEmpty() && !isUnDone;
            }

            public boolean canRedo() {
                return !edits.isEmpty() && isUnDone;
            }

        }
    }
}
