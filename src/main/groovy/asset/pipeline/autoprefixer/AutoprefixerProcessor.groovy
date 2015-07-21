package asset.pipeline.autoprefixer

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import groovy.util.logging.Log4j
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

@Log4j
class AutoprefixerProcessor extends AbstractProcessor {

    public static final ThreadLocal threadLocal = new ThreadLocal();
    Scriptable globalScope
    ClassLoader classLoader
    boolean enabled = true
    def browsers

    AutoprefixerProcessor(AssetCompiler precompiler) {
        super(precompiler)
        classLoader = this.class.classLoader

        def shellJsResource = classLoader.getResource('asset/pipeline/autoprefixer/shell.js')
        def autoprefixerJsResource = classLoader.getResource('asset/pipeline/autoprefixer/autoprefixer.js')
        def envRhinoJsResource = classLoader.getResource('asset/pipeline/autoprefixer/env.rhino.js')
        Context cx = Context.enter()

        cx.setOptimizationLevel(-1)
        globalScope = cx.initStandardObjects()
        cx.evaluateString(globalScope, shellJsResource.getText('UTF-8'), shellJsResource.file, 1, null)
        cx.evaluateString(globalScope, envRhinoJsResource.getText('UTF-8'), envRhinoJsResource.file, 1, null)
        cx.evaluateString(globalScope, autoprefixerJsResource.getText('UTF-8'), autoprefixerJsResource.file, 1, null)
        if (AssetPipelineConfigHolder.config?.autoprefix == false) {
            enabled = false
            log.info "disabling autoprefixer"
        }
    }

    String process(String input, AssetFile assetFile) {
        if (enabled) {
            log.info("prefixing $assetFile.name")
            try {
                threadLocal.set(assetFile);

                def cx = Context.enter()
                def compileScope = cx.newObject(globalScope)
                compileScope.setParentScope(globalScope)
                compileScope.put("lessSrc", compileScope, input)
                compileScope.put("browserArray", compileScope, browsers)
                def result = cx.evaluateString(compileScope, "autoprefixer.process(lessSrc).css", "autoprefix command", 0, null)
                return result.toString()
            } catch (Exception e) {
                throw new Exception("Autoprefixing failed: $e")
            } finally {
                Context.exit()
            }
        } else {
            return input
        }
    }

    static void print(text) {
        log.debug text
    }

}