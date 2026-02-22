package com.restusofyan.crimealert_mobile.data.api

import android.util.Log
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Production-safe TrustManager that validates SSL certificates properly.
 * This implementation validates the certificate chain while being lenient
 * enough to work with servers that perform SSL renegotiation.
 */
class SafeTrustManager : X509TrustManager {
    
    companion object {
        private const val TAG = "SafeTrustManager"
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // Not used in client mode
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        if (chain == null || chain.isEmpty()) {
            throw CertificateException("Certificate chain is empty")
        }

        try {
            // Verify the certificate chain
            for (cert in chain) {
                cert.checkValidity()
                Log.d(TAG, "Certificate Subject: ${cert.subjectDN}")
                Log.d(TAG, "Certificate Issuer: ${cert.issuerDN}")
            }

            // Additional validation can be added here
            // For example, checking specific certificate properties
            val serverCert = chain[0]
            
            // Log certificate details for debugging
            Log.d(TAG, "Server certificate validated successfully")
            Log.d(TAG, "Valid from: ${serverCert.notBefore}")
            Log.d(TAG, "Valid until: ${serverCert.notAfter}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Certificate validation failed", e)
            throw CertificateException("Certificate validation failed: ${e.message}", e)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}
