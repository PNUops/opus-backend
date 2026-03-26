package com.opus.opus.global.security;

import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(final String userId) {
        final Long memberId = Long.parseLong(userId);
        final Member member = memberRepository.findById(memberId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        return new MemberDetails(
                member.getId(),
                member.getName(),
                member.getPassword(),
                member.getRoles().stream()
                        .map(MemberRoleType::name)
                        .toList()
        );
    }
}
