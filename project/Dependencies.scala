import play.sbt.PlayImport.specs2

import sbt._

object Dependencies {
  object Versions {

    lazy val playSlickVersion          = "6.1.0"
    lazy val slickPgVersion            = "0.22.2"
    lazy val playMailerVersion         = "10.0.0"
    lazy val pekkoHttpVersion          = "1.0.1"
    lazy val pekkoVersion              = "1.0.2"
    lazy val enumeratumVersion         = "1.8.0"
    lazy val sentryVersion             = "6.34.0"
    lazy val jbcrypt                   = "0.4"
    lazy val specs2MatcherExtraVersion = "4.20.6"
    lazy val scalaCheckVersion         = "1.18.0"
    lazy val catsCoreVersion           = "2.12.0"
    lazy val pureConfigVersion         = "0.17.6"
    lazy val playJsonExtensionsVersion = "0.42.0"
    lazy val awsJavaSdkS3Version       = "1.12.736"
    lazy val jacksonModuleScalaVersion = "2.17.1"
    lazy val postgresqlVersion         = "42.7.3"
    lazy val refinedVersion            = "0.11.2"
    lazy val spoiwoVersion             = "2.2.1"
    lazy val itext7CoreVersion         = "8.0.4"
    lazy val html2pdfVersion           = "5.0.4"
    lazy val chimneyVersion            = "0.8.5"
    lazy val sttp                      = "3.9.7"
    lazy val sttpPlayJson              = "3.9.7"
    lazy val flyWayVersion             = "10.14.0"
    lazy val janino                    = "3.1.12"
    // Cannot be updated to "7.4" because of the following error when logging as JSON:
    // java.lang.NoSuchMethodError: 'java.time.Instant ch.qos.logback.classic.spi.ILoggingEvent.getInstant()'
    // If we want to upgrade, we MUST check json logs (env var USE_TEXT_LOGS set to false) to see if this error still happen
    lazy val logstashLogbackEncoder = "7.3"


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
    val commonsCompiler = "org.codehaus.janino" % "commons-compiler" % Versions.janino // Needed for janino
    val logstashLogBackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % Versions.logstashLogbackEncoder
    val sttpPlayJson = "com.softwaremill.sttp.client3" %% "play-json"                        % Versions.sttpPlayJson
    val sttp         = "com.softwaremill.sttp.client3" %% "core"                             % Versions.sttp
    val asyncSttp    = "com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % Versions.sttp
    val sentry       = "io.sentry"                      % "sentry-logback"                   % Versions.sentryVersion
    val catsCore     = "org.typelevel"                 %% "cats-core"                        % Versions.catsCoreVersion
    val pureConfig         = "com.github.pureconfig" %% "pureconfig"            % Versions.pureConfigVersion
    val playJsonExtensions = "ai.x"                  %% "play-json-extensions"  % Versions.playJsonExtensionsVersion
    val playSlick          = "org.playframework"     %% "play-slick"            % Versions.playSlickVersion
    val slickPg            = "com.github.tminglei"   %% "slick-pg"              % Versions.slickPgVersion
    val slickPgPlayJson    = "com.github.tminglei"   %% "slick-pg_play-json"    % Versions.slickPgVersion
    val playMailer         = "org.playframework"     %% "play-mailer"           % Versions.playMailerVersion
    val pekkoConnectorS3   = "org.apache.pekko"      %% "pekko-connectors-s3"   % Versions.pekkoVersion
    val pekkoConnectorCSV  = "org.apache.pekko"      %% "pekko-connectors-csv"  % Versions.pekkoVersion
    val pekkoConnectorFile = "org.apache.pekko"      %% "pekko-connectors-file" % Versions.pekkoVersion
    val pekkoHttp          = "org.apache.pekko"      %% "pekko-http"            % Versions.pekkoHttpVersion
    val pekkoHttpXml       = "org.apache.pekko"      %% "pekko-http-xml"        % Versions.pekkoHttpVersion
    val jbcrypt            = "org.mindrot"            % "jbcrypt"               % "0.4"
    val enumeratumPlay     = "com.beachape"          %% "enumeratum-play"       % Versions.enumeratumVersion
    val awsJavaSdkS3       = "com.amazonaws"          % "aws-java-sdk-s3"       % Versions.awsJavaSdkS3Version
    val jacksonModuleScala =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScalaVersion
    val postgresql = "org.postgresql"       % "postgresql"  % Versions.postgresqlVersion
    val refinded   = "eu.timepit"          %% "refined"     % Versions.refinedVersion
    val spoiwo     = "com.norbitltd"       %% "spoiwo"      % Versions.spoiwoVersion
    val itext7Core = "com.itextpdf"         % "itext7-core" % Versions.itext7CoreVersion
    val html2pdf   = "com.itextpdf"         % "html2pdf"    % Versions.html2pdfVersion
    val chimney    = "io.scalaland"        %% "chimney"     % Versions.chimneyVersion
    val playGuard  = "com.digitaltangible" %% "play-guard"  % "3.0.0"
    val argon2Jvm      = "de.mkammerer"   % "argon2-jvm"      % "2.11"
  }

  val AppDependencies = Seq(
    Compile.janino,
    Compile.commonsCompiler,
    Compile.logstashLogBackEncoder,
    Compile.sttp,
    Compile.asyncSttp,
    Compile.sttpPlayJson,
    Compile.sentry,
    Compile.catsCore,
    Compile.pureConfig,
    Compile.playJsonExtensions,
    Compile.playSlick,
    Compile.slickPg,
    Compile.slickPgPlayJson,
    Compile.playMailer,
    Compile.pekkoConnectorS3,
    Compile.pekkoConnectorCSV,
    Compile.pekkoConnectorFile,
    Compile.pekkoHttp,
    Compile.pekkoHttpXml,
    Compile.jbcrypt,
    Compile.enumeratumPlay,
    Compile.awsJavaSdkS3,
    Compile.jacksonModuleScala,
    Compile.postgresql,
    Compile.refinded,
    Compile.spoiwo,
    Compile.itext7Core,
    Compile.html2pdf,
    Compile.chimney,
    Compile.flywayCore,
    Compile.flywayPostgres,
    Compile.playGuard,
    Compile.argon2Jvm,
    Test.specs2Import,
    Test.specs2MatcherExtra,
    Test.scalaCheck,
    Test.pekkoTestKit
  )
}
