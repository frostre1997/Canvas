// Top‑level build file
tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
