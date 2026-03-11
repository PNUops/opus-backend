package com.opus.opus.modules.member.api;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.MemberQueryService;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.PasswordUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Validated
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody final SignUpRequest signUpRequest) {
        memberCommandService.signUp(signUpRequest);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/sign-up/email-auth")
    public ResponseEntity<Void> signUpEmailAuth(@Valid @RequestBody final EmailAuthRequest emailAuthRequest) {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        return ResponseEntity.status(CREATED).build();
    }

    @PatchMapping("/sign-up/email-auth")
    public ResponseEntity<Void> confirmSignUpEmailAuth(
            @Valid @RequestBody final EmailAuthConfirmRequest emailAuthConfirmRequest) {
        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody final SignInRequest signInRequest) {
        final SignInResponse response = memberCommandService.signIn(signInRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-in/password-reset/email-auth")
    public ResponseEntity<Void> signInEmailAuth(@Valid @RequestBody final EmailAuthRequest emailAuthRequest) {
        memberCommandService.signInEmailAuth(emailAuthRequest);
        return ResponseEntity.status(CREATED).build();
    }

    @PatchMapping("/sign-in/password-reset/email-auth")
    public ResponseEntity<Void> confirmSignInEmailAuth(
            @Valid @RequestBody final EmailAuthConfirmRequest emailAuthConfirmRequest) {
        memberCommandService.confirmSignInEmailAuth(emailAuthConfirmRequest);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PatchMapping("/sign-in/password-reset")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody final PasswordUpdateRequest passwordUpdateRequest) {
        memberCommandService.updatePassword(passwordUpdateRequest);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping("/sign-in/{studentId}/email-find")
    public ResponseEntity<EmailFindResponse> getMyEmail(@PathVariable final String studentId) {
        final EmailFindResponse response = memberQueryService.getMyEmail(studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth/google")
    public ResponseEntity<Void> googleOAuthRedirect() {
        final String redirectURL = memberCommandService.getGoogleOAuthRedirectURL();

        URI uri = UriComponentsBuilder.fromUriString(redirectURL).encode().build().toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }

    @GetMapping("/oauth/google/callback")
    public ResponseEntity<SignInResponse> googleOAuthCallback(
            final String code,
            final String state,
            final String error
    ) {
        final SignInResponse response = memberCommandService.getGoogleOAuthCallback(code, state, error);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members/me/images/profile")
    public ResponseEntity<Resource> getProfileImage(@LoginMember final Member member) {
        final ImageResponse imageResponse = memberQueryService.getProfileImage(member);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @PatchMapping("/members/me/images/profile")
    public ResponseEntity<Void> modifyProfileImage(@LoginMember final Member member,
                                                   @RequestPart("image") final MultipartFile image) {
        memberCommandService.modifyProfileImage(member, image);

        return ResponseEntity.noContent().build();
    }
}
