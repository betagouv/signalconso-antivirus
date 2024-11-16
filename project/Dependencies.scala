import play.sbt.PlayImport.specs2

import sbt._

object Dependencies {
  object Versions {

    lazy val playSlickVersion          = "6.1.1"
    lazy val slickPgVersion            = "0.22.2"
    lazy val pekkoHttpVersion          = "1.0.1"
    lazy val pekkoVersion              = "1.0.2"
    lazy val enumeratumVersion         = "1.8.2"
    lazy val sentryVersion             = "7.17.0"
    lazy val jbcrypt                   = "0.4"
    lazy val specs2MatcherExtraVersion = "4.20.9"
    lazy val scalaCheckVersion         = "1.18.1"
    lazy val catsCoreVersion           = "2.12.0"
    lazy val pureConfigVersion         = "0.17.7"
    lazy val awsJavaSdkS3Version       = "1.12.736"
    lazy val jacksonModuleScalaVersion = "2.18.1"
    lazy val postgresqlVersion         = "42.7.4"
    lazy val refinedVersion            = "0.11.2"
    lazy val flyWayVersion             = "10.21.0"
    lazy val janino                    = "3.1.12"
    lazy val logstashLogbackEncoder = "8.0"

  }

  object Test {
    val specs2Import       = specs2            % "test"
    val specs2MatcherExtra = "org.specs2"     %% "specs2-matcher-extra" % Versions.specs2MatcherExtraVersion % "test"
    val scalaCheck         = "org.scalacheck" %% "scalacheck"           % Versions.scalaCheckVersion         % "test"
    val pekkoTestKit = "org.apache.pekko" %% "pekko-actor-testkit-typed" % Versions.pekkoVersion % "test"

  }

  object Compile {
    val flywayCore     = "org.flywaydb" % "flyway-core"                % Versions.flyWayVersion
    val flywayPostgres = "org.flywaydb" % "flyway-database-postgresql" % Versions.flyWayVersion
    val janino = "org.codehaus.janino" % "janino" % Versions.janino // Needed for the <if> in logback conf
    val logstashLogBackEncoder = "net.logstash.logback"   % "logstash-logback-encoder" % Versions.logstashLogbackEncoder
    val sentryLogback          = "io.sentry"              % "sentry-logback"           % Versions.sentryVersion
    val sentryCore             = "io.sentry"              % "sentry"                   % Versions.sentryVersion
    val catsCore               = "org.typelevel"         %% "cats-core"                % Versions.catsCoreVersion
    val pureConfig             = "com.github.pureconfig" %% "pureconfig"               % Versions.pureConfigVersion
    val playSlick              = "org.playframework"     %% "play-slick"               % Versions.playSlickVersion
    val slickPg                = "com.github.tminglei"   %% "slick-pg"                 % Versions.slickPgVersion
    val slickPgPlayJson        = "com.github.tminglei"   %% "slick-pg_play-json"       % Versions.slickPgVersion
    val pekkoConnectorS3       = "org.apache.pekko"      %% "pekko-connectors-s3"      % Versions.pekkoVersion
    val pekkoHttp              = "org.apache.pekko"      %% "pekko-http"               % Versions.pekkoHttpVersion
    val pekkoHttpXml           = "org.apache.pekko"      %% "pekko-http-xml"           % Versions.pekkoHttpVersion
    val jbcrypt                = "org.mindrot"            % "jbcrypt"                  % "0.4"
    val enumeratumPlay         = "com.beachape"          %% "enumeratum-play"          % Versions.enumeratumVersion
    val awsJavaSdkS3           = "com.amazonaws"          % "aws-java-sdk-s3"          % Versions.awsJavaSdkS3Version
    val jacksonModuleScala =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScalaVersion
    val postgresql = "org.postgresql" % "postgresql" % Versions.postgresqlVersion
    val argon2Jvm  = "de.mkammerer"   % "argon2-jvm" % "2.11"
  }

  val AppDependencies = Seq(
    Compile.janino,
    Compile.logstashLogBackEncoder,
    Compile.sentryLogback,
    Compile.sentryCore,
    Compile.catsCore,
    Compile.pureConfig,
    Compile.playSlick,
    Compile.slickPg,
    Compile.slickPgPlayJson,
    Compile.pekkoConnectorS3,
    Compile.pekkoHttp,
    Compile.pekkoHttpXml,
    Compile.jbcrypt,
    Compile.enumeratumPlay,
    Compile.awsJavaSdkS3,
    Compile.jacksonModuleScala,
    Compile.postgresql,
    Compile.flywayCore,
    Compile.flywayPostgres,
    Compile.argon2Jvm,
    Test.specs2Import,
    Test.specs2MatcherExtra,
    Test.scalaCheck,
    Test.pekkoTestKit
  )
}
