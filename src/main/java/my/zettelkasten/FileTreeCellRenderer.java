package my.zettelkasten;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

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
            String name = fileNode.file.getName();
            String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;

            String datetimePart = "";
            String titlePart = "";

            if (baseName.matches("^\\d{8}(\\d{4})?(\\s.*)?")) {
                int dateEnd = baseName.indexOf(' ');
                if (dateEnd == -1) {
                    datetimePart = baseName;
                } else {
                    datetimePart = baseName.substring(0, dateEnd);
                    titlePart = baseName.substring(dateEnd + 1);
                }
            } else {
                titlePart = baseName;
            }

            StringBuilder labelText = new StringBuilder("<html><b>").append(datetimePart).append("</b>");
            if (!titlePart.isEmpty()) {
                labelText.append(" &nbsp;|&nbsp; ").append(titlePart);
            }
            labelText.append("</html>");

            label.setText(labelText.toString());
            label.setIcon(baseName.toLowerCase().contains("brouillon") ? draftIcon : noteIcon);
        }

        return label;
    }
}
