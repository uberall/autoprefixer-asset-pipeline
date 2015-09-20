package asset.pipeline.autoprefixer

import asset.pipeline.AssetCompiler
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.CssAssetFile
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Unroll
@Stepwise
class AutoprefixerProcessorSpec extends Specification {


    private static final String CSS_INPUT = '''
h1 {
    transition: height 0s;
}
'''
    private static final String AUTOPREFIXED_OUTPUT = '''
h1 {
    -webkit-transition: height 0s;
            transition: height 0s;
}
'''

    private static final String AUTOPREFIXED_OLD_OUTPUT = '''
h1 {
    -webkit-transition: height 0s;
       -moz-transition: height 0s;
         -o-transition: height 0s;
            transition: height 0s;
}
'''

    def "process should work"() {
        given:
        AutoprefixerProcessor processor = new AutoprefixerProcessor(new AssetCompiler())
        CssAssetFile file = new CssAssetFile()
        file.path = "test.css"
        when:
        def result = processor.process(CSS_INPUT, file)
        then:
        result == AUTOPREFIXED_OUTPUT
    }

    def "browser array should be respected"() {
        given:
        AssetPipelineConfigHolder.config = [autoprefixer: [browsers: 'last 30 versions']]
        AutoprefixerProcessor processor = new AutoprefixerProcessor(new AssetCompiler())
        CssAssetFile file = new CssAssetFile()
        file.path = "test.css"
        when:
        def result = processor.process(CSS_INPUT, file)
        then:
        result == AUTOPREFIXED_OLD_OUTPUT
    }

    def "process should do nothing if disabled"() {
        given:
        AssetPipelineConfigHolder.config = [autoprefixer: [enabled: false]]
        AutoprefixerProcessor processor = new AutoprefixerProcessor(new AssetCompiler())
        CssAssetFile file = new CssAssetFile()
        file.path = "test.css"
        when:
        def result = processor.process(CSS_INPUT, file)
        then:
        result == CSS_INPUT
    }

}
