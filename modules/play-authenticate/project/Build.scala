import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play-authenticate"
    val appVersion      = "0.2.5-SNAPSHOT"

    val appDependencies = Seq(
        javaCore
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      organization := "com.feth",

      /** --------------------- Remote repositories --------------------- **/
      resolvers += "Apache" at "http://repo1.maven.org/maven2/",
      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),
        
      /** --------------------- Play Authenticate module dependencies --------------------- **/
      libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.2",
      libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m",
      libraryDependencies += "commons-lang" % "commons-lang" % "2.6",
      libraryDependencies += "com.feth" %% "play-easymail" % "0.2-SNAPSHOT"
    )
}
