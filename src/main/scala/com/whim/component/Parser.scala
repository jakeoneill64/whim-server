package com.whim.component

trait Parser[A]{
  def parse(toParse: String): A
}
