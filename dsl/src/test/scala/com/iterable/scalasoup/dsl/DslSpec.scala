package com.iterable.scalasoup.dsl

import cats.implicits._
import com.iterable.scalasoup._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DslSpec extends AnyFlatSpec with Matchers {

  "Editing a document" should "leave the original document unchanged" in {
    val modifications = for {
      document <- modifyDocument
      _        <- document.setTitle("New Title")
      _        <- document.setBaseUri("https://jsoup.org/")
    } yield document

    val doc = ScalaSoup.parse("<html><head><title>Old Title</title></head><body></body></html>")

    val result = doc.withModifications(modifications)

    doc.title shouldBe "Old Title"
    doc.baseUri shouldBe ""

    result.title shouldBe "New Title"
    result.baseUri shouldBe "https://jsoup.org/"
  }

  "The DSL" should "support combining programs" in {
    val asProgram = for {
      document <- modifyDocument
    } yield document.selectChildren("a")

    val modifications = for {
      as <- asProgram
      _ <- as.foldMapM(_.addClass("foo"))
    } yield ()

    val doc = ScalaSoup.parse("<html><head><title>Old Title</title></head><body></body></html>")

    val result = doc.withModifications(modifications)
  }

  "Editing the body element" should "leave the original document unchanged" in {
    val modifications = for {
      document <- modifyDocument
      _        <- document.body.foldMapM(_.addClass("foo"))
    } yield document

    val doc = ScalaSoup.parse("<html><body><div></div></body></html>")

    val result = doc.withModifications(modifications)

    doc.body shouldBe defined
    doc.body.get.hasClass("foo") shouldBe false

    result.body shouldBe defined
    result.body.get.hasClass("foo") shouldBe true
  }

  "Removing attributes" should "leave the original document unchanged" in {
    val modifications = for {
      document <- modifyDocument
      _        <- document.selectChildren("a").foldMapM(_.removeAttr("target"))
    } yield document

    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val result = doc.withModifications(modifications)

    result.html should not include "target"
    doc.html should include ("target")
  }

  "Removing attributes via attributes collection" should "leave the original document unchanged" in {
    val modifications = for {
      document <- modifyDocument
      _        <- document.selectChildren("a").foldMapM(_.attributes.remove("target"))
    } yield document

    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val result = doc.withModifications(modifications)

    result.html should not include "target"
    doc.html should include ("target")
  }

  "ScalaSoup" should "throw if trying to modify element not owned by document" in {
    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val modifications = for {
      d        <- modifyDocument
      _        <- doc.selectChildren("a").foldMapM(_.removeAttr("target"))
    } yield d

    an [IllegalStateException] should be thrownBy {
      doc.withModifications(modifications)
    }
  }

  "ScalaSoup" should "throw if trying to modify attributes not owned by document" in {
    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val modifications = for {
      d        <- modifyDocument
      _        <- doc.attributes.remove("foo")
    } yield d

    an [IllegalStateException] should be thrownBy {
      doc.withModifications(modifications)
    }
  }

  "ScalaSoup" should "throw if trying to modify attribute not owned by document" in {
    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val modifications = for {
      d        <- modifyDocument
      _        <- doc.select("a").flatMap(_.attributes.headOption).foldMapM(_.setValue("foo"))
    } yield d

    an [IllegalStateException] should be thrownBy {
      doc.withModifications(modifications)
    }
  }

  "Removing the head element" should "remove it" in {
    val doc = Document.createShell("")

    val modifications = for {
      d <- modifyDocument
      _ <- d.head.foldMapM(_.remove)
    } yield d

    val result = doc.withModifications(modifications)

    doc.head should not be empty
    result.head should be (empty)
  }

  "Removing the body element" should "remove it" in {
    val doc = Document.createShell("")

    val modifications = for {
      d <- modifyDocument
      _ <- d.body.foldMapM(_.remove)
    } yield d

    val result = doc.withModifications(modifications)

    doc.body should not be empty
    result.body should be (empty)
  }

  "Removing an element without a parent" should "be a type error" in {
    val noParent: Element[ParentState.NoParent] = Element("div")
    assertTypeError("noParent.remove")
  }

  "Removing an element with a parent" should "compile" in {
    val hasParent: Option[Element[ParentState.HasParent]] = Document.createShell("").body
    hasParent.foreach(_.remove)
  }

  "getOrCreateBody" should "get or create the body element if it was removed" in {
    val doc = ScalaSoup.parse("<div></div>")
      mutable.MutableNode(doc.body.get).remove()

    doc.body shouldBe empty
    val modifications = for {
      d <- modifyDocument
      _ <- d.setOutputSettings(d.outputSettings.copy(prettyPrint = false))
      body <- d.getOrCreateBody
      _ <- body.appendElement("span")
    } yield d
    doc.withModifications(modifications).html shouldBe "<html><head></head><body><span></span></body></html>"
  }

  "getOrCreateHead" should "get or create the head element if it was removed" in {
    val doc = ScalaSoup.parse("<body></body>")
      mutable.MutableNode(doc.head.get).remove()

    doc.head shouldBe empty
    val modifications = for {
      d <- modifyDocument
      _ <- d.setOutputSettings(d.outputSettings.copy(prettyPrint = false))
      head <- d.getOrCreateHead
      _ <- head.appendElement("script")
    } yield d
    doc.withModifications(modifications).html shouldBe "<html><head><script></script></head><body></body></html>"
  }
}
