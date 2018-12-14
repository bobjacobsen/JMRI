package jmri.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import jmri.jmrit.XmlFile;

import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * Provide structured access to metadata.
 * <p>
 * Intended to be used for e.g. loading configuration information 
 * at run time.  Not intended for flat info, I18N keys, etc
 * which should continue to be stored in properties files.
 *
 * @author	Bob Jacobsen, Copyright (C) 2018
 */
public class MetaDataStore {

    /**
     * Get the list of key/value pairs at the 
     * top leve in this store.
     * @return May be empty
     */
    @Nonnull
    Map<String, String> getItems() {
         return Collections.unmodifiableMap(items);
    }

    /**
     * Get the list of key/value pairs at the 
     * top leve in this store.
     * @return May be empty
     */
    @Nonnull
    Map<String, MetaDataStore> getNestedContents() {
        return Collections.unmodifiableMap(nestedContent);
    }
    
    /**
     * Read a data file and return the head item
     */
    @Nonnull
    static public MetaDataStore loadContent(File file) throws IOException, JDOMException {
        XmlFile pf = new XmlFile() {
        };  // XmlFile is abstract
        
        Element root = pf.rootFromFile(file);

        return loadContent(root);
    }

    /**
     * Unpack JDOM XML content to MetaDataStore elements
     */    
    @Nonnull
    static public MetaDataStore loadContent(Element root) {
        MetaDataStore retval = new MetaDataStore();
        for (Element c: root.getChildren("item")) {
            String key = c.getChild("key").getText();
            String value = c.getChild("value").getText();
            retval.items.put(key,value);
        }
        for (Element c: root.getChildren("store")) {
            String key = c.getChild("key").getText();
            MetaDataStore value = loadContent(c);
            retval.nestedContent.put(key,value);
        }

        return retval;
    }
    
    final private HashMap<String, String> items = new HashMap<String, String>();
    final private HashMap<String, MetaDataStore> nestedContent = new HashMap<String, MetaDataStore>();
}
