# autoprefixer-asset-pipeline
Adds css autoprefixing to [Asset-Pipeline](https://github.com/bertramdev/asset-pipeline-core/) using [Autoprefixer](https://github.com/postcss/autoprefixer)

# configuration
there is currently 2 configuration options
```
asset.autoprefixer.enabled = true
```
is set to true by default. Set to False if you want to disable e.g. in development environment.

```
asset.autoprefixer.browsers = ['last 2 version']
```
is set to null by default. Set to a list of String to support special or older browsers. For more info see [Browserslist](https://github.com/ai/browserslist)
