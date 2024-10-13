package com.whim.generic

trait Parser[A]{
  def parse(toParse: String): A
}
