/*
 * The MIT License
 *
 * Copyright 2016 Manuel Montenegro (mmontene@ucm.es).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package es.ucm.pdfmeta.gui;

import es.ucm.pdfmeta.controller.Controller;
import es.ucm.pdfmeta.model.MetadataModel;
import static es.ucm.pdfmeta.model.MetadataModel.AUTHOR_PROPERTY_NAME;
import static es.ucm.pdfmeta.model.MetadataModel.TITLE_PROPERTY_NAME;
import static es.ucm.pdfmeta.model.MetadataModel.BIBTEX_PROPERTY_NAME;
import es.ucm.pdfmeta.model.PropertyListener;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;


/**
 *
 * @author Manuel Montenegro (mmontene@ucm.es)
 */
public class MainDialog extends javax.swing.JDialog {
    private boolean saved = false;
    
    /**
     * Creates new form NewJDialog
     */
    MainDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.getRootPane().setDefaultButton(setMetadataButton);
    }
    
    public MainDialog(String fileName, MetadataModel<String> model, Controller controller) {
        this(null, true);
        setTitle("Set metadata: " + fileName);
        authorsField.setText(model.getProperty("author").getValue());
        titleField.setText(model.getProperty("title").getValue());
        bibtexArea.setText(model.getProperty("bibtex").getValue());
        
        final MainDialog that = this;
        
        model.getProperty("author").addPropertyListener(new PropertyListener<String>() {
            @Override
            public void propertyChanged(String name, String oldValue, String newValue) {
                authorsField.setText(newValue);
            }
        });

        model.getProperty("title").addPropertyListener(new PropertyListener<String>() {
            @Override
            public void propertyChanged(String name, String oldValue, String newValue) {
                titleField.setText(newValue);
            }
        });
        
        model.getProperty("bibtex").addPropertyListener(new PropertyListener<String>() {
            @Override
            public void propertyChanged(String name, String oldValue, String newValue) {
                bibtexArea.setText(newValue);
            }
        });
        
        setMetadataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saved = true;
                controller.changeProperty(AUTHOR_PROPERTY_NAME, authorsField.getText());
                controller.changeProperty(TITLE_PROPERTY_NAME, titleField.getText());
                controller.changeProperty(BIBTEX_PROPERTY_NAME, bibtexArea.getText());
                that.dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saved = false;
                that.dispose();
            }
        });
        
        fillinButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard cl = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tr = cl.getContents(null);
                if (tr != null && tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        String data = (String)
                                tr.getTransferData(DataFlavor.stringFlavor);
                        data = data.trim();
                        if (data.startsWith("@")) {
                            String authors = extractAuthors(data);
                            String title = extractTitle(data);
                            
                            if (authors != null) controller.changeProperty(AUTHOR_PROPERTY_NAME, authors);
                            if (title != null) controller.changeProperty(TITLE_PROPERTY_NAME, title);
                            controller.changeProperty(BIBTEX_PROPERTY_NAME, data);
                        } else {
                            JOptionPane.showMessageDialog(null, "Clipboard does not contain a bibtex entry.");
                        }
                    } catch (Exception ex) { } 
                } else {
                    JOptionPane.showMessageDialog(null, "Clipboard contents cannot be converted into a string");
                }
            }

            private String extractTitle(String data) {
                String title = null;
                Matcher m = TITLE_PATTERN_QUOTES.matcher(data);
                if (m.matches()) {
                    title = m.group(1).trim();
                    title = title.replace("{", "");
                    title = title.replace("}", "");
                } else {
                    m = TITLE_PATTERN_BRACES.matcher(data);
                    if (m.matches()) {
                        title = m.group(1).trim();
                    }
                }
                return title;
            }

            private String extractAuthors(String data) {
                Matcher m = AUTHOR_PATTERN.matcher(data);
                if (m.matches()) {
                    String authorsStr = m.group(1);
                    String[] authors = authorsStr.split("and");
                    for (int i = 0; i < authors.length; i++) {
                        String current = authors[i];
                        int commaPos = current.indexOf(',');
                        if (commaPos != -1) {
                            String surname = current.substring(0, commaPos).trim();
                            String firstName = current.substring(commaPos + 1).trim();
                            authors[i] = firstName + " " + surname;
                        } else {
                            authors[i] = current.trim();
                        }
                    }
                    return String.join(", ", authors);
                }
                return null;
            }
        });
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                saved = false;
            }
        });
        
    }
    private static final Pattern AUTHOR_PATTERN = Pattern.compile(".*author\\s*=\\s*[\"\\{]([^\"\\{\\}]*)[\"\\}].*",
                Pattern.DOTALL);
    private static final Pattern TITLE_PATTERN_QUOTES = Pattern.compile(".*title\\s*=\\s*\"([^\"]*)\".*",
                Pattern.DOTALL);
    private static final Pattern TITLE_PATTERN_BRACES = Pattern.compile(".*title\\s*=\\s*\\{([^\\}]*)\\}.*",
                Pattern.DOTALL);

    public boolean runDialog() {
        saved = false;
        setVisible(true);
        return saved;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dataPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        titleField = new javax.swing.JTextField();
        authorsLabel = new javax.swing.JLabel();
        authorsField = new javax.swing.JTextField();
        bibtexLabel = new javax.swing.JLabel();
        bibtexSP = new javax.swing.JScrollPane();
        bibtexArea = new javax.swing.JTextArea();
        fillinButton = new javax.swing.JButton();
        controllerPanel = new javax.swing.JPanel();
        setMetadataButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit PDF Metadata");
        setLocationByPlatform(true);

        dataPanel.setLayout(new java.awt.GridBagLayout());

        titleLabel.setDisplayedMnemonic('T');
        titleLabel.setLabelFor(titleField);
        titleLabel.setText("Title:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        dataPanel.add(titleLabel, gridBagConstraints);

        titleField.setColumns(50);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        dataPanel.add(titleField, gridBagConstraints);

        authorsLabel.setDisplayedMnemonic('A');
        authorsLabel.setLabelFor(authorsField);
        authorsLabel.setText("Authors:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        dataPanel.add(authorsLabel, gridBagConstraints);

        authorsField.setColumns(50);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        dataPanel.add(authorsField, gridBagConstraints);

        bibtexLabel.setDisplayedMnemonic('B');
        bibtexLabel.setLabelFor(bibtexArea);
        bibtexLabel.setText("BibTeX reference");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        dataPanel.add(bibtexLabel, gridBagConstraints);

        bibtexArea.setColumns(20);
        bibtexArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        bibtexArea.setRows(5);
        bibtexSP.setViewportView(bibtexArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        dataPanel.add(bibtexSP, gridBagConstraints);

        fillinButton.setMnemonic('F');
        fillinButton.setText("Fill in from clipboard");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        dataPanel.add(fillinButton, gridBagConstraints);

        getContentPane().add(dataPanel, java.awt.BorderLayout.CENTER);

        setMetadataButton.setMnemonic('S');
        setMetadataButton.setText("Save changes");
        controllerPanel.add(setMetadataButton);

        cancelButton.setMnemonic('C');
        cancelButton.setText("Close");
        controllerPanel.add(cancelButton);

        getContentPane().add(controllerPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MainDialog dialog = new MainDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField authorsField;
    private javax.swing.JLabel authorsLabel;
    private javax.swing.JTextArea bibtexArea;
    private javax.swing.JLabel bibtexLabel;
    private javax.swing.JScrollPane bibtexSP;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controllerPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JButton fillinButton;
    private javax.swing.JButton setMetadataButton;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
