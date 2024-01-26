fun main() {
    val noteId1 = NoteService.add("Заметка", "test")
    NoteService.createComment(noteId1, "Добавляем комментарий в заметке 1")
    NoteService.createComment(noteId1, "Добавляем комментарий 2 к заметке 1")
    val commentId3 = NoteService.createComment(noteId1, "Добавляем комментарий 3 к заметке 1")

    NoteService.edit(noteId1, "Новый заголовок заметки", "Новый текст")

    NoteService.deleteComment(commentId3)

    val noteId2 = NoteService.add("New task", "debug for program")
    NoteService.createComment(noteId2, "Какой-то комментарий к заметке ДВА")


    val noteList = NoteService.get(sort = 1)
    println(noteList)

    println(NoteService.getById(3))
    println(NoteService.getComments(2))

    NoteService.restoreComment(3)
    println(noteList)
}