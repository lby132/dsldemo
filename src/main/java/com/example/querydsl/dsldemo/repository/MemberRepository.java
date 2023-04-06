package com.example.querydsl.dsldemo.repository;

import com.example.querydsl.dsldemo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor {

    List<Member> findByUsername(String username);
}
