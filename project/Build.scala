import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "umappin"
    val appVersion      = "1.0"

    // Application General dependencies
    val appDependencies = Seq(
      javaCore,
      javaJdbc,
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "com.google.code.morphia" % "morphia" % "0.99",
      "com.google.code.morphia" % "morphia-logging-slf4j" % "0.99",
      "be.objectify"  %%  "deadbolt-java"     % "2.1-SNAPSHOT"
    )

    // Play Authenticate module build settings
    val playAuthenticate = play.Project(
      "play-authenticate", "1.0-SNAPSHOT", Seq(javaCore), path = file("modules/play-authenticate")
    ).settings(
      /** --------------------- Remote repositories --------------------- **/
      resolvers += "Apache" at "http://repo1.maven.org/maven2/",
      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),
      /** --------------------- Module dependencies --------------------- **/
      libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.2",
      libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m",
      libraryDependencies += "commons-lang" % "commons-lang" % "2.6",
      libraryDependencies += "com.feth" %% "play-easymail" % "0.2-SNAPSHOT"
    )

    // Main Application build settings
    val main = play.Project(
      appName, appVersion, appDependencies
    ).settings(
      ebeanEnabled := false,
      /** --------------------- Remote repositories --------------------- **/
      // Objectify (Deadbolt) resolvers
      resolvers += Resolver.url("Objectify Play Repository (release)", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("Objectify Play Repository (snapshot)", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),
      // Easymail resolvers (again, not sure why ..)
      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),
      // Mongo morphia resolvers
      resolvers += "Maven repository" at "http://morphia.googlecode.com/svn/mavenrepo/"
        
    //Main Application submodules/subprojects dependencies
    ).dependsOn(
      playAuthenticate
    ).aggregate(
      playAuthenticate
    )

}
