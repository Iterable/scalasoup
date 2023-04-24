package com.iterable.scalasoup

import org.jsoup.nodes.Document.QuirksMode

import scala.jdk.CollectionConverters._
import java.nio.charset.Charset
import scala.collection.immutable.{List, Set}

package object mutable {
  private[scalasoup] implicit class MutableNode[A <: ParentState](val node: Node[A]) extends AnyVal {

    def setAttr(attributeKey: String, attributeValue: String): Unit = node.underlying.attr(attributeKey, attributeValue)

    def removeAttr(attributeKey: String): Unit = node.underlying.removeAttr(attributeKey)

    def clearAttributes(): Unit = node.underlying.clearAttributes()

    def setBaseUri(baseUri: String): Unit = node.underlying.setBaseUri(baseUri)

    def remove()(implicit ev: HasParent[A]): Unit = node.underlying.remove()

    def before(html: String)(implicit ev: HasParent[A]): Unit = node.underlying.before(html)

    def before(node: Node[_ <: ParentState])(implicit ev: HasParent[A]): Unit =
      node.underlying.before(node.underlying.clone)

    def after(html: String)(implicit ev: HasParent[A]): Unit = node.underlying.after(html)

    def after(n: Node[_ <: ParentState])(implicit ev: HasParent[A]): Unit =
      node.underlying.after(n.underlying.clone)

    def wrap(html: String)(implicit ev: HasParent[A]): Unit =node.underlying.wrap(html)

    def unwrap()(implicit ev: HasParent[A]): Option[Node[ParentState.HasParent]] =
      Option(node.underlying.unwrap()).map(Node.fromUnderlying)

    def replaceWith(in: Node[_ <: ParentState])(implicit ev: HasParent[A]): Unit =
      node.underlying.replaceWith(in.underlying.clone)
  }

  private[scalasoup] implicit class MutableTextNode[A <: ParentState](val node: TextNode[A]) extends AnyVal {
    def setText(text: String): Unit = node.underlying.text(text)

    def splitText(offset: Int): TextNode[A] = new TextNode(node.underlying.splitText(offset))
  }

  private[scalasoup] implicit class MutableDataNode[A <: ParentState](val node: DataNode[A]) extends AnyVal {
    def setData(data: String): Unit = node.underlying.setWholeData(data)
  }

  private[scalasoup] implicit class MutableElement[A <: ParentState](val element: Element[A]) extends AnyVal {

    def setTagName(tagName: String): Unit = element.underlying.tagName(tagName)

    def setAttr(attributeKey: String, attributeValue: Boolean): Unit =
      element.underlying.attr(attributeKey, attributeValue)

    def appendChild(child: Node[_ <: ParentState]): Unit = element.underlying.appendChild(child.underlying.clone)

    def appendTo(parent: Element[_ <: ParentState]): Unit = element.underlying.appendTo(parent.underlying.clone)

    def prependChild(child: Node[_ <: ParentState]): Unit = element.underlying.prependChild(child.underlying.clone)

    def insertChildren(index: Int, children: List[Node[_ <: ParentState]]): Unit =
      element.underlying.insertChildren(index, children.map(_.underlying.clone).asJava)

    def insertChildren(index: Int, children: Node[_ <: ParentState]*): Unit = insertChildren(index, children.toList)

    def appendElement(tagName: String): Element[ParentState.HasParent] = Element.fromUnderlying(element.underlying.appendElement(tagName))

    def prependElement(tagName: String): Element[ParentState.HasParent] = Element.fromUnderlying(element.underlying.prependElement(tagName))

    def appendText(text: String): Unit = element.underlying.appendText(text)

    def prependText(text: String): Unit = element.underlying.prependText(text)

    def append(html: String): Unit = element.underlying.append(html: String)

    def prepend(html: String): Unit = element.underlying.prepend(html: String)

    def empty(): Unit = element.underlying.empty()

    def setText(text: String): Unit = element.underlying.text(text)

    def setClassNames(classNames: Set[String]): Unit = element.underlying.classNames(classNames.asJava)

    def addClass(className: String): Unit = element.underlying.addClass(className)

    def removeClass(className: String): Unit = element.underlying.removeClass(className)

    def toggleClass(className: String): Unit = element.underlying.toggleClass(className)

    def setValue(value: String): Unit = element.underlying.`val`(value)

    def setHtml(html: String): Unit = element.underlying.html(html)
  }

  private[scalasoup] implicit class MutableDocument[A <: ParentState](val document: Document[A]) extends AnyVal {

    def setTitle(title: String): Unit = document.underlying.title(title)

    def normalise(): Unit = document.underlying.normalise()

    def head(): Element[ParentState.HasParent] = new Element(document.underlying.head())

    def body(): Element[ParentState.HasParent] = new Element(document.underlying.body())

    def setCharset(charset: Charset): Unit = document.underlying.charset(charset)

    def setUpdateMetaCharsetElement(update: Boolean): Unit = document.underlying.updateMetaCharsetElement(update)

    def setOutputSettings(outputSettings: OutputSettings): Unit =
      document.underlying.outputSettings(outputSettings.underlying)

    def setQuirksMode(quirksMode: QuirksMode): Unit =
      document.underlying.quirksMode(quirksMode)
  }
}
