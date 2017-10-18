.. _quickstart:


Quick Start
===========

If your project is already using JUnit 5, simply import the `hoverfly-java-junit5` library.

For Maven, add the following dependency to your pom:

.. parsed-literal::

    <dependency>
        <groupId>io.specto</groupId>
        <artifactId>hoverfly-java-junit5</artifactId>
        <version>\ |version|\ </version>
        <scope>test</scope>
    </dependency>


Or with Gradle add the dependency to your *.gradle file:

.. parsed-literal::

   testCompile "io.specto:hoverfly-java-junit5:|version|"


If you haven't yet use JUnit 5, here is an example of Gradle configuration to get you up and running with Hoverfly and JUnit 5 in your project.

.. parsed-literal::

    buildscript {
        dependencies {
            classpath "org.junit.platform:junit-platform-gradle-plugin:1.0.1"
        }

        repositories {
            mavenCentral()
        }
    }

    apply plugin: "org.junit.platform.gradle.plugin"

    dependencies {
        testCompile "io.specto:hoverfly-java-junit5:|version|"

        testRuntime("org.junit.platform:junit-platform-launcher:1.0.1")
        testRuntime("org.junit.jupiter:junit-jupiter-engine:5.0.1")
        testRuntime("org.junit.vintage:junit-vintage-engine:4.12.1")
    }

