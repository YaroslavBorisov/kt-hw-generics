import java.lang.RuntimeException

data class Note(
    val id: Int,
    var title: String,
    var text: String,
    var date: Long,
    var comments: Int = 0,
    var readComments: Int = 0,
    var viewUrl: String = "",
    var privacyView: String = "",
    var canComment: Boolean = true,
    var textWiki: String = ""
) : Comparable<Note> {
    override fun compareTo(other: Note): Int {
        return (this.date - other.date).toInt()
    }
}

data class Comment(
    val id: Int,
    val noteId: Int,
    var message: String,
    val date: Long,
    var isDeleted: Boolean = false
) : Comparable<Comment> {
    override fun compareTo(other: Comment): Int {
        return (this.date - other.date).toInt()
    }
}

class NoteNotFoundException(message: String) : RuntimeException(message)

object NoteService {
    private val notes = mutableMapOf<Int, Note>()
    private val comments = mutableMapOf<Int, Comment>()
    private var lastNoteId: Int = 0
    private var lastCommentId: Int = 0

    fun clear() {
        notes.clear()
        comments.clear()

        lastNoteId = 0
        lastCommentId = 0
    }

    fun add(
        title: String,
        text: String,
        privacy: Int = 0,
        commentPrivacy: Int = 0,
        privacyView: String = "",
        privacyComment: String = ""
    ): Int {
        return addNoteOrComment(Note(++lastNoteId, title, text, System.currentTimeMillis()))
    }

    fun createComment(noteId: Int, message: String): Int {
        if (!notes.contains(noteId)) {
            throw NoteNotFoundException("Не найден пост с id= $noteId")
        }
        return addNoteOrComment(Comment(++lastCommentId, noteId, message, System.currentTimeMillis()))
    }

    private fun <T> addNoteOrComment(value: T): Int {
        var last: Int = 0
        if (value is Note) {
            notes.put(lastNoteId, value)
            last = lastNoteId
        } else if (value is Comment) {
            comments.put(lastCommentId, value)
            notes[value.noteId]!!.comments ++
            last = lastCommentId
        }
        return last
    }

    fun delete(noteId: Int): Int {
        if (notes.contains(noteId)) {
            notes.remove(noteId)
            for (comment in comments) {
                if (comment.value.noteId == noteId) comments.remove(comment.key)
            }
            return 1
        }
        return 180
    }

    fun deleteComment(commentId: Int): Int {
        for (comment in comments) {
            if (comment.value.id == commentId && !comment.value.isDeleted) {
                comment.value.isDeleted = true
                notes[comment.value.noteId]!!.comments --
                return 1
            }
        }
        return 180
    }

    fun edit(
        noteId: Int,
        title: String,
        text: String,
        privacy: Int = 0,
        commentPrivacy: Int = 0,
        privacyView: String = "",
        privacyComment: String = ""
    ): Int {
        if (notes.contains(noteId)) {
            val note = notes[noteId]
            note!!.title = title
            note.text = text
            return 1
        }
        return 180
    }

    fun editComment(commentId: Int, message: String): Int {
        if (message.length < 2) return 180
        for (comment in comments) {
            if (comment.value.id == commentId && !comment.value.isDeleted) {
                comment.value.message = message
                return 1
            }
        }
        return 180
    }

    fun get(noteIds: String = "", offset: Int = 0, count: Int = 0, sort: Int = 0): List<Note> {
        var noteList = mutableListOf<Note>()
        val noteIdList = noteIds.split(",")
        for (note in notes) {
            if (noteIds != "") {
                for (filterKey in noteIdList) {
                    try {
                        if (Integer.parseInt(filterKey) == note.key) noteList += note.value
                    } catch (e: RuntimeException) {
                    }
                }

            } else noteList += note.value
        }
        noteList = noteList.subList(offset, offset - 1 + if (count == 0) noteList.size - offset + 1 else count)
        if (sort == 0) noteList.sort() else noteList.sortDescending()
        return noteList
    }

    fun getById(noteId: Int): Note? {
        return notes[noteId]
    }

    fun getComments(noteId: Int, sort: Int = 0, offset: Int = 0, count: Int = 0): List<Comment> {
        var commentList = mutableListOf<Comment>()
        for (comment in comments) {
            if (comment.value.noteId == noteId && !comment.value.isDeleted) {
                commentList += comment.value
            }
        }

        commentList = commentList.subList(offset, offset - 1 + if (count == 0) commentList.size - offset + 1 else count)
        if (sort == 0) commentList.sort() else commentList.sortDescending()
        return commentList
    }


    fun restoreComment(commentId: Int): Int {
        if (comments.contains(commentId) && comments[commentId]!!.isDeleted) {
            comments[commentId]!!.isDeleted = false
            notes[comments[commentId]!!.noteId]!!.comments ++
            return 1
        }
        return 183
    }
}