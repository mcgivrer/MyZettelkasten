package my.zettelkasten;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

class PreferencesDialog extends JDialog {

    public PreferencesDialog(TextEditorApp parent) {
        super(parent, true);
        setTitle(parent.bundle.getString("preferences.dialog.title"));
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Langue
        JLabel langLabel = new JLabel(parent.bundle.getString("preferences.language.label"));
        JComboBox<String> langCombo = new JComboBox<>(new String[]{"fr", "en"});
        langCombo.setSelectedItem(Locale.getDefault().getLanguage());

        // Fonts
        JLabel fontLabel = new JLabel("Police");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        JComboBox<String> fontCombo = new JComboBox<>(fonts);
        fontCombo.setSelectedItem(FontPreferences.getFontFamily());

        JLabel sizeLabel = new JLabel("Taille");
        JComboBox<Integer> sizeCombo = new JComboBox<>();
        for (int i = 8; i <= 36; i += 2) sizeCombo.addItem(i);
        sizeCombo.setSelectedItem(FontPreferences.getFontSize());

        // Ajout des champs
        formPanel.add(langLabel);
        formPanel.add(langCombo);
        formPanel.add(fontLabel);
        formPanel.add(fontCombo);
        formPanel.add(sizeLabel);
        formPanel.add(sizeCombo);

        add(formPanel, BorderLayout.CENTER);

        // Bouton OK
        JButton okButton = new JButton(parent.bundle.getString("preferences.ok"));
        okButton.addActionListener(e -> {
            String lang = (String) langCombo.getSelectedItem();
            if (lang != null) {
                Locale.setDefault(new Locale(lang));
            }

            String selectedFont = (String) fontCombo.getSelectedItem();
            int selectedSize = (Integer) sizeCombo.getSelectedItem();
            FontPreferences.setFont(selectedFont, selectedSize);
            ((TextEditorApp) parent).applyFontPreferencesToOpenTabs(); // ← ajout ici
            JOptionPane.showMessageDialog(this, TextEditorApp.bundle.getString("preferences.restart.info"));
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
