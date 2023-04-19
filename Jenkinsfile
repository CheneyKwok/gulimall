pipeline {
  agent {
    node {
      label 'maven'
    }

  parameters {
      string(name: 'PROJECT_VERSION', defaultValue: '', description: '')
      string(name: 'PROJECT_NAME', defaultValue: '', description: '')
  }

  environment {
      DOCKER_CREDENTIAL_ID = 'aliyun-dockerhub-id'
      GITHUB_CREDENTIAL_ID = 'github-id'
      KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
      REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
      DOCKERHUB_NAMESPACE = '2399214024'
      GITHUB_ACCOUNT = 'CheneyKwok'
      SONAR_CREDENTIAL_ID = 'sonar-token'
  }

  }
  stages {

    stage('clone code') {
      agent none
      steps {
        git(url: 'https://github.com/CheneyKwok/gulimall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
      }
    }


    stage('sonarqube analysis') {
      steps {
        container ('maven') {
          withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
            withSonarQubeEnv('sonar') {
             sh "mvn sonar:sonar -o -gs `pwd`/configuration/settings.xml -Dsonar.login=$SONAR_TOKEN"
            }
          }
          timeout(time: 1, unit: 'HOURS') {
            waitForQualityGate abortPipeline: true
          }
        }
      }
    }



  }
}