package io.specto.hoverfly.junit.core;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static io.specto.hoverfly.junit.core.HoverflyUtils.findResourceOnClasspath;


class SslConfigurer {

    private static final String HOVERFLY_TRUST_STORE = "hoverfly.jks";
    private static final String HOVERFLY_TRUST_STORE_PASS = "hoverfly";
    private static final String TLS_PROTOCOL = "TLSv1.2";

    /**
     * Sets the JVM trust store so Hoverfly's SSL certificate is trusted
     */
    void setTrustStore() {
        try {
            KeyStore hoverflyTrustStore = createHoverflyTrustStore();
            TrustManager[] trustManagers = createTrustManagers(hoverflyTrustStore);
            // initialize an ssl context to use these managers and set as default
            SSLContext sslContext = createSslContext(trustManagers);
            SSLContext.setDefault(sslContext);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to set Hoverfly trust store", e);
        }
    }

    private KeyStore createHoverflyTrustStore() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        InputStream trustStream = findResourceOnClasspath(HOVERFLY_TRUST_STORE).openStream();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(trustStream, HOVERFLY_TRUST_STORE_PASS.toCharArray());
        return trustStore;
    }

    private TrustManager[] createTrustManagers(KeyStore hoverflyTrustStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // initialize a trust manager factory with default trust store
        X509TrustManager defaultTm = getTrustManager(tmf, null);

        // initialize a trust manager factory with hoverfly trust store
        X509TrustManager hoverflyTm = getTrustManager(tmf, hoverflyTrustStore);

        X509TrustManager customTm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                defaultTm.checkClientTrusted(x509Certificates, s);
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                try {
                    hoverflyTm.checkServerTrusted(x509Certificates, s);
                } catch (CertificateException e) {
                    defaultTm.checkServerTrusted(x509Certificates, s);
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return defaultTm.getAcceptedIssuers();
            }
        };
        return new TrustManager[] { customTm };
    }

    private SSLContext createSslContext(TrustManager[] trustManagers) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance(TLS_PROTOCOL);
        sslContext.init(null, trustManagers, null);
        return sslContext;
    }

    private X509TrustManager getTrustManager(TrustManagerFactory trustManagerFactory, KeyStore trustStore) throws KeyStoreException {
        trustManagerFactory.init(trustStore);

        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        return Arrays.stream(trustManagers)
                    .filter(tm -> tm instanceof X509TrustManager)
                    .map(tm -> (X509TrustManager) tm)
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
    }
}
