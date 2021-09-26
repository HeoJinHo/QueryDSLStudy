package study.querydsl.repository;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.UserSearchDTO;
import study.querydsl.entity.QUsersEntity;
import study.querydsl.entity.UsersEntity;

import javax.persistence.EntityManager;
import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QUsersEntity.usersEntity;

@Repository
public class UserRepository {

    private final EntityManager manager;

    // QueryDSL 의존성 주입
    private JPAQueryFactory queryFactory;

    // 생성자
    public UserRepository(EntityManager manager) {
        this.manager = manager;
        // 기본적으로 QueryDSL 은 jpa를 활용하여 사용하기 때문에
        // EntityManager 를 주입시켜줘야 합니다.
        this.queryFactory = new JPAQueryFactory(manager);
    }


    // 전체 조회
    public List<UsersEntity> findAll() {
        List<UsersEntity> result = queryFactory
                .select(usersEntity)
                .from(usersEntity)
                .fetch();

        return result;
    }


    public List<UsersEntity> findSearch(UserSearchDTO searchDTO) {
        return queryFactory
                .selectFrom(usersEntity)
                .where(
                        // 동적 쿼리 생성
                        userIdEquals(searchDTO.getUserId()),
                        usersEntity.userName.contains(searchDTO.getUserName()),
                        userAgeGoe(searchDTO.getUserAge())
                )
                .fetch();
    }


    // userID가 있을 경우 조건 반환 / null 일경우 null 반환
    private BooleanExpression userIdEquals(Long id) {
        return id != null ? usersEntity.id.eq(id) : null;
    }

    // userAge 입력한 값보다 이상일 경우
    private BooleanExpression userAgeGoe(Integer age) {
        return age != null ? usersEntity.userAge.goe(age) : null;
    }




}
