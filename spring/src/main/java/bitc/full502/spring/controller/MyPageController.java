package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.*;
import bitc.full502.spring.dto.BookingResponseDto;
import bitc.full502.spring.dto.CommDto;
import bitc.full502.spring.dto.FlightWishDto;
import bitc.full502.spring.dto.PostDto;
import bitc.full502.spring.service.CommService;
import bitc.full502.spring.service.FlBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@CrossOrigin(origins = "*")
public class MyPageController {

    private final UsersRepository usersRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommRepository commRepository;
    private final CommService commService;
    private final FlBookService flBookService;
    private final FlWishRepository flWishRepository;

    private Users getUserByPk(Long userPk) {
        return usersRepository.findById(userPk)
                .orElseThrow(() -> new IllegalArgumentException("invalid userPk"));
    }

    /** 1) 내가 쓴 글 */
    @GetMapping("/posts")
    public List<PostDto> myPosts(@RequestParam("userPk") Long userPk) {
        Users user = getUserByPk(userPk);
        return postRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(p -> PostDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .content(p.getContent())
                        .imgUrl(p.getImg() == null ? null : "/" + p.getImg().replace("\\", "/"))
                        .likeCount(postLikeRepository.countByPost(p))
                        .lookCount(p.getLookCount() == null ? 0L : p.getLookCount())
                        .author(p.getUser().getUsersId())
                        .liked(false)
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .toList();
    }

    /** 3) 좋아요 한 게시글 */
    @GetMapping("/liked-posts")
    public List<PostDto> likedPosts(@RequestParam("userPk") Long userPk) {
        Users user = getUserByPk(userPk);
        return postLikeRepository.findByUserOrderByPostCreatedAtDesc(user).stream()
                .map(pl -> {
                    Post p = pl.getPost();
                    return PostDto.builder()
                            .id(p.getId())
                            .title(p.getTitle())
                            .content(p.getContent())
                            .imgUrl(p.getImg() == null ? null : "/" + p.getImg().replace("\\", "/"))
                            .likeCount(postLikeRepository.countByPost(p))
                            .lookCount(p.getLookCount() == null ? 0L : p.getLookCount())
                            .author(p.getUser().getUsersId())
                            .liked(true)
                            .createdAt(p.getCreatedAt())
                            .updatedAt(p.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    /** 2) 내가 쓴 댓글 */
    @GetMapping("/comments")
    public List<CommDto> myComments(@RequestParam("userPk") Long userPk) {
        Users user = getUserByPk(userPk);
        return commService.listMyComments(user.getUsersId()); // ✅ service 호출
    }

    /** ✅ 4) 항공 예매내역 */
    @GetMapping("/flight-bookings")
    public List<BookingResponseDto> myFlightBookings(@RequestParam("userPk") Long userPk) {
        return flBookService.getBookingsByUser(userPk);
    }

    /** ✅ 5) 항공 즐겨찾기 */
    @GetMapping("/flight-wishlist")
    public List<FlightWishDto> myFlightWishlist(@RequestParam("userPk") Long userPk) {
        return flWishRepository.findByUser_Id(userPk).stream()
                .map(w -> new FlightWishDto(
                        w.getId(),
                        w.getFlight().getAirline(),
                        w.getFlight().getFlNo(),
                        w.getFlight().getDep(),
                        w.getFlight().getArr(),
                        null // 썸네일 사용 안 하면 null
                ))
                .toList();
    }

}
