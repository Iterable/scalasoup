package com.iterable.scalasoup

sealed trait ParentState

object ParentState {
  sealed trait HasParent extends ParentState
  sealed trait NoParent extends ParentState
}