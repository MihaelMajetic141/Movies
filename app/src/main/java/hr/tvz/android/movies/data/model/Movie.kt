package hr.tvz.android.movies.data.model

data class Movie(
    var id: Long,
    var movieUrl: String,
    var title: String,
    var posterUrl: String,
    var releaseYear: Int,
    var lengthMin: Float,
    var imdbRating: Float,
    var ratingCount: Float,
    var plot: String,
    var directors: String,
    var writers: String,
    var stars: String,
    var genres: String
)