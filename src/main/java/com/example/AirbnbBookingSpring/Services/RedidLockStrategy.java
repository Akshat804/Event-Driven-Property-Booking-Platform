package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.Writes.AvailabilityWriteRepository;
import com.example.AirbnbBookingSpring.models.Availability;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedidLockStrategy implements ConcurrencyControlStrategy {

    private static final String LOCK_KEY_PREFIX = "lock:availability:";
    private static final String LOCK_OWNER_KEY_PREFIX = "lock-owner:availability:";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final AvailabilityWriteRepository availabilityWriteRepository;

    // ---------------- ACQUIRE SCRIPT ----------------
    private static final String ACQUIRE_LOCK_SCRIPT = """
        -- Check if any date is already locked
        for i = 1, #KEYS do
            if redis.call('EXISTS', KEYS[i]) == 1 then
                return 0
            end
        end

        -- Lock all dates atomically
        for i = 1, #KEYS do
            redis.call(
                'SET',
                KEYS[i],
                ARGV[1],
                'EX',
                tonumber(ARGV[2])
            )
        end

        return 1
        """;

    // ---------------- RELEASE SCRIPT ----------------
    private static final String RELEASE_LOCK_SCRIPT = """
        -- Delete only if this caller owns the lock
        for i = 1, #KEYS do
            if redis.call('GET', KEYS[i]) == ARGV[1] then
                redis.call('DEL', KEYS[i])
            end
        end

        return 1
        """;

    @Override
    public void releaselock(
            long airbnbId,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {

        String lockOwner = getLockOwner(airbnbId, checkInDate, checkOutDate);
        if (lockOwner == null) {
            return;
        }

        List<String> keys =
                generateLockKeys(
                        airbnbId,
                        checkInDate,
                        checkOutDate
                );

        releaseLock(keys, lockOwner);
        deleteLockOwner(airbnbId, checkInDate, checkOutDate);
    }

    @Override
    public List<Availability> lockAndCheckAvailability(
            long airbnbId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Long userId
    ) {

        String lockOwner = UUID.randomUUID().toString();

        List<String> keys =
                generateLockKeys(
                        airbnbId,
                        checkInDate,
                        checkOutDate
                );

        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>();

        script.setScriptText(ACQUIRE_LOCK_SCRIPT);
        script.setResultType(Long.class);

        Long locked =
                redisTemplate.execute(
                        script,
                        keys,
                        lockOwner,
                        String.valueOf(
                                LOCK_TIMEOUT.toSeconds()
                        )
                );

        if (locked == null || locked == 0L) {
            throw new RuntimeException(
                    "Some dates are already locked."
            );
        }

        saveLockOwner(airbnbId, checkInDate, checkOutDate, lockOwner);

        try {
            long bookedSlots =
                    availabilityWriteRepository
                            .countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(
                                    airbnbId,
                                    checkInDate,
                                    checkOutDate
                            );

            if (bookedSlots > 0) {
                throw new RuntimeException(
                        "Airbnb is not available for all dates."
                );
            }

            return availabilityWriteRepository
                    .findByAirbnbIdAndDateBetween(
                            airbnbId,
                            checkInDate,
                            checkOutDate
                    );

        } catch (Exception e) {

            releaseLock(keys, lockOwner);
            deleteLockOwner(airbnbId, checkInDate, checkOutDate);

            throw e;
        }
    }

    private List<String> generateLockKeys(
            long airbnbId,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {

        List<String> keys = new ArrayList<>();

        LocalDate current = checkInDate;

        while (!current.isAfter(checkOutDate)) {

            keys.add(
                    LOCK_KEY_PREFIX
                            + airbnbId
                            + ":"
                            + current
            );

            current = current.plusDays(1);
        }

        return keys;
    }

    // ---------------- DEMO METHODS ----------------
    // Replace with DB/Saga/Redis storage in production

    private void releaseLock(
            List<String> keys,
            String lockOwner
    ) {
        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>();

        script.setScriptText(RELEASE_LOCK_SCRIPT);
        script.setResultType(Long.class);

        redisTemplate.execute(
                script,
                keys,
                lockOwner
        );
    }

    private void saveLockOwner(
            long airbnbId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String owner
    ) {
        redisTemplate.opsForValue()
                .set(
                        generateLockOwnerKey(airbnbId, checkInDate, checkOutDate),
                        owner,
                        LOCK_TIMEOUT
                );
    }

    private String getLockOwner(
            long airbnbId,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {
        return redisTemplate.opsForValue()
                .get(generateLockOwnerKey(airbnbId, checkInDate, checkOutDate));
    }

    private void deleteLockOwner(
            long airbnbId,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {
        redisTemplate.delete(generateLockOwnerKey(airbnbId, checkInDate, checkOutDate));
    }

    private String generateLockOwnerKey(
            long airbnbId,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {
        return LOCK_OWNER_KEY_PREFIX
                + airbnbId
                + ":"
                + checkInDate
                + ":"
                + checkOutDate;
    }
}
