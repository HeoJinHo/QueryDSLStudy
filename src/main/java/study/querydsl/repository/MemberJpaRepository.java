package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.dto.QMemberDTO;
import study.querydsl.dto.QMemberTeamDTO;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;

    private JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    // JPQL 사용시
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    // QueryDSL 사용시
    public List<Member> findAll_Querydsl() {
        return queryFactory.selectFrom(member).fetch();
    }

    // JPQL 사용시
    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    // QueryDSL 사용시
    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }





    // 실전 활용 jparepository
    public List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUserName())) {
            builder.and(member.username.eq(condition.getUserName()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(member.team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }



        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        member.team.id.as("teamId"),
                        member.team.name.as("teamName")
                ))
                .from(member)
                .where(builder)
                .join(member.team, team)
                .fetch();
    }

    // 실전 활용 QueryDSL dinamicQuery
    public List<MemberTeamDTO> searchByDinamicQuery(MemberSearchCondition condition) {
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
//                        searchAllCon(condition)
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


    private BooleanExpression searchAllCon(MemberSearchCondition condition) {
//    private BooleanExpression searchAllCon(String userNameCond, String teamNameCond, Integer ageGoeCon, Integer ageLoeCon) {
//        return userNameEq(userNameCond)
//                .and(teamNameEq(teamNameCond))
//                .and(userAgeGoe(ageGoeCon))
//                .and(userAgeLoe(ageLoeCon));
//

        return userNameEq(condition.getUserName())
                .and(teamNameEq(condition.getTeamName()))
                .and(userAgeGoe(condition.getAgeGoe()))
                .and(userAgeLoe(condition.getAgeLoe()));
    }


}
