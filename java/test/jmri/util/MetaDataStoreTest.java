package jmri.util;

import java.io.*;
import org.jdom2.*;

import org.junit.*;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class MetaDataStoreTest {

    @Test
    public void testLoadAndAccess() throws IOException, JDOMException {
        MetaDataStore m = MetaDataStore.loadContent(new File("java/test/jmri/util/MetaDataStoreTest.xml"));
        
        Assert.assertNotNull(m);
        Assert.assertNotNull(m.getItems());
        Assert.assertNotNull(m.getNestedContents());
         
        Assert.assertEquals("Val1", m.getItems().get("Key1"));
        Assert.assertEquals("Val2", m.getItems().get("Key2"));
        Assert.assertEquals("Val3", m.getItems().get("Key3"));
        
        Assert.assertNotNull(m.getNestedContents().get("Store 1"));
        Assert.assertNotNull(m.getNestedContents().get("Store 1").getItems());
        Assert.assertNotNull(m.getNestedContents().get("Store 1").getNestedContents());

        Assert.assertEquals("Val1", m.getNestedContents().get("Store 1").getItems().get("KeyA"));
        Assert.assertEquals("Val2", m.getNestedContents().get("Store 1").getItems().get("KeyB"));
        Assert.assertEquals("Val3", m.getNestedContents().get("Store 1").getItems().get("KeyC"));
        
        Assert.assertNotNull(m.getNestedContents().get("Store 2"));
        Assert.assertNotNull(m.getNestedContents().get("Store 2").getItems());
        Assert.assertNotNull(m.getNestedContents().get("Store 2").getNestedContents());

        Assert.assertEquals("Val21", m.getNestedContents().get("Store 2").getItems().get("Key1"));
        Assert.assertEquals("Val22", m.getNestedContents().get("Store 2").getItems().get("Key2"));
        Assert.assertEquals("Val23", m.getNestedContents().get("Store 2").getItems().get("Key3"));
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
