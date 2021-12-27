
 - [ ] Confirm that tests still run headless (we're creating a JTextArea in ScriptOutput)


Future Tasks

 - [ ] Conditionals and LogixNG allow you to enter and execute a single Jython (and only Jython) command. We need to generalize that some day.
 - [ ] JmriScriptEngineManager requires GraalVM to compile; figure out how to bypass that

 - [ ] Can you check for GraalVM installed languages so they can be automatically added i.e.
    - [ ] in JmriScriptEngineManager
    - [ ] in ScriptFileChooser

 - [ ] this file is linked from package-info Javadoc, but the link is broken

Concerns & Risks
 - [ ] Can users realistically [install GraalVM](https://www.graalvm.org/docs/getting-started/#install-graalvm)?
    - [ ] Does using [Native Images](https://www.graalvm.org/docs/getting-started/#native-images) retire this?

Links of interest:
 -  [GraalVM docs](https://www.graalvm.org/docs/introduction/)
    - [Polyglot Programming](https://www.graalvm.org/reference-manual/polyglot-programming/)
        - [Passing Options](https://www.graalvm.org/reference-manual/polyglot-programming/#passing-options-for-language-launchers)
    - [GraalVM Python](https://www.graalvm.org/reference-manual/python/)
        - [Jython Compatibility](https://www.graalvm.org/reference-manual/python/Jython/)
 - Javadocs
    - Graal: [org.graalvm.polyglot](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/package-summary.html)
        - [Context.Builder class](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Context.Builder.html)
        - [Source class](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Source.html)
    - [org.apache.commons.io.output.WriterOutputStream](https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/output/WriterOutputStream.html#WriterOutputStream(java.io.Writer))
    - [JmriScriptEngineManager](https://www.jmri.org/JavaDoc/doc/jmri/script/JmriScriptEngineManager.html)
    - [javax.script](https://docs.oracle.com/javase/8/docs/api/javax/script/package-summary.html)
        - [AbstractScriptEngine](https://docs.oracle.com/javase/8/docs/api/javax/script/AbstractScriptEngine.html)
        - []()
    - []()
    - []()
 - Source
    - [GraalJSEngineFactory](https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.scriptengine/src/com/oracle/truffle/js/scriptengine/GraalJSEngineFactory.java)
    - []()
 - Javax.script docs
    - [white paper](https://web.archive.org/web/20110604035534/http://java.sun.com/developer/technicalArticles/J2SE/Desktop/scripting/)
    - []()
    - []()

------

```
//         var m = JmriScriptEngineManager.polyglot.getEngine().getLanguages();
//         for (var k : m.keySet()) {
//             log.debug("key:   {}", k);
//             var v = m.get(k);
//             log.debug("   name: {} ID: {}", v.getName(), v.getId());
//             log.debug("   mime: {}", v.getDefaultMimeType());
//         }

//  key:   js [main]
//     name: JavaScript ID: js [main]
//     mime: application/javascript ID: {} [main]
//  key:   llvm [main]
//     name: LLVM ID: llvm [main]
//     mime: application/x-llvm-ir-bitcode ID: {} [main]
//  key:   python [main]
//     name: Python ID: python [main]
//     mime: text/x-python ID: {} [main]
```
