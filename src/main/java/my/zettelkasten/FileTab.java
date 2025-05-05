package my.zettelkasten;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class FileTab extends JPanel {
    private File file;
    private JTextArea textArea;
    MarkdownPreviewPanel previewPanel;
    private final JToggleButton togglePreview;
    private final JPanel contentPanel;
    private boolean modified = false;

    public FileTab(File file, String content) {
        super(new BorderLayout());
        this.file = file;

        this.textArea = new JTextArea(content);
        this.textArea.setFont(FontPreferences.getFont());

        this.previewPanel = new MarkdownPreviewPanel();
        this.previewPanel.updateMarkdown(content);

        this.togglePreview = new JToggleButton("👁");
        togglePreview.setToolTipText(TextEditorApp.bundle.getString("preview.toggle.tooltip"));
        this.togglePreview.setFocusable(false);

        // Barre supérieure
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(togglePreview);
        add(topBar, BorderLayout.NORTH);

        // Panel central (modulable)
        contentPanel = new JPanel(new BorderLayout());
        JScrollPane scrollText = new JScrollPane(textArea);
        contentPanel.add(scrollText, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Suivi des changements texte → preview
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void changedUpdate(DocumentEvent e) {
                updatePreview();

            }

            private void updatePreview() {
                if (!modified) {
                    modified = true;
                }
                SwingUtilities.invokeLater(() -> {
                    previewPanel.updateMarkdown(textArea.getText());
                    updateTabHeaderFont(Font.ITALIC);
                });
            }
        });

        // Basculer affichage preview
        togglePreview.addActionListener(e -> updateSplit());
    }

    public void updateTabHeaderFont(int style) {
        int index = ((JTabbedPane) getParent()).indexOfComponent(this);
        if (index != -1) {
            Component tabComponent = ((JTabbedPane) getParent()).getTabComponentAt(index);
            if (tabComponent instanceof JPanel tabPanel) {
                for (Component comp : tabPanel.getComponents()) {
                    if (comp instanceof JLabel label) {
                        Font current = label.getFont();
                        label.setFont(current.deriveFont(style));
                        break;
                    }
                }
            }
        }
    }

    public void markSaved() {
        this.modified = false;
        updateTabHeaderFont(Font.PLAIN);
    }

    private void updateSplit() {
        contentPanel.removeAll();

        if (togglePreview.isSelected()) {
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    new JScrollPane(textArea), previewPanel);
            split.setResizeWeight(0.5);
            contentPanel.add(split, BorderLayout.CENTER);
        } else {
            contentPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setFont(Font font) {
        if (textArea != null) {
            textArea.setFont(font);
        }
    }

    public File getFile() {
        return file;
    }

    public String getContent() {
        return textArea.getText();
    }

    public void setContent(String content) {
        textArea.setText(content);
    }

    public boolean isModified() {
        try {
            return !Files.readString(file.toPath()).equals(textArea.getText());
        } catch (IOException e) {
            return true;
        }
    }

    public void setFile(File file) {
        this.file = file;
    }

}
