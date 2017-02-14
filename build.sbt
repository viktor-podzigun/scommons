import common.Common
import definitions._

lazy val root = (project in file("."))
  //.settings(Common.settings)
  //.configs(IntegrationTest)
  .aggregate(
  `scommons-client`
)

lazy val `scommons-client` = Client.definition