def call(Map pipelineParams)
{
  node {
    stage ('Git Checkout') {
      def scmVars = checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch', localBranch: "**"]], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/SnehaKailasa23/Dosakaya1.git']]]
      println scmVars.GIT_BRANCH
    }

    stage ('Gradle Build') {
      sh 'echo `git name-rev --name-only HEAD`'
      if ("${env.BRANCH_NAME}".contains('master'))
      {
        println "Release"
        sh './gradlew clean build assembleRelease'
      }
      else
      {
        println "Debug"
        sh './gradlew clean build assembleDebug'
      }
      sh ''' echo "These are the apk's generated with this build."
      ls -l /var/lib/jenkins/workspace/Android_Project/app/build/outputs/apk '''
    }

    stage ('Archiving Artifacts') {
        archiveArtifacts artifacts: 'app/build/outputs/apk/*.apk', fingerprint: true 
    }
  }
}
