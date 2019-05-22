properties([
        parameters(
                [
                        booleanParam(
                                name: 'DEPLOY_BRANCH_TO_TST',
                                defaultValue: false
                        )
                ]
        )
])

def branch
def revision
def registryIp

pipeline {
    agent {
        kubernetes {
            label 'build-service-pod'
            defaultContainer 'jnlp'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    job: build-service
spec:
  containers:
  - name: maven
    image: maven:3.6.0-jdk-11-slim
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: repository
      mountPath: /root/.m2/repository
  - name: docker
    image: docker:18.09.2
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: repository
    persistentVolumeClaim:
      claimName: repository
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    options {
        skipDefaultCheckout true
    }
    stages {
        stage ('checkout') {
            steps {
                script {
                    def repo = checkout scm
                    revision = sh(script: 'git log -1 --format=\'%h.%ad\' --date=format:%Y%m%d-%H%M | cat', returnStdout: true).trim()
                    branch = repo.GIT_BRANCH.take(20).replaceAll('/', '_')
                    if (branch != 'master') {
                        revision += "-${branch}"
                    }
                    sh "echo 'Building revision: ${revision}'"
                }
            }

        }
        stage ('compile') {
            steps {
                container('maven') {
                    sh 'mvn clean compile test-compile'
                }
            }
        }
        stage ('unit test') {
            steps {
                container('maven') {
                    sh 'mvn test'
                }
            }
        }
        stage ('integration test') {
            steps {
                container ('maven') {
                    sh 'mvn verify'
                }
            }
        }
        stage ('build artifact') {
            steps {
                container('maven') {
                    sh "mvn package -Dmaven.test.skip -Drevision=${revision}"
                }
                container('docker') {
                    script {
                        registryIp = sh(script: 'getent hosts registry.kube-system | awk \'{ print $1 ; exit }\'', returnStdout: true).trim()
                        sh "docker build . -t kirlirable/demo-app:${revision} --build-arg REVISION=${revision}"
                    }
                }
            }
        }
        stage ('publish artifact') {
            steps {
                container('docker') {
                    withDockerRegistry([ credentialsId: "2e5f8c0d-e5c5-4419-95cf-97c7635f53e7", url: "" ]) {
                        sh "docker push kirlirable/demo-app:${revision}"
                    }
                }
            }
        }
        stage ('deploy to env') {
            steps {
                build job: 'Deploy', parameters: [
                        [$class: 'StringParameterValue', name: 'GIT_REPO', value: 'demo-app'],
                        [$class: 'StringParameterValue', name: 'VERSION', value: revision],
                        [$class: 'StringParameterValue', name: 'ENV', value: params.DEPLOY_BRANCH_TO_TST ? 'test' : 'staging']
                ], wait: false
            }
        }
    }
}
