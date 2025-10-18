# Packaging Notes

## 1) Build a Fat JAR (quick test distribution)
- Ensure Java 21+ and JavaFX SDK available (or use Gradle JavaFX plugin).
- Include platform-specific JavaFX modules (controls, graphics) using Gradle.

**Gradle (example):**
```gradle
plugins {
    id 'application'
}

application {
    mainClass = 'swimworkoutbuilder.Main' // adjust to your real main class
}

dependencies {
    implementation "org.openjfx:javafx-controls:21"
    implementation "org.openjfx:javafx-graphics:21"
    implementation "org.openjfx:javafx-fxml:21"
}

tasks.withType(Jar) {
    manifest {
        attributes 'Main-Class': application.mainClass
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
}
```

Run: `./gradlew clean jar` → `build/libs/*.jar`

## 2) Native Bundles (recommended)
Use **jlink** to create a minimized runtime, then **jpackage** to build installers.

**Example (macOS DMG):**
```bash
jlink --module-path $JAVA_HOME/jmods --add-modules java.base,java.desktop,java.logging       --output build/runtime

jpackage --type dmg   --name SwimWorkoutBuilder   --input build/libs   --main-jar SwimWorkoutBuilder-all.jar   --dest build/dist   --app-version 1.0.0   --icon icons/app.icns
```

**Windows (MSI):** change `--type msi` and provide `.ico`.

## Notes
- Test on each platform’s target runtime.
- Sign and notarize (macOS) if distributing broadly.
- Keep resources within the JAR (CSS, FXML, icons) with classpath loading.
