def call(Map pipelineParams)
{
	node {
		def scmVars
		def server = Artifactory.server 'server1'
		def rtGradle = Artifactory.newGradleBuild()
	   
		stage ('Git Checkout') {
		  scmVars = checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch', localBranch: "**"]], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/SnehaKailasa23/Dosakaya1.git']]]
		}

		stage ('Gradle Build') {
			rtGradle.deployer server: server, repo: 'Gradle_Test'
			rtGradle.useWrapper = true
			if (scmVars.GIT_BRANCH.contains('master'))
			{
				def buildInfo = rtGradle.run rootDir: "", buildFile: 'build.gradle', tasks: 'clean build assembleRelease'
			}
			else
			{
				def buildInfo = rtGradle.run rootDir: "", buildFile: 'build.gradle', tasks: 'clean build assembleDebug'	
			}
			def uploadSpec = """{
				"files": [
				{
					"pattern": "app/build/outputs/apk/*.apk",
					"target": "Gradle_Test/"
				}
				]
			}"""
			server.upload(uploadSpec)
			//rtGradle.deployer.deployArtifacts buildInfo		//deploys both the artifacts
			rtGradle.deployer.artifactDeploymentPatterns.addInclude("*release*.apk").addExclude("*debug.apk")	//deploys only release.apk
			server.publishBuildInfo buildInfo	  
		}

	}
}
