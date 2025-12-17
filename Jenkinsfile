pipeline {
    agent any

    environment {
        DOCKER_SPRING_IMAGE = "eyamosbahi/student-management"
        DOCKER_ANGULAR_IMAGE = "eyamosbahi/student-frontend"
        DOCKER_TAG = "1.0.${BUILD_NUMBER}"
        DOCKER_CREDENTIALS_ID = "dockerhub-credentials"
        SONAR_PROJECT_KEY = "student-management"
        SONAR_HOST_URL = "http://localhost:9000"
        // Pour de la sécurité, SONAR_TOKEN doit venir du credentials store Jenkins (voir étapes suivantes)
    }

    stages {
        stage('Checkout Spring Boot') {
            steps {
                // Si le code est déjà dans le workspace, cette étape peut juste afficher un message.
                echo '✅ Spring Boot code already in workspace'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.login=$SONAR_TOKEN
                        """
                    }
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build Spring Boot') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG} ."
                    sh "docker tag ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG} ${DOCKER_SPRING_IMAGE}:latest"
                }
            }
        }

        stage('Docker Push Spring Boot') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}", 
                        usernameVariable: 'DOCKER_USER', 
                        passwordVariable: 'DOCKER_PASS')]) {
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        sh "docker push ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG}"
                        sh "docker push ${DOCKER_SPRING_IMAGE}:latest"
                    }
                }
            }
        }

        stage('Deploy Spring Boot to Kubernetes') {
            steps {
                script {
                    sh """
                        minikube image load ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG}
                        kubectl set image deployment/spring-app spring-app=${DOCKER_SPRING_IMAGE}:${DOCKER_TAG} -n devops
                        kubectl rollout status deployment/spring-app -n devops --timeout=5m
                    """
                }
            }
        }

        stage('Checkout Angular') {
            steps {
                // IDEAL : remplacer ce bloc par checkout vraie repo Angular, ou copier le code avant le build
                echo '✅ Angular frontend code already in workspace or copied'
            }
        }

        stage('Docker Build Angular') {
            steps {
                script {
                    dir('/mnt/c/Users/ahmed/student-frontend') {
                        sh "docker build -t ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} ."
                        sh "docker tag ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} ${DOCKER_ANGULAR_IMAGE}:latest"
                    }
                }
            }
        }

        stage('Docker Push Angular') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}", 
                        usernameVariable: 'DOCKER_USER', 
                        passwordVariable: 'DOCKER_PASS')]) {
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        sh "docker push ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG}"
                        sh "docker push ${DOCKER_ANGULAR_IMAGE}:latest"
                    }
                }
            }
        }

        stage('Deploy Angular to Kubernetes') {
            steps {
                script {
                    sh """
                        minikube image load ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG}
                        kubectl set image deployment/angular-app angular-app=${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} -n devops
                        kubectl rollout status deployment/angular-app -n devops --timeout=5m
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    sh """
                        echo "=== Pods Status ==="
                        kubectl get pods -n devops
                        echo ""
                        echo "=== Services ==="
                        kubectl get svc -n devops
                    """
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
            echo "Spring Boot: ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG}"
            echo "Angular: ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG}"
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
