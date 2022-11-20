package dev.yavuztas.cap.capsource.feed

import io.vertx.core.buffer.Buffer

interface FeedData {

  fun asMutable(): Buffer

  fun asDuplicate(): Buffer {
    return Buffer.buffer(asMutable().byteBuf)
  }

  fun asReadOnly(): Buffer {
    return Buffer.buffer(asMutable().byteBuf.asReadOnly())
  }

}
