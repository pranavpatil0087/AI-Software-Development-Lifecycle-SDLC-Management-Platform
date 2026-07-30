package com.sdlcplatform.repository;

import com.sdlcplatform.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    List<UserSkill> findByUserId(UUID userId);

    @Modifying
    @Query("delete from UserSkill s where s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}