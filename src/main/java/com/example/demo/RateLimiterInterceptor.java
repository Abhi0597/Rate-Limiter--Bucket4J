package com.example.demo;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import java.time.*;

public class RateLimiterInterceptor implements HandlerInterceptor {

	  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	  private final Bucket Bucket = Bucket4j.builder()
	      .addLimit(Bandwidth.classic(5, Refill.intervally(10, Duration.ofHours(1))))
	      .build();

	  @Override
	  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
	      Object handler) throws Exception {
		 
		 String apiKey = request.getHeader("X-api-key");

		 Bucket requestBucket;
		 if (apiKey != null && !apiKey.isBlank()) {
			 if(this.buckets.containsKey(apiKey)) {
			    requestBucket = this.buckets.get(apiKey);
			    
			 }else {
				 this.buckets.put(apiKey,RegisteredUserBucket());
				 requestBucket = this.buckets.get(apiKey);
				 
			 }
		 }
		 else {
			 requestBucket = this.Bucket;
		 }
		 
		ConsumptionProbe probe = requestBucket.tryConsumeAndReturnRemaining(1);
	    if (probe.isConsumed()) {
	      response.addHeader("X-Rate-Limit-Remaining",
	          Long.toString(probe.getRemainingTokens()));
	      return true;
	    }

	    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
	    response.addHeader("X-Rate-Limit-Retry-After-Milliseconds",
	        Long.toString(TimeUnit.NANOSECONDS.toMillis(probe.getNanosToWaitForRefill())));
	    return false;
	  }
      
	  
	  private static Bucket RegisteredUserBucket() {
		    return Bucket4j.builder()
		        .addLimit(Bandwidth.classic(10, Refill.intervally(100, Duration.ofHours(1))))
		        .build();
	}
	 

	}