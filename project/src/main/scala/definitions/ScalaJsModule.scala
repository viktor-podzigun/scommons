package definitions

import org.sbtidea.SbtIdeaPlugin.ideaExcludeFolders
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait ScalaJsModule extends BasicModule {

  override def definition: Project = {
    super.definition
      .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
      .settings(
        scalaJSModuleKind := ModuleKind.CommonJSModule,
        //Opt-in @ScalaJSDefined by default
        scalacOptions += "-P:scalajs:sjsDefinedByDefault",
        requiresDOM in Test := true,
        enableReloadWorkflow := true,
        emitSourceMaps := false,
        ideaExcludeFolders ++= List(
          s"$id/build",
          s"$id/node_modules",
          s"$id/target"
        ),
        cleanKeepFiles ++= Seq(
          target.value / "scala-2.12" / "scalajs-bundler" / "main" / "node_modules",
          target.value / "scala-2.12" / "scalajs-bundler" / "test" / "node_modules",
          target.value / "scalajs-bundler-jsdom" / "node_modules"
        )
      )
  }
}