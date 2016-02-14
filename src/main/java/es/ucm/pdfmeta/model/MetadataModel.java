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

package es.ucm.pdfmeta.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author Manuel Montenegro (mmontene@ucm.es)
 */
public class MetadataModel<T> {
    public static final String BIBTEX_PROPERTY_NAME = "bibtex";
    public static final String TITLE_PROPERTY_NAME = "title";
    public static final String AUTHOR_PROPERTY_NAME = "author";

    private final Map<String, MetadataProperty<T>> properties;

    public MetadataModel() {
        this.properties = new HashMap<>();
        /*
        properties.put("author", new MetadataProperty<>(
                "author",
                doc.getDocumentInformation().getAuthor()
        ));
        this.title = new MetadataProperty<>(
                "title",
                doc.getDocumentInformation().getTitle()
        );
        this.bibtex = new MetadataProperty<>(
                "bibtex",
                doc.getDocumentInformation().getCustomMetadataValue("bibtex")
        );
                */
    }

    public void setProperty(String name, MetadataProperty<T> property) {
        properties.put(name, property);
    }
    
    public MetadataProperty<T> getProperty(String name) {
        return properties.get(name);
    }
    
    public void saveToDocument(PDDocument doc) {
        /*
        doc.getDocumentInformation().setAuthor(author.getValue());
        doc.getDocumentInformation().setTitle(title.getValue());
        doc.getDocumentInformation().setCustomMetadataValue("bibtex", bibtex.getValue());
                */
    }
}
