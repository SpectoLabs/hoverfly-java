package io.specto.hoverfly.junit.core;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static io.specto.hoverfly.junit.core.HoverflyUtils.findResourceOnClasspath;


public class SslConfigurer {

    public static final String HOVERFLY_TRUST_STORE = "hoverfly.jks";
    public static final String HOVERFLY_TRUST_STORE_PASS = "hoverfly";

    /**
     * Sets the JVM trust store so Hoverflies SSL certificate is trusted
     */
    void setTrustStore() {
        try {
            // initialize a trust manager factory with default trust store
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);

            X509TrustManager defaultTm = getTrustManager(tmf);

            // load Hoverfly trust store
            InputStream trustStream = findResourceOnClasspath(HOVERFLY_TRUST_STORE).openStream();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStream, HOVERFLY_TRUST_STORE_PASS.toCharArray());

            // initialize a trust manager factory with hoverfly trust store
            tmf.init(trustStore);

            X509TrustManager hoverflyTm = getTrustManager(tmf);

            TrustManager customTm = new X509TrustManager() {
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


            // initialize an ssl context to use these managers and set as default
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{customTm}, null);
            SSLContext.setDefault(sslContext);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to set Hoverfly trust store", e);
        }
    }

    private static X509TrustManager getTrustManager(TrustManagerFactory trustManagerFactory) {
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        X509TrustManager trustManager = null;
        for (TrustManager tm : trustManagers) {
            if (tm instanceof X509TrustManager) {
                trustManager = (X509TrustManager) tm;
            }
        }
        return Optional.ofNullable(trustManager).orElseThrow(IllegalStateException::new);
    }
}
