package eu.kanade.presentation.manga

enum class DownloadAction {
    NEXT_1_CHAPTER,
    NEXT_5_CHAPTERS,
    NEXT_10_CHAPTERS,
    NEXT_25_CHAPTERS,
    UNREAD_CHAPTERS,
    BOOKMARKED_CHAPTERS,
}

enum class EditCoverAction {
    EDIT,
    DELETE,
}

enum class MangaScreenItem {
    INFO_BOX,
    ACTION_ROW,

     -->
    METADATA_INFO,

     <--
    DESCRIPTION_WITH_TAG,

     -->
    INFO_BUTTONS,
    CHAPTER_PREVIEW_LOADING,
    CHAPTER_PREVIEW_ROW,
    CHAPTER_PREVIEW_MORE,

     <--
    CHAPTER_HEADER,
    CHAPTER,

     -->
    RELATED_MANGAS,
     <--
}
