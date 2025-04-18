package jmri.jmrit.decoderdefn;

import javax.swing.JLabel;

import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.*;

/**
 * DecoderFileTest.java
 *
 * @author Bob Jacobsen, Copyright (C) 2001, 2002, 2025
 */
public class DecoderFileTest {

    ProgDebugger p = new ProgDebugger();

    @Test
    public void testSingleVersionNumber() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", 16, 3, null);
        d.setOneVersion(18);
        Assertions.assertTrue(d.isVersion(18), "single 18 OK");
        Assertions.assertFalse(d.isVersion(19), "single 19 not OK");
    }

    @Test
    public void testRangeVersionNumber() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "24", "25",
                "family", "filename", 16, 3, null);
        d.setVersionRange(18, 22);
        Assertions.assertFalse(d.isVersion(17), "single 17 not OK");
        Assertions.assertTrue(d.isVersion(18), "single 18 OK");
        Assertions.assertTrue(d.isVersion(19), "single 19 OK");
        Assertions.assertTrue(d.isVersion(20), "single 20 OK");
        Assertions.assertTrue(d.isVersion(21), "single 21 OK");
        Assertions.assertTrue(d.isVersion(22), "single 22 OK");
        Assertions.assertFalse(d.isVersion(23), "single 23 not OK");
    }

    @Test
    public void testCtorRange() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "18", "22",
                "family", "filename", 16, 3, null);
        Assertions.assertFalse(d.isVersion(17), "single 17 not OK");
        Assertions.assertTrue(d.isVersion(18), "single 18 OK");
        Assertions.assertTrue(d.isVersion(19), "single 19 OK");
        Assertions.assertTrue(d.isVersion(20), "single 20 OK");
        Assertions.assertTrue(d.isVersion(21), "single 21 OK");
        Assertions.assertTrue(d.isVersion(22), "single 22 OK");
        Assertions.assertFalse(d.isVersion(23), "single 23 not OK");
    }

    @Test
    public void testCtorLow() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "18", null,
                "family", "filename", 16, 3, null);
        Assertions.assertFalse(d.isVersion(17), "single 17 not OK");
        Assertions.assertTrue(d.isVersion(18), "single 18 OK");
        Assertions.assertFalse(d.isVersion(19), "single 19 not OK");
    }

    @Test
    public void testCtorHigh() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", null, "18",
                "family", "filename", 16, 3, null);
        Assertions.assertFalse(d.isVersion(17), "single 17 not OK");
        Assertions.assertTrue(d.isVersion(18), "single 18 OK");
        Assertions.assertFalse(d.isVersion(19), "single 19 not OK");
    }

    @Test
    public void testSeveralSingleVersionNumber() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", 16, 3, null);
        d.setOneVersion(18);
        Assertions.assertTrue(d.isVersion(18), "single 18 OK");
        Assertions.assertFalse(d.isVersion(19), "single 19 not OK");
        d.setOneVersion(19);
        Assertions.assertTrue(d.isVersion(19), "single 19 OK");
        Assertions.assertFalse(d.isVersion(21), "single 21 not OK");
        d.setOneVersion(21);
        Assertions.assertTrue(d.isVersion(21), "single 21 OK");
    }

    @Test
    public void testMfgName() {
        setupDecoder();
        Assertions.assertEquals("Digitrax", DecoderFile.getMfgName(decoder), "mfg name ");
    }

    @Test
    public void testModes() {
        setupDecoder();
        Assertions.assertEquals("AMODE", DecoderFile.getProgrammingModes(decoder), "programming mode ");
    }

    @Test
    public void testLoadTable() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus = new JLabel(" OK ");
        CvTableModel cvModel = new CvTableModel(progStatus, p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                new String[]{"Name", "Value"}, cvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", 16, 16, null);

        d.loadVariableModel(decoder, variableModel);
        Assertions.assertEquals(3, variableModel.getRowCount(), "read rows ");
        Assertions.assertEquals("Address", variableModel.getLabel(0), "first row name ");
        Assertions.assertEquals("Normal direction of motion", variableModel.getLabel(2), "third row name ");
    }

    @Test
    public void testIncludeCheck() {
        Element e;
        // test some examples
        e = new Element("Test");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "1", "model", "family", "", ""), "1 in null");

        (e = new Element("Test")).setAttribute("include", "1,2");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "1", "model", "family", "", ""), "1 in 1,2");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "2", "model", "family", "", ""), "2 in 1,2");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "3", "model", "family", "", ""), "3 in 1,2");

        (e = new Element("Test")).setAttribute("include", "105,205");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 in 105,205");

        (e = new Element("Test")).setAttribute("include", "205,105");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 in 205,105");

        (e = new Element("Test")).setAttribute("include", "1050,205");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 not in 1050,205");

        (e = new Element("Test")).setAttribute("include", "50,1050");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 not in 50,1050");

        (e = new Element("Test")).setAttribute("include", "827004,827008,827104,827108,827106,828043,828045,828047");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "827004", "model", "family", "", ""), "827004");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "827005", "model", "family", "", ""), "Not 827005");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "827108", "model", "family", "", ""), "827108");
    }

    @Test
    public void testIncludeCheckRippleDown() {
        Element e;

        // with nothing in element
        e = new Element("Test");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "1", "model", "family", "1,2", ""), "1 in 1,2");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "2", "model", "family", "1,2", ""), "2 in 1,2");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""), "3 in 1,2");

        // with irrelevant element
        (e = new Element("Test")).setAttribute("include", "4,5");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "1", "model", "family", "1,2", ""), "1 in 1,2");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "2", "model", "family", "1,2", ""), "2 in 1,2");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""), "3 in 1,2");

        (e = new Element("Test")).setAttribute("include", "105,205");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 in 105,205");

        e = new Element("Test");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "105", "model", "family", "205,105", ""), "105 in 205,105");
        (e = new Element("Test")).setAttribute("include", "1205,1105");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "105", "model", "family", "205,105", ""), "105 in 205,105");

        e = new Element("Test");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "1050,205", ""), "105 not in 1050,205");
        (e = new Element("Test")).setAttribute("include", "222,333");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "1050,205", ""), "105 not in 1050,205");

        e = new Element("Test");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "50,1050", ""), "105 not in 50,1050");

        e = new Element("Test");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "827004", "model", "family", "827004,827008,827104,827108,827106,828043,828045,828047", ""), "827004");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "827005", "model", "family", "827004,827008,827104,827108,827106,828043,828045,828047", ""), "Not 827005");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "827108", "model", "family", "827004,827008,827104,827108,827106,828043,828045,828047", ""), "827108");
    }

    @Test
    public void testIncludeCheckModel() {
        Element e;

        // with nothing in element
        e = new Element("Test");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "1", "model", "family", "model", ""), "1 in model");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "2", "model", "family", "1,2,model", ""), "2 in 1,2,model");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""), "3 in 1,2");

        // with irrelevant element
        (e = new Element("Test")).setAttribute("include", "4,5");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "1", "model", "family", "model", ""), "1 in model");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "2", "model", "family", "1,2,model", ""), "2 in 1,2,model");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""), "3 in 1,2");
    }

    @Test
    public void testExcludeCheck() {
        Element e;
        // test some examples
        e = new Element("Test");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "1", "model", "family", "", ""), "1 in null");

        (e = new Element("Test")).setAttribute("exclude", "1,2");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "1", "model", "family", "", ""), "1 in 1,2");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "2", "model", "family", "", ""), "2 in 1,2");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "3", "model", "family", "", ""), "3 in 1,2");

        (e = new Element("Test")).setAttribute("exclude", "105,205");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 in 105,205");

        (e = new Element("Test")).setAttribute("exclude", "205,105");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 in 205,105");

        (e = new Element("Test")).setAttribute("exclude", "1050,205");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 not in 1050,205");

        (e = new Element("Test")).setAttribute("exclude", "50,1050");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "105", "model", "family", "", ""), "105 not in 50,1050");

        (e = new Element("Test")).setAttribute("exclude", "827004,827008,827104,827108,827106,828043,828045,828047");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "827004", "model", "family", "", ""), "827004");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "827005", "model", "family", "", ""), "Not 827005");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "827108", "model", "family", "", ""), "827108");
    }

    @Test
    public void testExcludeCheckRippleDown() {
        Element e;

        e = new Element("Test");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "1", "model", "family", "", "model"), "1 in model");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2,model"), "2 in 1,2,model");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2,model"), "3 in 1,2,model");

        (e = new Element("Test")).setAttribute("exclude", "4,5");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "1", "model", "family", "", "1,2"), "1 in 1,2");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2"), "2 in 1,2");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2"), "3 in 1,2");

        e = new Element("Test");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "", "105,205"), "105 in 105,205");
        (e = new Element("Test")).setAttribute("exclude", "305,405");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "105", "model", "family", "", "105,205"), "105 in 105,205");
    }

    @Test
    public void testExcludeCheckModel() {
        Element e;

        e = new Element("Test");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "1", "model", "family", "", "1,2"), "1 in model");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2"), "2 in 1,2,model");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2"), "3 in 1,2");

        (e = new Element("Test")).setAttribute("exclude", "4,5");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "1", "model", "family", "", "1,2"), "1 in model");
        Assertions.assertFalse(DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2"), "2 in 1,2,model");
        Assertions.assertTrue(DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2"), "3 in 1,2");
    }

    @Test
    public void testMinOut() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus = new JLabel(" OK ");
        CvTableModel cvModel = new CvTableModel(progStatus, p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                new String[]{"Name", "Value"}, cvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", 16, 3, null);

        d.loadVariableModel(decoder, variableModel);
        Assertions.assertEquals(2, variableModel.getRowCount(), "read rows ");
    }

    @Test
    public void testMinFn() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus = new JLabel(" OK ");
        CvTableModel cvModel = new CvTableModel(progStatus, p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                new String[]{"Name", "Value"}, cvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", "sv2id", "sv2man", "sv2Prod",
                3, 16, null, "repl", "replFam",
                "AMODE");

        d.loadVariableModel(decoder, variableModel);
        Assertions.assertEquals(2, variableModel.getRowCount(), "read rows ");
    }

    // static variables for the test XML structures
    Element root = null;
    public Element decoder = null;
    public Element model = null;
    public Element mode = null;
    Document doc = null;

    // provide a test document in the above static variables
    public void setupDecoder() {
        // create a JDOM tree with just some elements
        root = new Element("decoder-config");
        doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        root.addContent(decoder = new Element("decoder")
                .addContent(new Element("family")
                        .setAttribute("family", "DH142 etc")
                        .setAttribute("mfg", "Digitrax")
                        .setAttribute("defnVersion", "242")
                        .setAttribute("comment", "DH142 decoder: FX, transponding")
                        .addContent(model = new Element("model")
                                .setAttribute("model", "33")
                                .setAttribute("maxFnNum", "31")
                                .setAttribute("productID", "567")
                        )
                )
                .addContent(new Element("programming")
                        .setAttribute("direct", "byteOnly")
                        .setAttribute("paged", "yes")
                        .setAttribute("register", "yes")
                        .setAttribute("ops", "yes")
                        .addContent(mode = new Element("mode").setText("AMODE"))
                )
                .addContent(new Element("variables")
                        .addContent(new Element("variable")
                                .setAttribute("label", "Address")
                                .setAttribute("CV", "1")
                                .setAttribute("minFn", "4")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("readOnly", "no")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "127")
                                )
                        )
                        .addContent(new Element("variable")
                                .setAttribute("label", "Acceleration rate")
                                .setAttribute("CV", "3")
                                .setAttribute("minOut", "2")
                                .setAttribute("mask", "VVVVVVVV")
                                .setAttribute("readOnly", "no")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "127")
                                )
                        )
                        .addContent(new Element("variable")
                                .setAttribute("label", "Normal direction of motion")
                                .setAttribute("CV", "29")
                                .setAttribute("minFn", "2")
                                .setAttribute("minOut", "5")
                                .setAttribute("mask", "XXXXXXXV")
                                .setAttribute("readOnly", "no")
                                .addContent(new Element("enumVal")
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "forward")
                                        )
                                        .addContent(new Element("enumChoice")
                                                .setAttribute("choice", "reverse")
                                        )
                                )
                        )
                )
        ); // end of adding contents
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderFileTest.class);
}
