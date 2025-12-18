pipeline {
    agent {
        label 'QA-Automation-16'
    }

    parameters {
        string(
            name: 'BRANCH',
            defaultValue: 'master',
            description: 'Git branch to checkout'
        )
        choice(
            name: 'TEST_SUITE',
            choices: ['all', 'auth-tests', 'hierarchy-tests', 'handsets-tests', 'infrastructure-tests', 'monitoring-tests', 'api-tests'],
            description: 'Select which test suite to run'
        )
        booleanParam(
            name: 'HEADLESS_MODE',
            defaultValue: true,
            description: 'Run browser tests in headless mode'
        )
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m -XX:MaxMetaspaceSize=256m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out source code from GitHub (branch: ${params.BRANCH})..."
                git branch: "${params.BRANCH}",
                    credentialsId: 'f0d79d40-42a1-41d4-ac93-c58d81c5c773',
                    url: 'https://github.com/wvbrandt/aimwright-jenkins.git'
            }
        }

        stage('Install Playwright Browsers') {
            steps {
                script {
                    echo 'Installing Playwright browser dependencies...'
                    env.JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
                    sh 'mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" || true'
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Building project...'
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    echo "Running test suite: ${params.TEST_SUITE}"

                    // Headless mode is configured in settings.properties
                    if (params.HEADLESS_MODE) {
                        echo 'Running tests in headless mode'
                    }

                    // Start Xvfb for virtual display
                    sh '''
                        # Start Xvfb on display :99
                        Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
                        XVFB_PID=$!
                        echo $XVFB_PID > /tmp/xvfb.pid
                        export DISPLAY=:99

                        # Wait for Xvfb to start
                        sleep 2
                    '''

                    // Run all tests or specific suite
                    if (params.TEST_SUITE == 'all') {
                        sh 'DISPLAY=:99 mvn test'
                    } else {
                        // Run specific test suite using its dedicated XML file
                        sh "DISPLAY=:99 mvn test -Dtestng.suiteXmlFile=testng-${params.TEST_SUITE}.xml"
                    }

                    // Stop Xvfb
                    sh '''
                        if [ -f /tmp/xvfb.pid ]; then
                            kill $(cat /tmp/xvfb.pid) || true
                            rm /tmp/xvfb.pid
                        fi
                    '''
                }
            }
            post {
                always {
                    echo 'Archiving test results...'

                    // Clean up Xvfb process
                    sh '''
                        if [ -f /tmp/xvfb.pid ]; then
                            kill $(cat /tmp/xvfb.pid) || true
                            rm /tmp/xvfb.pid
                        fi
                    '''

                    // Publish TestNG results
                    step([$class: 'Publisher',
                        reportFilenamePattern: '**/testng-results.xml'])

                    // Publish JUnit-style results for Jenkins
                    junit testResults: '**/surefire-reports/*.xml',
                         allowEmptyResults: true,
                         keepLongStdio: true
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'Archiving test artifacts...'
                archiveArtifacts artifacts: 'target/logs/**/*',
                                allowEmptyArchive: true,
                                fingerprint: true

                archiveArtifacts artifacts: 'target/failure-screenshots/**/*',
                                allowEmptyArchive: true,
                                fingerprint: true

                archiveArtifacts artifacts: 'target/surefire-reports/**/*',
                                allowEmptyArchive: true,
                                fingerprint: true
            }
        }

        stage('Generate Test Report') {
            steps {
                echo 'Publishing HTML test reports...'
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/surefire-reports',
                    reportFiles: 'index.html',
                    reportName: 'Test Report',
                    reportTitles: 'Aimwright Test Results'
                ])
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs(
                deleteDirs: true,
                patterns: [
                    [pattern: 'target/classes/**', type: 'INCLUDE'],
                    [pattern: 'target/test-classes/**', type: 'INCLUDE']
                ]
            )
        }
        success {
            echo 'Pipeline completed successfully!'
            // You can add email notifications or Slack notifications here
        }
        failure {
            echo 'Pipeline failed!'
            // You can add email notifications or Slack notifications here
        }
        unstable {
            echo 'Pipeline is unstable (some tests failed)'
            // You can add email notifications or Slack notifications here
        }
    }
}
