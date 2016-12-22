package org.echocat.gradle.plugins.golang.utils;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.currentThread;
import static org.apache.commons.io.IOUtils.closeQuietly;
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
    private final List<Object> _arguments = new ArrayList<>();
    private final OutputStream _stdout;
    private final OutputStream _stderr;
    private final Path _executable;

    private Path _workingDirectory;
    private final Set<String> _failKeywords = new HashSet<>();

    @Nonnull
    public static Executor executor(@Nonnull String executable) {
        return executor(Paths.get(executable).toAbsolutePath());
    }

    @Nonnull
    public static Executor executor(@Nonnull Path executable) {
        return executor(executable, null, null);
    }

    @Nonnull
    public static Executor executor(@Nonnull String executable, @Nonnull Logger logger) {
        return executor(Paths.get(executable).toAbsolutePath(), logger);
    }

    @Nonnull
    public static Executor executor(@Nonnull Path executable, @Nonnull Logger logger) {
        return executor(executable, new LoggingOutputStream(logger, false), new LoggingOutputStream(logger, true));
    }

    @Nonnull
    public static Executor executor(@Nonnull Path executable, @Nullable OutputStream stdout, @Nullable OutputStream stderr) {
        return new Executor(executable, stdout, stderr);
    }

    @Nonnull
    public static Executor executor(@Nonnull Path executable, @Nullable StdStreams streams) {
        return executor(executable, streams != null ? streams.out() : null, streams != null ? streams.err() : null);
    }

    public Executor(@Nonnull Path executable, @Nullable OutputStream stdout, @Nullable OutputStream stderr) {
        _stdout = stdout != null ? stdout : new ByteArrayOutputStream();
        _stderr = stderr != null ? stderr : _stdout;
        _executable = executable;
        removeEnv("GOROOT");
        removeEnv("GOROOT_BOOTSTRAP");
        removeEnv("GOPATH");
        removeEnv("GOOS");
        removeEnv("GOARCH");
    }

    @Nonnull
    public Executor removeEnv(String name) {
        _environment.remove(name);
        return this;
    }

    @Nonnull
    public Executor env(String name, Object value) {
        if (value == null) {
            _environment.remove(name);
        } else {
            _environment.put(name, value.toString());
        }
        return this;
    }

    @Nonnull
    public Executor workingDirectory(Path workingDirectory) {
        _workingDirectory = workingDirectory;
        return this;
    }

    @Nonnull
    public Executor argument(Object argument) {
        _arguments.add(argument);
        return this;
    }

    @Nonnull
    public Executor arguments(Object... arguments) {
        if (arguments != null) {
            Collections.addAll(_arguments, arguments);
        }
        return this;
    }

    @Nonnull
    public Executor arguments(Collection<?> arguments) {
        if (arguments != null) {
            _arguments.addAll(arguments);
        }
        return this;
    }

    @Nonnull
    public Executor failKeyword(String keyword) {
        _failKeywords.add(keyword);
        return this;
    }

    @Nonnull
    public Executor failKeywords(String... keywords) {
        Collections.addAll(_failKeywords, keywords);
        return this;
    }

    @Nonnull
    public Executor execute() throws IOException {
        return execute(DEFAULT_EXCEPTION_PRODUCER);
    }

    @Nonnull
    public <T extends Throwable> Executor execute(ExecutionFailedExceptionProducer<T> executionFailedExceptionProducer) throws T, IOException {
        final String[] commandLine = commandLine();
        final File workingDirectory = _workingDirectory != null ? _workingDirectory.toFile() : null;
        final Process process = getRuntime().exec(commandLine, envp(), workingDirectory);
        try (final ProcessOutput po = new ProcessOutput(process, _stdout, _stderr)) {
            try {
                try {
                    po.waitFor();
                } catch (final InterruptedException ignored) {
                    currentThread().interrupt();
                }
                final int exitCode = process.waitFor();
                final String[] lines = _stdout instanceof ByteArrayOutputStream ? split(getStdoutAsString(), '\n') : null;
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
        } finally {
            if (_stdout instanceof LoggingOutputStream) {
                closeQuietly(_stdout);
            }
            if (_stderr instanceof LoggingOutputStream) {
                closeQuietly(_stderr);
            }
        }
        return this;
    }

    protected boolean containsProblem(@Nullable String[] lines) {
        if (lines == null) {
            return false;
        }
        for (final String line : lines) {
            for (final String failKeyword : _failKeywords) {
                if (line.contains(failKeyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void toLog(@Nullable String[] lines, boolean asProblem) {
        if (asProblem || LOGGER.isDebugEnabled()) {
            if (lines != null) {
                for (final String line : lines) {
                    if (asProblem) {
                        LOGGER.error(line);
                    } else {
                        LOGGER.debug(line);
                    }
                }
            }
        }
    }

    @Nonnull
    protected String[] commandLine() throws IOException {
        final String[] result = new String[_arguments.size() + 1];
        int i = 0;
        result[i++] = _executable.toAbsolutePath().toString();
        for (final Object argument : _arguments) {
            result[i++] = argument != null ? argument.toString() : "";
        }
        return result;
    }

    @Nonnull
    protected String[] envp() {
        final String[] result = new String[_environment.size()];
        int i = 0;
        for (final Entry<String, String> entry : _environment.entrySet()) {
            result[i++] = entry.getKey() + "=" + entry.getValue();
        }
        return result;
    }

    @Nonnull
    public String getStdoutAsString() {
        if (!(_stdout instanceof ByteArrayOutputStream)) {
            throw new IllegalStateException("Not an ByteArrayOutputStream.");
        }
        return _stdout.toString();
    }

    @Nonnull
    public byte[] getStdoutAsBytes() {
        if (!(_stdout instanceof ByteArrayOutputStream)) {
            throw new IllegalStateException("Not an ByteArrayOutputStream.");
        }
        return ((ByteArrayOutputStream) _stdout).toByteArray();
    }

    @Nonnull
    public String getStderrAsString() {
        if (!(_stderr instanceof ByteArrayOutputStream)) {
            throw new IllegalStateException("Not an ByteArrayOutputStream.");
        }
        return _stderr.toString();
    }

    @Nonnull
    public byte[] getStderrAsBytes() {
        if (!(_stderr instanceof ByteArrayOutputStream)) {
            throw new IllegalStateException("Not an ByteArrayOutputStream.");
        }
        return ((ByteArrayOutputStream) _stderr).toByteArray();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_executable);
        for (final Object argument : _arguments) {
            sb.append(' ').append(argument != null ? argument : "");
        }
        return sb.toString();
    }

    public interface ExecutionFailedExceptionProducer<T extends Throwable> {

        @Nonnull
        public T produceFor(@Nonnull Executor executor, @Nonnull String[] commandLine, int errorCode);

    }
}
