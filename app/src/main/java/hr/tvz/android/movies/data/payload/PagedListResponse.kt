package hr.tvz.android.movies.data.payload

data class PagedListResponse<T>(
    var content: List<T>,
    var pageable: Pageable,
    var last: Boolean,
    var totalElements: Int,
    var totalPages: Int,
    var first: Boolean,
    var numberOfElements: Int,
    var size: Int,
    var number: Int,
    var sort: Sort,
    var empty: Boolean
) {
    data class Pageable(
        var pageNumber: Int,
        var pageSize: Int,
        var sort: Sort,
        var offset: Int,
        var paged: Boolean,
        var unpaged: Boolean
    )

    data class Sort(
        var empty: Boolean,
        var sorted: Boolean,
        var unsorted: Boolean
    )
}
