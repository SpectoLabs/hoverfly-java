package io.specto.hoverfly.util;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SslUtils {

    private SslUtils() {}

    public static SSLContext createSslContextFromCertFile(Path pemFile) {
        try (InputStream pemInputStream = new FileInputStream(pemFile.toFile())) {

            KeyStore trustStore = createTrustStore(pemInputStream);
            TrustManager[] trustManagers = createTrustManagers(trustStore);

            return createSslContext(trustManagers);
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Failed to create SSLContext from file " + pemFile.toString(), e);
        }
    }

    private static KeyStore createTrustStore(InputStream pemInputStream) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X509");
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(pemInputStream);

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null);

        String alias = cert.getSubjectX500Principal().getName();
        trustStore.setCertificateEntry(alias, cert);
        return trustStore;
    }

    private static TrustManager[] createTrustManagers(KeyStore trustStore) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    private static SSLContext createSslContext(TrustManager[] trustManagers) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext;
    }
}
