package definitions

import common.{Libs, TestLibs}
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ClientUi extends ScalaJsModule {

  override val id: String = "scommons-client-ui"

  override def definition: Project = super.definition
    .settings(
      description := "Common Scala.js, React.js web-client utilities and components",
      coverageExcludedPackages := ".*Css" +
        ";.*BaseStateController" +
        ";.*BaseStateAndRouteController" +
        ";scommons.client.ui.popup.WithAutoHide" + // causes "a dangling UndefinedParam", see https://github.com/scoverage/scalac-scoverage-plugin/issues/196
        ";scommons.client.ui.popup.raw" +
        ";scommons.client.ui.select.raw" +
        ";scommons.client.ui.HTML.InnerHTML",

      webpackConfigFile in Test := Some(
        baseDirectory.value / "src" / "main" / "resources" / "scommons.webpack.config.js"
      ),

      npmDependencies in Compile ++= Seq(
        "react-modal" -> "3.1.2",
        "react-select" -> "2.0.0"
      ),

      npmDevDependencies in Compile ++= Seq(
        "css-loader" -> "0.28.7",
        "extract-text-webpack-plugin" -> "3.0.0",
        "resolve-url-loader" -> "3.0.0",
        "url-loader" -> "1.1.2",
        "file-loader" -> "1.1.4",
        "style-loader" -> "0.18.2",
        "webpack-merge" -> "4.2.1"
      )
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override val superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-api", "scommons-api-coreJS", None),
    ("scommons-react", "scommons-react-core", None),
    ("scommons-react", "scommons-react-test-dom", Some("test"))
  )

  override val runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    Libs.scommonsReactCore.value,
    Libs.scommonsApiCore.value,

    Libs.sjsReactJsRouterDom.value,     // Optional. For react-router-dom facade
    Libs.sjsReactJsRouterRedux.value,   // Optional. For react-router-redux facade
    Libs.sjsReactJsRedux.value,         // Optional. For react-redux facade
    Libs.sjsReactJsReduxDevTools.value  // Optional. For redux-devtools facade
  ))

  override val testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    TestLibs.scommonsReactTestDom.value
  ).map(_ % "test"))
}
