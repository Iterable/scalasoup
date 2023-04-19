package com.iterable.scalasoup

import com.iterable.scalasoup.Safelist.SafelistCall

final case class Safelist private[scalasoup] (
                                                private [scalasoup] val seed: () => org.jsoup.safety.Safelist,
                                                private [scalasoup] val calls: List[SafelistCall]
                                              ) {
  def addTags(tags: String*): Safelist =
    withCopy(_.addTags(tags: _*))

  def removeTags(tags: String*): Safelist =
    withCopy(_.removeTags(tags: _*))

  def addAttributes(tag: String, attributes: String*): Safelist =
    withCopy(_.addAttributes(tag, attributes: _*))

  def removeAttributes(tag: String, attributes: String*): Safelist =
    withCopy(_.removeAttributes(tag, attributes: _*))

  def addEnforcedAttribute(tag: String, attribute: String, value: String): Safelist =
    withCopy(_.addEnforcedAttribute(tag, attribute, value))

  def removeEnforcedAttribute(tag: String, attribute: String): Safelist =
    withCopy(_.removeEnforcedAttribute(tag, attribute))

  def preserveRelativeLinks(preserve: Boolean): Safelist =
    withCopy(_.preserveRelativeLinks(preserve))

  def addProtocols(tag: String, attribute: String, protocols: String*): Safelist =
    withCopy(_.addProtocols(tag, attribute, protocols: _*))

  def removeProtocols(tag: String, attribute: String, removeProtocols: String*): Safelist =
    withCopy(_.removeProtocols(tag, attribute, removeProtocols: _*))

  private[scalasoup] def underlying: org.jsoup.safety.Safelist = {
    val whitelist = seed()
    calls.foreach(call => call(whitelist))
    whitelist
  }

  private def withCopy(call: SafelistCall) = copy(calls = calls :+ call)
}

object Safelist {
  type SafelistCall = org.jsoup.safety.Safelist => Unit

  def none: Safelist = Safelist(() => org.jsoup.safety.Safelist.none(), List.empty[SafelistCall])

  def simpleText: Safelist = Safelist(() => org.jsoup.safety.Safelist.none(), List.empty[SafelistCall])

  def basic: Safelist = Safelist(() => org.jsoup.safety.Safelist.none(), List.empty[SafelistCall])

  def basicWithImages: Safelist = Safelist(() => org.jsoup.safety.Safelist.none(), List.empty[SafelistCall])

  def relaxed: Safelist = Safelist(() => org.jsoup.safety.Safelist.none(), List.empty[SafelistCall])
}