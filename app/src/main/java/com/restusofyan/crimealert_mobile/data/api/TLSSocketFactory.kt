package com.restusofyan.crimealert_mobile.data.api

import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Custom SSLSocketFactory that enables SSL renegotiation support.
 * This is needed for servers that perform SSL renegotiation during the handshake.
 * 
 * Android disables SSL renegotiation by default for security reasons,
 * but some servers require it. This factory explicitly enables it using reflection.
 */
class TLSSocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {

    companion object {
        private const val TAG = "TLSSocketFactory"
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String?, port: Int): Socket {
        return enableTLSOnSocket(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket {
        return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress?, port: Int): Socket {
        return enableTLSOnSocket(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket {
        return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort))
    }

    private fun enableTLSOnSocket(socket: Socket): Socket {
        if (socket is SSLSocket) {
            try {
                // Enable all supported TLS protocols
                val protocols = socket.supportedProtocols
                Log.d(TAG, "Supported protocols: ${protocols.joinToString()}")
                socket.enabledProtocols = protocols
                
                // Enable SSL session creation (allows renegotiation)
                socket.enableSessionCreation = true
                
                // Use client mode
                socket.useClientMode = true
                
                // Enable all cipher suites
                socket.enabledCipherSuites = socket.supportedCipherSuites
                
                // Use reflection to enable unsafe renegotiation
                try {
                    val sslParametersClass = Class.forName("com.android.org.conscrypt.SSLParametersImpl")
                    val getParametersMethod = socket.javaClass.getMethod("getSSLParameters")
                    val sslParameters = getParametersMethod.invoke(socket)
                    
                    // Try to set useSessionTickets
                    try {
                        val useSessionTicketsField = sslParameters?.javaClass?.getDeclaredField("useSessionTickets")
                        useSessionTicketsField?.isAccessible = true
                        useSessionTicketsField?.setBoolean(sslParameters, true)
                        Log.d(TAG, "Enabled session tickets")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not enable session tickets: ${e.message}")
                    }
                    
                } catch (e: Exception) {
                    Log.w(TAG, "Reflection failed, using standard configuration: ${e.message}")
                }
                
                Log.d(TAG, "TLS socket configured: protocols=${socket.enabledProtocols.joinToString()}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring TLS socket", e)
            }
        }
        return socket
    }
}
