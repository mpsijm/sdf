plugins {
  id("org.metaborg.spoofax.gradle.langspec")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
  `maven-publish`
}

ecj {
  toolVersion = "3.21.0"
}
tasks.withType<JavaCompile> { // ECJ does not support headerOutputDirectory (-h argument).
  options.headerOutputDirectory.convention(provider { null })
}

// HACK: Temporarily set group to 'org.metaborggggg' to prevent substitution of baseline version of SDF3 to this project.
// I could not find another way to disable this substitution.
group = "org.metaborggggg"
