package org.boosted.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnforceBoosted {
    private static final List<String> whiteListedMethods = Collections.unmodifiableList(Arrays.asList(
            "org.boosted.BoostedThreadExecutor;executeTask"
    ));

    public static void enforceBoostedThreadExecutor(String injectedMethod) {
        StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        // TODO formalized enforcing mechanism to make sure that methods
        // that have been changed by Boosted cannot be misused
        // the current solution is quite ad hoc
        // and does not account for whether the correct executor is being used
        List<String> callingFrame = stackWalker.walk(frames ->
                frames
                        .dropWhile(frame -> !injectedMethod.equals(frame.getClassName() + ";" + frame.getMethodName()))
                        .skip(1)
                        .map(frame -> frame.getClassName() + ";" + frame.getMethodName()).limit(5).collect(Collectors.toList())
        );
        Optional<String> relevantFrame = callingFrame.stream().filter(method -> whiteListedMethods.contains(method)).findFirst();
        if(relevantFrame.isEmpty()) {
            throw new UnsupportedOperationException("Calling " + injectedMethod + " outside of a org.boosted.BoostedThreadExecutor;executeTask is not allowed. Actual frames: " + callingFrame);
        }
    }
}
