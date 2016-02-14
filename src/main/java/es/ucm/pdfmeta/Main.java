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

package es.ucm.pdfmeta;

import es.ucm.pdfmeta.controller.Controller;
import es.ucm.pdfmeta.gui.MainDialog;
import es.ucm.pdfmeta.model.MetadataModel;
import static es.ucm.pdfmeta.model.MetadataModel.AUTHOR_PROPERTY_NAME;
import static es.ucm.pdfmeta.model.MetadataModel.BIBTEX_PROPERTY_NAME;
import static es.ucm.pdfmeta.model.MetadataModel.TITLE_PROPERTY_NAME;
import es.ucm.pdfmeta.model.MetadataProperty;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author Manuel Montenegro (mmontene@ucm.es)
 */
public class Main {
    public static void main(String[] args) {
        PDDocument doc = null;
        try {
            if (args.length > 0) {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
                for (String arg : args) {
                    File f = new File(arg);
                    doc = PDDocument.load(f);
                    MetadataModel<String> m = buildModelFromDocument(doc);
                    Controller c = new Controller(m);

                    MainDialog md = new MainDialog(arg, m, c);
                    if (md.runDialog()) {
                        modifyDocFromModel(doc, m);
                        doc.save(arg);
                    } 
                }
            } else {
                System.err.println("error: no input file(s) specified");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (doc != null) try { doc.close(); } catch (IOException ex) {}
        }
    

    }

    private static void modifyDocFromModel(PDDocument doc, MetadataModel<String> m) {
        doc.getDocumentInformation().setAuthor(m.getProperty(AUTHOR_PROPERTY_NAME).getValue());
        doc.getDocumentInformation().setTitle(m.getProperty(TITLE_PROPERTY_NAME).getValue());
        doc.getDocumentInformation().setCustomMetadataValue(BIBTEX_PROPERTY_NAME, m.getProperty(BIBTEX_PROPERTY_NAME).getValue());
    }

    private static MetadataModel<String> buildModelFromDocument(PDDocument doc) {
        MetadataModel<String> m = new MetadataModel<>();
        m.setProperty(AUTHOR_PROPERTY_NAME, new MetadataProperty<>(
                AUTHOR_PROPERTY_NAME,
                doc.getDocumentInformation().getAuthor()
        ));
        m.setProperty(TITLE_PROPERTY_NAME, new MetadataProperty<>(
                TITLE_PROPERTY_NAME,
                doc.getDocumentInformation().getTitle()
        ));
        m.setProperty(BIBTEX_PROPERTY_NAME, new MetadataProperty<>(
                BIBTEX_PROPERTY_NAME,
                doc.getDocumentInformation().getCustomMetadataValue(BIBTEX_PROPERTY_NAME)
        ));
        return m;
    }

}
