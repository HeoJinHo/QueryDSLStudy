package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDTO;
import study.querydsl.dto.QMemberDTO;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;

import static study.querydsl.entity.QMember.*;

import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void dataSettings() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);


        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {

        String qlString = "select m from Member m where m.username = :username";
        //member1을 찾아라.
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");

    }


    @Test
    public void startQueryDSL() throws Exception {
        // given
        QMember m = new QMember("m");

        Member member = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        // when
        Assertions.assertThat(member.getUsername()).isEqualTo("member1");

        // then


    }

    @Test
    public void startQuertDSL2() throws Exception {
        // when
        Member member = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        // then
        Assertions.assertThat(member.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQuertDSL3() throws Exception {
        // given
        Member member = queryFactory.selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1").and(QMember.member.age.eq(10)))
                .fetchOne();

        // when
        Assertions.assertThat(member.getUsername()).isEqualTo("member1");

        // then
    }


    @Test
    public void startQueryDSL4() throws Exception {
        // given
//        List<Member> fetchAll = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(QMember.member)
//                .fetchOne();
//
//
//        Member fetchFirst = queryFactory
//                .selectFrom(QMember.member)
//                .fetchFirst();


//        QueryResults<Member> results = queryFactory
//                .selectFrom(member)
//                .fetchResults();
//
//        results.getTotal();
//        List<Member> content = results.getResults();

        queryFactory
                .selectFrom(member)
                .fetchCount();


        // when


        // then


    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력(null last)
     *
     * @throws Exception
     */
    @Test
    public void 정렬() throws Exception {
        // given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();


        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(memberNull.getUsername()).isEqualTo(null);

        // when


        // then


    }


    @Test
    public void 페이징() throws Exception {
        // given
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        Assertions.assertThat(result.size()).isEqualTo(2);

        // when


        // then


    }


    @Test
    public void 페이징2() throws Exception {
        // given
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        Assertions.assertThat(queryResults.getTotal()).isEqualTo(4);
        Assertions.assertThat(queryResults.getLimit()).isEqualTo(2);
        Assertions.assertThat(queryResults.getOffset()).isEqualTo(1);
        Assertions.assertThat(queryResults.getResults().size()).isEqualTo(2);

        // when


        // then


    }


    @Test
    public void 집합함수() throws Exception {
        List<Tuple> all = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = all.get(0);

        Assertions.assertThat(tuple.get(member.count())).isEqualTo(4);
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        Assertions.assertThat(tuple.get(member.age.max())).isEqualTo(40);
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(10);


    }


    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     *
     * @throws Exception
     */
    @Test
    public void 그룹바이() throws Exception {

        List<Tuple> result = queryFactory
                .select(QTeam.team.name, member.age.avg())
                .from(member)
                .join(member.team, QTeam.team)
                .groupBy(QTeam.team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);


        Assertions.assertThat(teamA.get(QTeam.team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);


        Assertions.assertThat(teamB.get(QTeam.team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(35);


    }

    /**
     * 팀 A에 소속된 모든 회원을 찾아라
     *
     * @throws Exception
     */
    @Test
    public void 기본Join() throws Exception {


        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, QTeam.team)
                .where(QTeam.team.name.eq("teamA"))
                .fetch();


        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");

    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     *
     * @throws Exception
     */
    @Test
    public void 연관관계_없는테이블_조인() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, QTeam.team)
                .where(member.username.eq(QTeam.team.name))
                .fetch();


        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }


    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     *
     * @throws Exception
     */
    @Test
    public void JoinOnFilter() throws Exception {

        List<Tuple> result = queryFactory
                .select(member, QTeam.team)
                .from(member)
                .leftJoin(member.team, QTeam.team).on(QTeam.team.name.eq("teamA"))
                .fetch();


        for (Tuple tuple : result) {
            System.out.println(tuple);
        }


    }


    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     *
     * @throws Exception
     */
    @Test
    public void join_on_no_relation() throws Exception {

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, QTeam.team)
                .from(member)
                .leftJoin(QTeam.team).on(member.username.eq(QTeam.team.name))
                .fetch();


        for (Tuple tuple : result) {
            System.out.println(tuple);
        }


    }


    @PersistenceUnit
    EntityManagerFactory emf;


    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();


        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();


        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertThat(loaded).as("패치 조인 미적용").isFalse();


    }


    @Test
    public void fetchJoin() throws Exception {
        em.flush();
        em.clear();


        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .join(member.team, QTeam.team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();


        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertThat(loaded).as("패치 조인 적용").isTrue();


    }


    /**
     * 나이가 가장 많은 회원 조회
     *
     * @throws Exception
     */
    @Test
    public void subQuery() throws Exception {

        QMember sMember = new QMember("sMember");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions.select(sMember.age.max())
                                .from(sMember)
                ))
                .fetch();


        Assertions.assertThat(result)
                .extracting("age")
                .containsExactly(40);

    }


    @Test
    public void basicCase() throws Exception {
        List<String> fetch = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }

    }

    @Test
    public void complexCase() throws Exception {

        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30")
                        .otherwise("기타"))
                .from(member)
                .fetch();


        for (String s : result) {

            System.out.println(s);
        }


    }


    @Test
    public void 상수_문자_더하기() throws Exception {

        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {

            System.out.println(tuple);
        }
    }

    @Test
    public void 문자더하기() throws Exception {

        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();


        for (String s : fetch) {
            System.out.println(s);
        }
    }


    @Test
    public void simpleProjection() throws Exception {

        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();


        for (String s : result) {
            System.out.println("s = " + s);
        }

    }


    @Test
    public void simpleTupleProjection() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();


        for (Tuple tuple : result) {
            String userName = tuple.get(member.username);

            Integer userAge = tuple.get(member.age);
            System.out.println("userName = " + userName);
            System.out.println("userAge = " + userAge);
        }

    }


    @Test
    public void findDtoByJPQL() throws Exception {
        List<MemberDTO> memberList = em.createQuery(
                "select new study.querydsl.dto.MemberDTO(m.username, m.age) from Member m", MemberDTO.class).getResultList();


        for (MemberDTO memberDTO : memberList) {
            System.out.println("memberDTO = " + memberDTO);
        }


    }

    @Test
    public void findDtoByQueryDSLSetter() throws Exception {
        List<MemberDTO> result = queryFactory
                .select(Projections.bean(
                        MemberDTO.class,
                        member.username,
                        member.age)
                )
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }


    @Test
    public void findDtoByQueryDSLField() throws Exception {
        List<MemberDTO> result = queryFactory
                .select(Projections.fields(
                        MemberDTO.class,
                        member.username,
                        member.age)
                )
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }


    @Test
    public void findDtoByQueryDSLConstruct() throws Exception {
        List<MemberDTO> result = queryFactory
                .select(Projections.constructor(
                        MemberDTO.class,
                        member.username,
                        member.age)
                )
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }

    }


    @Test
    public void findDtoByQueryUserDTO() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(
                        Projections.fields(
                                UserDto.class,
                                member.username.as("name"),
                                Expressions.as(
                                        JPAExpressions
                                                .select(memberSub.age.max()).from(memberSub), "age")
                        ))
                .from(member)
                .fetch();

        for (UserDto memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }


    @Test
    public void findDtoQueryProjection() throws Exception {
        List<MemberDTO> results = queryFactory
                .select(new QMemberDTO(member.username, member.age))
                .from(member)
                .fetch();


        for (MemberDTO result : results) {
            System.out.println("result = " + result);
        }

    }

    @Test
    public void dynamicQuery_BooleanBuilder() throws Exception {

//        String usernameParam = null;
        String usernameParam = "member1";
//        Integer ageParam = null;
        Integer ageParam = 10;


        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null)
            builder.and(member.username.eq(usernameCond));

        if (ageCond != null)
            builder.and(member.age.eq(ageCond));


        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }


    @Test
    public void dynamicQuery_WhereParam() throws Exception {


//        String usernameParam = null;
        String usernameParam = "member1";
//        Integer ageParam = null;
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }


    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;

    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;

    }

    private BooleanExpression allEq(String usernameCond, Integer ageCon) {
        return usernameEq(usernameCond).and(ageEq(ageCon));
    }


    @Test
    public void bulkUpdate() throws Exception {
        // 벌크 update 를 할경우 영속성 컨텍스트랑 달라져서
        // 다시 조회시 영속성 컨텍스트 기준으로 가져와서 데이터가 DB랑 다르게 조회함
        // bulkUpdate를 할경우 em.flush(), em.clear() 를 필수적으로 해줘야함
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
    }


    @Test
    public void bulkPlusAdd() throws Exception {
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.multiply(1))
                .execute();
    }


    @Test
    public void bulkDelete() throws Exception {

        queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

    }

    @Test
    public void sqlFunction() throws Exception {
        List<String> result = queryFactory
                .select(
                        Expressions.stringTemplate(
                                "function('replace', {0}, {1}, {2})",
                                member.username,
                                "member", "M"
                        )
                )
                .from(member)
                .fetch();


        for (String s : result) {
            System.out.println("s = " + s);
        }


    }

    @Test
    public void sqlFunction2() throws Exception {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(
//                        Expressions.stringTemplate(
//                                "function('lower', {0})", member.username
//                        )
                        member.username.lower()
                ))
                .fetch();


        for (String s : result) {
            System.out.println("s = " + s);
        }

    }





}











