package com.iterable.scalasoup

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.http4s.client.middleware.FollowRedirect
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ElementSpec extends AnyFlatSpec with Matchers {

  "Using an invalid regex string" should "fail to compile" in {
    val document = Document.createShell("")
    assertTypeError("""document.elementsMatchingOwnText("(")""")
  }

  /**
    * TODO Tests for CssSelectorString
    * CssSelectorString.fromString success
    * CssSelectorString.fromString failure
    * CssSelectorString.fromStringUnsafe success
    * CssSelectorString.fromStringUnsafe failure
    */

  "The select method" should "compile with a valid CSS selector" in {
    val document = Document.createShell("")
    document.select("a")
  }

  "The select method" should "fail to compile with an invalid CSS selector" in {
    val document = Document.createShell("")
    assertTypeError("""document.select("a[")""")
  }

  "The equals method" should "check equality with the underlying JSoup Node and Attributes" in {
    val doc = ScalaSoup.parse("""<html lang="en"><head></head><body title="Hello World">Hello, World!</body></html>""")
    doc.body.get.childNodes.head.equals(doc.body.get.childNodes.head) shouldBe true
    doc.body.get.attributes.head.equals(doc.body.get.attributes.head) shouldBe true
    doc.body.get.attributes.equals(doc.body.get.attributes) shouldBe true
  }

  "The Wikipedia readme example" should "compile" in {
    import org.http4s.blaze.client.BlazeClientBuilder

    val task = BlazeClientBuilder[IO].resource.use { client =>
      val httpClient = FollowRedirect[IO](maxRedirects = 3)(client)
      val uri = "https://en.wikipedia.org/"

      httpClient.expect[String](uri).map { html =>

        val doc = ScalaSoup.parse(html, uri)

        println(doc.title)
        val newsHeadlines = doc.select("#mp-itn b a")
        for (headline <- newsHeadlines) {
          println(s"${headline.attr("title")} ${headline.absUrl("href")}")
        }
      }
    }

    task.unsafeRunSync()(IORuntime.global)
  }

  // TODO: test all of the withFoo methods.
  "Simple mutation methods (withFoo)" should "leave the original element unchanged" in {
    val original = Element("div")
    val updated = original.withAddClass("foo")

    original.hasClass("foo") shouldBe false
    updated.hasClass("foo") shouldBe true
  }

  // TODO: test all of the excludingSelf methods.
  "Selecting children" should "exclude the current element, even when it matches the selector" in {
    val document = ScalaSoup.parse("""<html lang="en"><head><title>Hello, World!</title></head><body>Hello, World!</body></html>""")
    val children = document.selectChildren("*")
    val all = document.select("*")

    children.filter(_.nodeName === "#document") should be(empty)
    all.filter(_.nodeName === "#document") should not be empty
  }

  // TODO: Test HasParentElement and UnknownParentElement methods.
}