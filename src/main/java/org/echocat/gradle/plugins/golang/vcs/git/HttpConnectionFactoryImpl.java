package org.echocat.gradle.plugins.golang.vcs.git;

import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.JDKHttpConnectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import static java.io.File.separator;
import static java.lang.System.getProperty;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HttpConnectionFactoryImpl extends JDKHttpConnectionFactory {


    @Override
    public HttpConnection create(URL url) throws IOException {
        return configure(super.create(url));
    }

    @Override
    public HttpConnection create(URL url, Proxy proxy) throws IOException {
        return configure(super.create(url, proxy));
    }

    @Nonnull
    protected HttpConnection configure(@Nonnull HttpConnection input) throws IOException {
        try {
            final KeyStore keyStore = loadKeyStore();

            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            final TrustManager[] defaultTrustManagers = trustManagerFactory.getTrustManagers();

            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, null);
            final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            input.configure(keyManagers, defaultTrustManagers, null);

            input.setInstanceFollowRedirects(true);

            return input;
        } catch (final GeneralSecurityException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Nonnull
    protected KeyStore loadKeyStore() throws GeneralSecurityException, IOException {
        final KeyStore result = loadDefaultKeyStore();

        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        try (final InputStream is = getClass().getResourceAsStream("/org/echocat/gradle/plugins/golang/ca.crt")) {
            final Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(is);
            int i = 1;
            for (final Certificate certificate : certificates) {
                result.setCertificateEntry("cert" + i++, certificate);
            }
        }

        return result;
    }

    @Nonnull
    protected KeyStore loadDefaultKeyStore() throws GeneralSecurityException, IOException {
        final InputStream is = tryOpenDefaultKeyStoreInputStream();
        try {
            return readDefaultKeyStoreFrom(is);
        } finally {
            closeQuietly(is);
        }
    }

    @Nullable
    protected InputStream tryOpenDefaultKeyStoreInputStream() throws IOException {
        final String trustStorePath = getProperty("javax.net.ssl.trustStore");
        if (isNotEmpty(trustStorePath)) {
            return new FileInputStream(trustStorePath);
        }

        final String javaHome = getProperty("java.home");
        final File jssecacertsFile = new File(javaHome + separator + "lib" + separator + "security" + separator + "jssecacerts");
        if (jssecacertsFile.isFile()) {
            return new FileInputStream(jssecacertsFile);
        }
        final File cacertsFile = new File(javaHome + separator + "lib" + separator + "security" + separator + "cacerts");
        if (cacertsFile.isFile()) {
            return new FileInputStream(cacertsFile);
        }
        return null;
    }

    @Nonnull
    protected KeyStore readDefaultKeyStoreFrom(@Nullable InputStream is) throws GeneralSecurityException, IOException {
        final String type = getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
        final String provider = getProperty("javax.net.ssl.trustStoreProvider");

        final KeyStore result;
        if (isEmpty(provider)) {
            result = KeyStore.getInstance(type);
        } else {
            result = KeyStore.getInstance(type, provider);
        }

        final String password = getProperty("javax.net.ssl.trustStorePassword");
        result.load(is, isNotEmpty(password) ? password.toCharArray() : null);

        return result;
    }
}
