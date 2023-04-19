
val commonSettings = Seq(
  organization       := "com.iterable",
  scalaVersion       := "2.13.7",
  version            := "0.1.0",
  // TODO https://tpolecat.github.io/2017/04/25/scalac-flags.html
  scalacOptions      := Seq(
    "-deprecation",
    "-Xfatal-warnings"
  ),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.4" % Test
  ),
  // TODO: WartRemover, scalafmt, etc.
  (Compile / compile / wartremoverErrors) := Seq(
    //    Wart.Any,
    Wart.AnyVal,
    Wart.ArrayEquals,
    Wart.AsInstanceOf,
    Wart.DefaultArguments,
    Wart.EitherProjectionPartial,
    Wart.Enumeration,
    Wart.Equals,
    Wart.ExplicitImplicitTypes,
    Wart.FinalCaseClass,
    Wart.FinalVal,
    //    Wart.ImplicitConversion,
    //    Wart.ImplicitParameter,
    Wart.IsInstanceOf,
    Wart.IterableOps,
    Wart.JavaConversions,
    Wart.JavaSerializable,
    Wart.LeakingSealed,
    Wart.MutableDataStructures,
    //    Wart.NonUnitStatements,
    //    Wart.Nothing,
    Wart.Null,
    Wart.Option2Iterable,
    Wart.OptionPartial,
    //    Wart.Overloading,
    Wart.Product,
    Wart.PublicInference,
    Wart.Recursion,
    Wart.Return,
    Wart.Serializable,
    Wart.StringPlusAny,
    Wart.Throw,
    Wart.ToString,
    Wart.TryPartial,
    Wart.Var,
    Wart.While
  )
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  aggregate(core, dsl)

lazy val core = (project in file("core")).
  settings(commonSettings:_*).
  settings(
    name := "scalasoup",
    libraryDependencies ++= Seq(
      "org.jsoup"      %  "jsoup"                 % "1.15.1",
      "eu.timepit"     %% "refined"               % "0.10.3",
      "org.http4s"     %% "http4s-blaze-client"   % "0.23.14" % Test,
      "org.scalacheck" %% "scalacheck"            % "1.17.0"    % Test
    )
  )

lazy val dsl = (project in file("dsl")).
  settings(commonSettings:_*).
  settings(
    name := "scalasoup-dsl",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"    % "2.6.0",
      "org.typelevel" %% "cats-free"    % "2.6.0"
    )
  ).
  dependsOn(core)