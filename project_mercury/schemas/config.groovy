environments {
    defaultSample {
        /**
         * The ssh block is optional.
         * It  is used to set up a tunnel to the casssandra contactpoint via the host
         * as a minimum the ssh block requires the `host`
         * The remoteHost defaults to the cassandra  contactpoint
         * localPort and remotePort default to 9042
         *
         */
        ssh {
            // This is used to turn off ssh tunneling from the command line
            // a direct connection will be attempted only if this isnt set or it is set to false explicitly
            // with the current configuration process we can only override but not create configuration options
            useSsh = true

            /**
             * host is required as a minimum when a SSH tunnel is required
             */
            host = 'bastion.dev.spr.com'

            /**
             * the ssh user on the ssh.host
             * Optional; defaults to ubuntu
             */
            user = 'some user on bastion.dev.spr.com'
            /**
             * the ssh identity on the ssh.host
             * null defaults to ~/.ssh/id_rsa.
             * CASSANDRA_GATEWAY_KEY env variable
             * may also be used to
             * override the identity file path
             * during runtime.
             */
            identity = null

            /**
             * optional block
             * all attributes are optional
             */
            localPortForward {

                /**
                 * The local port of the ssh tunnel
                 * The local process will connect to this port
                 * optional, defaults to 9042
                 * this is currently ignored by the code since the Cassandra
                 * has the code hard coded to 9042
                 */
                // localPort = 9042

                /**
                 * The destination of the ssh tunnel
                 * optional, defaults to the cassandra contactpoint (the actual cassandra server hostname or ip)
                 *  when remoteHost is not set and it defaults to the cassandra contactpoint
                 *  then the cassandra contactpoint is set to localhost (to be tunneld through)
                 */
                remoteHost = '10.16.4.12'

                /**
                 * The remote port of the ssh tunnel
                 * optional, defaults to 9042, this is the cassandra port of the actual cassandra host
                 */
                remotePort = 9042
            }
        }

        /**
         * required block
         * defines the cassandra connection
         */
        cassandra {
            /**
             * required
             * cassandra hostname or IP
             */
            contactPoint = "cassandra.dev.spr.com"

            /**
             * required block
             * defines the cassandra user credentials
             */
            authentication {
                // required
                username = "someCassandraUser"
                // required
                password = "thepasswordforthatUser"
            }

            /**
             * required block
             * defines the cassandra TLS certificate
             */
            tls {
                trustStore = "path/to/truststore.jks"
                trustStorePassword = "passwordForTruststore"
            }
        }
    }

    local {
        cassandra {
            contactPoint = "cassandra.local.phx-spr.com"

            authentication {
                username = "cassandra"
                password = "cassandra"
            }

            tls {
                trustStore = "/etc/tls/dse/local/truststore.jks"
                trustStorePassword = "cassandra"
            }
        }
    }

    sandbox {
        cassandra {
            contactPoint = "cassandra_node1"

            authentication {
                username = "cassandra"
                password = "cassandra"
            }

            tls {
                trustStore = "etc/tls/dse/sandbox/truststore.jks"
                trustStorePassword = System.getenv('CASSANDRA_TRUSTSTORE_PASSWORD')
            }
        }
    }

    dev {
        ssh {
            useSsh = true
            user = "pcm_user"
            host = System.getenv('CASSANDRA_GATEWAY')
        }
        cassandra {
            contactPoint = System.getenv('CASSANDRA_ENDPOINT_IP')

            authentication {
                username = "cassandra"
                password = "cassandra"
            }

            tls {
                trustStore = "/etc/tls/dse/dev/truststore.jks"
                trustStorePassword = System.getenv('CASSANDRA_TRUSTSTORE_PASSWORD')
            }
        }
    }

    qaint {
        ssh {
            useSsh = true
            user = "pcm_user"
            host = System.getenv('CASSANDRA_GATEWAY')
        }
        cassandra {
            contactPoint = System.getenv('CASSANDRA_ENDPOINT_IP')

            authentication {
                username = "cassandra"
                password = "cassandra"
            }

            tls {
                trustStore = "/etc/tls/dse/qaint/truststore.jks"
                trustStorePassword = System.getenv('CASSANDRA_TRUSTSTORE_PASSWORD')
            }
        }
    }

    ppe {
        ssh {
            useSsh = true
            user = "pcm_user"
            host = System.getenv('CASSANDRA_GATEWAY')
        }
        cassandra {
            contactPoint = System.getenv('CASSANDRA_ENDPOINT_IP')

            authentication {
                username = "cassandra"
                password = "cassandra"
            }

            tls {
                trustStore = "/etc/tls/dse/ppe/truststore.jks"
                trustStorePassword = System.getenv('CASSANDRA_TRUSTSTORE_PASSWORD')
            }
        }
    }

    stg {
        ssh {
            useSsh = true
            user = "pcm_user"
            host = System.getenv('CASSANDRA_GATEWAY')
        }
        cassandra {
            contactPoint = System.getenv('CASSANDRA_ENDPOINT_IP')

            authentication {
                username = "cassandra"
                password = "cassandra"
            }

            tls {
                trustStore = "/etc/tls/dse/stg/truststore.jks"
                trustStorePassword = System.getenv('CASSANDRA_TRUSTSTORE_PASSWORD')
            }
        }
    }

    prod {
        ssh {
            useSsh = true
            user = "pcm_user"
            host = System.getenv('CASSANDRA_GATEWAY')
        }

        cassandra {
            contactPoint = System.getenv('CASSANDRA_ENDPOINT_IP')

            authentication {
                username = "cassandra"
                password = "cassandra"
            }

            tls {
                trustStore = "/etc/tls/dse/prod/truststore.jks"
                trustStorePassword = System.getenv('CASSANDRA_TRUSTSTORE_PASSWORD')
            }
        }
    }
}
