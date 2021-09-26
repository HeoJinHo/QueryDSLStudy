package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.dto.QMemberTeamDTO;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {

        QueryResults<MemberTeamDTO> results = queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        member.team.id.as("teamId"),
                        member.team.name.as("teamName"))
                )
                .from(member)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        userAgeGoe(condition.getAgeGoe()),
                        userAgeLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .join(member.team, team)
                .fetchResults();
        List<MemberTeamDTO> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        member.team.id.as("teamId"),
                        member.team.name.as("teamName"))
                )
                .from(member)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        userAgeGoe(condition.getAgeGoe()),
                        userAgeLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .join(member.team, team)
                .fetch();

        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        userAgeGoe(condition.getAgeGoe()),
                        userAgeLoe(condition.getAgeLoe())
                );


        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
//        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        member.team.id.as("teamId"),
                        member.team.name.as("teamName"))
                )
                .from(member)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        userAgeGoe(condition.getAgeGoe()),
                        userAgeLoe(condition.getAgeLoe())
                )
                .join(member.team, team)
                .fetch();
    }


    private BooleanExpression userNameEq(String userNameCond) {
        return hasText(userNameCond) ? member.username.eq(userNameCond) : null;
    }

    private BooleanExpression teamNameEq(String teamNameCond) {
        return hasText(teamNameCond) ? team.name.eq(teamNameCond) : null;
    }

    private BooleanExpression userAgeGoe(Integer ageGoeCon) {
        return ageGoeCon != null ? member.age.goe(ageGoeCon) : null;
    }

    private BooleanExpression userAgeLoe(Integer ageLoeCon) {
        return ageLoeCon != null ? member.age.loe(ageLoeCon) : null;
    }
}
