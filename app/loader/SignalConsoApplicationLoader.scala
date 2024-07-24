package loader

import _root_.controllers._
import actors._
import config._
import org.apache.pekko.actor.typed
import org.apache.pekko.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import org.flywaydb.core.Flyway
import play.api._
import play.api.db.slick.DbName
import play.api.db.slick.SlickComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import repositories.FileDataRepository
import service.AntivirusService
import service.S3Service
import service.S3ServiceInterface
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class SignalConsoApplicationLoader() extends ApplicationLoader {
  var components: SignalConsoComponents = _

  override def load(context: ApplicationLoader.Context): Application = {
    components = new SignalConsoComponents(context)
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    components.application
  }
}

class SignalConsoComponents(
    context: ApplicationLoader.Context
) extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with play.filters.cors.CORSComponents
    with AhcWSComponents
    with SlickComponents {

  val logger: Logger = Logger(this.getClass)

  val applicationConfiguration: ApplicationConfiguration = ConfigSource.default.loadOrThrow[ApplicationConfiguration]

  // Run database migration scripts
  Flyway
    .configure()
    .dataSource(
      applicationConfiguration.flyway.jdbcUrl,
      applicationConfiguration.flyway.user,
      applicationConfiguration.flyway.password
    )
    .schemas("signalconso_antivirus")
    // DATA_LOSS / DESTRUCTIVE / BE AWARE ---- Keep to "false"
    // Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake!
    // This is useful for initial Flyway production deployments on projects with an existing DB.
    // See https://flywaydb.org/documentation/configuration/parameters/baselineOnMigrate for more information
    .baselineOnMigrate(applicationConfiguration.flyway.baselineOnMigrate)
    .load()
    .migrate()

  //  Repositories

  val dbConfig: DatabaseConfig[JdbcProfile] = slickApi.dbConfig[JdbcProfile](DbName("default"))

  implicit val bucketConfiguration: BucketConfiguration = BucketConfiguration(
    keyId = configuration.get[String]("pekko.connectors.s3.aws.credentials.access-key-id"),
    secretKey = configuration.get[String]("pekko.connectors.s3.aws.credentials.secret-access-key"),
    amazonBucketName = applicationConfiguration.amazonBucketName
  )

  val s3Service: S3ServiceInterface = new S3Service()

  val fileDataRepository = new FileDataRepository(dbConfig)

  val antivirusScanActor: typed.ActorRef[AntivirusScanActor.ScanCommand] = actorSystem.spawn(
    AntivirusScanActor.create(applicationConfiguration.app, fileDataRepository, s3Service),
    "antivirus-scan-actor"
  )

  val antivirusService = new AntivirusService(antivirusScanActor, fileDataRepository)

  val antivirusController =
    new AntivirusController(
      controllerComponents,
      applicationConfiguration.app.apiAuthenticationToken,
      applicationConfiguration.app.tmpDirectory,
      antivirusService
    )

  io.sentry.Sentry.captureException(
    new Exception("This is a test Alert, used to check that Sentry alert are still active on each new deployments.")
  )

  // Routes
  lazy val router: Router =
    new _root_.router.Routes(
      httpErrorHandler,
      antivirusController
    )

  override def httpFilters: Seq[EssentialFilter] =
    Seq(
      new LoggingFilter(),
      securityHeadersFilter,
      allowedHostsFilter,
      corsFilter
    )

}
