pipeline {
    agent {
        label 'ubuntu-18.04 && x64 && hw'
    }

    options {
        // Сколько последних сборок храним?
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Отключаем параллельный запуск
        disableConcurrentBuilds()
    }

    stages {
        stage('Prepare') {
            steps {
                withCredentials([
                    string(credentialsId: 'DGIS_DIRECTORY_APP_KEY', variable: 'DIRECTORY_KEY'),
                    string(credentialsId: 'DGIS_MAP_API_KEY', variable: 'MAP_KEY')
                ]) {
                    script {
                        def localProperties = """\
                            sdk.dir=/opt/android-sdk/

                            dgisMapApiKey=${env.MAP_KEY}
                            dgisDirectoryApiKey=${env.DIRECTORY_KEY}
                        """.stripIndent()

                        writeFile file: "local.properties", text: localProperties
                    }
                }
            }
        }

        stage('Build & Test') {
            steps {
                sh './gradlew clean build bundle --info'
            }
        }

        stage('Signing artifacts') {
            steps {
                withCredentials([
                        file(credentialsId: 'RELEASE_KEYSTORE', variable: 'RELEASE_KEYSTORE'),
                        string(credentialsId: 'RELEASE_KEYSTORE_PASSWORD', variable: 'RELEASE_KEYSTORE_PASSWORD'),
                        string(credentialsId: 'NSDK_RELEASE_KEY_ALIAS', variable: 'RELEASE_KEY_ALIAS'),
                        string(credentialsId: 'NSDK_RELEASE_KEY_PASSWORD', variable: 'RELEASE_KEY_PASSWORD'),
                ]) {
                    // Signing output bundle
                    sh """
                        jarsigner \
                            -keystore \$RELEASE_KEYSTORE \
                            -storepass \$RELEASE_KEYSTORE_PASSWORD \
                            -keypass \$RELEASE_KEY_PASSWORD \
                            build/app/outputs/bundle/release/app-release.aab \
                            \$RELEASE_KEY_ALIAS
                    """
                    sh 'jarsigner -verify build/app/outputs/bundle/release/app-release.aab -keystore \$RELEASE_KEYSTORE'

                    // Signing output apk
                    sh """
                        jarsigner \
                            -keystore \$RELEASE_KEYSTORE \
                            -storepass \$RELEASE_KEYSTORE_PASSWORD \
                            -keypass \$RELEASE_KEY_PASSWORD \
                            -signedjar build/app/outputs/apk/release/app-release.apk \
                            build/app/outputs/apk/release/app-release-unsigned.apk \
                            \$RELEASE_KEY_ALIAS
                    """
                    sh 'jarsigner -verify build/app/outputs/apk/release/app-release.apk -keystore \$RELEASE_KEYSTORE'
                }
            }
        }

        stage('Develop deploy') {
            when {
                branch 'develop'
            }
            steps {
                archiveArtifacts(artifacts: 'build/app/outputs/apk/release/app-release.apk')
                archiveArtifacts(artifacts: 'build/app/outputs/bundle/release/app-release.aab')
                archiveArtifacts(artifacts: 'build/app/outputs/apk/debug/app-debug.apk')
                archiveArtifacts(artifacts: 'build/app/outputs/bundle/debug/app-debug.aab')
            }
        }

        stage('Release deploy') {
            when {
                branch 'master'
            }
            steps {
                archiveArtifacts(artifacts: 'build/app/outputs/apk/release/app-release.apk')
                archiveArtifacts(artifacts: 'build/app/outputs/bundle/release/app-release.aab')
            }
        }

        stage('Documentation') {
            steps {
                withCredentials([
                    string(credentialsId: 'NSDK_GITLAB_PROJECT_TOKEN', variable: 'GITLAB_PROJECT_TOKEN'),
                    string(credentialsId: 'NSDK_GITLAB_PROJECT_ID', variable: 'GITLAB_PROJECT_ID'),
                    string(credentialsId: 'GITLAB_URL', variable: 'GITLAB_URL')
                ]) {
                    sh(
                        label: 'Запуск регенерации документации',
                        script:"""
                            curl --location \
                            -X POST \
                            -F token=$GITLAB_PROJECT_TOKEN \
                            -F ref=master \
                            -F "variables[Android_SDK_CI]=true" \
                            https://${env.GITLAB_URL}/api/v4/projects/${env.GITLAB_PROJECT_ID}/trigger/pipeline
                        """
                    )
                }
            }
        }
    }
    post {
        always {
            sh './gradlew --stop'
        }
    }
}
