// Full pipeline: commit -> build -> unit tests -> package -> Selenium tests -> Tomcat deploy -> Docker image -> container.
// Requirements on the Jenkins agent (Linux): JDK 17+, Maven, Git, curl, Docker, Google Chrome or Chromium.
// Credentials to create in Jenkins (Manage Jenkins > Credentials):
//   tomcat-manager : Username/Password of a Tomcat user with the "manager-script" role
//   dockerhub      : Username/Password (or access token) for Docker Hub (only when PUSH_IMAGE and REGISTRY_LOGIN are on)
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    triggers {
        pollSCM('H/2 * * * *')   // check GitHub every ~2 minutes (replace with a webhook if Jenkins is reachable)
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'staging', 'prod'],
               description: 'Environment label baked into the WAR and shown in the application footer')
        string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8080',
               description: 'Base URL of the Tomcat server that receives the WAR')
        booleanParam(name: 'RUN_SELENIUM', defaultValue: true, description: 'Run the Selenium suite before deploying')
        string(name: 'DOCKER_REPO', defaultValue: 'yourdockerhubuser/certificate-portal',
               description: 'Image repository, for example myuser/certificate-portal or localhost:5000/certificate-portal')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: true, description: 'Push the image to the registry')
        booleanParam(name: 'REGISTRY_LOGIN', defaultValue: true, description: 'Log in with the dockerhub credential before pushing (turn off for an open local registry)')
        string(name: 'CONTAINER_PORT', defaultValue: '8081', description: 'Host port for the freshly deployed container')
    }

    environment {
        IMAGE_TAG = "1.0.${env.BUILD_NUMBER}"
        CONTAINER_NAME = 'certificate-portal'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --pretty=format:"%h %s"'
            }
        }

        stage('Build and unit tests') {
            steps {
                sh 'mvn -B clean test -Dapp.env=${DEPLOY_ENV}'
            }
            post {
                always { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests -Dapp.env=${DEPLOY_ENV}'
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Selenium tests') {
            when { expression { params.RUN_SELENIUM } }
            steps {
                // Runs against the WAR built above in an embedded Tomcat; a failure here stops the pipeline.
                sh 'mvn -B failsafe:integration-test failsafe:verify'
            }
            post {
                always {
                    junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'tomcat-manager', usernameVariable: 'TC_USER', passwordVariable: 'TC_PASS')]) {
                    sh '''
                        curl --fail --silent --show-error -u "$TC_USER:$TC_PASS" \
                             -T target/certificate-portal.war \
                             "${TOMCAT_URL}/manager/text/deploy?path=/certificate-portal&update=true"
                    '''
                }
                sh '''
                    curl --fail --silent --show-error --retry 15 --retry-delay 3 --retry-connrefused \
                         "${TOMCAT_URL}/certificate-portal/health"
                '''
            }
        }

        stage('Build Docker image') {
            steps {
                sh '''
                    docker build --build-arg APP_ENV=${DEPLOY_ENV} \
                                 -t ${DOCKER_REPO}:${IMAGE_TAG} -t ${DOCKER_REPO}:latest .
                    docker image ls ${DOCKER_REPO}
                '''
            }
        }

        stage('Push image') {
            when { expression { params.PUSH_IMAGE } }
            steps {
                script {
                    if (params.REGISTRY_LOGIN) {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                            sh 'echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin'
                        }
                    }
                    sh 'docker push ${DOCKER_REPO}:${IMAGE_TAG} && docker push ${DOCKER_REPO}:latest'
                    if (params.REGISTRY_LOGIN) {
                        sh 'docker logout'
                    }
                }
            }
        }

        stage('Run fresh container') {
            steps {
                sh '''
                    docker rm -f ${CONTAINER_NAME} 2>/dev/null || true
                    docker run -d --name ${CONTAINER_NAME} --restart unless-stopped \
                        -p ${CONTAINER_PORT}:8080 -v certportal-data:/data \
                        -e APP_ENV=${DEPLOY_ENV} ${DOCKER_REPO}:${IMAGE_TAG}
                    curl --fail --silent --show-error --retry 20 --retry-delay 3 --retry-connrefused \
                         http://localhost:${CONTAINER_PORT}/certificate-portal/health
                '''
            }
        }
    }

    post {
        success { echo "Pipeline OK: ${env.DOCKER_REPO}:${env.IMAGE_TAG} is running on port ${params.CONTAINER_PORT}" }
        failure { echo 'Pipeline failed - nothing after the failing stage was deployed.' }
    }
}
