import mill._
import mill.scalalib._
import $ivy.`com.goyeau::mill-scalafix::0.4.2`
import com.goyeau.mill.scalafix.ScalafixModule
import scalafmt._
import scalajslib._
import mill.scalajslib.api.ModuleKind

object config {
  val scalaVersion   = "3.5.1"
  val scalaJSVersion = "1.17.0"
}

trait AppScalaModule extends ScalaModule with ScalafixModule with ScalafmtModule {
  def scalaVersion  = config.scalaVersion
  def scalacOptions = Seq("-Wunused:all")
}

trait AppScalaJSModule extends AppScalaModule with ScalaJSModule with ScalafixModule with ScalafmtModule {
  def scalaJSVersion = config.scalaJSVersion
  def scalacOptions  = Seq("-Wunused:all")
}

object tasklist extends Module {

  object backend extends AppScalaModule {
    def moduleDeps = Seq(shared.jvm)
    def ivyDeps    = Agg(
      ivy"io.github.michalliss::foxxy-backend:0.0.7",
      ivy"io.github.michalliss::foxxy-repo:0.0.7",
      ivy"io.github.michalliss::foxxy-auth:0.0.7",
      ivy"dev.zio::zio-logging:2.3.2",
      ivy"dev.zio::zio-logging-slf4j2-bridge:2.3.2"
    )

    object test extends ScalaTests with TestModule.ZioTest {
      def ivyDeps = Agg(
        ivy"io.github.michalliss::foxxy-testing:0.0.7",
        ivy"dev.zio::zio-test:2.1.11",
        ivy"dev.zio::zio-test-sbt:2.1.11",
        ivy"dev.zio::zio-test-magnolia:2.1.11"
      )
    }
  }

  object frontend extends AppScalaJSModule {
    def moduleKind = ModuleKind.ESModule

    def moduleDeps = Seq(shared.js)
    def ivyDeps    = Agg(
      ivy"io.github.michalliss::foxxy-frontend::0.0.7",
      ivy"be.doeraene::web-components-ui5::2.0.0"
    )
  }

  object shared extends Module {
    trait SharedModule extends AppScalaModule with PlatformScalaModule

    object jvm extends SharedModule {
      def ivyDeps = Agg(ivy"io.github.michalliss::foxxy-shared:0.0.7")
    }

    object js extends SharedModule with AppScalaJSModule {
      def ivyDeps = Agg(ivy"io.github.michalliss::foxxy-shared::0.0.7")
    }
  }

  object frontend_vite extends Module {
    def moduleDeps = Seq(frontend)

    def compile = T {
      val jsPath = frontend.fastLinkJS().dest.path

      if (!os.exists(frontend_vite.millSourcePath / "app")) {
        os.makeDir(frontend_vite.millSourcePath / "app")
      }
      os.copy(
        jsPath / "main.js",
        frontend_vite.millSourcePath / "app" / "main.js",
        replaceExisting = true
      )
      os.copy(
        jsPath / "main.js.map",
        frontend_vite.millSourcePath / "app" / "main.js.map",
        replaceExisting = true
      )
    }
  }
}
