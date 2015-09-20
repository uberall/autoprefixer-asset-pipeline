package asset.pipeline.autoprefixer

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson
import groovy.util.logging.Log4j
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

@Log4j
class AutoprefixerProcessor extends AbstractProcessor {

    public static final ThreadLocal threadLocal = new ThreadLocal();

    boolean enabled = true
    static Scriptable globalScope
    static boolean contextInitialized = false
    def browsers

    AutoprefixerProcessor(AssetCompiler precompiler) {
        super(precompiler)
        if (config?.enabled == false) {
            log.info "disabling autoprefixer"
            enabled = false
            return
        }
        if (config?.browsers) {
            browsers = new Gson().toJson(config.browsers)
        }

        if (!contextInitialized) {
            ClassLoader classLoader = this.class.classLoader
            def shellJsResource = classLoader.getResource('asset/pipeline/autoprefixer/shell.js')
            def autoprefixerJsResource = classLoader.getResource('asset/pipeline/autoprefixer/autoprefixer.js')
            def envRhinoJsResource = classLoader.getResource('asset/pipeline/autoprefixer/env.rhino.js')
            Context cx = Context.enter()
            cx.setOptimizationLevel(-1)
            globalScope = cx.initStandardObjects()
            cx.evaluateString(globalScope, shellJsResource.getText('UTF-8'), shellJsResource.file, 1, null)
            cx.evaluateString(globalScope, envRhinoJsResource.getText('UTF-8'), envRhinoJsResource.file, 1, null)
            cx.evaluateString(globalScope, autoprefixerJsResource.getText('UTF-8'), autoprefixerJsResource.file, 1, null)
            contextInitialized = true
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
                def result = cx.evaluateString(compileScope, "autoprefixer.process(lessSrc${browsers ? ", $browsers" : ''}).css", "autoprefix command", 0, null)
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

    static def getConfig(){
        AssetPipelineConfigHolder.config?.autoprefixer
    }

    static void print(text) {
        log.debug text
    }

}