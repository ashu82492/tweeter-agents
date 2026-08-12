package com.think9.app.web;

import com.think9.app.security.JwtService;
import com.think9.identity.domain.User;
import com.think9.identity.repository.UserRepository;
import com.think9.identity.service.FollowService;
import com.think9.identity.service.UserService;
import com.think9.messaging.domain.Chat;
import com.think9.messaging.domain.Message;
import com.think9.messaging.service.MessagingService;
import com.think9.timeline.service.TimelineService;
import com.think9.tweets.domain.Tweet;
import com.think9.tweets.service.TweetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ApiController {
    private final UserService userService; private final UserRepository userRepository; private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; private final FollowService followService; private final TweetService tweetService;
    private final MessagingService messagingService; private final TimelineService timelineService;
    public ApiController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                         FollowService followService, TweetService tweetService, MessagingService messagingService, TimelineService timelineService) {
        this.userService = userService; this.userRepository = userRepository; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
        this.followService = followService; this.tweetService = tweetService; this.messagingService = messagingService; this.timelineService = timelineService;
    }
    @PostMapping("/auth/register") ResponseEntity<UserView> register(@Valid @RequestBody Registration request) {
        User user = userService.create(request.username(), request.password(), request.displayName()); return ResponseEntity.status(HttpStatus.CREATED).body(UserView.of(user)); }
    @PostMapping("/auth/login") TokenView login(@Valid @RequestBody Login request) {
        User user = userRepository.findByUsername(request.username()).filter(found -> passwordEncoder.matches(request.password(), found.passwordHash()))
                .orElseThrow(() -> new IllegalArgumentException("invalid username or password")); return new TokenView(jwtService.issue(user)); }
    @GetMapping("/users/{userId}") UserView getUser(@PathVariable("userId") UUID userId) { return UserView.of(userService.get(userId)); }
    @PostMapping("/users/{userId}/follow") FollowService.FollowResult follow(Authentication auth, @PathVariable("userId") UUID userId, @RequestHeader("Idempotency-Key") String key) { return followService.follow(actor(auth), userId, key); }
    @DeleteMapping("/users/{userId}/follow") FollowService.FollowResult unfollow(Authentication auth, @PathVariable("userId") UUID userId, @RequestHeader("Idempotency-Key") String key) { return followService.unfollow(actor(auth), userId, key); }
    @PostMapping("/tweets") ResponseEntity<Tweet> tweet(Authentication auth, @Valid @RequestBody Content request, @RequestHeader("Idempotency-Key") String key) { return ResponseEntity.status(HttpStatus.CREATED).body(tweetService.tweet(actor(auth), request.content(), key)); }
    @GetMapping("/tweets/{tweetId}") Tweet getTweet(@PathVariable("tweetId") UUID tweetId) { return tweetService.getTweet(tweetId); }
    @GetMapping("/users/{userId}/tweets") List<Tweet> getTweets(@PathVariable("userId") UUID userId, @RequestParam(name = "limit", defaultValue = "20") int limit) { return tweetService.getTweets(userId, limit); }
    @GetMapping("/timeline/feed") List<UUID> feed(Authentication auth, @RequestParam(name = "limit", defaultValue = "20") int limit) { return timelineService.fetchFeed(actor(auth), limit); }
    @PostMapping("/chats") ResponseEntity<Chat> createChat(Authentication auth, @Valid @RequestBody ChatRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(messagingService.createChat(actor(auth), request.participantId())); }
    @PostMapping("/chats/{chatId}/messages") ResponseEntity<Message> message(Authentication auth, @PathVariable("chatId") UUID chatId, @Valid @RequestBody Content request, @RequestHeader("Idempotency-Key") String key) { return ResponseEntity.status(HttpStatus.CREATED).body(messagingService.message(actor(auth), chatId, request.content(), key)); }
    @GetMapping("/chats/{chatId}/messages") List<Message> messages(Authentication auth, @PathVariable("chatId") UUID chatId, @RequestParam(name = "limit", defaultValue = "50") int limit) { return messagingService.read(actor(auth), chatId, limit); }
    private UUID actor(Authentication authentication) { return (UUID) authentication.getPrincipal(); }
    public record Registration(@NotBlank @Size(max = 64) String username, @NotBlank @Size(min = 8, max = 128) String password, @NotBlank @Size(max = 100) String displayName) {}
    public record Login(@NotBlank String username, @NotBlank String password) {}
    public record Content(@NotBlank @Size(max = 4000) String content) {}
    public record ChatRequest(@NotNull UUID participantId) {}
    public record TokenView(String accessToken) {}
    public record UserView(UUID id, String username, String displayName, String type) { static UserView of(User user) { return new UserView(user.id(), user.username(), user.displayName(), user.type().name()); } }
}