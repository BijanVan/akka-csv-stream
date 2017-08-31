package com.bijansoft

import akka.util.ByteString

case class Reading(id: Int, value: Double) {
  override def toString: String = s"Reading($id, $value)"
}

object Reading {
  def apply(fields: Seq[ByteString]): Reading =
    new Reading(fields.head.utf8String.toInt, fields.tail.head.utf8String.toDouble)
}
