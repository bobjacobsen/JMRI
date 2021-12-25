package jmri.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import javax.script.ScriptEngineFactory;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import jmri.util.FileUtil;

/**
 * Create scripting-specific FileChooser.
 *
 * The primary content here is a set of scripting-specific filters.
 *
 * @author Randall Wood
 * @author Bob Jacobsen    copyright 2021
 */
public class ScriptFileChooser extends JFileChooser {

    public ScriptFileChooser() {
        super(FileUtil.getScriptsPath());
        this.init();
    }

    public ScriptFileChooser(String path) {
        super(path);
        this.init();
    }

    public ScriptFileChooser(File dir) {
        super(dir);
        this.init();
    }

    private void init() {
        List<String> allExtensions = new ArrayList<>();
        HashMap<String, FileFilter> filters = new HashMap<>();
        List<String> filterNames = new ArrayList<>();

        // add the ScriptEngine-managed files
        addJmriScriptEngineManagerExtensions(allExtensions, filters, filterNames);

        // add GraalVM specific
        addGraalExtensions(allExtensions, filters, filterNames);

        // create and install the actual filter
        log.debug("allExtensions length is {}", allExtensions.size());
        FileFilter allScripts = new FileNameExtensionFilter(Bundle.getMessage("allScripts"), allExtensions.toArray(new String[allExtensions.size()]));
        this.addChoosableFileFilter(allScripts);
        filterNames.stream().sorted().forEach((filter) -> {
            this.addChoosableFileFilter(filters.get(filter));
        });

        // set initial configuration
        this.setFileFilter(allScripts);
        this.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    private void addJmriScriptEngineManagerExtensions(List<String> allExtensions, HashMap<String, FileFilter> filters, List<String> filterNames) {
        JmriScriptEngineManager.getDefault().getManager().getEngineFactories().stream().forEach((ScriptEngineFactory factory) -> {
            List<String> extensions = factory.getExtensions();
            allExtensions.addAll(extensions);
            String name = this.fileForLanguage(factory.getLanguageName());
            filterNames.add(name);
            filters.put(name, new FileNameExtensionFilter(name, extensions.toArray(new String[extensions.size()])));
        });
    }

    private void addGraalExtensions(List<String> allExtensions, HashMap<String, FileFilter> filters, List<String> filterNames) {
        var name = "Python 3 Script Files";
        var extension = "py3";
        allExtensions.add(extension);
        filters.put(name, new FileNameExtensionFilter(name, new String[]{extension}) );
        filterNames.add(name);
    }

    private String fileForLanguage(String language) {
        try {
            return Bundle.getMessage(language);
        } catch (MissingResourceException ex) {
            if (!language.endsWith(Bundle.getMessage("files"))) { // NOI18N
                return language + " " + Bundle.getMessage("files");
            }
            return language;
        }
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScriptFileChooser.class);
}
