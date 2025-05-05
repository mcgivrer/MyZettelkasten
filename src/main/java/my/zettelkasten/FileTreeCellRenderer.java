package my.zettelkasten;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    private final Icon noteIcon = UIManager.getIcon("FileView.fileIcon");
    private final Icon draftIcon = UIManager.getIcon("FileView.directoryIcon");

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (userObject instanceof TextEditorApp.FileNode fileNode) {
            String[] name = extractDateAndTitle(fileNode.file.getName());

            String datetimePart = name[0];
            String titlePart = name[1];

            StringBuilder labelText = new StringBuilder("<html><b>").append(datetimePart).append("</b>");
            if (!datetimePart.isEmpty() && !titlePart.isEmpty()) {
                labelText.append(" - ");
            }
            if (!titlePart.isEmpty()) {
                labelText.append(titlePart.replaceAll("-", " "));
            }
            labelText.append("</html>");

            label.setText(labelText.toString());
            label.setIcon(titlePart.toLowerCase().contains("brouillon") ? draftIcon : noteIcon);
        }

        return label;
    }

    private String[] extractDateAndTitle(String filename) {
        // Enlève l’extension
        String name = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;

        // Match : 8 ou 12 chiffres en début (yyyyMMdd ou yyyyMMddHHmm)
        Pattern pattern = Pattern.compile("^(\\d{12}?)-(\\s?.*)");
        Matcher matcher = pattern.matcher(name);

        if (matcher.matches()) {
            String date = matcher.group(1);
            String title = matcher.group(2) != null ? matcher.group(2) : filename;
            return new String[]{date, title};
        }

        // fallback
        return new String[]{"", name};
    }

}
