def call(Map pipelineParams)
{
  node {
    def scmVars
    stage ('Git Checkout') {
      scmVars = checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch', localBranch: "**"]], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/SnehaKailasa23/Dosakaya1.git']]]
      println scmVars.GIT_BRANCH
    }

    stage ('Gradle Build') {
      def BranchName = sh(script: 'git name-rev --name-only HEAD', returnStdout: true)
      if (BranchName.contains('master'))
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
