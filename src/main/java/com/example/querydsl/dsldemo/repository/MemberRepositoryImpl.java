package com.example.querydsl.dsldemo.repository;

import com.example.querydsl.dsldemo.dto.MemberSearchCondition;
import com.example.querydsl.dsldemo.dto.MemberTeamDto;
import com.example.querydsl.dsldemo.dto.QMemberTeamDto;
import com.example.querydsl.dsldemo.entity.Member;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.querydsl.dsldemo.entity.QMember.member;
import static com.example.querydsl.dsldemo.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    // QuerydslRepositorySupport
    // querydsl 3.x버전을 대상으로 만들어서 select가 아닌 from부터 시작해야함 현재 querydsl 4.x이상에 나온 JPAQueryFactory로 시작할수 없음
    // QueryFactory를 제공하지 않음 sort가 잘 되지 않음
//    private final JPAQueryFactory queryFactory;
    // extends QuerydslRepositorySupport를 해주고

    //QuerydslRepositorySupport를 사용하려면 이런식으로 써야함
    //    public MemberRepositoryImpl(EntityManager em) {
//        super(Member.class);
//        this.queryFactory = new JPAQueryFactory(em);
//    }
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
//
//        List<MemberTeamDto> result = from(member)
//                .leftJoin(member.team, team)
//                .where(
//                        usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe())
//                )
//                .select(new QMemberTeamDto(
//                        member.id.as("memberId"),
//                        member.username,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")))
//                .fetch();
//        return result;
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();

    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();

        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);

    }

    // QuerydslRepositorySupport 테스트용 코드
//    public Page<MemberTeamDto> searchPageSimple2(MemberSearchCondition condition, Pageable pageable) {
//        JPQLQuery<MemberTeamDto> jpaQuery = from(member)
//                .leftJoin(member.team, team)
//                .where(
//                        usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe())
//                )
//                .select(new QMemberTeamDto(
//                        member.id.as("memberId"),
//                        member.username,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")));
//
//        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpaQuery); applyPagination내부에 offset, limit이 있음
//        return null;
//    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        // 마지막 페이지일때 카운트쿼리가 나가지 않게 하기
        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
//        return new PageImpl<>(content, pageable, total);

    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }
}
