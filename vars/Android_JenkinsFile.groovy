def call(Map pipelineParams)
{
  node {
    stage ('Git Checkout') {
      checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/SnehaKailasa23/Dosakaya1.git']]]
    }

    stage ('Gradle Build') {
      sh ''' ./gradlew clean build assembleRelease
      echo "These are the apk's generated with this build."
      ls -l /var/lib/jenkins/workspace/Android_Project/app/build/outputs/apk '''
    }

    stage ('Archiving Artifacts') {
        archiveArtifacts artifacts: 'app/build/outputs/apk/*.apk', fingerprint: true 
    }
  }
}
