package kubeiaas.dbproxy.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class RetryAspect {

    @Around("@within(org.springframework.stereotype.Controller)")
    public Object doRetry(ProceedingJoinPoint joinPoint) throws Throwable {
        int retryTimes = 0;
        int maxRetryTimes = 3;
        long retryInterval = 500;

        Object result = null;
        while (retryTimes <= maxRetryTimes) {
            try {
                result = joinPoint.proceed();
                break;
            } catch (Exception ex) {
                log.error("Exception caught: ", ex);
                if (retryTimes < maxRetryTimes) {
                    log.error("Exception caught, retrying...", ex);
                } else {
                    log.error("Max retry times reached, aborting...");
                    throw ex;
                }
                Thread.sleep(retryInterval);
            }
            retryTimes++;
        }
        return result;
    }
}