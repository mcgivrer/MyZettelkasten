package my.zettelkasten;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class FileTab extends JPanel {
    private final File file;
    private final JTextArea textArea;

    public FileTab(File file, String content) {
        super(new BorderLayout());
        this.file = file;
        this.textArea = new JTextArea(content);
        this.textArea.setFont(FontPreferences.getFont());

        add(new JScrollPane(textArea), BorderLayout.CENTER);
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
}
