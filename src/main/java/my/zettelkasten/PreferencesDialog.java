package my.zettelkasten;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

class PreferencesDialog extends JDialog {

    public PreferencesDialog(TextEditorApp parent) {
        super(parent, true);
        setTitle(parent.bundle.getString("preferences.dialog.title"));
        setLayout(new BorderLayout());

        JLabel label = new JLabel(parent.bundle.getString("preferences.language.label"));
        JComboBox<String> langCombo = new JComboBox<>(new String[]{"fr", "en"});

        // Langue courante sélectionnée
        langCombo.setSelectedItem(Locale.getDefault().getLanguage());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(label);
        form.add(langCombo);
        add(form, BorderLayout.CENTER);

        JButton okButton = new JButton(parent.bundle.getString("preferences.ok"));
        okButton.addActionListener(e -> {
            String lang = (String) langCombo.getSelectedItem();
            if (lang != null) {
                // Enregistre la langue sélectionnée (ex: fichier ou prefs Java)
                Locale newLocale = new Locale(lang);
                Locale.setDefault(newLocale);
                JOptionPane.showMessageDialog(this, parent.bundle.getString("preferences.restart.info"));
                dispose();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(okButton);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
