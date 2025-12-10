pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6' // Configure this in Jenkins Global Tool Configuration
        jdk 'JDK 11'        // Configure this in Jenkins Global Tool Configuration
    }

    parameters {
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
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Install Playwright Browsers') {
            steps {
                echo 'Installing Playwright browser dependencies...'
                sh '''
                    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium" || true
                '''
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

                    // Run all tests or specific suite
                    if (params.TEST_SUITE == 'all') {
                        sh 'mvn test -Dtestng.suite.xml=testng.xml'
                    } else {
                        // Run specific test group
                        sh "mvn test -Dgroups=${params.TEST_SUITE}"
                    }
                }
            }
            post {
                always {
                    echo 'Archiving test results...'
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
