package my.zettelkasten;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class TextEditorApp extends JFrame {

    public static class FileNode {
        final File file;

        FileNode(File file) {
            this.file = file;
        }

        public String toString() {
            return file.getName();
        }
    }


    private JList<File> fileList = null;
    private final DefaultListModel<File> listModel;
    private final JTextArea textArea;
    private final JFileChooser fileChooser;
    private File currentDirectory;
    private File currentFile;
    public final ResourceBundle bundle;
    private JTabbedPane tabbedPane;
    private JMenuItem closeTabItem; // stocké pour mise à jour d’état
    private final java.util.List<File> allFiles = new ArrayList<>();
    private final JTextField searchField = new JTextField();


    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Fichiers");
    private final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    private final JTree fileTree = new JTree(treeModel);


    public TextEditorApp() {
        bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        setTitle(bundle.getString("app.title"));

        listModel = new DefaultListModel<>();

        searchField.setToolTipText("Rechercher un fichier...");
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(searchField, BorderLayout.NORTH);
        treePanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);
        fileTree.setCellRenderer(new FileTreeCellRenderer());

        fileTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) return;

                    Object nodeObj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                    if (nodeObj instanceof FileNode fileNode) {
                        openFile(fileNode.file);
                    }
                }
            }
        });

        tabbedPane = new JTabbedPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, tabbedPane);
        splitPane.setDividerLocation(200);
        tabbedPane.addChangeListener(e -> updateMenuState());
        updateMenuState(); // init

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterFiles();
            }

            public void removeUpdate(DocumentEvent e) {
                filterFiles();
            }

            public void changedUpdate(DocumentEvent e) {
                filterFiles();
            }
        });

        add(splitPane, BorderLayout.CENTER);

        textArea = new JTextArea();
        JScrollPane editorScrollPane = new JScrollPane(textArea);

        add(splitPane, BorderLayout.CENTER);
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        setupMenuBar();
        setupPopupMenu();

        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    fileList.setSelectedIndex(fileList.locationToIndex(evt.getPoint()));
                } else if (evt.getClickCount() == 2) {
                    File selectedFile = fileList.getSelectedValue();
                    if (selectedFile != null && selectedFile.isFile()) {
                        openFile(selectedFile);
                    }
                }
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void rebuildTree() {
        rootNode.removeAllChildren();
        String query = searchField.getText().toLowerCase().trim();

        Map<String, DefaultMutableTreeNode> groups = new LinkedHashMap<>();

        for (File file : allFiles) {
            String name = file.getName().toLowerCase();
            if (!name.contains(query)) continue;

            String group = computeGroupLabel(file);
            groups.computeIfAbsent(group, g -> {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(g);
                rootNode.add(node);
                return node;
            }).add(new DefaultMutableTreeNode(new FileNode(file)));
        }

        treeModel.reload();
        fileTree.expandRow(0);
    }

    private String computeGroupLabel(File file) {
        long sortKey = extractDateSortKey(file);
        if (sortKey == 0) return "📂 Inclassables";

        try {
            String keyStr = String.valueOf(sortKey);
            LocalDate fileDate;

            if (keyStr.length() >= 12) {
                LocalDateTime dateTime = LocalDateTime.parse(keyStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                fileDate = dateTime.toLocalDate();
            } else {
                fileDate = LocalDate.parse(keyStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }

            LocalDate today = LocalDate.now();

            if (fileDate.isEqual(today)) {
                return "🗓 Aujourd’hui";
            }

            LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
            if (!fileDate.isBefore(monday)) {
                return "📆 Cette semaine";
            }

            return "📂 Plus anciens";

        } catch (DateTimeParseException e) {
            return "📂 Inclassables";
        }
    }

    private void filterFiles() {
        String query = searchField.getText().toLowerCase().trim();
        listModel.clear();

        for (File file : allFiles) {
            String name = file.getName().toLowerCase();
            if (name.contains(query)) {
                listModel.addElement(file);
            }
        }
    }


    private void updateMenuState() {
        boolean hasTabs = tabbedPane.getTabCount() > 0;
        if (closeTabItem != null) {
            closeTabItem.setEnabled(hasTabs);
        }
    }

    private void openFileInTab(File file) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            FileTab tab = (FileTab) tabbedPane.getComponentAt(i);
            if (tab.getFile().equals(file)) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }

        try {
            String content = Files.readString(file.toPath());
            FileTab fileTab = new FileTab(file, content);
            tabbedPane.addTab(file.getName(), fileTab);
            int index = tabbedPane.indexOfComponent(fileTab);
            tabbedPane.setTabComponentAt(index, createTabHeader(file.getName(), fileTab));
            tabbedPane.setSelectedComponent(fileTab);
        } catch (IOException e) {
            showError("file.read.error", e.getMessage());
        }
    }

    private Component createTabHeader(String fullTitle, FileTab tab) {
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);

        String shortTitle = fullTitle.length() > 15 ? fullTitle.substring(0, 15) + "…" : fullTitle;

        JLabel titleLabel = new JLabel(shortTitle + " ");
        titleLabel.setToolTipText(fullTitle); // ← ajout tooltip

        JButton closeButton = new JButton("×");
        closeButton.setMargin(new Insets(0, 2, 0, 2));
        closeButton.setFocusable(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD));
        closeButton.setForeground(Color.RED);
        closeButton.setToolTipText(bundle.getString("tab.close.tooltip"));

        closeButton.addActionListener(e -> closeTab(tab));

        tabHeader.add(titleLabel);
        tabHeader.add(closeButton);

        return tabHeader;
    }

    private void closeTab(FileTab tab) {
        if (tab.isModified()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    bundle.getString("confirm.quit.message"),
                    bundle.getString("confirm.quit.title"),
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                return;
            } else if (choice == JOptionPane.YES_OPTION) {
                try {
                    Files.writeString(tab.getFile().toPath(), tab.getContent());
                } catch (IOException e) {
                    showError("file.save.error", e.getMessage());
                    return;
                }
            }
        }

        tabbedPane.remove(tab);
    }


    private void setupPreferencesMenu(JMenuBar menuBar) {
        JMenu preferencesMenu = new JMenu(bundle.getString("menu.preferences"));

        JMenuItem preferencesItem = new JMenuItem(bundle.getString("menu.preferences"));
        preferencesItem.addActionListener(e -> new PreferencesDialog(this));

        preferencesMenu.add(preferencesItem);
        menuBar.add(preferencesMenu);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(bundle.getString("menu.file"));

        JMenuItem chooseDirItem = new JMenuItem(bundle.getString("menu.choose.directory"));
        chooseDirItem.addActionListener(e -> chooseDirectory());

// Nouveau
        JMenuItem newFileItem = new JMenuItem(bundle.getString("menu.file.new"));
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newFileItem.addActionListener(e -> newEmptyTab());
        fileMenu.add(newFileItem);

        JMenuItem openItem = new JMenuItem(bundle.getString("menu.file.open"));
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFileDialog());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem(bundle.getString("menu.file.save"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveCurrentTab());
        fileMenu.add(saveItem);

// Fermer l’onglet
        closeTabItem = new JMenuItem(bundle.getString("menu.file.closeTab"));
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        closeTabItem.addActionListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof FileTab tab) {
                closeTab(tab);
            }
        });
        fileMenu.add(closeTabItem);

// Quitter
        JMenuItem exitItem = new JMenuItem(bundle.getString("menu.file.exit"));
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> confirmAndExit());
        fileMenu.add(exitItem);
        fileMenu.add(chooseDirItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(closeTabItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setupPreferencesMenu(menuBar);

        setJMenuBar(menuBar);
    }

    private void newEmptyTab() {
        File tempFile = new File("Nouveau " + (tabbedPane.getTabCount() + 1));
        FileTab fileTab = new FileTab(tempFile, "");
        tabbedPane.addTab(tempFile.getName(), fileTab);
        int index = tabbedPane.indexOfComponent(fileTab);
        tabbedPane.setTabComponentAt(index, createTabHeader(tempFile.getName(), fileTab));
        tabbedPane.setSelectedComponent(fileTab);
    }

    private void setupPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem openItem = new JMenuItem(bundle.getString("menu.open"));
        openItem.addActionListener(e -> {
            File selected = fileList.getSelectedValue();
            if (selected != null && selected.isFile()) openFile(selected);
        });

        JMenuItem saveItem = new JMenuItem(bundle.getString("menu.save"));
        saveItem.addActionListener(e -> saveFile(currentFile));

        JMenuItem quitItem = new JMenuItem(bundle.getString("menu.quit"));
        quitItem.addActionListener(e -> confirmAndExit());

        popup.add(openItem);
        popup.add(saveItem);
        popup.addSeparator();
        popup.add(quitItem);

        fileList.setComponentPopupMenu(popup);
    }

    private void chooseDirectory() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setDialogTitle(bundle.getString("menu.choose.directory"));

        int result = dirChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentDirectory = dirChooser.getSelectedFile();
            loadFiles(currentDirectory);
        }
    }

    private void loadFiles(File directory) {
        allFiles.clear();
        listModel.clear();

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".java"));
        if (files == null) return;

        Arrays.sort(files, Comparator.comparingLong(this::extractDateSortKey).reversed());
        allFiles.addAll(List.of(files));

        rebuildTree(); // affiche la liste filtrée
    }


    private long extractDateSortKey(File file) {
        String name = file.getName();
        String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;

        String datetimePart = "";
        if (baseName.matches("^\\d{8}(\\d{4})?\\s?.*")) {
            int end = baseName.indexOf(' ');
            datetimePart = end == -1 ? baseName : baseName.substring(0, end);
        }

        try {
            return Long.parseLong(datetimePart);
        } catch (NumberFormatException e) {
            return 0; // fichiers sans date -> en bas
        }
    }

    private void openFileDialog() {
        if (currentDirectory != null)
            fileChooser.setCurrentDirectory(currentDirectory);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            openFile(fileChooser.getSelectedFile());
        }
    }

    private void openFile(File file) {
        openFileInTab(file);
    }

    private void saveFile(File file) {
        if (file == null) {
            file = chooseSaveLocation();
            if (file == null) return;
        }

        try {
            Files.writeString(file.toPath(), textArea.getText());
            JOptionPane.showMessageDialog(this, bundle.getString("file.save.success"));
            currentFile = file;
        } catch (IOException e) {
            showError("file.save.error", e.getMessage());
        }
    }

    private void saveCurrentTab() {
        Component selected = tabbedPane.getSelectedComponent();
        if (selected instanceof FileTab tab) {
            try {
                Files.writeString(tab.getFile().toPath(), tab.getContent());
                JOptionPane.showMessageDialog(this, bundle.getString("file.save.success"));
            } catch (IOException e) {
                showError("file.save.error", e.getMessage());
            }
        }
    }

    private File chooseSaveLocation() {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void confirmAndExit() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            FileTab tab = (FileTab) tabbedPane.getComponentAt(i);
            if (tab.isModified()) {
                int choice = JOptionPane.showOptionDialog(
                        this,
                        bundle.getString("confirm.quit.message"),
                        bundle.getString("confirm.quit.title"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{
                                bundle.getString("confirm.quit.save"),
                                bundle.getString("confirm.quit.exit"),
                                bundle.getString("confirm.quit.cancel")
                        },
                        null
                );

                if (choice == 0) {
                    try {
                        Files.writeString(tab.getFile().toPath(), tab.getContent());
                    } catch (IOException e) {
                        showError("file.save.error", e.getMessage());
                        return;
                    }
                } else if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
                    return;
                }
            }
        }

        System.exit(0);
    }

    private void showError(String key, String detail) {
        String msg = MessageFormat.format(bundle.getString(key), detail);
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    static class FileCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            File file = (File) value;
            return super.getListCellRendererComponent(list, file.getName(), index, isSelected, cellHasFocus);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextEditorApp::new);
    }
}
