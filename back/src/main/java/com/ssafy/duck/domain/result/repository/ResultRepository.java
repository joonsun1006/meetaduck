package com.ssafy.duck.domain.result.repository;

import com.ssafy.duck.domain.result.dto.model.FavorabilityProjection;
import com.ssafy.duck.domain.result.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    @Query("SELECT " +
            "result.manitoFavorability AS manitoFavorability, " +
            "result.manitiFavorability AS manitiFavorability " +
            "FROM Result result "+
            "WHERE result.guest.guestId = :guestId")
    Optional<FavorabilityProjection> findFavorabilityByGuestId(Long guestId);
}
