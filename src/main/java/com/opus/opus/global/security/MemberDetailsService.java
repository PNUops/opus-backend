package com.opus.opus.global.security;

import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public UserDetails loadUserByUsername(final String userId) {
        final Long memberId = Long.parseLong(userId);
        final Member member = memberRepository.findById(memberId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        final List<String> roles = new ArrayList<>(member.getRoles().stream()
                .map(MemberRoleType::name)
                .toList());

        teamMemberRepository.findAllByMemberId(memberId).stream()
                .flatMap(tm -> tm.getRoles().stream())
                .map(TeamMemberRoleType::name)
                .distinct()
                .forEach(roles::add);

        return new MemberDetails(
                member.getId(),
                member.getName(),
                member.getPassword(),
                roles
        );
    }
}
