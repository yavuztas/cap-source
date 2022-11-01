package dev.yavuztas.cap.capsource.feed

import io.netty.buffer.ByteBuf
import io.vertx.core.buffer.Buffer

interface FeedData {

  fun asMutable(): Buffer

  fun asDuplicate(): ByteBuf {
    return asMutable().byteBuf
  }

  fun asReadOnly(): ByteBuf {
    return asDuplicate().asReadOnly()
  }

}
