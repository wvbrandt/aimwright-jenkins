# Jenkins CI/CD Setup Guide for Aimwright

This guide will help you set up Jenkins for automated testing of the Aimwright test framework.

## Prerequisites

- Jenkins server (version 2.387 or later)
- Git plugin installed in Jenkins
- Pipeline plugin installed in Jenkins
- HTML Publisher plugin installed in Jenkins
- TestNG Results plugin installed in Jenkins

## Jenkins Global Configuration

### 1. Configure JDK

1. Navigate to **Manage Jenkins** → **Global Tool Configuration**
2. Scroll to **JDK** section
3. Click **Add JDK**
4. Configure:
   - **Name**: `JDK 11`
   - **JAVA_HOME**: Path to your Java 11 installation (e.g., `/usr/lib/jvm/java-11-openjdk-amd64`)
   - Uncheck "Install automatically" if using existing JDK

### 2. Configure Maven

1. In the same **Global Tool Configuration** page
2. Scroll to **Maven** section
3. Click **Add Maven**
4. Configure:
   - **Name**: `Maven 3.8.6` (or your Maven version)
   - **MAVEN_HOME**: Path to Maven installation or check "Install automatically"

### 3. Install Required Plugins

Navigate to **Manage Jenkins** → **Manage Plugins** → **Available** and install:

- **Pipeline**: For Jenkinsfile support
- **Git**: For source code management
- **HTML Publisher**: For publishing HTML test reports
- **TestNG Results**: For TestNG test result visualization
- **JUnit**: For test result reporting

## Creating the Jenkins Pipeline Job

### 1. Create New Pipeline Job

1. From Jenkins dashboard, click **New Item**
2. Enter job name: `Aimwright-Tests`
3. Select **Pipeline**
4. Click **OK**

### 2. Configure Pipeline

#### General Settings
- **Description**: `Automated testing for AMiE platform using Aimwright framework`
- **Discard old builds**: Keep last 10 builds

#### Build Triggers (Optional)
- **Poll SCM**: `H/15 * * * *` (check for changes every 15 minutes)
- **GitHub hook trigger**: If using GitHub webhooks
- **Build periodically**: `H 2 * * *` (run nightly at 2 AM)

#### Pipeline Configuration
1. **Definition**: Pipeline script from SCM
2. **SCM**: Git
3. **Repository URL**: Your repository URL
4. **Credentials**: Add your Git credentials if needed
5. **Branch Specifier**: `*/main` or `*/master`
6. **Script Path**: `Jenkinsfile`

### 3. Save Configuration

Click **Save** to create the pipeline job.

## Pipeline Features

### Parameters

The pipeline supports the following parameters:

#### TEST_SUITE
Select which test suite to run:
- `all` - Run all tests (default)
- `auth-tests` - Authentication tests only
- `hierarchy-tests` - Organization/Location CRUD tests
- `handsets-tests` - Device and battery tests
- `infrastructure-tests` - Gateway tests
- `monitoring-tests` - Alerts and dashboard tests
- `api-tests` - API endpoint tests only

#### HEADLESS_MODE
- `true` (default) - Run browser tests without visible UI (recommended for CI)
- `false` - Show browser during test execution (for debugging)

### Pipeline Stages

1. **Checkout**: Retrieves source code from repository
2. **Install Playwright Browsers**: Installs Chromium browser for Playwright
3. **Build**: Compiles the project with Maven
4. **Run Tests**: Executes TestNG test suites
5. **Archive Artifacts**: Saves test results, screenshots, and logs
6. **Generate Test Report**: Creates HTML test report

## Running Tests

### Manual Trigger

1. Navigate to the pipeline job
2. Click **Build with Parameters**
3. Select desired test suite and headless mode
4. Click **Build**

### Viewing Results

After build completion:

1. **Test Results**: Click on build number → **Test Result**
2. **HTML Report**: Click on build number → **Test Report** (left sidebar)
3. **Console Output**: Click on build number → **Console Output**
4. **Artifacts**:
   - Screenshots: `target/failure-screenshots/`
   - Logs: `target/logs/`
   - Test reports: `target/surefire-reports/`

## Environment-Specific Configuration

### Managing Credentials

To avoid hardcoding credentials in `settings.properties`:

1. **Create Jenkins Credentials**:
   - Navigate to **Manage Jenkins** → **Manage Credentials**
   - Add credentials for test users
   - Note the credential IDs

2. **Modify Jenkinsfile** to inject credentials:
   ```groovy
   environment {
       ADMIN_USERNAME = credentials('amie-admin-user')
       ADMIN_PASSWORD = credentials('amie-admin-password')
   }
   ```

3. **Update test code** to read from environment variables

### Multiple Environments

To test against different environments (dev, staging, prod):

1. Add environment parameter to Jenkinsfile:
   ```groovy
   parameters {
       choice(
           name: 'ENVIRONMENT',
           choices: ['dev', 'staging', 'prod'],
           description: 'Target environment'
       )
   }
   ```

2. Create environment-specific config files:
   - `settings-dev.properties`
   - `settings-staging.properties`
   - `settings-prod.properties`

3. Update pipeline to select correct config based on parameter

## Troubleshooting

### Browser Installation Issues

If Playwright browser installation fails:

```bash
# SSH into Jenkins agent and run manually:
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
```

### Headless Mode Issues

If tests fail in headless mode but pass locally:
- Check browser options in `settings.properties`
- Add `--disable-dev-shm-usage` and `--no-sandbox` to browser options
- Verify X virtual framebuffer (Xvfb) is installed on Jenkins agent

### Memory Issues

If build fails with OutOfMemoryError:
- Increase `MAVEN_OPTS` in Jenkinsfile
- Add more memory to Jenkins agent
- Reduce parallel test thread count in `testng.xml`

### Test Artifacts Not Archived

Ensure paths in Jenkinsfile match actual output directories:
- Logs: `target/logs/`
- Screenshots: `target/failure-screenshots/`
- Reports: `target/surefire-reports/`

## Notifications

### Email Notifications

Add to Jenkinsfile `post` section:

```groovy
post {
    failure {
        emailext (
            subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            body: "Check console output at ${env.BUILD_URL}",
            to: 'team@example.com'
        )
    }
}
```

### Slack Notifications

1. Install **Slack Notification** plugin
2. Configure Slack workspace in Jenkins settings
3. Add to Jenkinsfile:

```groovy
post {
    always {
        slackSend (
            color: currentBuild.result == 'SUCCESS' ? 'good' : 'danger',
            message: "Build ${env.BUILD_NUMBER}: ${currentBuild.result}\n${env.BUILD_URL}"
        )
    }
}
```

## Best Practices

1. **Run tests in headless mode** on CI server to reduce resource usage
2. **Archive screenshots and traces** only on test failures to save disk space
3. **Set up nightly builds** to catch regressions early
4. **Use parameterized builds** for flexibility in test execution
5. **Monitor test trends** using TestNG Results plugin graphs
6. **Clean workspace** after builds to prevent disk space issues
7. **Use credentials management** instead of hardcoded passwords
8. **Tag stable builds** for deployment reference

## Next Steps

1. Configure Jenkins to use your source control system
2. Set up webhook triggers for automated builds on code commits
3. Configure email/Slack notifications for build status
4. Create separate jobs for smoke tests vs. full regression
5. Implement test result trending and analysis
6. Set up test reporting dashboard using Jenkins Blue Ocean

## Support

For Jenkins-specific issues:
- Jenkins Documentation: https://www.jenkins.io/doc/
- Pipeline Syntax: https://www.jenkins.io/doc/book/pipeline/syntax/
- Plugin Documentation: Check specific plugin pages

For Aimwright framework issues:
- Review test logs in `target/logs/`
- Check screenshots in `target/failure-screenshots/`
- Examine traces in `target/logs/run_traces/`
