package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {


    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;


    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(@RequestBody MemberSearchCondition condition) {
        return memberJpaRepository.searchByDinamicQuery(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDTO> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }
    @GetMapping("/v3/members")
    public Page<MemberTeamDTO> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }



}
