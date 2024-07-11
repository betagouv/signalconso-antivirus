package actors.antivirus

case class AntivirusScanExecution private (exitCode: Option[AntivirusScanExitCode], output: String)

object AntivirusScanExecution {
  def apply(exitCode: AntivirusScanExitCode, output: String): AntivirusScanExecution =
    AntivirusScanExecution(Some(exitCode), output)
}
