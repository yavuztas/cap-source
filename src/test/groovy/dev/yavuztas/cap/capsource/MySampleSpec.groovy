package dev.yavuztas.cap.capsource

import spock.lang.Specification

class MySampleSpec extends Specification {

  def "length should return size of string"() {
    expect:
    "hello".length() == 5
  }

  def "strings should match"() {
    expect:
    "hello" == "hexlo"
  }

}
