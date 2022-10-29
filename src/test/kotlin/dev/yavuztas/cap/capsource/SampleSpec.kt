package dev.yavuztas.cap.capsource

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SampleSpec : StringSpec({

    "length should return size of string" {
        "hello".length shouldBe 5
    }

})
