/**
 * Provides JMRI's built in scripting support.
 * <p>
 * Note that this package is in flux.
 * <ul>
 *   <li>Pre-JMRI 5, this used Java's
 *      {@link javax.script} support, specifically
 *      {@link javax.script.ScriptEngineManager} and
 *      {@link javax.script.ScriptEngine} et al to provide
 *      <ul>
 *          <li>python support via Jython, and
 *          <li>Javascript support via the Nashorn engine.
 *      </ul>.
 *    <li>JMRI 5 brings the beginning of support for the next
 *        generation of scripting support, specifically through
 *        <a href="https://www.graalvm.org/">GraalVM</a>.
 *        This is being done by migrating the internal
 *        structure of the
 *        {@link JmriScriptEngineManager} class.
 *        For more information on this, see the associated
 *        <a href="README-WIP.txt">README-WIP.txt</a> file.
 * </ul>
 *
 * <h2>Related Documentation</h2>
 *
 * For overviews, tutorials, examples, guides, and tool documentation, please
 * see:
 * <ul>
 * <li><a href="http://jmri.org/">JMRI project overview page</a>
 * <li><a href="http://jmri.org/help/en/html/doc/Technical/index.shtml">JMRI
 * project technical info</a>
 * </ul>
 *
 *    <p><em>Note:</em> The class diagram below has been trimmed to <u>not</u>
 *        show some redundant (to a superpackage of a superpackage) or
 *        patterned (connecting a .configurexml package by convention)
 *        to make it more readable.
 *
 * <!-- Put @see and @since tags down here. -->
 *
 * @see jmri.managers
 * @see jmri.implementation
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri;
