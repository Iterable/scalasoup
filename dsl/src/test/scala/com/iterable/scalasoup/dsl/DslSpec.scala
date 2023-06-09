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

    val result = doc.modify(modifications)

    doc.title shouldBe "Old Title"
    doc.baseUri shouldBe ""

    result.title shouldBe "New Title"
    result.baseUri shouldBe "https://jsoup.org/"
  }

  it should "combine separate modifyDocument chains" in {
    val modifications1 = for {
      d <- modifyDocument
      _ <- d.setTitle("New Title")
      _ <- d.setBaseUri("https://jsoup.org/")
    } yield ()
    val modifications2 = for {
      d <- modifyDocument
      _ <- d.setOutputSettings(d.outputSettings.copy(prettyPrint = false))
      _ <- d.body.get.childNodes.last.setAttr("data-blah", "blah")
      _ <- d.select("a").foldMapM(_.setAttr("href", "https://jsoup.org/"))
    } yield ()

    val result = ScalaSoup.parse("<html><head><title>Old Title</title></head><body><div><a></a></div></body></html>")
      .modify(modifications1 flatMap { _ => modifications2 })

    result.title shouldBe "New Title"
    result.baseUri shouldBe "https://jsoup.org/"

    result.body.get.html shouldBe "<div data-blah=\"blah\"><a href=\"https://jsoup.org/\"></a></div>"
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

    doc.modify(modifications)
  }

  "Editing the body element" should "leave the original document unchanged" in {
    val modifications = for {
      document <- modifyDocument
      _        <- document.body.foldMapM(_.addClass("foo"))
    } yield document

    val doc = ScalaSoup.parse("<html><body><div></div></body></html>")

    val result = doc.modify(modifications)

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

    val result = doc.modify(modifications)

    result.html should not include "target"
    doc.html should include ("target")
  }

  "Removing attributes via attributes collection" should "leave the original document unchanged" in {
    val modifications = for {
      document <- modifyDocument
      _        <- document.selectChildren("a").foldMapM(_.attributes.remove("target"))
    } yield document

    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val result = doc.modify(modifications)

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
      doc.modify(modifications)
    }
  }

  "ScalaSoup" should "throw if trying to modify attributes not owned by document" in {
    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val modifications = for {
      d        <- modifyDocument
      _        <- doc.attributes.remove("foo")
    } yield d

    an [IllegalStateException] should be thrownBy {
      doc.modify(modifications)
    }
  }

  "ScalaSoup" should "throw if trying to modify attribute not owned by document" in {
    val doc = ScalaSoup.parse("<a target=\"_blank\"></a>")

    val modifications = for {
      d        <- modifyDocument
      _        <- doc.select("a").flatMap(_.attributes.headOption).foldMapM(_.setValue("foo"))
    } yield d

    an [IllegalStateException] should be thrownBy {
      doc.modify(modifications)
    }
  }

  "Removing the head element" should "remove it" in {
    val doc = Document.createShell("")

    val modifications = for {
      d <- modifyDocument
      _ <- d.head.foldMapM(_.remove)
    } yield d

    val result = doc.modify(modifications)

    doc.head should not be empty
    result.head should be (empty)
  }

  "Removing the body element" should "remove it" in {
    val doc = Document.createShell("")

    val modifications = for {
      d <- modifyDocument
      _ <- d.body.foldMapM(_.remove)
    } yield d

    val result = doc.modify(modifications)

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
    doc.modify(modifications).html shouldBe "<html><head></head><body><span></span></body></html>"
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
    doc.modify(modifications).html shouldBe "<html><head><script></script></head><body></body></html>"
  }

  "setAttr" should "work for string or boolean attributes" in {
    val doc = ScalaSoup.parse("<body></body>")
    mutable.MutableNode(doc.head.get).remove()

    doc.head shouldBe empty
    val modifications = for {
      d <- modifyDocument
      _ <- d.setOutputSettings(d.outputSettings.copy(prettyPrint = false))
      body = d.body.get
      _ <- body.setAttr("data-str", "blah")
      _ <- body.setBooleanAttr("data-bool", true)
    } yield d
    doc.modify(modifications).html shouldBe
      "<html><body data-str=\"blah\" data-bool></body></html>"
  }

  "modifyAndAccumulate" should "modify and return the accumulated value" in {
    val doc = ScalaSoup.parse("<body></body>")
    mutable.MutableNode(doc.head.get).remove()

    doc.head shouldBe empty
    val modifications = for {
      d <- modifyDocument
      _ <- d.setOutputSettings(d.outputSettings.copy(prettyPrint = false))
      body = d.body.get
      a <- body.setAttr("data-str", "blah").map(_ => 1)
      b <- body.setBooleanAttr("data-bool", true).map(_ => 2)
      c <- body.appendElement("div").map(_ => 3)
    } yield List(a, b, c)
    val (result, acc) = doc.modifyAndAccumulate(modifications)
    result.html shouldBe "<html><body data-str=\"blah\" data-bool><div></div></body></html>"
    acc shouldBe List(1, 2, 3)
  }

  it should "fold accumulated values into a single list" in {
    val modifications = for {
      document <- modifyDocument
      target   <- document.selectChildren("a").foldMapM { e =>
        val originalTarget = e.attr("target")
        e.removeAttr("target").map(_ => List(originalTarget))
      }
    } yield target

    val doc = ScalaSoup.parse("<a target=\"_blank\"></a><a target=\"blah\"></a>", org.jsoup.parser.Parser.xmlParser())

    val (result, removedTargets) = doc.modifyAndAccumulate(modifications)

    result.html shouldBe "<a></a><a></a>"
    removedTargets shouldBe List("_blank", "blah")
  }
}
