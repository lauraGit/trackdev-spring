package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Invite;

import java.util.List;

@Component
public interface InviteRepository extends JpaRepository<Invite, Long> {
    List<Invite> findByEmail(@Param("email") String email);
}
