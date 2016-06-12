package org.echocat.gradle.plugins.golang.utils;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.Runtime.getRuntime;
import static org.apache.commons.lang3.StringUtils.split;
import static org.slf4j.LoggerFactory.getLogger;

public class Executor {

    private static final Logger LOGGER = getLogger(Executor.class);
    private static final ExecutionFailedExceptionProducer<IOException> DEFAULT_EXCEPTION_PRODUCER = new ExecutionFailedExceptionProducer<IOException>() {
        @Nonnull
        @Override
        public IOException produceFor(@Nonnull Executor executor, @Nonnull String[] commandLine, int errorCode) {
            return new IOException("Got unexpected exit code " + errorCode + " while executing " + Arrays.toString(commandLine) + ".");
        }
    };

    private final Map<String, String> _environment = new HashMap<>(System.getenv());
    private final List<String> _arguments = new ArrayList<>();
    private final ByteArrayOutputStream _stdout = new ByteArrayOutputStream();
    private final ByteArrayOutputStream _stderr = new ByteArrayOutputStream();

    private File _workingDirectory;
    private File _executable;
    private final Set<String> _failKeywords = new HashSet<>();

    public static Executor executor() {
        return new Executor();
    }

    public Executor removeEnv(String name) {
        _environment.remove(name);
        return this;
    }

    public Executor env(String name, Object value) {
        if (value == null) {
            _environment.remove(name);
        } else {
            _environment.put(name, value.toString());
        }
        return this;
    }

    public Executor executable(File executable) {
        _executable = executable;
        return this;
    }

    public Executor workingDirectory(File workingDirectory) {
        _workingDirectory = workingDirectory;
        return this;
    }

    public Executor argument(String argument) {
        _arguments.add(argument);
        return this;
    }

    public Executor arguments(String... arguments) {
        Collections.addAll(_arguments, arguments);
        return this;
    }

    public Executor failKeyword(String keyword) {
        _failKeywords.add(keyword);
        return this;
    }

    public Executor failKeywords(String... keywords) {
        Collections.addAll(_failKeywords, keywords);
        return this;
    }

    public Executor execute() throws IOException {
        return execute(DEFAULT_EXCEPTION_PRODUCER);
    }

    public <T extends Throwable> Executor execute(ExecutionFailedExceptionProducer<T> executionFailedExceptionProducer) throws T, IOException {
        final String[] commandLine = commandLine();
        final Process process = getRuntime().exec(commandLine, envp(), _workingDirectory);
        try (final ProcessOutput po = new ProcessOutput(process, _stdout, _stdout)) {
            try {
                final int exitCode = process.waitFor();
                final String[] lines = split(getStdoutAsString(), '\n');
                if (exitCode != 0) {
                    toLog(lines, true);
                    throw executionFailedExceptionProducer.produceFor(this, commandLine, exitCode);
                }
                if (containsProblem(lines)) {
                    toLog(lines, true);
                    throw new IOException("Problem while executing " + Arrays.toString(commandLine) + ".");
                }
                toLog(lines, false);
            } catch (final InterruptedException e) {
                throw new IOException("Got interrupted while executing " + Arrays.toString(commandLine) + ".", e);
            }
        }
        return this;
    }

    protected boolean containsProblem(String[] lines) {
        for (final String line : lines) {
            for (final String failKeyword : _failKeywords) {
                if (line.contains(failKeyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void toLog(String[] lines, boolean asProblem) {
        if (asProblem || LOGGER.isDebugEnabled()) {
            for (final String line : lines) {
                if (asProblem) {
                    LOGGER.error(line);
                } else {
                    LOGGER.debug(line);
                }
            }
        }
    }

    protected String[] commandLine() throws IOException {
        final File executable = _executable;
        if (executable == null) {
            throw new IOException("There was no executable provided.");
        }
        final String[] result = new String[_arguments.size() + 1];
        int i = 0;
        result[i++] = _executable.getCanonicalPath();
        for (final String argument : _arguments) {
            result[i++] = argument;
        }
        return result;
    }

    protected String[] envp() {
        final String[] result = new String[_environment.size()];
        int i = 0;
        for (final Entry<String, String> entry : _environment.entrySet()) {
            result[i++] = entry.getKey() + "=" + entry.getValue();
        }
        return result;
    }

    public String getStdoutAsString() {
        return _stdout.toString();
    }

    public byte[] getStdoutAsBytes() {
        return _stdout.toByteArray();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_executable);
        for (final String argument : _arguments) {
            sb.append(' ').append(argument);
        }
        return sb.toString();
    }

    public interface ExecutionFailedExceptionProducer<T extends Throwable> {

        @Nonnull
        public T produceFor(@Nonnull Executor executor, @Nonnull String[] commandLine, int errorCode);

    }
}
