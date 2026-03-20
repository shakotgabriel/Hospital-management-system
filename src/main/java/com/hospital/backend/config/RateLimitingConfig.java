package com.hospital.backend.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component
public class RateLimitingConfig {

	private static final double PERMITS_PER_SECOND = 100.0 / 60.0;

	private final LoadingCache<String, RateLimiter> limiters;

	public RateLimitingConfig() {
		this.limiters = CacheBuilder.newBuilder()
			.maximumSize(100000)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<String, RateLimiter>() {
				@Override
				public RateLimiter load(@Nonnull String key) {
					Objects.requireNonNull(key, "IP address cannot be null");
					return RateLimiter.create(PERMITS_PER_SECOND);
				}
			});
	}

	public boolean allowRequest(String ip) {
		try {
			RateLimiter limiter = limiters.get(Objects.requireNonNull(ip, "IP address cannot be null"));
			return limiter.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			return true;
		}
	}
}
