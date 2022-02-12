package de.ph1b.audiobook.scanner

import android.content.Context
import android.graphics.Bitmap
import de.ph1b.audiobook.common.ImageHelper
import de.ph1b.audiobook.data.Book2
import de.ph1b.audiobook.data.repo.BookRepo2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class CoverSaver
@Inject constructor(
  private val imageHelper: ImageHelper,
  private val repo: BookRepo2,
  private val context: Context,
) {

  suspend fun save(bookId: Book2.Id, cover: Bitmap) {
    val newCover = newBookCoverFile()
    imageHelper.saveCover(cover, newCover)
    setBookCover(newCover, bookId)
  }

  suspend fun newBookCoverFile(): File {
    val coversFolder = withContext(Dispatchers.IO) {
      File(context.filesDir, "bookCovers")
        .also { coverFolder -> coverFolder.mkdirs() }
    }
    return File(coversFolder, "${UUID.randomUUID()}.png")
  }

  suspend fun setBookCover(cover: File, bookId: Book2.Id) {
    val oldCover = repo.flow(bookId).first()?.content?.cover
    if (oldCover != null) {
      withContext(Dispatchers.IO) {
        oldCover.delete()
      }
    }

    repo.updateBook(bookId) {
      it.copy(cover = cover)
    }
  }
}