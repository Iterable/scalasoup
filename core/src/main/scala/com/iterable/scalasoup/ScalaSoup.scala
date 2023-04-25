package com.iterable.scalasoup

import org.jsoup.Jsoup
import org.jsoup.parser.Parser

import java.io.{File, InputStream}
import java.net.URL

object ScalaSoup {

  def parse(html: String): Document[ParentState.NoParent] = new Document(Jsoup.parse(html))

  def parse(html: String, baseUri: String): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(html, baseUri))

  def parse(html: String, parser: Parser): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(html, parser))

  def parse(html: String, baseUri: String, parser: Parser): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(html, baseUri, parser))

  def parse(file: File): Document[ParentState.NoParent] = new Document(Jsoup.parse(file))

  def parse(file: File, charsetName: String): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(file, charsetName))

  def parse(file: File, charsetName: String, baseUri: String): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(file, charsetName, baseUri))

  def parse(file: File, charsetName: String, baseUri: String, parser: Parser): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(file, charsetName, baseUri, parser))

  def parse(in: InputStream, charsetName: String, baseUri: String): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(in, charsetName, baseUri))

  def parse(in: InputStream, charsetName: String, baseUri: String, parser: Parser): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(in, charsetName, baseUri, parser))

  def parse(url: URL, timeoutMillis: Int): Document[ParentState.NoParent] =
    new Document(Jsoup.parse(url, timeoutMillis))

  def parseBodyFragment(bodyHtml: String, baseUri: String): Document[ParentState.NoParent] =
    new Document(Jsoup.parseBodyFragment(bodyHtml, baseUri))

  def parseBodyFragment(bodyHtml: String): Document[ParentState.NoParent] =
    new Document(Jsoup.parseBodyFragment(bodyHtml))

  def clean(bodyHtml: String, baseUri: String, safelist: Safelist): String =
    Jsoup.clean(bodyHtml, baseUri, safelist.underlying)

  def clean(bodyHtml: String, safelist: Safelist): String =
    Jsoup.clean(bodyHtml, safelist.underlying)

  def clean(bodyHtml: String, baseUri: String, safelist: Safelist, outputSettings: OutputSettings): String =
    Jsoup.clean(bodyHtml, baseUri, safelist.underlying, outputSettings.underlying)

  def isValid(bodyHtml: String, safelist: Safelist): Boolean = Jsoup.isValid(bodyHtml, safelist.underlying)

  private[scalasoup] def withClone[A <: java.lang.Cloneable, B](underlying: A)(clone: A => A)(ctor: A => B)(op: A => Unit): B = {
    val c = clone(underlying)
    op(c)
    ctor(c)
  }
}

