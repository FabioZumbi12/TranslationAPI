Plugin for realtime item, entity, chat and custom translations.

## Dev builds: 
Available on jenkins: [![Build Status](http://host.areaz12server.net.br:8081/buildStatus/icon?job=TranslationAPI)](http://host.areaz12server.net.br:8081/job/TranslationAPI/)

## Source:
The source is available on GitHub: https://github.com/FabioZumbi12/TranslationAPI

## API repository:

**Repository:**  
TranslationAPI is hosted on Maven Central
### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.fabiozumbi12</groupId>
        <artifactId>TranslationAPI</artifactId>
        <version>1.2</version>
    </dependency>

    <!-- Import Javadocs -->
    <dependency>
        <groupId>io.github.fabiozumbi12</groupId>
        <artifactId>TranslationAPI</artifactId>
        <version>1.2</version>
        <classifier>javadoc</classifier>
    </dependency> 
</dependencies>  
```

### Gradle:
```
repositories {
    mavenCentral()
    maven { url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/' } // Only for snapshots
}

dependencies {
    compileOnly ("io.github.fabiozumbi12:TranslationAPI:1.2")
}
```

## WIKI and usage:

Click here to go to wiki and see the methods and usage examples: [WIKI](https://github.com/FabioZumbi12/TranslationAPI/wiki)