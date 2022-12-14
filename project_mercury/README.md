## Versioning

Mercury leverages automatically generated versions following [semantic version](https://semver.org/) pattern.

The versioning is provided by [gradle-git](https://github.com/ajoberstar/gradle-git)'s opinionated plugin and it manages 
git tagging for releases, provided the correct `gradlew release` parameters. All artifacts are built with the inferred version
and the project should not contain any `version` property hardcoded anywhere either in `gradle.build` or elsewhere.

For details on usage, see: [SemVer documentation for the gradle-git's opinionated plugin](https://github.com/ajoberstar/gradle-git/wiki/Release%20Plugins#how-do-i-use-the-opinion-plugin)

***Tagging releases is the job of the CI/CD apparatus and NO developer should do it manually from their developing environment.***

## High-Level Design

Diagrams are available in Lucidchart, contact team collaborators to get access to those as required.

## Installing

More about [Mercury System Configuration](https://unsightly-rock.cloudvent.net/platform/setup-dev-services-osx/)

## Starting up

**Run using local DB (requires local-vm)** 

```
./gradlew run
```

**Output legacy layout for logs**

```
./gradlew run -Dlogging=legacy
```

**Specify instance type on run**
```
./gradlew run -Dinstance.type=learner
```
Supported values are: `learner`, `author`, `default`. 
Defaults to the `default` value when no arguments are supplied.
#   p r o j e c t _ m e r c u r y  
 #   p r o j e c t _ m e r c u r y  
 