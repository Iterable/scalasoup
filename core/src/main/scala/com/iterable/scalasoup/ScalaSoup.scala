package com.iterable.scalasoup

object ScalaSoup {
  def parse(html: String, baseUri: String): Document[ParentState.NoParent] =
    new Document(org.jsoup.Jsoup.parse(html, baseUri))

  def parse(html: String, baseUri: String, parser: org.jsoup.parser.Parser): Document[ParentState.NoParent] =
    new Document(org.jsoup.Jsoup.parse(html, baseUri, parser))

  def parse(html: String): Document[ParentState.NoParent] = new Document(org.jsoup.Jsoup.parse(html))

  def parseBodyFragment(bodyHtml: String, baseUri: String): Document[ParentState.NoParent] =
    new Document(org.jsoup.Jsoup.parseBodyFragment(bodyHtml, baseUri))

  def parseBodyFragment(bodyHtml: String): Document[ParentState.NoParent] =
    new Document(org.jsoup.Jsoup.parseBodyFragment(bodyHtml))

  def clean(bodyHtml: String, baseUri: String, safelist: Safelist): String =
    org.jsoup.Jsoup.clean(bodyHtml, baseUri, safelist.underlying)

  def clean(bodyHtml: String, safelist: Safelist): String =
    org.jsoup.Jsoup.clean(bodyHtml, safelist.underlying)

  def clean(bodyHtml: String, baseUri: String, safelist: Safelist, outputSettings: OutputSettings): String =
    org.jsoup.Jsoup.clean(bodyHtml, baseUri, safelist.underlying, outputSettings.underlying)

  def isValid(bodyHtml: String, safelist: Safelist): Boolean = org.jsoup.Jsoup.isValid(bodyHtml, safelist.underlying)

  private[scalasoup] def withClone[A <: java.lang.Cloneable, B](underlying: A)(clone: A => A)(ctor: A => B)(op: A => Unit): B = {
    val c = clone(underlying)
    op(c)
    ctor(c)
  }
}

