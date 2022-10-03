package org.boosted.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnforceBoosted {
    private static final List<String> whiteListedMethods = List.of("org.boosted.BoostedThreadExecutor;executeTask");

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
        Optional<String> relevantFrame = callingFrame.stream().filter(whiteListedMethods::contains).findFirst();
        if(relevantFrame.isEmpty()) {
            throw new UnsupportedOperationException("Calling " + injectedMethod + " outside of a org.boosted.BoostedThreadExecutor;executeTask is not allowed. Actual frames: " + callingFrame);
        }
    }
}
