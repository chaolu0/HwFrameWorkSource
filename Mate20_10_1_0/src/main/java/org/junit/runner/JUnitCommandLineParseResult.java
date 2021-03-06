package org.junit.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.internal.Classes;
import org.junit.runner.FilterFactory;
import org.junit.runners.model.InitializationError;

class JUnitCommandLineParseResult {
    private final List<Class<?>> classes = new ArrayList();
    private final List<String> filterSpecs = new ArrayList();
    private final List<Throwable> parserErrors = new ArrayList();

    JUnitCommandLineParseResult() {
    }

    public List<String> getFilterSpecs() {
        return Collections.unmodifiableList(this.filterSpecs);
    }

    public List<Class<?>> getClasses() {
        return Collections.unmodifiableList(this.classes);
    }

    public static JUnitCommandLineParseResult parse(String[] args) {
        JUnitCommandLineParseResult result = new JUnitCommandLineParseResult();
        result.parseArgs(args);
        return result;
    }

    private void parseArgs(String[] args) {
        parseParameters(parseOptions(args));
    }

    /* access modifiers changed from: package-private */
    public String[] parseOptions(String... args) {
        String filterSpec;
        int i = 0;
        while (true) {
            if (i == args.length) {
                break;
            }
            String arg = args[i];
            if (arg.equals("--")) {
                return copyArray(args, i + 1, args.length);
            }
            if (!arg.startsWith("--")) {
                return copyArray(args, i, args.length);
            }
            if (arg.startsWith("--filter=") || arg.equals("--filter")) {
                if (arg.equals("--filter")) {
                    i++;
                    if (i >= args.length) {
                        List<Throwable> list = this.parserErrors;
                        list.add(new CommandLineParserError(arg + " value not specified"));
                        break;
                    }
                    filterSpec = args[i];
                } else {
                    filterSpec = arg.substring(arg.indexOf(61) + 1);
                }
                this.filterSpecs.add(filterSpec);
            } else {
                List<Throwable> list2 = this.parserErrors;
                list2.add(new CommandLineParserError("JUnit knows nothing about the " + arg + " option"));
            }
            i++;
        }
        return new String[0];
    }

    private String[] copyArray(String[] args, int from, int to) {
        ArrayList<String> result = new ArrayList<>();
        for (int j = from; j != to; j++) {
            result.add(args[j]);
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    /* access modifiers changed from: package-private */
    public void parseParameters(String[] args) {
        for (String arg : args) {
            try {
                this.classes.add(Classes.getClass(arg));
            } catch (ClassNotFoundException e) {
                this.parserErrors.add(new IllegalArgumentException("Could not find class [" + arg + "]", e));
            }
        }
    }

    private Request errorReport(Throwable cause) {
        return Request.errorReport(JUnitCommandLineParseResult.class, cause);
    }

    public Request createRequest(Computer computer) {
        if (!this.parserErrors.isEmpty()) {
            return errorReport(new InitializationError(this.parserErrors));
        }
        List<Class<?>> list = this.classes;
        return applyFilterSpecs(Request.classes(computer, (Class[]) list.toArray(new Class[list.size()])));
    }

    private Request applyFilterSpecs(Request request) {
        try {
            for (String filterSpec : this.filterSpecs) {
                request = request.filterWith(FilterFactories.createFilterFromFilterSpec(request, filterSpec));
            }
            return request;
        } catch (FilterFactory.FilterNotCreatedException e) {
            return errorReport(e);
        }
    }

    public static class CommandLineParserError extends Exception {
        private static final long serialVersionUID = 1;

        public CommandLineParserError(String message) {
            super(message);
        }
    }
}
