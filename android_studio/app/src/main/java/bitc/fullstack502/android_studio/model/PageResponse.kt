package bitc.fullstack502.android_studio.model

// Page<T> 응답을 그대로 매핑할 수 있는 DTO
data class PageResponse<T>(
    val content: List<T>,   // 실제 데이터 리스트
    val totalElements: Long, // 전체 데이터 수
    val totalPages: Int,     // 전체 페이지 수
    val size: Int,           // 페이지당 데이터 수
    val number: Int,         // 현재 페이지 번호 (0부터 시작)
    val first: Boolean,      // 첫 페이지 여부
    val last: Boolean,       // 마지막 페이지 여부
    val empty: Boolean       // 비어있는지 여부
)