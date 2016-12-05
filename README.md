# Gradle Golang Plugin

**This project is still under heavy development.**

The gradle-golang plugin is is designed to give you the power of a professional build tool (in this case Gradle) to
build, test (and other great stuff) with your go code. And this without pain of think about the correct installation
go sdk, set the right environment variables, download dependencies, ...

## Topics

* [Features](#features)
* [Get it](#get-it)
* [Requirements](#requirements)
* [Quick start](#quick-start)
* [Settings](#settings)
* [Tasks](#tasks)
* [Running external tools](#running-external-tools)
* [Contributing](#contributing)
* [License](#license)

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

## Get it

> To determinate the current released version please refer our
> [gradle plugin page](https://plugins.gradle.org/plugin/org.echocat.golang).

Plugin dependency for your ``build.gradle``
```groovy
plugins {
  id "org.echocat.golang" version "<latest version of this plugin>"
}
```

## Requirements

* Gradle wrapper script or Gradle (minimum 3)
* Java (minimum 7)

See [Quick start](#quick-start) for information how to install requirements.

## Quick start

1. Ensure that a working JDK is installed.
    type ``java -version`` on your shell. If the result is NOT a minimum of 7 please go to 
    [Oracle JDK download page](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and download the
    latest version or use your favorite package manager to install it. 

2. Download and extract [gradle-wrapper.zip](assets/gradle-wrapper.zip) into root directory of your project.
    > To use the Gradle Wrapper is the recommend solution because there is not requirement of other tools. All is
      shipped with the your project (excluding the JDK). Other way is install [Gradle](https://www.gradle.org/) direct 
      on your computer.

3. Create ``build.gradle`` in root directory of your project with the following content:

    ```groovy
    plugins {
        id 'org.echocat.golang' version '<latest version of this plugin>'
    }
    
    group 'github.com/my-user/my-project'
    
    golang {
        // Set default platforms to build but make it overwritable via -Dplatforms=<..>
        platforms = System.getProperty("platforms", "linux-amd64,windows-amd64,darwin-amd64")
        dependencies {
            build 'github.com/urfave/cli'
            test 'github.com/stretchr/testify'
        }
        build {
            // Use temporary GOPATH to build everthing in
            useTemporaryGopath = true
        }
    }
    ```

4. Now just run
    ```bash
    # On Linux and macOS
    ./gradlew build
    # On Windows
    gradlew build
    ```

    Now your whole project will be build and the output binaries are located under ``build/out/``. In this example you 
    can expect the binaries for ``linux-amd64``, ``windows-amd64`` and ``darwin-amd64``.

## Settings

```groovy
// Package name of your project.
// Example: github.com/my_user/my_project
group = '' // String - REQUIRED (if golang.packageName is not set)

golang {
    // Comma separated list of platforms to build
    platforms = 'linux-386,linux-amd64,windows-386,windows-amd64,darwin-amd64' // String
    // Overwrite the package name of 'group' - be useful for overriding settings for 
    // specific tasks.
    packageName = '<same as group>' // String
    // Platform of the building host. Automatically set by 'validate' task
    hostPlatform = '<automatically detected>' // Platform
    // Location where to place the go toolchain and other assets temporarily
    cacheRoot = '~/.go' // Path
    
    dependencies {
        // Here you can specify dependencies in Golang familiar way  
        // configuration: 
        //    * build: Required for build and testing
        //    * test:  Required only for test
        //    * tool:  Tool required only while build process
        // packageName: Name of package to import. Should be the same as used in source code for import.
        // version:     Can identify the branchname, commit revisions, ... of a package.
        <configuration> '<packageName>[:<version>]'
        // Example: build 'github.com/urfave/cli'
        // ...
    
        // Dependency required only for testing
        test '<package name>[:<version>]'
        // ...
    
        // Tool dependency required only while build process
        tool '<package name>[:<version>]'
        // ...

        // If true it will always download every dependency also if there are no updates available.
        forceUpdate = false // Boolean

        // If true it will delete unknown dependencies on clean task.
        deleteUnknownDependencies = true // Boolean

        // If true it will delete all dependencies on clean task.
        deleteAllCachedDependenciesOnClean = false // Boolean

        // Directory where to cache all dependencies in.
        dependencyCache = 'vendor' // Path
    }

    build {
        // GOPATH to use for build.
        // Will be replaced with a temporary one if useTemporaryGopath is set to true. 
        gopath = '${GOPATH}' // Path

        // If enabled a temporary GOPATH is created to build in. 
        useTemporaryGopath = false // Boolean

        // Is used to identify sources to be processed 
        includes = [] // []String

        // Is used to identify sources to be NOT processed 
        excludes = ['.git/**', '.svn/**', 'build.gradle', 'build/**', '.gradle/**', 'gradle/**'] // []String

        // Optional arguments to pass to go build tool 
        arguments = [] // []String

        // Name of the generated output filename.
        // Placeholders:
        // * %{platform} - Platform name like 'linux-amd64'
        // * %{extension} - Platform specific executable extension like on Windows='.exe' or Linux=''  
        // * %{separator} - Separator in paths like on Linux='/' or Windows='\'  
        // * %{pathSeparator} - Separator to split paths like on Linux=':' or Windows=';'  
        outputFilenamePattern = 'build/out/<project name>-%{platform}%{extension}' // String

        // Definitions to pass to ld
        definitions = [] // [String]String
    }
    
    testing {
        // If true no tests will be executed.
        skip = false // Boolean

        // Explicit packages to test
        // If provided 'includes' and 'excludes' will be ignored.
        packages = [] // []String

        // Searches in this directories for test go sources to be tested.
        includes = [] // []String

        // Do not searches in this directories for test go sources to be tested.
        excludes = [] // []String

        // Optional arguments to pass to the go test tool
        arguments = [] // []String

        // Optional arguments to pass to the go test itself
        testArguments = [] // []String
    }
    
    toolchain {
        // Always build toolchain also if already there and working
        forceBuildToolchain = false // Boolean

        // Used go version
        goversion = 'go1.6.2' // String

        // Used GOROOT. This will normally automated detected by validate task
        goroot = '<automatically detected>' // Path

        // Use cgo or not
        cgoEnabled = false // Boolean

        // Used GOROOT_BOOTSTRAP. This will normally automated detected by validate task
        bootstrapGoroot = '<automatically detected>' // Path

        // Location where to download bootstrap toolchain and target toolchain
        downloadUriRoot = 'https://storage.googleapis.com/golang/' // URI
    }
}

dependencies {
    // Here you can define dependencies in Gradle familiar way.
    // In this context it is not possible to provide all possible variables and you have to provide more
    // meta information than under "golang.dependencies".

    // configuration: 
    //    * build: Required for build and testing
    //    * test:  Required only for test
    //    * tool:  Tool required only while build process
    // packageName: It is required to split the package up to meet default Gradle behaviours.
    //    Example: "github.com/urfave/cli" -> "github.com:urfave/cli"
    // version: Identifies the version of the dependency package to use. If you want to use the default one
    //    provide the special keyword "default".
    <configuration> '<providerPartOfPackageName>:<restOfPackageName>:<version>'
    // Example: build 'github.com:urfave/cli:default'
    // ...
}
```

## Tasks

* [build](#build)
* [clean](#clean)
* [getTools](#getTools)
* [prepareSources](#prepareSources)
* [prepareToolchain](#prepareToolchain)
* [test](#test)
* [validate](#validate)

Run tasks using...

```bash
# On Linux and macOS
./gradlew <task> [...]
# On Windows
gradlew <task> [...]
```

### ``build``

Build the source code of your project and create binaries for it under ``build/out/``.

Depends on: ``validate``, ``prepareToolchain``, ``prepareSources``, ``test``, ``getTools``

### ``clean``

Clean all generated artifacts by your build including not referenced dependencies. 

Depends on: ``validate``

### ``getTools``

Download and build required tools. 

Depends on: ``validate``, ``prepareToolchain``

### ``prepareSources``

Process sources and copy it to location for building (if required). 

Depends on: ``validate``

### ``prepareToolchain``

Download go bootstrap toolchain (if not available on host) and build go toolchain for all target and host platforms. 

Depends on: ``validate``

### ``test``

Executes all tests of the target package and depended source packages. Optionally create coverage profile in go format
 and HTML. Test output will be located under  ``build/testing/``

Depends on: ``validate``, ``prepareToolchain``, ``prepareSources``, ``getTools``

### ``validate``

Detect parameters of the whole host system relative to the configuration and resolve missing parameters.
 Every other tasks needs this task to work. 

## Running external tools

### Buildin tools

At the end of ``build.gradle``:
```groovy
// ...

// Create new task based on the GolangTask support implementation
class VetTask extends org.echocat.gradle.plugins.golang.tasks.GolangTask {
    void run() {
        // Create a new executor that will execute to configured go binary with the actual logger
        //
        // To use the "toolchain.goBinary" is required because this plugin will download
        // and configure Go by itself and respect characteristic of every platform.
        //
        // If you use "logger" you can see the stdout of the command with "gradle --info"
        // ... every output that was logged to stderr will be logged also without "--info" to
        // WARN level.
        // You can omit "logger" if you only want to see output in case of errors.
        org.echocat.gradle.plugins.golang.utils.Executor.executor(toolchain.goBinary, logger)
                // Provide the command the resolved GOROOT.
                // Do not resolve it by your own. Trust the plugin way because it respects
                // characteristic of every platform.
                .env("GOROOT", toolchain.goroot)
                // Provide the command the resolved GOPATH.
                // Do not resolve it by your own. Trust the plugin way because it respects
                // characteristic of every platform and also handle temporary and separated
                // environments...
                .env("GOPATH", build.gopathAsString)
                // Provide the arguments for the command....
                .arguments("vet", "-x", golang.packageName)
                // Execute everything. If it fails (with some exit code not equal to 0) the
                // whole build process will be fail also.
                .execute()
    }
}
// Create this as a new Gradle task
task vet(type: VetTask)
// Make test task depend on this task. This will cause in every moment you
// call "gradle test" also vet is called.
test.dependsOn vet
```

### External tools

In the dependencies section of your ``build.gradle``:
```groovy
// ...
golang {
    // ...
    dependencies {
        // ...
        // This will download and build the golint tool on build of this project
        tool 'github.com/golang/lint/golint'
    }
    // ...
````

At the end of ``build.gradle``:
```groovy
// ...

class LintTask extends org.echocat.gradle.plugins.golang.tasks.GolangTask {
    void run() {
        // Create a new executor that will call the downloaded and build tool.
        // Tool binaries are generally located under:
        // <project dir>/build/<tool package name><executable suffix>
        // On Linux/macOS the suffix is empty - but on Windows ".exe": So never forget to append
        // ${toolchain.executableSuffix} to executable string.
        org.echocat.gradle.plugins.golang.utils.Executor.executor("${project.buildDir}/tools/github.com/golang/lint/golint${toolchain.executableSuffix}")
                .env("GOROOT", toolchain.goroot)
                .env("GOPATH", build.gopathAsString)
                // "-set_exit_status" will force lint to fail with exit code 1 if any violation
                // is found. This will cause the build process to fail in this case.
                .arguments("-set_exit_status", golang.packageName)
                .execute()
    }
}
task lint(type: LintTask)
test.dependsOn lint
```

## Contributing

gradle-golang-plugin is an open source project of [echocat](https://echocat.org). So if you want to make this project even better, you can
contribute to this project on [Github](https://github.com/echocat/gradle-golang-plugin) by
[fork us](https://github.com/echocat/gradle-golang-plugin/fork).

If you commit code to this project you have to accept that this code will be released under the [license](#license) of this project.

## License

See [LICENSE](LICENSE.txt) file.
