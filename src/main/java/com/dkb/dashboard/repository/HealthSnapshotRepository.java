package com.dkb.dashboard.repository;

import com.dkb.dashboard.model.HealthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/** Persistence for {@link HealthSnapshot} history rows. */
public interface HealthSnapshotRepository extends JpaRepository<HealthSnapshot, Long> {

    /** Snapshots captured after the given instant, oldest first. */
    List<HealthSnapshot> findByCapturedAtAfterOrderByCapturedAtAsc(Instant since);

    /** Delete snapshots older than the cutoff (retention pruning). */
    long deleteByCapturedAtBefore(Instant cutoff);
}
