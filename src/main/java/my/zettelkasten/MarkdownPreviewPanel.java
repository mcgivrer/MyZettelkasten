package my.zettelkasten;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import javax.swing.*;
import java.awt.*;

public class MarkdownPreviewPanel extends JPanel {
    private final JEditorPane htmlPane;
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownPreviewPanel() {
        super(new BorderLayout());

        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();

        htmlPane = new JEditorPane();
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.setText("<html><body><i>Aucune prévisualisation</i></body></html>");

        add(new JScrollPane(htmlPane), BorderLayout.CENTER);
    }

    public void updateMarkdown(String markdownText) {
        Node document = parser.parse(markdownText);
        String html = renderer.render(document);
        htmlPane.setText(html);
        htmlPane.setCaretPosition(0); // scroll en haut
    }
}
