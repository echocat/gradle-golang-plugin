# Gradle Golang Plugin

**This project is still under heavy development.**

## Introduction

The gradle-golang plugin is is designed to give you the power of a professional build tool (in this case Gradle) to
build, test (and other great stuff) with your go code. And this without pain of think about the correct installation
go sdk, set the right environment variables, download dependencies, ...

## Features

* Build your whole project
* Automatically download dependencies (if required by your code or your used external packages)
* Pinning of external dependencies to specific versions, tags, ...
* No pre installed go toolchain is required - only a working JDK (in minimum version 1.7). 
* Automatically download go toolchain and build it for all target platforms
* Very easy cross-compile 
* Automatically download and build go tools
* Choice to build in the default system ``GOPATH`` or in a temporary one to do not influence your build result 
* Test target package and all used packages of your code
* Create test coverage report (one report for all packages)


... and the best: Gradle already provides you a lot of other great plugins to help you managing your project like:

* [gradle-release](https://github.com/researchgate/gradle-release) - Create release on your SCM and prepare next version
  including tests, build, etc ...
* [gradle-github-plugin](https://github.com/riiid/gradle-github-plugin) - Upload build artifacts on your GitHub release
  page.

Find more plugins on [Gradle plugin repository](https://plugins.gradle.org/).

## Usage

> This example refers gradle >2.1. For detailed examples please refer our
> [gradle plugin page](https://plugins.gradle.org/plugin/org.echocat.golang).

```groovy
plugins {
    id "org.echocat.golang" version "<your desired version>"
}
```

## Contributing

gradle-golang-plugin is an open source project of [echocat](https://echocat.org). So if you want to make this project even better, you can
contribute to this project on [Github](https://github.com/echocat/gradle-golang-plugin) by
[fork us](https://github.com/echocat/gradle-golang-plugin/fork).

If you commit code to this project you have to accept that this code will be released under the [license](#license) of this project.

## License

See [LICENSE](LICENSE.txt) file.
